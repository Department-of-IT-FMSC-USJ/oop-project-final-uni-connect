#!/bin/bash
set -e

SRC="/Users/tharukagunaratne/Desktop/oop-project-final-uni-connect-main"
REPO="/Users/tharukagunaratne/Desktop/oop-project-final-uni-connect-main/.uni-connect-temp"

cd "$REPO"
git fetch origin

echo "============================================="
echo "Step 1: Revert main to pre-merge state"
echo "============================================="

# Force main back to the last good commit (44c112e)
git checkout main
git reset --hard 44c112e
git push origin main --force

echo "✅ Main branch restored to commit 44c112e"

echo ""
echo "============================================="
echo "Step 2: Delete old broken branches from remote"
echo "============================================="

BRANCHES=(
    "academic-mentor/FE-009"
    "academic-mentor/BE-009"
    "academic-mentor/FE-010"
    "academic-mentor/BE-010"
    "academic-mentor/FE-011"
    "academic-mentor/BE-011"
    "industry-mentor/FE-012"
    "industry-mentor/BE-012"
    "industry-mentor/FE-013"
    "industry-mentor/BE-013"
    "industry-mentor/FE-014"
    "industry-mentor/BE-014"
    "hod-portal/FE-019"
    "hod-portal/BE-019"
    "hod-portal/FE-020"
    "hod-portal/BE-020"
    "hod-portal/FE-021"
    "hod-portal/BE-021"
)

for branch in "${BRANCHES[@]}"; do
    echo "Deleting remote branch: $branch"
    git push origin --delete "$branch" 2>/dev/null || echo "  (branch $branch not found on remote, skipping)"
    git branch -D "$branch" 2>/dev/null || true
done

echo "✅ Old branches deleted"

echo ""
echo "============================================="
echo "Step 3: Recreate branches PROPERLY"
echo "  (keeping all main files, only ADDING new files)"
echo "============================================="

##############################
# HELPER FUNCTIONS
##############################

create_branch() {
    local branch_name="$1"
    echo ""
    echo "---------------------------------------------"
    echo "Creating branch: $branch_name"
    echo "---------------------------------------------"
    git checkout main
    git checkout -b "$branch_name"
}

commit_and_push() {
    local branch_name="$1"
    local commit_msg="$2"
    git add -A
    git commit -m "$commit_msg"
    git push origin "$branch_name"
    echo "✅ Pushed branch: $branch_name"
}

##############################
# ACADEMIC MENTOR BRANCHES
# (Only ADD files on top of main, never delete)
##############################

# --- academic-mentor/FE-009 ---
create_branch "academic-mentor/FE-009"
mkdir -p academic-mentor/frontend/src/modules/FE-009
mkdir -p academic-mentor/frontend/src/pages
mkdir -p academic-mentor/frontend/src/layouts
mkdir -p academic-mentor/frontend/public
cp "$SRC/academic-mentor/frontend/package.json" academic-mentor/frontend/
cp "$SRC/academic-mentor/frontend/astro.config.mjs" academic-mentor/frontend/
cp "$SRC/academic-mentor/frontend/tsconfig.json" academic-mentor/frontend/
cp "$SRC/academic-mentor/frontend/src/layouts/Layout.astro" academic-mentor/frontend/src/layouts/
cp "$SRC/academic-mentor/frontend/src/pages/index.astro" academic-mentor/frontend/src/pages/
[ -f "$SRC/academic-mentor/frontend/src/env.d.ts" ] && cp "$SRC/academic-mentor/frontend/src/env.d.ts" academic-mentor/frontend/src/
cp -r "$SRC/academic-mentor/frontend/src/modules/FE-009/"* academic-mentor/frontend/src/modules/FE-009/
cp "$SRC/academic-mentor/frontend/src/pages/sessions.astro" academic-mentor/frontend/src/pages/
commit_and_push "academic-mentor/FE-009" "feat(academic-mentor): Mentoring Session Creation Interface [FE-009]"

