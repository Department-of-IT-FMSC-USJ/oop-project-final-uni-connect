<script>
    import { onMount } from "svelte";
    let isEditing = false;
    let isLoading = false;
    let saveMessage = "";

    let profile = {
        fullName: "",
        email: "",
        role: "",
        phone: "",
        department: "Department of Information Technology",
        profilePicture: "",
        registrationNumber: "",
        cpmNumber: "",
        yearOfStudy: "",
    };

    onMount(async () => {
        await fetchProfile();
    });

    const fetchProfile = async () => {
        try {
            const response = await fetch(
                "http://localhost:8080/api/users/profile",
                {
                    headers: {
                        Authorization: `Bearer ${localStorage.getItem("token")}`,
                    },
                },
            );
            if (response.ok) {
                const data = await response.json();
                console.log("DEBUG: Received profile data:", data);
                profile = {
                    ...profile,
                    ...data,
                    department: "Department of Information Technology", // Shared requirement
                    profilePicture:
                        data.profilePicture || profile.profilePicture || "",
                };
            }
        } catch (error) {
            console.error("Error fetching profile:", error);
        }
    };

    const handleFileChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            const reader = new FileReader();
            reader.onloadend = () => {
                profile.profilePicture = reader.result;
            };
            reader.readAsDataURL(file);
        }
    };

    const toggleEdit = () => {
        isEditing = !isEditing;
        saveMessage = "";
    };

    const saveProfile = async () => {
        isLoading = true;
        saveMessage = "";

        try {
            console.log("Saving profile data:", profile);
            const response = await fetch(
                "http://localhost:8080/api/users/profile",
                {
                    method: "PUT",
                    headers: {
                        "Content-Type": "application/json",
                        Authorization: `Bearer ${localStorage.getItem("token")}`,
                    },
                    body: JSON.stringify(profile),
                },
            );

            if (response.ok) {
                isLoading = false;
                isEditing = false;
                saveMessage = "Profile updated successfully!";
                await fetchProfile(); // Refresh data
            } else {
                saveMessage = "Failed to update profile.";
                isLoading = false;
            }
        } catch (error) {
            console.error("Error saving profile:", error);
            saveMessage = "An error occurred.";
            isLoading = false;
        }
    };
</script>

