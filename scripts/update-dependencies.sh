#!/usr/bin/env bash
# =============================================================================
# update-dependencies.sh
# =============================================================================
# Automatically updates Minecraft version, library dependencies in
# libs.versions.toml, and development mod download links in dev/build.gradle.
#
# Usage:
#   ./scripts/update-dependencies.sh [MC_VERSION]
#
# Environment:
#   AUTO_YES=1  Skip confirmation prompt and apply changes automatically.
# =============================================================================

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "$SCRIPT_DIR/common.sh"

require_tools curl jq python3 sed
verify_project_files

CURRENT_MC="$(get_toml_version "minecraft")"
[[ -n "$CURRENT_MC" ]] || die "Could not read minecraft version from $VERSIONS_FILE"

TARGET_MC="${1:-$CURRENT_MC}"

echo
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo -e "${BOLD}  Dystopia Mods — Dependency & Minecraft Version Updater${RESET}"
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo
info "Current Minecraft version : ${YELLOW}${CURRENT_MC}${RESET}"
info "Target  Minecraft version : ${GREEN}${TARGET_MC}${RESET}"
echo

# 1. Fabric Loader
info "Fetching latest stable Fabric Loader ..."
LOADER_JSON="$(fetch_http "${FABRIC_META_API}/versions/loader")"
NEW_LOADER="$(echo "$LOADER_JSON" | jq -r 'map(select(.stable == true)) | .[0].version')"
[[ -n "$NEW_LOADER" && "$NEW_LOADER" != "null" ]] || die "Could not resolve Fabric Loader version."
success "Fabric Loader       : ${NEW_LOADER}"

# 2. Fabric API
info "Fetching latest Fabric API for MC ${TARGET_MC} ..."
NEW_FABRIC_API="$(fetch_modrinth_version "P7dR8mSH" "$TARGET_MC")"
if [[ "$NEW_FABRIC_API" == "null" || -z "$NEW_FABRIC_API" ]]; then
    warn "No Fabric API found for MC ${TARGET_MC} — keeping current version."
    NEW_FABRIC_API="$(get_toml_version "fabric-api")"
else
    success "Fabric API          : ${NEW_FABRIC_API}"
fi

# 3. Fabric Permissions API
info "Fetching latest Fabric Permissions API ..."
NEW_FABRIC_PERMISSIONS="$(fetch_maven_release "me.lucko" "fabric-permissions-api")"
[[ -n "$NEW_FABRIC_PERMISSIONS" ]] || NEW_FABRIC_PERMISSIONS="$(get_toml_version "fabric-permissions")"
success "Fabric Permissions  : ${NEW_FABRIC_PERMISSIONS}"

# 4. EdenMC API
info "Fetching latest EdenMC API ..."
NEW_EDENMC_API="$(fetch_maven_release "xyz.memothelemo.edenmc" "api")"
[[ -n "$NEW_EDENMC_API" ]] || NEW_EDENMC_API="$(get_toml_version "edenmc-api")"
success "EdenMC API          : ${NEW_EDENMC_API}"

# 5. Polymer Core
info "Fetching latest Polymer for MC ${TARGET_MC} ..."
NEW_POLYMER="$(fetch_polymer_version "$TARGET_MC")"
if [[ -z "$NEW_POLYMER" ]]; then
    warn "No Polymer found for MC ${TARGET_MC} — keeping current version."
    NEW_POLYMER="$(get_toml_version "polymer")"
else
    success "Polymer Core        : ${NEW_POLYMER}"
fi

# 6. Adventure API & Platform
info "Fetching latest Adventure ..."
NEW_ADVENTURE_API="$(fetch_maven_release "net.kyori" "adventure-api")"
NEW_ADVENTURE_FABRIC="$(fetch_maven_release "net.kyori" "adventure-platform-fabric")"
[[ -n "$NEW_ADVENTURE_API" ]] || NEW_ADVENTURE_API="$(get_toml_version "adventure-api")"
[[ -n "$NEW_ADVENTURE_FABRIC" ]] || NEW_ADVENTURE_FABRIC="$(get_toml_version "adventure-fabric")"
success "Adventure API       : ${NEW_ADVENTURE_API}"
success "Adventure Fabric    : ${NEW_ADVENTURE_FABRIC}"

# 7. Development Environment Download Links
info "Fetching latest Floodgate download URL for MC ${TARGET_MC} ..."
NEW_FLOODGATE_URL="$(fetch_modrinth_file_url "bWrNNfkb" "$TARGET_MC")"
[[ "$NEW_FLOODGATE_URL" != "null" && -n "$NEW_FLOODGATE_URL" ]] || NEW_FLOODGATE_URL="$(get_dev_task_url "downloadFloodgate")"
success "Floodgate Jar       : ${NEW_FLOODGATE_URL}"

info "Fetching latest Geyser download URL for MC ${TARGET_MC} ..."
NEW_GEYSER_URL="$(fetch_modrinth_file_url "wKkoqHrH" "$TARGET_MC")"
[[ "$NEW_GEYSER_URL" != "null" && -n "$NEW_GEYSER_URL" ]] || NEW_GEYSER_URL="$(get_dev_task_url "downloadGeyser")"
success "Geyser Jar          : ${NEW_GEYSER_URL}"