# --- academic-mentor/BE-009 ---
create_branch "academic-mentor/BE-009"
mkdir -p academic-mentor/backend/src/main/java/com/academicmentor/common/dto
mkdir -p academic-mentor/backend/src/main/java/com/academicmentor/common/exception
mkdir -p academic-mentor/backend/src/main/java/com/academicmentor/config
mkdir -p academic-mentor/backend/src/main/java/com/academicmentor/modules/sessions
mkdir -p academic-mentor/backend/src/main/resources
cp "$SRC/academic-mentor/backend/pom.xml" academic-mentor/backend/
cp "$SRC/academic-mentor/backend/src/main/java/com/academicmentor/Application.java" academic-mentor/backend/src/main/java/com/academicmentor/
cp "$SRC/academic-mentor/backend/src/main/java/com/academicmentor/common/dto/ApiResponseDTO.java" academic-mentor/backend/src/main/java/com/academicmentor/common/dto/
cp "$SRC/academic-mentor/backend/src/main/java/com/academicmentor/common/exception/"*.java academic-mentor/backend/src/main/java/com/academicmentor/common/exception/
cp "$SRC/academic-mentor/backend/src/main/java/com/academicmentor/config/CorsConfig.java" academic-mentor/backend/src/main/java/com/academicmentor/config/
cp "$SRC/academic-mentor/backend/src/main/resources/application.properties" academic-mentor/backend/src/main/resources/
cp -r "$SRC/academic-mentor/backend/src/main/java/com/academicmentor/modules/sessions/"* academic-mentor/backend/src/main/java/com/academicmentor/modules/sessions/
commit_and_push "academic-mentor/BE-009" "feat(academic-mentor): Mentoring Session Creation Processing [BE-009]"

# --- academic-mentor/FE-010 ---
create_branch "academic-mentor/FE-010"
mkdir -p academic-mentor/frontend/src/modules/FE-010
mkdir -p academic-mentor/frontend/src/pages
mkdir -p academic-mentor/frontend/src/layouts
cp "$SRC/academic-mentor/frontend/package.json" academic-mentor/frontend/
cp "$SRC/academic-mentor/frontend/astro.config.mjs" academic-mentor/frontend/
cp "$SRC/academic-mentor/frontend/tsconfig.json" academic-mentor/frontend/
cp "$SRC/academic-mentor/frontend/src/layouts/Layout.astro" academic-mentor/frontend/src/layouts/
cp "$SRC/academic-mentor/frontend/src/pages/index.astro" academic-mentor/frontend/src/pages/
[ -f "$SRC/academic-mentor/frontend/src/env.d.ts" ] && cp "$SRC/academic-mentor/frontend/src/env.d.ts" academic-mentor/frontend/src/
cp -r "$SRC/academic-mentor/frontend/src/modules/FE-010/"* academic-mentor/frontend/src/modules/FE-010/
cp "$SRC/academic-mentor/frontend/src/pages/allocations.astro" academic-mentor/frontend/src/pages/
commit_and_push "academic-mentor/FE-010" "feat(academic-mentor): Student Allocation Dashboard [FE-010]"

# --- academic-mentor/BE-010 ---
create_branch "academic-mentor/BE-010"
mkdir -p academic-mentor/backend/src/main/java/com/academicmentor/common/dto
mkdir -p academic-mentor/backend/src/main/java/com/academicmentor/common/exception
mkdir -p academic-mentor/backend/src/main/java/com/academicmentor/config
mkdir -p academic-mentor/backend/src/main/java/com/academicmentor/modules/allocations
mkdir -p academic-mentor/backend/src/main/resources
cp "$SRC/academic-mentor/backend/pom.xml" academic-mentor/backend/
cp "$SRC/academic-mentor/backend/src/main/java/com/academicmentor/Application.java" academic-mentor/backend/src/main/java/com/academicmentor/
cp "$SRC/academic-mentor/backend/src/main/java/com/academicmentor/common/dto/ApiResponseDTO.java" academic-mentor/backend/src/main/java/com/academicmentor/common/dto/
cp "$SRC/academic-mentor/backend/src/main/java/com/academicmentor/common/exception/"*.java academic-mentor/backend/src/main/java/com/academicmentor/common/exception/
cp "$SRC/academic-mentor/backend/src/main/java/com/academicmentor/config/CorsConfig.java" academic-mentor/backend/src/main/java/com/academicmentor/config/
cp "$SRC/academic-mentor/backend/src/main/resources/application.properties" academic-mentor/backend/src/main/resources/
cp -r "$SRC/academic-mentor/backend/src/main/java/com/academicmentor/modules/allocations/"* academic-mentor/backend/src/main/java/com/academicmentor/modules/allocations/
commit_and_push "academic-mentor/BE-010" "feat(academic-mentor): Automatic Student Allocation Processing [BE-010]"