<div class="glass-panel profile-card">
    <div class="profile-header">
        <div
            class="avatar"
            style={profile.profilePicture
                ? `background: url(${profile.profilePicture}) center/cover;`
                : ""}
        >
            {#if !profile.profilePicture}
                {profile.fullName.charAt(0)}
            {/if}
        </div>
        <div class="profile-title">
            <h2>{profile.fullName}</h2>
            <p class="role-badge">{profile.role}</p>
        </div>
        <button class="btn btn-primary edit-btn" on:click={toggleEdit}>
            {isEditing ? "Cancel Edit" : "Edit Profile"}
        </button>
    </div>

    {#if saveMessage}
        <div class="alert success">{saveMessage}</div>
    {/if}

    <form class="profile-form" on:submit|preventDefault={saveProfile}>
        <div class="form-row">
            <div class="form-group half-width">
                <label class="form-label" for="fullName">Full Name</label>
                <input
                    class="form-control"
                    type="text"
                    id="fullName"
                    bind:value={profile.fullName}
                    disabled={!isEditing}
                />
            </div>
            <div class="form-group half-width">
                <label class="form-label" for="email">Email</label>
                <input
                    class="form-control"
                    type="email"
                    id="email"
                    bind:value={profile.email}
                    disabled={true}
                />
                <small class="muted-text">Email cannot be changed.</small>
            </div>
        </div>

        <div class="form-row">
            <div class="form-group half-width">
                <label class="form-label" for="phone">Phone</label>
                <input
                    class="form-control"
                    type="tel"
                    id="phone"
                    bind:value={profile.phone}
                    disabled={!isEditing}
                />
            </div>
            <div class="form-group half-width">
                <label class="form-label" for="department">Department</label>
                <input
                    class="form-control"
                    type="text"
                    id="department"
                    bind:value={profile.department}
                    disabled={true}
                />
                <small class="muted-text">Department cannot be changed.</small>
            </div>
        </div>

        {#if profile.role === "UNDERGRADUATE"}
            <div class="form-row">
                <div class="form-group half-width">
                    <label class="form-label" for="registrationNumber"
                        >Reg (MC) No</label
                    >
                    <input
                        class="form-control"
                        type="text"
                        id="registrationNumber"
                        bind:value={profile.registrationNumber}
                        disabled={!isEditing}
                    />
                </div>
                <div class="form-group half-width">
                    <label class="form-label" for="cpmNumber">CPM No</label>
                    <input
                        class="form-control"
                        type="text"
                        id="cpmNumber"
                        bind:value={profile.cpmNumber}
                        disabled={!isEditing}
                    />
                </div>
            </div>
            <div class="form-group">
                <label class="form-label" for="yearOfStudy">Year of Study</label
                >
                <input
                    class="form-control"
                    type="text"
                    id="yearOfStudy"
                    bind:value={profile.yearOfStudy}
                    disabled={!isEditing}
                />
            </div>
        {/if}

        {#if isEditing}
            <div class="form-group">
                <label class="form-label" for="updateProfilePicture"
                    >Update Profile Picture</label
                >
                <input
                    class="form-control"
                    type="file"
                    id="updateProfilePicture"
                    accept="image/*"
                    on:change={handleFileChange}
                />
            </div>
        {/if}

        {#if isEditing}
            <div class="form-actions mt-2">
                <button
                    type="submit"
                    class="btn btn-primary"
                    disabled={isLoading}
                >
                    {isLoading ? "Saving..." : "Save Changes"}
                </button>
                <button
                    type="button"
                    class="btn btn-outline"
                    on:click={() => (isEditing = false)}
                >
                    Cancel
                </button>
            </div>
        {/if}
    </form>
</div>

<style>
    .profile-card {
        padding: 3rem;
    }

    .profile-header {
        display: flex;
        align-items: center;
        gap: 1.5rem;
        margin-bottom: 2rem;
        border-bottom: 1px solid var(--glass-border);
        padding-bottom: 2rem;
    }

    .avatar {
        width: 80px;
        height: 80px;
        border-radius: 50%;
        background: linear-gradient(
            135deg,
            var(--primary-light),
            var(--primary)
        );
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 2.5rem;
        font-weight: 700;
        color: white;
        box-shadow: 0 4px 15px rgba(0, 0, 0, 0.2);
    }

    .profile-title h2 {
        font-size: 1.75rem;
        margin-bottom: 0.25rem;
    }

    .role-badge {
        display: inline-block;
        padding: 0.25rem 0.75rem;
        background: rgba(
            15,
            23,
            42,
            0.05
        ); /* Darker translucent for light bg */
        border: 1px solid rgba(15, 23, 42, 0.1);
        border-radius: 20px;
        font-size: 0.8rem;
        color: var(--text-muted);
    }

    .edit-btn {
        margin-left: auto;
    }

    .form-row {
        display: flex;
        gap: 1.5rem;
        flex-wrap: wrap;
        margin-bottom: 0.5rem;
    }

    .half-width {
        flex: 1;
        min-width: 200px;
    }

    .form-control:disabled {
        opacity: 0.8;
        background-color: rgba(15, 23, 42, 0.03); /* Slight gray for disabled */
        color: var(--text-muted);
        cursor: not-allowed;
    }

    .muted-text {
        font-size: 0.75rem;
        color: var(--text-muted);
        opacity: 0.8;
    }

    .alert {
        padding: 1rem;
        border-radius: 8px;
        margin-bottom: 1.5rem;
        background: rgba(46, 204, 113, 0.1);
        border: 1px solid rgba(46, 204, 113, 0.3);
        color: #2ecc71;
    }

    .form-actions {
        display: flex;
        justify-content: flex-end;
        gap: 1rem;
    }

    .mt-2 {
        margin-top: 2rem;
    }
</style>
