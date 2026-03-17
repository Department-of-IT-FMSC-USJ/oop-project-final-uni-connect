<script>
    import { onMount } from "svelte";
    import RoleSelectModal from "./RoleSelectModal.svelte";

    const API_BASE = "http://localhost:8080";
    const allowedRoles = ["UNDERGRADUATE", "DEPARTMENT_REP", "DEPARTMENT_HEAD"];

    let token = "";
    let profile = null;
    let accessState = "loading";
    let accessMessage = "";
    let role = "";
    let showAccessLoginModal = false;
    let pauseAutoRefresh = false;
    let proofFormEl;

    let proofForm = {
        title: "",
        description: "",
        cpm: "",
        eventDate: "",
        category: "ACTIVITY",
        proofType: "",
        proofData: ""
    };

    let allocateForm = {
        studentId: "",
        proofId: "",
        category: "ACTIVITY",
        points: "",
        note: ""
    };

    $: maxPoints = allocateForm.category === "AWARD" ? 25 : 5;

    let reviewForm = {
        recordId: "",
        action: "APPROVE",
        adjustedPoints: "",
        note: ""
    };

    let loading = false;
    let infoMessage = "";
    let errorMessage = "";

    let myProofs = [];
    let myPoints = [];
    let pendingPoints = [];
    let eligibleStudents = [];
    let repProofs = [];
    let selectedProof = null;
    let pendingEdits = {};
    let repEdits = {};
    let headEdits = {};
    let headLocks = {};
    let headDirty = {};
    let statusFilter = "ALL";
    let searchTerm = "";
    let refreshTimer = null;

    onMount(() => {
        token = localStorage.getItem("token") || "";
        if (!token) {
            window.location.href = "/profile";
            return;
        }
        loadProfile();
    });

    async function loadProfile() {
        accessState = "loading";
        try {
            const response = await fetch(`${API_BASE}/api/users/profile`, {
                headers: authHeaders()
            });
            if (!response.ok) {
                throw new Error("Unable to load profile.");
            }
            profile = await response.json();
            role = profile.role;
            if (!allowedRoles.includes(role)) {
                accessState = "denied";
                accessMessage = "Access restricted to undergraduates, department reps, and department heads.";
                return;
            }
            accessState = "allowed";
            startAutoRefresh();
        } catch (error) {
            accessState = "denied";
            accessMessage = error.message || "Unable to validate access.";
        }
    }

    function authHeaders() {
        return token ? { Authorization: `Bearer ${token}` } : {};
    }

    function jsonHeaders() {
        return {
            "Content-Type": "application/json",
            ...authHeaders()
        };
    }

    function isRepLocked(proof) {
        if (!proof || !proof.pointStatus) return false;
        const status = String(proof.pointStatus).toUpperCase();
        return status === "APPROVED" || status === "REJECTED";
    }

    function resetMessages() {
        infoMessage = "";
        errorMessage = "";
    }

    function startAutoRefresh() {
        if (refreshTimer) {
            clearInterval(refreshTimer);
        }
        if (role === "UNDERGRADUATE") {
            refreshTimer = setInterval(() => {
                loadProfile();
                loadMyPoints();
                loadMyProofs();
            }, 10000);
        } else if (role === "DEPARTMENT_REP" || role === "DEPARTMENT_HEAD") {
            refreshTimer = setInterval(() => {
                loadRepProofs({ keepMessages: true });
            }, 10000);
        }
    }

    async function handleProofSubmit() {
        resetMessages();
        loading = true;
        try {
            const response = await fetch(`${API_BASE}/api/proofs`, {
                method: "POST",
                headers: jsonHeaders(),
                body: JSON.stringify({
                    title: proofForm.title,
                    description: proofForm.description,
                    cpm: proofForm.cpm,
                    eventDate: proofForm.eventDate,
                    pointCategory: proofForm.category,
                    proofType: proofForm.proofType,
                    proofData: proofForm.proofData
                })
            });
            if (!response.ok) {
                throw new Error(await response.text());
            }
            infoMessage = "Proof submitted successfully.";
            proofForm = { title: "", description: "", cpm: "", eventDate: "", category: "ACTIVITY", proofType: "", proofData: "" };
            pauseAutoRefresh = false;
            await loadMyProofs();
        } catch (error) {
            errorMessage = error.message || "Failed to submit proof.";
        } finally {
            loading = false;
        }
    }

    async function loadMyProofs() {
        resetMessages();
        try {
            const response = await fetch(`${API_BASE}/api/proofs/my`, {
                headers: authHeaders()
            });
            if (!response.ok) {
                throw new Error(await response.text());
            }
            myProofs = await response.json();
        } catch (error) {
            errorMessage = error.message || "Failed to load proofs.";
        }
    }

    async function loadMyPoints() {
        resetMessages();
        try {
            const response = await fetch(`${API_BASE}/api/points/my`, {
                headers: authHeaders()
            });
            if (!response.ok) {
                throw new Error(await response.text());
            }
            myPoints = await response.json();
        } catch (error) {
            errorMessage = error.message || "Failed to load points.";
        }
    }

    async function loadRepProofs({ keepMessages = false } = {}) {
        if (!keepMessages) {
            resetMessages();
        }
        try {
            const response = await fetch(`${API_BASE}/api/proofs`, {
                headers: authHeaders()
            });
            if (!response.ok) {
                throw new Error(await response.text());
            }
            repProofs = await response.json();
            repEdits = repProofs.reduce((acc, proof) => {
                acc[proof.id] = {
                    points: proof.latestPoints ?? 1,
                    category: proof.pointCategory || "ACTIVITY"
                };
                return acc;
            }, {});
            if (role === "DEPARTMENT_HEAD") {
                const existingEdits = headEdits;
                headEdits = repProofs.reduce((acc, proof) => {
                    const current = existingEdits[proof.id];
                    const baselinePoints = Number(proof.latestPoints ?? 0);
                    if (current && Number(current.points) !== baselinePoints) {
                        acc[proof.id] = current;
                    } else {
                        acc[proof.id] = {
                            points: proof.latestPoints ?? 1,
                            category: proof.pointCategory || "ACTIVITY"
                        };
                    }
                    return acc;
                }, {});
                headLocks = repProofs.reduce((acc, proof) => {
                    acc[proof.id] = String(proof.pointStatus || "").toUpperCase() !== "PENDING";
                    return acc;
                }, {});
            }
        } catch (error) {
            errorMessage = error.message || "Failed to load submissions.";
        }
    }

    async function handleAllocatePoints() {
        resetMessages();
        loading = true;
        try {
            const payload = {
                studentId: Number(allocateForm.studentId),
                proofId: allocateForm.proofId ? Number(allocateForm.proofId) : null,
                category: allocateForm.category,
                points: Number(allocateForm.points),
                note: allocateForm.note
            };
            const response = await fetch(`${API_BASE}/api/points/allocate`, {
                method: "POST",
                headers: jsonHeaders(),
                body: JSON.stringify(payload)
            });
            if (!response.ok) {
                throw new Error(await response.text());
            }
            const record = await response.json();
            infoMessage = "Points allocated and marked as pending.";
            if (record?.proofId) {
                repProofs = repProofs.map((proof) =>
                    proof.id === record.proofId
                        ? { ...proof, latestPoints: record.points, pointStatus: record.status }
                        : proof
                );
                if (selectedProof?.id === record.proofId) {
                    selectedProof = {
                        ...selectedProof,
                        latestPoints: record.points,
                        pointStatus: record.status
                    };
                }
            }
            allocateForm = { studentId: "", proofId: "", category: "ACTIVITY", points: "", note: "" };
        } catch (error) {
            errorMessage = error.message || "Failed to allocate points.";
        } finally {
            loading = false;
        }
    }

    async function loadPending() {
        resetMessages();
        try {
            const response = await fetch(`${API_BASE}/api/points/pending`, {
                headers: authHeaders()
            });
            if (!response.ok) {
                throw new Error(await response.text());
            }
            pendingPoints = await response.json();
            pendingEdits = pendingPoints.reduce((acc, record) => {
                acc[record.id] = {
                    points: record.points ?? 0,
                    category: record.category || "ACTIVITY"
                };
                return acc;
            }, {});
        } catch (error) {
            errorMessage = error.message || "Failed to load pending points.";
        }
    }

    async function handleReviewPoints() {
        resetMessages();
        loading = true;
        try {
            const payload = {
                action: reviewForm.action,
                adjustedPoints: reviewForm.adjustedPoints ? Number(reviewForm.adjustedPoints) : null,
                note: reviewForm.note
            };
            const response = await fetch(`${API_BASE}/api/points/${reviewForm.recordId}/review`, {
                method: "PUT",
                headers: jsonHeaders(),
                body: JSON.stringify(payload)
            });
            if (!response.ok) {
                throw new Error(await response.text());
            }
            infoMessage = "Review submitted.";
            reviewForm = { recordId: "", action: "APPROVE", adjustedPoints: "", note: "" };
            await loadPending();
        } catch (error) {
            errorMessage = error.message || "Failed to review points.";
        } finally {
            loading = false;
        }
    }

    function setCategory(category) {
        allocateForm.category = category;
        const nextMax = category === "AWARD" ? 25 : 5;
        if (!allocateForm.points || Number(allocateForm.points) > nextMax) {
            allocateForm.points = nextMax;
        }
    }

    function getCategoryMax(category) {
        if (category === "AWARD") return 25;
        return 5;
    }

    function updateRepPoints(proofId, delta) {
        const proof = repProofs.find((p) => p.id === proofId);
        if (isRepLocked(proof)) return;
        const edit = repEdits[proofId];
        if (!edit) return;
        const max = getCategoryMax(edit.category);
        const next = Math.max(1, Math.min(max, Number(edit.points || 0) + delta));
        repEdits = { ...repEdits, [proofId]: { ...edit, points: next } };
    }

    function updateRepCategory(proofId, category) {
        const proof = repProofs.find((p) => p.id === proofId);
        if (isRepLocked(proof)) return;
        const edit = repEdits[proofId];
        if (!edit) return;
        const max = getCategoryMax(category);
        const nextPoints = Math.min(Number(edit.points || 1), max) || max;
        repEdits = { ...repEdits, [proofId]: { ...edit, category, points: nextPoints } };
    }

    async function saveRepEdit(proof) {
        if (isRepLocked(proof)) {
            errorMessage = "This point allocation is already finalized and cannot be edited.";
            return;
        }
        resetMessages();
        loading = true;
        try {
            const edit = repEdits[proof.id];
            if (!edit) {
                throw new Error("No changes to save.");
            }
            const payload = {
                studentId: Number(proof.studentId),
                proofId: Number(proof.id),
                category: edit.category,
                points: Number(edit.points),
                note: "Inline allocation"
            };
            const response = await fetch(`${API_BASE}/api/points/allocate`, {
                method: "POST",
                headers: jsonHeaders(),
                body: JSON.stringify(payload)
            });
            if (!response.ok) {
                throw new Error(await response.text());
            }
            const record = await response.json();
            repProofs = repProofs.map((item) =>
                item.id === proof.id
                    ? {
                          ...item,
                          latestPoints: record.points,
                          pointStatus: record.status,
                          pointCategory: record.category
                      }
                    : item
            );
            repEdits = {
                ...repEdits,
                [proof.id]: { points: record.points, category: record.category }
            };
            infoMessage = "Points allocated and marked as pending.";
        } catch (error) {
            errorMessage = error.message || "Failed to allocate points.";
        } finally {
            loading = false;
        }
    }

    function updatePendingPoints(recordId, delta) {
        const edit = pendingEdits[recordId];
        if (!edit) return;
        const max = getCategoryMax(edit.category);
        const next = Math.max(1, Math.min(max, Number(edit.points || 0) + delta));
        pendingEdits = { ...pendingEdits, [recordId]: { ...edit, points: next } };
    }

    function updatePendingCategory(recordId, category) {
        const edit = pendingEdits[recordId];
        if (!edit) return;
        const max = getCategoryMax(category);
        const nextPoints = Math.min(Number(edit.points || 1), max) || max;
        pendingEdits = { ...pendingEdits, [recordId]: { ...edit, category, points: nextPoints } };
    }

    function updateHeadPoints(proofId, delta) {
        const edit = headEdits[proofId];
        if (!edit) return;
        const max = getCategoryMax(edit.category);
        const next = Math.max(1, Math.min(max, Number(edit.points || 0) + delta));
        headEdits = { ...headEdits, [proofId]: { ...edit, points: next } };
        headDirty = { ...headDirty, [proofId]: true };
    }

    function isHeadDirty(proof) {
        const edit = headEdits[proof.id];
        if (!edit) return false;
        return Number(edit.points) !== Number(proof.latestPoints ?? 0);
    }

    function isHeadLocked(proof) {
        return Boolean(headLocks[proof.id]);
    }

    async function saveHeadAdjustment(proof) {
        resetMessages();
        if (!proof?.latestRecordId) {
            errorMessage = "No point record found to save.";
            return;
        }
        if (isHeadLocked(proof)) {
            return;
        }
        const edit = headEdits[proof.id];
        const desiredPoints = edit?.points ?? proof.latestPoints;
        const desiredCategory = edit?.category ?? proof.pointCategory;
        if (!isHeadDirty(proof)) {
            infoMessage = "No changes to save.";
            return;
        }
        loading = true;
        try {
            const response = await fetch(`${API_BASE}/api/points/${proof.latestRecordId}/pending-adjust`, {
                method: "PUT",
                headers: jsonHeaders(),
                body: JSON.stringify({
                    adjustedPoints: desiredPoints,
                    category: desiredCategory,
                    note: "Adjusted by department head"
                })
            });
            if (!response.ok) {
                const payload = await response.text();
                throw new Error(`Save failed (${response.status}): ${payload || "Unknown error"}`);
            }
            let updated = null;
            try {
                updated = await response.json();
            } catch (parseError) {
                updated = null;
            }
            infoMessage = "Points updated.";
            const nextProofId = updated?.proofId ?? proof.id;
            const nextRecordId = updated?.id ?? proof.latestRecordId ?? null;
            repProofs = repProofs.map((item) =>
                String(item.id) === String(nextProofId) ||
                (nextRecordId && String(item.latestRecordId) === String(nextRecordId))
                    ? {
                          ...item,
                          latestPoints: updated?.points ?? desiredPoints ?? item.latestPoints,
                          pointStatus: item.pointStatus,
                          pointCategory: updated?.category ?? desiredCategory ?? item.pointCategory,
                          latestRecordId: nextRecordId ?? item.latestRecordId
                      }
                    : item
            );
            if (selectedProof?.id === nextProofId) {
                selectedProof = {
                    ...selectedProof,
                    latestPoints: updated?.points ?? desiredPoints ?? selectedProof.latestPoints,
                    pointStatus: selectedProof.pointStatus,
                    pointCategory: updated?.category ?? desiredCategory ?? selectedProof.pointCategory,
                    latestRecordId: nextRecordId ?? selectedProof.latestRecordId
                };
            }
            headEdits = {
                ...headEdits,
                [proof.id]: { points: updated?.points ?? desiredPoints, category: desiredCategory }
            };
            headDirty = { ...headDirty, [proof.id]: false };
            await loadRepProofs({ keepMessages: true });
        } catch (error) {
            errorMessage = error.message || "Failed to save points.";
        } finally {
            loading = false;
        }
    }

    async function savePendingEdit(record) {
        resetMessages();
        loading = true;
        try {
            const edit = pendingEdits[record.id];
            if (!edit) {
                throw new Error("No changes to save.");
            }
            const payload = {
                action: "ADJUST",
                adjustedPoints: Number(edit.points),
                category: edit.category,
                note: "Inline adjustment"
            };
            const response = await fetch(`${API_BASE}/api/points/${record.id}/review`, {
                method: "PUT",
                headers: jsonHeaders(),
                body: JSON.stringify(payload)
            });
            if (!response.ok) {
                throw new Error(await response.text());
            }
            const updated = await response.json();
            pendingPoints = pendingPoints.map((item) => (item.id === updated.id ? updated : item));
            pendingEdits = {
                ...pendingEdits,
                [updated.id]: { points: updated.points, category: updated.category }
            };
            infoMessage = "Point record updated.";
        } catch (error) {
            errorMessage = error.message || "Failed to update points.";
        } finally {
            loading = false;
        }
    }

    async function loadEligible() {
        resetMessages();
        try {
            const response = await fetch(`${API_BASE}/api/mentors/eligible`, {
                headers: authHeaders()
            });
            if (!response.ok) {
                throw new Error(await response.text());
            }
            eligibleStudents = await response.json();
        } catch (error) {
            errorMessage = error.message || "Failed to load eligible students.";
        }
    }

    async function handleProofFileChange(event) {
        const file = event.target.files?.[0];
        if (!file) {
            return;
        }
        proofForm.proofType = file.type || "file";
        const reader = new FileReader();
        reader.onload = () => {
            proofForm.proofData = String(reader.result || "");
        };
        reader.readAsDataURL(file);
    }

    function selectProof(proof) {
        selectedProof = proof;
        allocateForm.studentId = String(proof.studentId ?? "");
        allocateForm.proofId = String(proof.id ?? "");
        setCategory(allocateForm.category || "ACTIVITY");
    }

    function isImageProof(proof) {
        return Boolean(proof?.proofData && proof.proofData.startsWith("data:image"));
    }

    function isPdfProof(proof) {
        return Boolean(proof?.proofData && proof.proofData.startsWith("data:application/pdf"));
    }

    $: filteredRepProofs = repProofs.filter((proof) => {
        const statusMatch = statusFilter === "ALL" || String(proof.pointStatus || "").toUpperCase() === statusFilter;
        const query = searchTerm.trim().toLowerCase();
        if (!query) {
            return statusMatch;
        }
        const studentName = String(proof.studentName || "").toLowerCase();
        const studentId = String(proof.studentId || "").toLowerCase();
        const cpm = String(proof.cpm || "").toLowerCase();
        const title = String(proof.title || "").toLowerCase();
        return statusMatch && (studentName.includes(query) || studentId.includes(query) || cpm.includes(query) || title.includes(query));
    });

    $: groupedRepProofs = filteredRepProofs.reduce((acc, proof) => {
        const key = proof.studentId ?? "unknown";
        const studentName = proof.studentName || "Unknown Student";
        if (!acc[key]) {
            acc[key] = { studentId: key, studentName, submissions: [] };
        }
        acc[key].submissions.push(proof);
        return acc;
    }, {});

    $: groupedRepProofsList = Object.values(groupedRepProofs);

    function statusClass(status) {
        const normalized = String(status || "").toLowerCase();
        if (normalized === "pending") return "status-pill status-pending";
        if (normalized === "approved") return "status-pill status-approved";
        if (normalized === "rejected") return "status-pill status-rejected";
        return "status-pill";
    }

    function formatCategory(category) {
        const normalized = String(category || "").toUpperCase();
        if (normalized === "AWARD") return "Achievement";
        if (normalized === "ACTIVITY") return "Participation";
        if (normalized === "DIRECT") return "Direct";
        return "-";
    }

    function getConfirmState(proof) {
        const status = String(proof?.pointStatus || "").toUpperCase();
        if (loading) return { disabled: true, reason: "loading" };
        if (!proof?.latestRecordId) return { disabled: true, reason: "missing record id" };
        if (status !== "PENDING") return { disabled: true, reason: `status ${status || "empty"}` };
        return { disabled: false, reason: "ready" };
    }

    function getSaveState(proof) {
        const status = String(proof?.pointStatus || "").toUpperCase();
        if (loading) return { disabled: true, reason: "loading" };
        if (!proof?.latestRecordId) return { disabled: true, reason: "missing record id" };
        if (status !== "PENDING") return { disabled: true, reason: `status ${status || "empty"}` };
        if (isHeadLocked(proof)) return { disabled: true, reason: "locked" };
        if (!headDirty[proof.id] && !isHeadDirty(proof)) return { disabled: true, reason: "no changes" };
        return { disabled: false, reason: "ready" };
    }

    async function confirmHeadReview(proof, action = "APPROVE") {
        resetMessages();
        if (!proof?.latestRecordId) {
            errorMessage = "No point record found to confirm.";
            return;
        }
        if (isHeadLocked(proof)) {
            return;
        }
        const previousStatus = proof.pointStatus;
        const previousCategory = proof.pointCategory;
        const previousPoints = proof.latestPoints;
        const previousRecordId = proof.latestRecordId;
        const optimisticId = proof.id;
        headLocks = { ...headLocks, [proof.id]: true };
        repProofs = repProofs.map((item) =>
            String(item.id) === String(optimisticId)
                ? {
                      ...item,
                      pointStatus: action === "REJECT" ? "REJECTED" : "APPROVED"
                  }
                : item
        );
        loading = true;
        try {
            const response = await fetch(`${API_BASE}/api/points/${proof.latestRecordId}/review`, {
                method: "PUT",
                headers: jsonHeaders(),
                body: JSON.stringify({
                    action,
                    note: action === "REJECT" ? "Rejected by department head" : "Confirmed by department head"
                })
            });
            if (!response.ok) {
                throw new Error(await response.text());
            }
            let updated = null;
            try {
                updated = await response.json();
            } catch (parseError) {
                updated = null;
            }
            infoMessage = action === "REJECT" ? "Submission rejected." : "Submission confirmed.";
            const nextStatus = updated?.status?.name ?? updated?.status ?? "APPROVED";
            const resolvedStatus = action === "REJECT" ? "REJECTED" : nextStatus;
            const nextProofId = updated?.proofId ?? proof.id;
            const nextRecordId = updated?.id ?? proof.latestRecordId ?? null;
            repProofs = repProofs.map((item) =>
                String(item.id) === String(nextProofId) ||
                (nextRecordId && String(item.latestRecordId) === String(nextRecordId))
                    ? {
                          ...item,
                          latestPoints: updated?.points ?? item.latestPoints,
                          pointStatus: resolvedStatus,
                          pointCategory: updated?.category ?? item.pointCategory,
                          latestRecordId: nextRecordId ?? item.latestRecordId
                      }
                    : item
            );
            if (selectedProof?.id === nextProofId) {
                selectedProof = {
                    ...selectedProof,
                    latestPoints: updated?.points ?? selectedProof.latestPoints,
                    pointStatus: resolvedStatus,
                    pointCategory: updated?.category ?? selectedProof.pointCategory,
                    latestRecordId: nextRecordId ?? selectedProof.latestRecordId
                };
            }
            headLocks = { ...headLocks, [proof.id]: true };
        } catch (error) {
            repProofs = repProofs.map((item) =>
                String(item.id) === String(optimisticId)
                    ? {
                          ...item,
                          pointStatus: previousStatus,
                          pointCategory: previousCategory,
                          latestPoints: previousPoints,
                          latestRecordId: previousRecordId
                      }
                    : item
            );
            headLocks = { ...headLocks, [proof.id]: false };
            errorMessage = error.message || "Failed to review submission.";
        } finally {
            loading = false;
        }
    }

    function undoHeadLock(proof) {
        undoHeadReview(proof);
    }

    async function undoHeadReview(proof) {
        resetMessages();
        if (!proof?.latestRecordId) {
            errorMessage = "No point record found to undo.";
            return;
        }
        loading = true;
        try {
            const response = await fetch(`${API_BASE}/api/points/${proof.latestRecordId}/undo`, {
                method: "PUT",
                headers: jsonHeaders()
            });
            if (!response.ok) {
                throw new Error(await response.text());
            }
            let updated = null;
            try {
                updated = await response.json();
            } catch (parseError) {
                updated = null;
            }
            const nextStatus = updated?.status?.name ?? updated?.status ?? "PENDING";
            const nextProofId = updated?.proofId ?? proof.id;
            const nextRecordId = updated?.id ?? proof.latestRecordId ?? null;
            repProofs = repProofs.map((item) =>
                String(item.id) === String(nextProofId) ||
                (nextRecordId && String(item.latestRecordId) === String(nextRecordId))
                    ? {
                          ...item,
                          pointStatus: nextStatus,
                          latestPoints: updated?.points ?? item.latestPoints,
                          pointCategory: updated?.category ?? item.pointCategory,
                          latestRecordId: nextRecordId ?? item.latestRecordId
                      }
                    : item
            );
            if (selectedProof?.id === nextProofId) {
                selectedProof = {
                    ...selectedProof,
                    pointStatus: nextStatus,
                    latestPoints: updated?.points ?? selectedProof.latestPoints,
                    pointCategory: updated?.category ?? selectedProof.pointCategory,
                    latestRecordId: nextRecordId ?? selectedProof.latestRecordId
                };
            }
            headLocks = { ...headLocks, [proof.id]: false };
            headEdits = {
                ...headEdits,
                [proof.id]: {
                    points: updated?.points ?? proof.latestPoints ?? headEdits[proof.id]?.points,
                    category: updated?.category ?? proof.pointCategory ?? headEdits[proof.id]?.category
                }
            };
            infoMessage = "Review undone.";
        } catch (error) {
            errorMessage = error.message || "Failed to undo review.";
        } finally {
            loading = false;
        }
    }