# --- academic-mentor/FE-011 ---
create_branch "academic-mentor/FE-011"
mkdir -p academic-mentor/frontend/src/modules/FE-011
mkdir -p academic-mentor/frontend/src/pages
mkdir -p academic-mentor/frontend/src/layouts
cp "$SRC/academic-mentor/frontend/package.json" academic-mentor/frontend/
cp "$SRC/academic-mentor/frontend/astro.config.mjs" academic-mentor/frontend/
cp "$SRC/academic-mentor/frontend/tsconfig.json" academic-mentor/frontend/
cp "$SRC/academic-mentor/frontend/src/layouts/Layout.astro" academic-mentor/frontend/src/layouts/
cp "$SRC/academic-mentor/frontend/src/pages/index.astro" academic-mentor/frontend/src/pages/
[ -f "$SRC/academic-mentor/frontend/src/env.d.ts" ] && cp "$SRC/academic-mentor/frontend/src/env.d.ts" academic-mentor/frontend/src/
cp -r "$SRC/academic-mentor/frontend/src/modules/FE-011/"* academic-mentor/frontend/src/modules/FE-011/
cp "$SRC/academic-mentor/frontend/src/pages/contact.astro" academic-mentor/frontend/src/pages/
commit_and_push "academic-mentor/FE-011" "feat(academic-mentor): Mentor Contact Information Display [FE-011]"

# --- academic-mentor/BE-011 ---
create_branch "academic-mentor/BE-011"
mkdir -p academic-mentor/backend/src/main/java/com/academicmentor/common/dto
mkdir -p academic-mentor/backend/src/main/java/com/academicmentor/common/exception
mkdir -p academic-mentor/backend/src/main/java/com/academicmentor/config
mkdir -p academic-mentor/backend/src/main/java/com/academicmentor/modules/contactinfo
mkdir -p academic-mentor/backend/src/main/resources
cp "$SRC/academic-mentor/backend/pom.xml" academic-mentor/backend/
cp "$SRC/academic-mentor/backend/src/main/java/com/academicmentor/Application.java" academic-mentor/backend/src/main/java/com/academicmentor/
cp "$SRC/academic-mentor/backend/src/main/java/com/academicmentor/common/dto/ApiResponseDTO.java" academic-mentor/backend/src/main/java/com/academicmentor/common/dto/
cp "$SRC/academic-mentor/backend/src/main/java/com/academicmentor/common/exception/"*.java academic-mentor/backend/src/main/java/com/academicmentor/common/exception/
cp "$SRC/academic-mentor/backend/src/main/java/com/academicmentor/config/CorsConfig.java" academic-mentor/backend/src/main/java/com/academicmentor/config/
cp "$SRC/academic-mentor/backend/src/main/resources/application.properties" academic-mentor/backend/src/main/resources/
cp -r "$SRC/academic-mentor/backend/src/main/java/com/academicmentor/modules/contactinfo/"* academic-mentor/backend/src/main/java/com/academicmentor/modules/contactinfo/
commit_and_push "academic-mentor/BE-011" "feat(academic-mentor): Mentor Contact Information Management [BE-011]"


##############################
# INDUSTRY MENTOR BRANCHES
##############################

# --- industry-mentor/FE-012 ---
create_branch "industry-mentor/FE-012"
mkdir -p industry-mentor/frontend/src/modules/FE-012
mkdir -p industry-mentor/frontend/src/pages
mkdir -p industry-mentor/frontend/src/layouts
cp "$SRC/industry-mentor/frontend/package.json" industry-mentor/frontend/
cp "$SRC/industry-mentor/frontend/astro.config.mjs" industry-mentor/frontend/
cp "$SRC/industry-mentor/frontend/tsconfig.json" industry-mentor/frontend/
cp "$SRC/industry-mentor/frontend/src/layouts/Layout.astro" industry-mentor/frontend/src/layouts/
cp "$SRC/industry-mentor/frontend/src/pages/index.astro" industry-mentor/frontend/src/pages/
[ -f "$SRC/industry-mentor/frontend/src/env.d.ts" ] && cp "$SRC/industry-mentor/frontend/src/env.d.ts" industry-mentor/frontend/src/
cp -r "$SRC/industry-mentor/frontend/src/modules/FE-012/"* industry-mentor/frontend/src/modules/FE-012/
cp "$SRC/industry-mentor/frontend/src/pages/sessions.astro" industry-mentor/frontend/src/pages/
commit_and_push "industry-mentor/FE-012" "feat(industry-mentor): Industry Mentor Session Creation Interface [FE-012]"