info "Fetching latest LuckPerms download URL for MC ${TARGET_MC} ..."
NEW_LUCKPERMS_URL="$(fetch_modrinth_file_url "Vebnzrzj" "$TARGET_MC")"
[[ "$NEW_LUCKPERMS_URL" != "null" && -n "$NEW_LUCKPERMS_URL" ]] || NEW_LUCKPERMS_URL="$(get_dev_task_url "downloadLuckPerms")"
success "LuckPerms Jar       : ${NEW_LUCKPERMS_URL}"

info "Fetching latest Eden download URL from GitHub ..."
NEW_EDEN_URL="$(fetch_github_asset_url "Dystopiko/eden-minecraft" "^eden-.*\\.jar$")"
[[ "$NEW_EDEN_URL" != "null" && -n "$NEW_EDEN_URL" ]] || NEW_EDEN_URL="$(get_dev_task_url "downloadEden")"
success "Eden Jar            : ${NEW_EDEN_URL}"

# ── Summary & User Confirmation ───────────────────────────────────────────────

echo
echo -e "${BOLD}━━ Planned Changes Summary ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo -e "${BOLD}[ libs.versions.toml ]${RESET}"
printf "  %-20s : %b -> %b\n" "minecraft"          "${YELLOW}$(get_toml_version "minecraft")${RESET}"          "${GREEN}${TARGET_MC}${RESET}"
printf "  %-20s : %b -> %b\n" "fabric-loader"      "${YELLOW}$(get_toml_version "fabric-loader")${RESET}"      "${GREEN}${NEW_LOADER}${RESET}"
printf "  %-20s : %b -> %b\n" "fabric-api"         "${YELLOW}$(get_toml_version "fabric-api")${RESET}"         "${GREEN}${NEW_FABRIC_API}${RESET}"
printf "  %-20s : %b -> %b\n" "fabric-permissions" "${YELLOW}$(get_toml_version "fabric-permissions")${RESET}" "${GREEN}${NEW_FABRIC_PERMISSIONS}${RESET}"
printf "  %-20s : %b -> %b\n" "edenmc-api"         "${YELLOW}$(get_toml_version "edenmc-api")${RESET}"         "${GREEN}${NEW_EDENMC_API}${RESET}"
printf "  %-20s : %b -> %b\n" "polymer"            "${YELLOW}$(get_toml_version "polymer")${RESET}"            "${GREEN}${NEW_POLYMER}${RESET}"
printf "  %-20s : %b -> %b\n" "adventure-api"      "${YELLOW}$(get_toml_version "adventure-api")${RESET}"      "${GREEN}${NEW_ADVENTURE_API}${RESET}"
printf "  %-20s : %b -> %b\n" "adventure-fabric"   "${YELLOW}$(get_toml_version "adventure-fabric")${RESET}"   "${GREEN}${NEW_ADVENTURE_FABRIC}${RESET}"

echo
echo -e "${BOLD}[ dev/build.gradle ]${RESET}"
printf "  %-20s : %b -> %b\n" "downloadFloodgate"  "${YELLOW}$(get_dev_task_url "downloadFloodgate")${RESET}"  "${GREEN}${NEW_FLOODGATE_URL}${RESET}"
printf "  %-20s : %b -> %b\n" "downloadGeyser"     "${YELLOW}$(get_dev_task_url "downloadGeyser")${RESET}"     "${GREEN}${NEW_GEYSER_URL}${RESET}"
printf "  %-20s : %b -> %b\n" "downloadLuckPerms"  "${YELLOW}$(get_dev_task_url "downloadLuckPerms")${RESET}"  "${GREEN}${NEW_LUCKPERMS_URL}${RESET}"
printf "  %-20s : %b -> %b\n" "downloadEden"       "${YELLOW}$(get_dev_task_url "downloadEden")${RESET}"       "${GREEN}${NEW_EDEN_URL}${RESET}"
echo

if [[ "${AUTO_YES:-0}" != "1" ]]; then
    read -rp "$(echo -e "${BOLD}Apply these changes? [y/N]${RESET} ")" CONFIRM
    [[ "$CONFIRM" =~ ^[Yy]$ ]] || { echo "Aborted — no files were modified."; exit 0; }
fi
echo

# ── Apply Changes ─────────────────────────────────────────────────────────────

info "Patching libs.versions.toml ..."
set_toml_version "minecraft"          "$TARGET_MC"
set_toml_version "fabric-loader"      "$NEW_LOADER"
set_toml_version "fabric-api"         "$NEW_FABRIC_API"
set_toml_version "fabric-permissions" "$NEW_FABRIC_PERMISSIONS"
set_toml_version "edenmc-api"         "$NEW_EDENMC_API"
set_toml_version "polymer"            "$NEW_POLYMER"
set_toml_version "adventure-api"      "$NEW_ADVENTURE_API"
set_toml_version "adventure-fabric"   "$NEW_ADVENTURE_FABRIC"
success "libs.versions.toml updated successfully."

info "Patching dev/build.gradle ..."
set_dev_task_url "downloadFloodgate" "$NEW_FLOODGATE_URL"
set_dev_task_url "downloadGeyser"    "$NEW_GEYSER_URL"
set_dev_task_url "downloadLuckPerms" "$NEW_LUCKPERMS_URL"
set_dev_task_url "downloadEden"      "$NEW_EDEN_URL"
success "dev/build.gradle updated successfully."

echo
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
success "All dependencies successfully updated!  MC ${YELLOW}${CURRENT_MC}${RESET} -> ${GREEN}${TARGET_MC}${RESET}"
echo -e "${BOLD}━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━${RESET}"
echo
info "Suggested next steps:"
echo "  git diff                           # review all patched files"
echo "  ./gradlew build                    # build all subprojects"
echo