</script>

{#if accessState === "loading"}
    <div class="status-card glass-panel">
        <p>Validating access...</p>
    </div>
{:else if accessState === "denied"}
    <div class="status-card glass-panel">
        <h2>Access Restricted</h2>
        <p>{accessMessage}</p>
        <div class="status-actions">
            <button class="btn btn-primary" type="button" on:click={() => (showAccessLoginModal = true)}>Login</button>
            <a class="btn btn-outline" href="/">Back to Home</a>
        </div>
    </div>
    {#if showAccessLoginModal}
        <div class="modal-backdrop" on:click={() => (showAccessLoginModal = false)}>
            <div class="modal-card" on:click|stopPropagation>
                <div class="modal-header">
                    <h3>Login / Sign Up</h3>
                    <button class="btn btn-outline btn-xs" type="button" on:click={() => (showAccessLoginModal = false)}>
                        Close
                    </button>
                </div>
                <RoleSelectModal lockMentors={true} />
            </div>
        </div>
    {/if}
{:else}
    <div class="overview-grid">
        <div class="glass-panel overview-card">
            <h3>Signed In</h3>
            <p>{profile.fullName}</p>
            <span class="pill">{profile.role}</span>
        </div>
        {#if role === "UNDERGRADUATE"}
            <div class="glass-panel overview-card">
                <h3>Total Points</h3>
                <p>{profile.cumulativePoints ?? 0}</p>
                <span class="pill">{profile.mentorEligible ? "Mentor Eligible" : "Not Eligible"}</span>
            </div>
            <div class="glass-panel overview-card">
                <h3>Department</h3>
                <p>{profile.department || "Department of Information Technology"}</p>
                <span class="pill">Access Granted</span>
            </div>
        {/if}
    </div>

    {#if infoMessage}
        <div class="alert success">{infoMessage}</div>
    {/if}
    {#if errorMessage}
        <div class="alert error">{errorMessage}</div>
    {/if}

    <div class="section-grid">
        {#if role === "UNDERGRADUATE"}
            <section class="glass-panel section-card">
                <div class="section-header">
                    <h2>Undergraduate Proof Upload</h2>
                    <p>Submit proof for activities or achievements.</p>
                </div>
                <form
                    class="form"
                    bind:this={proofFormEl}
                    on:focusin={updatePauseFromActive}
                    on:focusout={() => setTimeout(updatePauseFromActive, 0)}
                    on:submit|preventDefault={handleProofSubmit}
                >
                    <div class="form-group">
                        <label class="form-label">Title</label>
                        <input class="form-control" bind:value={proofForm.title} required />
                    </div>
                    <div class="form-group">
                        <label class="form-label">Description</label>
                        <textarea class="form-control" rows="3" bind:value={proofForm.description}></textarea>
                    </div>
                    <div class="form-group">
                        <label class="form-label">CPM</label>
                        <input class="form-control" bind:value={proofForm.cpm} required />
                    </div>
                    <div class="form-group">
                        <label class="form-label">Point Category</label>
                        <div class="radio-group">
                            <label class="radio-option">
                                <input
                                    type="radio"
                                    name="pointCategory"
                                    value="ACTIVITY"
                                    bind:group={proofForm.category}
                                />
                                Participation
                            </label>
                            <label class="radio-option">
                                <input
                                    type="radio"
                                    name="pointCategory"
                                    value="AWARD"
                                    bind:group={proofForm.category}
                                />
                                Achievement
                            </label>
                        </div>
                    </div>
                    <div class="form-group">
                        <label class="form-label">Event Date</label>
                        <input class="form-control" type="date" bind:value={proofForm.eventDate} required />
                    </div>
                    <div class="form-group">
                        <label class="form-label">Proof Upload (optional)</label>
                        <input class="form-control" type="file" on:change={handleProofFileChange} />
                    </div>
                <button class="btn btn-primary" type="submit" disabled={loading}>Submit Proof</button>
            </form>

                <div class="table-actions">
                    <button class="btn btn-outline" type="button" on:click={loadMyProofs}>Load My Proofs</button>
                </div>

                <div class="data-table">
                    <h4>My Proof Submissions</h4>
                    {#if myProofs.length === 0}
                        <p class="empty">No proofs loaded.</p>
                    {:else}
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>Title</th>
                                    <th>Date</th>
                                    <th>Category</th>
                                    <th>Points</th>
                                    <th>Status</th>
                                </tr>
                            </thead>
                            <tbody>
                                {#each myProofs as proof}
                                    <tr>
                                        <td>{proof.submissionCode ?? proof.id}</td>
                                        <td>{proof.title}</td>
                                        <td>{proof.eventDate}</td>
                                        <td>{formatCategory(proof.pointCategory)}</td>
                                        <td>{proof.latestPoints ?? "-"}</td>
                                        <td>
                                            {#if proof.pointStatus}
                                                <span class={statusClass(proof.pointStatus)}>{proof.pointStatus}</span>
                                            {:else}
                                                <span class="empty">-</span>
                                            {/if}
                                        </td>
                                    </tr>
                                {/each}
                            </tbody>
                        </table>
                    {/if}
                </div>
            </section>
        {/if}

        {#if role === "DEPARTMENT_REP"}
            <section class="glass-panel section-card">
                <div class="section-header">
                    <h2>Student Submissions</h2>
                    <p>Load undergraduate submissions and allocate points.</p>
                </div>
                <div class="table-actions">
                    <button class="btn btn-outline" type="button" on:click={loadRepProofs}>Load Student Submissions</button>
                    <select class="form-control compact" bind:value={statusFilter}>
                        <option value="ALL">All Statuses</option>
                        <option value="PENDING">Pending</option>
                        <option value="APPROVED">Approved</option>
                        <option value="REJECTED">Rejected</option>
                    </select>
                    <input class="form-control compact" placeholder="Search by CPM, student name or ID" bind:value={searchTerm} />
                </div>
                <div class="data-table">
                    <h4>Submission List</h4>
                    {#if repProofs.length === 0}
                        <p class="empty">No submissions loaded.</p>
                    {:else if filteredRepProofs.length === 0}
                        <p class="empty">No submissions match the filters.</p>
                    {:else}
                        <table>
                            <thead>
                                <tr>
                                    <th>ID</th>
                                    <th>CPM</th>
                                    <th>Student</th>
                                    <th>Event Title</th>
                                    <th>Description</th>
                                    <th>Proof</th>
                                    <th>Point Type</th>
                                    <th>Allocated Points</th>
                                    <th>Status</th>
                                    <th>Action</th>
                                </tr>
                            </thead>
                            <tbody>
                                {#each filteredRepProofs as proof}
                                    <tr>
                                        <td>{proof.submissionCode ?? proof.id}</td>
                                        <td>{proof.cpm || "-"}</td>
                                        <td>{proof.studentName}</td>
                                        <td>{proof.title}</td>
                                        <td>{proof.description || "-"}</td>
                                        <td>
                                            {#if proof.proofData}
                                                <span>Uploaded</span>
                                            {:else}
                                                <span class="empty">No file</span>
                                            {/if}
                                        </td>
                                        <td>
                                            <select
                                                class="form-control compact"
                                                value={repEdits[proof.id]?.category || proof.pointCategory || "ACTIVITY"}
                                                on:change={(e) => updateRepCategory(proof.id, e.target.value)}
                                                disabled={isRepLocked(proof)}
                                            >
                                                <option value="ACTIVITY">Participation</option>
                                                <option value="AWARD">Achievement</option>
                                            </select>
                                        </td>
                                        <td>
                                            <div class="points-control">
                                                <button
                                                    class="btn btn-outline btn-xs"
                                                    type="button"
                                                    on:click={() => updateRepPoints(proof.id, -1)}
                                                    disabled={isRepLocked(proof)}
                                                >
                                                    -
                                                </button>
                                                <span class="points-value">{repEdits[proof.id]?.points ?? proof.latestPoints ?? "-"}</span>
                                                <button
                                                    class="btn btn-outline btn-xs"
                                                    type="button"
                                                    on:click={() => updateRepPoints(proof.id, 1)}
                                                    disabled={isRepLocked(proof)}
                                                >
                                                    +
                                                </button>
                                            </div>
                                        </td>
                                        <td>
                                            {#if proof.pointStatus}
                                                <span class={statusClass(proof.pointStatus)}>{proof.pointStatus}</span>
                                            {:else}
                                                <span class="empty">-</span>
                                            {/if}
                                        </td>
                                        <td>
                                            <div class="action-group">
                                                <button
                                                    class="btn btn-primary btn-xs"
                                                    type="button"
                                                    on:click={() => saveRepEdit(proof)}
                                                    disabled={loading || isRepLocked(proof)}
                                                >
                                                    Save
                                                </button>
                                                <button class="btn btn-outline btn-xs" type="button" on:click={() => selectProof(proof)}>
                                                    View
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                {/each}
                            </tbody>
                        </table>
                    {/if}
                </div>
            </section>
        {/if}

        {#if (role === "DEPARTMENT_REP" || role === "DEPARTMENT_HEAD") && selectedProof}
            <div class="modal-backdrop" on:click={() => (selectedProof = null)}></div>
            <div class="modal-card">
                <div class="modal-header">
                    <h3>Submission Details</h3>
                    <button class="btn btn-outline btn-xs" type="button" on:click={() => (selectedProof = null)}>
                        Close
                    </button>
                </div>
                <div class="details-grid">
                    <div>
                        <p class="detail-label">Student</p>
                        <p class="detail-value">{selectedProof.studentName} (ID {selectedProof.studentId})</p>
                    </div>
                    <div>
                        <p class="detail-label">Submission ID</p>
                        <p class="detail-value">{selectedProof.id}</p>
                    </div>
                    <div>
                        <p class="detail-label">Title</p>
                        <p class="detail-value">{selectedProof.title}</p>
                    </div>
                    <div>
                        <p class="detail-label">Event Date</p>
                        <p class="detail-value">{selectedProof.eventDate}</p>
                    </div>
                    <div>
                        <p class="detail-label">Proof Type</p>
                        <p class="detail-value">{selectedProof.proofType || "-"}</p>
                    </div>
                    <div>
                        <p class="detail-label">Submitted At</p>
                        <p class="detail-value">{selectedProof.createdAt || "-"}</p>
                    </div>
                    <div class="detail-full">
                        <p class="detail-label">Description</p>
                        <p class="detail-value">{selectedProof.description || "-"}</p>
                    </div>
                    <div class="detail-full">
                        <p class="detail-label">Proof</p>
                        {#if selectedProof.proofData}
                            {#if isImageProof(selectedProof)}
                                <img class="proof-preview" src={selectedProof.proofData} alt="Proof upload" />
                            {:else if isPdfProof(selectedProof)}
                                <iframe class="proof-preview" src={selectedProof.proofData} title="Proof document"></iframe>
                            {:else}
                                <a class="btn btn-outline btn-xs" href={selectedProof.proofData} target="_blank" rel="noreferrer">Open Proof</a>
                            {/if}
                        {:else}
                            <span class="empty">No proof uploaded.</span>
                        {/if}
                    </div>
                </div>

                {#if role === "DEPARTMENT_REP"}
                    <form class="form" on:submit|preventDefault={handleAllocatePoints}>
                        <div class="form-row">
                            <div class="form-group">
                                <label class="form-label">Point Type</label>
                                <select class="form-control" bind:value={allocateForm.category} on:change={(e) => setCategory(e.target.value)}>
                                    <option value="ACTIVITY">Participation (1-5)</option>
                                    <option value="AWARD">Achievement (1-25)</option>
                                </select>
                            </div>
                            <div class="form-group">
                                <label class="form-label">Points</label>
                                <input class="form-control" type="number" min="1" max={maxPoints} bind:value={allocateForm.points} required />
                                <small class="hint">Max {maxPoints} points for this type.</small>
                            </div>
                        </div>
                        <div class="form-group">
                            <label class="form-label">Note</label>
                            <input class="form-control" bind:value={allocateForm.note} />
                        </div>
                        <button class="btn btn-primary" type="submit" disabled={loading}>Allocate Points</button>
                    </form>
                {/if}
            </div>
        {/if}

        {#if role === "DEPARTMENT_HEAD"}
            <section class="glass-panel section-card">
                <div class="section-header">
                    <h2>Head Review</h2>
                    <p>Review final point allocations and confirm submissions.</p>
                </div>
                <div class="table-actions">
                    <button class="btn btn-outline" type="button" on:click={loadRepProofs}>Load Student Submissions</button>
                    <select class="form-control compact" bind:value={statusFilter}>
                        <option value="ALL">All Statuses</option>
                        <option value="PENDING">Pending</option>
                        <option value="APPROVED">Approved</option>
                        <option value="REJECTED">Rejected</option>
                    </select>
                    <input class="form-control compact" placeholder="Search by CPM, student name or ID" bind:value={searchTerm} />
                </div>
                <div class="data-table">
                    <h4>Submission List</h4>
                    {#if repProofs.length === 0}
                        <p class="empty">No submissions loaded.</p>
                    {:else if filteredRepProofs.length === 0}
                        <p class="empty">No submissions match the filters.</p>
                    {:else}
                        {#each groupedRepProofsList as group}
                            <div class="group-block">
                                <div class="group-header">
                                    <span class="group-title">{group.studentName} (ID {group.studentId})</span>
                                    <span class="group-count">{group.submissions.length} submission(s)</span>
                                </div>
                                <table>
                                    <thead>
                                        <tr>
                                            <th>ID</th>
                                            <th>CPM</th>
                                            <th>Point Type</th>
                                            <th>Allocated Points</th>
                                            <th>Status</th>
                                            <th>Reviewed By</th>
                                            <th>Action</th>
                                        </tr>
                                    </thead>
                                    <tbody>
                                        {#each group.submissions as proof}
                                            <tr>
                                                <td>{proof.submissionCode ?? proof.id}</td>
                                                <td>{proof.cpm || "-"}</td>
                                                <td>{formatCategory(proof.pointCategory)}</td>
                                                <td>
                                                    <div class="points-control">
                                                        <button
                                                            class="btn btn-outline btn-xs"
                                                            type="button"
                                                            on:click={() => updateHeadPoints(proof.id, -1)}
                                                        >
                                                            -
                                                        </button>
                                                        <span class="points-value">
                                                            {headEdits[proof.id]?.points ?? proof.latestPoints ?? "-"}
                                                        </span>
                                                        <button
                                                            class="btn btn-outline btn-xs"
                                                            type="button"
                                                            on:click={() => updateHeadPoints(proof.id, 1)}
                                                        >
                                                            +
                                                        </button>
                                                    </div>
                                                    <button
                                                        class="btn btn-primary btn-xs"
                                                        type="button"
                                                        on:click={() => saveHeadAdjustment(proof)}
                                                        disabled={getSaveState(proof).disabled}
                                                        title={`Save: ${getSaveState(proof).reason}`}
                                                    >
                                                        Save
                                                    </button>
                                                </td>
                                                <td>
                                                    {#if proof.pointStatus}
                                                        <span class={statusClass(proof.pointStatus)}>{proof.pointStatus}</span>
                                                    {:else}
                                                        <span class="empty">-</span>
                                                    {/if}
                                                </td>
                                                <td>
                                                    {#if proof.latestAllocatedByName}
                                                        {proof.latestAllocatedByName}
                                                    {:else}
                                                        <span class="empty">-</span>
                                                    {/if}
                                                </td>
                                                <td>
                                                    <div class="action-group">
                                                        <button
                                                            class="btn btn-primary btn-xs"
                                                            type="button"
                                                            on:click={() => confirmHeadReview(proof, "APPROVE")}
                                                            disabled={getConfirmState(proof).disabled || isHeadLocked(proof)}
                                                            title="Confirm submission"
                                                        >
                                                            Confirm
                                                        </button>
                                                        <button
                                                            class="btn btn-outline btn-xs btn-danger"
                                                            type="button"
                                                            on:click={() => confirmHeadReview(proof, "REJECT")}
                                                            disabled={getConfirmState(proof).disabled || isHeadLocked(proof)}
                                                            title="Reject submission"
                                                        >
                                                            Reject
                                                        </button>
                                                        <button
                                                            class="btn btn-outline btn-xs"
                                                            type="button"
                                                            on:click={() => undoHeadLock(proof)}
                                                            disabled={!isHeadLocked(proof)}
                                                            title="Change status to pending"
                                                        >
                                                            Undo
                                                        </button>
                                                        <button class="btn btn-outline btn-xs" type="button" on:click={() => selectProof(proof)} title="View submission">
                                                            View
                                                        </button>
                                                    </div>
                                                </td>
                                            </tr>
                                        {/each}
                                    </tbody>
                                </table>
                            </div>
                        {/each}
                    {/if}
                </div>

                <div class="section-divider"></div>

                <div class="section-header">
                    <h2>Eligible Students</h2>
                    <p>View students who unlocked industry mentor access.</p>
                </div>
                <div class="table-actions">
                    <button class="btn btn-outline" type="button" on:click={loadEligible}>Load Eligible Students</button>
                </div>
                <div class="data-table">
                    <h4>Eligible List</h4>
                    {#if eligibleStudents.length === 0}
                        <p class="empty">No eligible students loaded.</p>
                    {:else}
                        <table>
                            <thead>
                                <tr>
                                    <th>Student</th>
                                    <th>Department</th>
                                    <th>Total Points</th>
                                    <th>Proofs</th>
                                </tr>
                            </thead>
                            <tbody>
                                {#each eligibleStudents as student}
                                    <tr>
                                        <td>{student.fullName}</td>
                                        <td>{student.department}</td>
                                        <td>{student.totalPoints}</td>
                                        <td>{student.proofs?.length ?? 0}</td>
                                    </tr>
                                {/each}
                            </tbody>
                        </table>
                    {/if}
                </div>
            </section>
        {/if}
    </div>
{/if}

<style>
    .status-card {
        padding: 2.5rem;
        text-align: center;
        max-width: 600px;
        margin: 0 auto;
    }

    .status-card h2 {
        font-size: 1.8rem;
        margin-bottom: 0.75rem;
    }

    .status-actions {
        display: flex;
        gap: 1rem;
        justify-content: center;
        margin-top: 1.5rem;
        flex-wrap: wrap;
    }

    .overview-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
        gap: 1.5rem;
    }

    .overview-card {
        padding: 1.5rem;
        display: flex;
        flex-direction: column;
        gap: 0.5rem;
    }

    .overview-card h3 {
        font-size: 1rem;
        color: var(--text-muted);
    }

    .overview-card p {
        font-size: 1.6rem;
        font-weight: 700;
        color: var(--text-main);
    }

    .pill {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 0.3rem 0.8rem;
        border-radius: 999px;
        font-size: 0.75rem;
        font-weight: 600;
        background: rgba(30, 64, 175, 0.1);
        color: #1e3a8a;
        width: fit-content;
    }

    .alert {
        padding: 0.9rem 1.2rem;
        border-radius: 10px;
        margin-top: 1rem;
    }

    .alert.success {
        background: rgba(34, 197, 94, 0.15);
        color: #166534;
        border: 1px solid rgba(34, 197, 94, 0.3);
    }

    .alert.error {
        background: rgba(220, 38, 38, 0.1);
        color: #991b1b;
        border: 1px solid rgba(220, 38, 38, 0.25);
    }

    .section-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(320px, 1fr));
        gap: 1.8rem;
    }

    .section-card {
        padding: 1.8rem;
        display: flex;
        flex-direction: column;
        gap: 1.25rem;
    }

    .section-header h2 {
        font-size: 1.35rem;
    }

    .section-header p {
        color: var(--text-muted);
        margin-top: 0.3rem;
    }

    .form {
        display: flex;
        flex-direction: column;
        gap: 1rem;
    }

    .radio-group {
        display: flex;
        gap: 1rem;
        flex-wrap: wrap;
    }

    .radio-option {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
        font-size: 0.9rem;
        color: var(--text-main);
    }

    .form-row {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
        gap: 1rem;
    }

    .table-actions {
        display: flex;
        gap: 0.75rem;
        flex-wrap: wrap;
    }

    .data-table {
        border-top: 1px solid var(--glass-border);
        padding-top: 1rem;
    }

    .data-table h4 {
        margin-bottom: 0.75rem;
    }

    .group-block {
        border: 1px solid rgba(148, 163, 184, 0.25);
        border-radius: 14px;
        padding: 1rem;
        margin-bottom: 1rem;
        background: rgba(255, 255, 255, 0.6);
    }

    .group-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 1rem;
        margin-bottom: 0.75rem;
        font-weight: 600;
        color: var(--text-main);
    }

    .group-title {
        font-size: 0.95rem;
    }

    .group-count {
        font-size: 0.8rem;
        color: var(--text-muted);
    }

    .hint {
        display: block;
        margin-top: 0.35rem;
        color: var(--text-muted);
        font-size: 0.8rem;
    }

    .section-divider {
        height: 1px;
        background: rgba(148, 163, 184, 0.3);
        margin: 0.5rem 0 1rem;
    }

    .details-grid {
        display: grid;
        grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
        gap: 1rem;
        padding: 1rem;
        border-radius: 16px;
        background: rgba(255, 255, 255, 0.55);
        border: 1px solid rgba(148, 163, 184, 0.25);
    }

    .detail-full {
        grid-column: 1 / -1;
    }

    .detail-label {
        font-size: 0.85rem;
        color: var(--text-muted);
        margin-bottom: 0.35rem;
    }

    .detail-value {
        font-weight: 600;
        color: var(--text-main);
    }

    .proof-preview {
        width: 100%;
        max-height: 340px;
        border-radius: 12px;
        border: 1px solid rgba(148, 163, 184, 0.3);
        object-fit: contain;
        background: white;
    }

    .btn-xs {
        padding: 0.3rem 0.65rem;
        font-size: 0.75rem;
    }

    .compact {
        max-width: 220px;
        padding: 0.45rem 0.7rem;
        font-size: 0.85rem;
    }

    .points-control {
        display: inline-flex;
        align-items: center;
        gap: 0.5rem;
    }

    .points-value {
        min-width: 2.5rem;
        text-align: center;
        font-weight: 600;
        color: var(--text-main);
    }

    .action-group {
        display: inline-flex;
        gap: 0.5rem;
        flex-wrap: wrap;
    }

    .status-pill {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        padding: 0.25rem 0.6rem;
        border-radius: 999px;
        font-size: 0.72rem;
        font-weight: 600;
        text-transform: uppercase;
        letter-spacing: 0.04em;
        background: rgba(148, 163, 184, 0.2);
        color: #475569;
        border: 1px solid rgba(148, 163, 184, 0.35);
    }

    .status-pending {
        background: rgba(234, 179, 8, 0.18);
        color: #92400e;
        border-color: rgba(234, 179, 8, 0.4);
    }

    .status-approved {
        background: rgba(34, 197, 94, 0.18);
        color: #166534;
        border-color: rgba(34, 197, 94, 0.4);
    }

    .status-rejected {
        background: rgba(239, 68, 68, 0.18);
        color: #991b1b;
        border-color: rgba(239, 68, 68, 0.4);
    }

    .btn-danger {
        border-color: rgba(239, 68, 68, 0.6);
        color: #ffffff;
        background: rgba(239, 68, 68, 0.8);
    }

    .btn-danger:hover {
        background: rgba(220, 38, 38, 0.9);
    }

    button:disabled {
        opacity: 0.55;
        cursor: not-allowed;
        filter: grayscale(0.2);
    }

    .modal-backdrop {
        position: fixed;
        inset: 0;
        background: rgba(15, 23, 42, 0.4);
        z-index: 20;
    }

    .modal-card {
        position: fixed;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: min(900px, 92vw);
        max-height: 88vh;
        overflow-y: auto;
        padding: 1.6rem;
        background: rgba(255, 255, 255, 0.96);
        border-radius: 20px;
        border: 1px solid rgba(148, 163, 184, 0.3);
        box-shadow: 0 30px 60px rgba(15, 23, 42, 0.2);
        z-index: 30;
        display: flex;
        flex-direction: column;
        gap: 1.2rem;
    }

    .modal-header {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: 1rem;
    }

    table {
        width: 100%;
        border-collapse: collapse;
        font-size: 0.9rem;
    }

    th,
    td {
        text-align: left;
        padding: 0.5rem 0.25rem;
        border-bottom: 1px solid rgba(148, 163, 184, 0.3);
    }

    th {
        color: var(--text-muted);
        font-weight: 600;
    }

    .empty {
        color: var(--text-muted);
        font-size: 0.9rem;
    }

    @media (max-width: 700px) {
        .section-card {
            padding: 1.4rem;
        }
    }
</style>