# --- industry-mentor/BE-012 ---
create_branch "industry-mentor/BE-012"
mkdir -p industry-mentor/backend/src/main/java/com/industrymentor/common/dto
mkdir -p industry-mentor/backend/src/main/java/com/industrymentor/common/exception
mkdir -p industry-mentor/backend/src/main/java/com/industrymentor/config
mkdir -p industry-mentor/backend/src/main/java/com/industrymentor/modules/sessions
mkdir -p industry-mentor/backend/src/main/resources
cp "$SRC/industry-mentor/backend/pom.xml" industry-mentor/backend/
cp "$SRC/industry-mentor/backend/src/main/java/com/industrymentor/Application.java" industry-mentor/backend/src/main/java/com/industrymentor/
cp "$SRC/industry-mentor/backend/src/main/java/com/industrymentor/common/dto/ApiResponseDTO.java" industry-mentor/backend/src/main/java/com/industrymentor/common/dto/
cp "$SRC/industry-mentor/backend/src/main/java/com/industrymentor/common/exception/"*.java industry-mentor/backend/src/main/java/com/industrymentor/common/exception/
cp "$SRC/industry-mentor/backend/src/main/java/com/industrymentor/config/CorsConfig.java" industry-mentor/backend/src/main/java/com/industrymentor/config/
cp "$SRC/industry-mentor/backend/src/main/resources/application.properties" industry-mentor/backend/src/main/resources/
cp -r "$SRC/industry-mentor/backend/src/main/java/com/industrymentor/modules/sessions/"* industry-mentor/backend/src/main/java/com/industrymentor/modules/sessions/
commit_and_push "industry-mentor/BE-012" "feat(industry-mentor): Industry Mentor Session Management [BE-012]"

# --- industry-mentor/FE-013 ---
create_branch "industry-mentor/FE-013"
mkdir -p industry-mentor/frontend/src/modules/FE-013
mkdir -p industry-mentor/frontend/src/pages
mkdir -p industry-mentor/frontend/src/layouts
cp "$SRC/industry-mentor/frontend/package.json" industry-mentor/frontend/
cp "$SRC/industry-mentor/frontend/astro.config.mjs" industry-mentor/frontend/
cp "$SRC/industry-mentor/frontend/tsconfig.json" industry-mentor/frontend/
cp "$SRC/industry-mentor/frontend/src/layouts/Layout.astro" industry-mentor/frontend/src/layouts/
cp "$SRC/industry-mentor/frontend/src/pages/index.astro" industry-mentor/frontend/src/pages/
[ -f "$SRC/industry-mentor/frontend/src/env.d.ts" ] && cp "$SRC/industry-mentor/frontend/src/env.d.ts" industry-mentor/frontend/src/
cp -r "$SRC/industry-mentor/frontend/src/modules/FE-013/"* industry-mentor/frontend/src/modules/FE-013/
cp "$SRC/industry-mentor/frontend/src/pages/suggestions.astro" industry-mentor/frontend/src/pages/
commit_and_push "industry-mentor/FE-013" "feat(industry-mentor): Curriculum Suggestion Submission Interface [FE-013]"

# --- industry-mentor/BE-013 ---
create_branch "industry-mentor/BE-013"
mkdir -p industry-mentor/backend/src/main/java/com/industrymentor/common/dto
mkdir -p industry-mentor/backend/src/main/java/com/industrymentor/common/exception
mkdir -p industry-mentor/backend/src/main/java/com/industrymentor/config
mkdir -p industry-mentor/backend/src/main/java/com/industrymentor/modules/suggestions
mkdir -p industry-mentor/backend/src/main/resources
cp "$SRC/industry-mentor/backend/pom.xml" industry-mentor/backend/
cp "$SRC/industry-mentor/backend/src/main/java/com/industrymentor/Application.java" industry-mentor/backend/src/main/java/com/industrymentor/
cp "$SRC/industry-mentor/backend/src/main/java/com/industrymentor/common/dto/ApiResponseDTO.java" industry-mentor/backend/src/main/java/com/industrymentor/common/dto/
cp "$SRC/industry-mentor/backend/src/main/java/com/industrymentor/common/exception/"*.java industry-mentor/backend/src/main/java/com/industrymentor/common/exception/
cp "$SRC/industry-mentor/backend/src/main/java/com/industrymentor/config/CorsConfig.java" industry-mentor/backend/src/main/java/com/industrymentor/config/
cp "$SRC/industry-mentor/backend/src/main/resources/application.properties" industry-mentor/backend/src/main/resources/
cp -r "$SRC/industry-mentor/backend/src/main/java/com/industrymentor/modules/suggestions/"* industry-mentor/backend/src/main/java/com/industrymentor/modules/suggestions/
commit_and_push "industry-mentor/BE-013" "feat(industry-mentor): Curriculum Suggestion Management [BE-013]"

# --- industry-mentor/FE-014 ---
create_branch "industry-mentor/FE-014"
mkdir -p industry-mentor/frontend/src/modules/FE-014
mkdir -p industry-mentor/frontend/src/pages
mkdir -p industry-mentor/frontend/src/layouts
cp "$SRC/industry-mentor/frontend/package.json" industry-mentor/frontend/
cp "$SRC/industry-mentor/frontend/astro.config.mjs" industry-mentor/frontend/
cp "$SRC/industry-mentor/frontend/tsconfig.json" industry-mentor/frontend/
cp "$SRC/industry-mentor/frontend/src/layouts/Layout.astro" industry-mentor/frontend/src/layouts/
cp "$SRC/industry-mentor/frontend/src/pages/index.astro" industry-mentor/frontend/src/pages/
[ -f "$SRC/industry-mentor/frontend/src/env.d.ts" ] && cp "$SRC/industry-mentor/frontend/src/env.d.ts" industry-mentor/frontend/src/
cp -r "$SRC/industry-mentor/frontend/src/modules/FE-014/"* industry-mentor/frontend/src/modules/FE-014/
cp "$SRC/industry-mentor/frontend/src/pages/rewards.astro" industry-mentor/frontend/src/pages/
commit_and_push "industry-mentor/FE-014" "feat(industry-mentor): Industry Mentor Reward Points Dashboard [FE-014]"

# --- industry-mentor/BE-014 ---
create_branch "industry-mentor/BE-014"
mkdir -p industry-mentor/backend/src/main/java/com/industrymentor/common/dto
mkdir -p industry-mentor/backend/src/main/java/com/industrymentor/common/exception
mkdir -p industry-mentor/backend/src/main/java/com/industrymentor/config
mkdir -p industry-mentor/backend/src/main/java/com/industrymentor/modules/rewards
mkdir -p industry-mentor/backend/src/main/resources
cp "$SRC/industry-mentor/backend/pom.xml" industry-mentor/backend/
cp "$SRC/industry-mentor/backend/src/main/java/com/industrymentor/Application.java" industry-mentor/backend/src/main/java/com/industrymentor/
cp "$SRC/industry-mentor/backend/src/main/java/com/industrymentor/common/dto/ApiResponseDTO.java" industry-mentor/backend/src/main/java/com/industrymentor/common/dto/
cp "$SRC/industry-mentor/backend/src/main/java/com/industrymentor/common/exception/"*.java industry-mentor/backend/src/main/java/com/industrymentor/common/exception/
cp "$SRC/industry-mentor/backend/src/main/java/com/industrymentor/config/CorsConfig.java" industry-mentor/backend/src/main/java/com/industrymentor/config/
cp "$SRC/industry-mentor/backend/src/main/resources/application.properties" industry-mentor/backend/src/main/resources/
cp -r "$SRC/industry-mentor/backend/src/main/java/com/industrymentor/modules/rewards/"* industry-mentor/backend/src/main/java/com/industrymentor/modules/rewards/
commit_and_push "industry-mentor/BE-014" "feat(industry-mentor): Mentor Reward Point Calculation [BE-014]"


##############################
# HOD PORTAL BRANCHES
##############################

# --- hod-portal/FE-019 ---
create_branch "hod-portal/FE-019"
mkdir -p hod-portal/frontend/src/modules/FE-019
mkdir -p hod-portal/frontend/src/pages
mkdir -p hod-portal/frontend/src/layouts
cp "$SRC/hod-portal/frontend/package.json" hod-portal/frontend/
cp "$SRC/hod-portal/frontend/astro.config.mjs" hod-portal/frontend/
cp "$SRC/hod-portal/frontend/tsconfig.json" hod-portal/frontend/
cp "$SRC/hod-portal/frontend/src/layouts/Layout.astro" hod-portal/frontend/src/layouts/
cp "$SRC/hod-portal/frontend/src/pages/index.astro" hod-portal/frontend/src/pages/
[ -f "$SRC/hod-portal/frontend/src/env.d.ts" ] && cp "$SRC/hod-portal/frontend/src/env.d.ts" hod-portal/frontend/src/
cp -r "$SRC/hod-portal/frontend/src/modules/FE-019/"* hod-portal/frontend/src/modules/FE-019/
cp "$SRC/hod-portal/frontend/src/pages/mentor-verification.astro" hod-portal/frontend/src/pages/
commit_and_push "hod-portal/FE-019" "feat(hod-portal): Industry Mentor Verification Interface [FE-019]"

# --- hod-portal/BE-019 ---
create_branch "hod-portal/BE-019"
mkdir -p hod-portal/backend/src/main/java/com/hodportal/common/dto
mkdir -p hod-portal/backend/src/main/java/com/hodportal/common/exception
mkdir -p hod-portal/backend/src/main/java/com/hodportal/config
mkdir -p hod-portal/backend/src/main/java/com/hodportal/modules/mentorverification
mkdir -p hod-portal/backend/src/main/resources
cp "$SRC/hod-portal/backend/pom.xml" hod-portal/backend/
cp "$SRC/hod-portal/backend/src/main/java/com/hodportal/Application.java" hod-portal/backend/src/main/java/com/hodportal/
cp "$SRC/hod-portal/backend/src/main/java/com/hodportal/common/dto/ApiResponseDTO.java" hod-portal/backend/src/main/java/com/hodportal/common/dto/
cp "$SRC/hod-portal/backend/src/main/java/com/hodportal/common/exception/"*.java hod-portal/backend/src/main/java/com/hodportal/common/exception/
cp "$SRC/hod-portal/backend/src/main/java/com/hodportal/config/CorsConfig.java" hod-portal/backend/src/main/java/com/hodportal/config/
cp "$SRC/hod-portal/backend/src/main/resources/application.properties" hod-portal/backend/src/main/resources/
cp -r "$SRC/hod-portal/backend/src/main/java/com/hodportal/modules/mentorverification/"* hod-portal/backend/src/main/java/com/hodportal/modules/mentorverification/
commit_and_push "hod-portal/BE-019" "feat(hod-portal): Industry Mentor Verification Management [BE-019]"

# --- hod-portal/FE-020 ---
create_branch "hod-portal/FE-020"
mkdir -p hod-portal/frontend/src/modules/FE-020
mkdir -p hod-portal/frontend/src/pages
mkdir -p hod-portal/frontend/src/layouts
cp "$SRC/hod-portal/frontend/package.json" hod-portal/frontend/
cp "$SRC/hod-portal/frontend/astro.config.mjs" hod-portal/frontend/
cp "$SRC/hod-portal/frontend/tsconfig.json" hod-portal/frontend/
cp "$SRC/hod-portal/frontend/src/layouts/Layout.astro" hod-portal/frontend/src/layouts/
cp "$SRC/hod-portal/frontend/src/pages/index.astro" hod-portal/frontend/src/pages/
[ -f "$SRC/hod-portal/frontend/src/env.d.ts" ] && cp "$SRC/hod-portal/frontend/src/env.d.ts" hod-portal/frontend/src/
cp -r "$SRC/hod-portal/frontend/src/modules/FE-020/"* hod-portal/frontend/src/modules/FE-020/
cp "$SRC/hod-portal/frontend/src/pages/feedback.astro" hod-portal/frontend/src/pages/
commit_and_push "hod-portal/FE-020" "feat(hod-portal): Feedback Review Dashboard [FE-020]"

# --- hod-portal/BE-020 ---
create_branch "hod-portal/BE-020"
mkdir -p hod-portal/backend/src/main/java/com/hodportal/common/dto
mkdir -p hod-portal/backend/src/main/java/com/hodportal/common/exception
mkdir -p hod-portal/backend/src/main/java/com/hodportal/config
mkdir -p hod-portal/backend/src/main/java/com/hodportal/modules/feedbackreview
mkdir -p hod-portal/backend/src/main/resources
cp "$SRC/hod-portal/backend/pom.xml" hod-portal/backend/
cp "$SRC/hod-portal/backend/src/main/java/com/hodportal/Application.java" hod-portal/backend/src/main/java/com/hodportal/
cp "$SRC/hod-portal/backend/src/main/java/com/hodportal/common/dto/ApiResponseDTO.java" hod-portal/backend/src/main/java/com/hodportal/common/dto/
cp "$SRC/hod-portal/backend/src/main/java/com/hodportal/common/exception/"*.java hod-portal/backend/src/main/java/com/hodportal/common/exception/
cp "$SRC/hod-portal/backend/src/main/java/com/hodportal/config/CorsConfig.java" hod-portal/backend/src/main/java/com/hodportal/config/
cp "$SRC/hod-portal/backend/src/main/resources/application.properties" hod-portal/backend/src/main/resources/
cp -r "$SRC/hod-portal/backend/src/main/java/com/hodportal/modules/feedbackreview/"* hod-portal/backend/src/main/java/com/hodportal/modules/feedbackreview/
commit_and_push "hod-portal/BE-020" "feat(hod-portal): Feedback Review Processing [BE-020]"

# --- hod-portal/FE-021 ---
create_branch "hod-portal/FE-021"
mkdir -p hod-portal/frontend/src/modules/FE-021
mkdir -p hod-portal/frontend/src/pages
mkdir -p hod-portal/frontend/src/layouts
cp "$SRC/hod-portal/frontend/package.json" hod-portal/frontend/
cp "$SRC/hod-portal/frontend/astro.config.mjs" hod-portal/frontend/
cp "$SRC/hod-portal/frontend/tsconfig.json" hod-portal/frontend/
cp "$SRC/hod-portal/frontend/src/layouts/Layout.astro" hod-portal/frontend/src/layouts/
cp "$SRC/hod-portal/frontend/src/pages/index.astro" hod-portal/frontend/src/pages/
[ -f "$SRC/hod-portal/frontend/src/env.d.ts" ] && cp "$SRC/hod-portal/frontend/src/env.d.ts" hod-portal/frontend/src/
cp -r "$SRC/hod-portal/frontend/src/modules/FE-021/"* hod-portal/frontend/src/modules/FE-021/
cp "$SRC/hod-portal/frontend/src/pages/contributions.astro" hod-portal/frontend/src/pages/
commit_and_push "hod-portal/FE-021" "feat(hod-portal): Contribution Record Review Interface [FE-021]"

# --- hod-portal/BE-021 ---
create_branch "hod-portal/BE-021"
mkdir -p hod-portal/backend/src/main/java/com/hodportal/common/dto
mkdir -p hod-portal/backend/src/main/java/com/hodportal/common/exception
mkdir -p hod-portal/backend/src/main/java/com/hodportal/config
mkdir -p hod-portal/backend/src/main/java/com/hodportal/modules/contributionreview
mkdir -p hod-portal/backend/src/main/resources
cp "$SRC/hod-portal/backend/pom.xml" hod-portal/backend/
cp "$SRC/hod-portal/backend/src/main/java/com/hodportal/Application.java" hod-portal/backend/src/main/java/com/hodportal/
cp "$SRC/hod-portal/backend/src/main/java/com/hodportal/common/dto/ApiResponseDTO.java" hod-portal/backend/src/main/java/com/hodportal/common/dto/
cp "$SRC/hod-portal/backend/src/main/java/com/hodportal/common/exception/"*.java hod-portal/backend/src/main/java/com/hodportal/common/exception/
cp "$SRC/hod-portal/backend/src/main/java/com/hodportal/config/CorsConfig.java" hod-portal/backend/src/main/java/com/hodportal/config/
cp "$SRC/hod-portal/backend/src/main/resources/application.properties" hod-portal/backend/src/main/resources/
cp -r "$SRC/hod-portal/backend/src/main/java/com/hodportal/modules/contributionreview/"* hod-portal/backend/src/main/java/com/hodportal/modules/contributionreview/
commit_and_push "hod-portal/BE-021" "feat(hod-portal): Final Contribution Approval Processing [BE-021]"


##############################
# VERIFICATION
##############################

echo ""
echo "============================================="
echo "✅ ALL DONE! Main restored + 18 branches recreated"
echo "============================================="
echo ""
echo "Main branch content:"
git checkout main
git ls-tree --name-only HEAD
echo ""
echo "All branches on remote:"
git ls-remote --heads origin 2>&1 | grep -E "(academic-mentor|industry-mentor|hod-portal)/(FE|BE)-0"
