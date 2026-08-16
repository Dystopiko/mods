#!/usr/bin/env bash
# =============================================================================
# common.sh — Shared utility functions for Dystopia build & update scripts
# =============================================================================

[[ -n "${_DYSTOPIA_COMMON_SH_LOADED:-}" ]] && return 0
_DYSTOPIA_COMMON_SH_LOADED=1

# ── Color & Log Formatting ───────────────────────────────────────────────────
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
BOLD='\033[1m'
RESET='\033[0m'

info()    { echo -e "${CYAN}${BOLD}[INFO]${RESET}  $*"; }
success() { echo -e "${GREEN}${BOLD}[ OK ]${RESET}  $*"; }
warn()    { echo -e "${YELLOW}${BOLD}[WARN]${RESET}  $*"; }
error()   { echo -e "${RED}${BOLD}[ERR]${RESET}   $*" >&2; }
die()     { error "$*"; exit 1; }

# ── Dependency Check ─────────────────────────────────────────────────────────
require_tools() {
    for tool in "$@"; do
        command -v "$tool" &>/dev/null || die "Missing required command: $tool"
    done
}

# ── Project Paths & Verification ─────────────────────────────────────────────
COMMON_SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$COMMON_SCRIPT_DIR/.." && pwd)"
VERSIONS_FILE="$PROJECT_ROOT/libs.versions.toml"
DEV_BUILD_GRADLE="$PROJECT_ROOT/dev/build.gradle"

verify_project_files() {
    [[ -f "$VERSIONS_FILE" ]] || die "Missing versions file: $VERSIONS_FILE"
    [[ -f "$DEV_BUILD_GRADLE" ]] || die "Missing dev build script: $DEV_BUILD_GRADLE"
}

# ── File Readers & Write Patchers ────────────────────────────────────────────

get_toml_version() {
    grep -E "^${1}\s*=" "$VERSIONS_FILE" | sed -E 's/.*=\s*"([^"]+)".*/\1/' | head -1
}

set_toml_version() {
    local key="$1" value="$2"
    sed -i -E "s|^(${key}[[:space:]]*=[[:space:]]*)\"[^\"]+\"|\1\"${value}\"|" "$VERSIONS_FILE"
}

get_dev_task_url() {
    local task="$1"
    python3 -c '
import sys, re
path, task_name = sys.argv[1], sys.argv[2]
with open(path, "r", encoding="utf-8") as f:
    content = f.read()
match = re.search(rf"tasks\.register\(\"{task_name}\",\s*DownloadTask\)\s*{{\s*(?://[^\n]*\n\s*)?url\s*=\s*\"([^\"]+)\"", content)
print(match.group(1) if match else "")
' "$DEV_BUILD_GRADLE" "$task"
}

set_dev_task_url() {
    local task="$1" url="$2"
    python3 -c '
import sys, re
path, task_name, new_url = sys.argv[1], sys.argv[2], sys.argv[3]
with open(path, "r", encoding="utf-8") as f:
    content = f.read()
pattern = rf"(tasks\.register\(\"{task_name}\",\s*DownloadTask\)\s*{{\s*(?://[^\n]*\n\s*)?url\s*=\s*\")[^\"]+(\")"
content = re.sub(pattern, rf"\g<1>{new_url}\g<2>", content)
with open(path, "w", encoding="utf-8") as f:
    f.write(content)
' "$DEV_BUILD_GRADLE" "$task" "$url"
}


# ── API & Network Fetchers ───────────────────────────────────────────────────

MODRINTH_API="https://api.modrinth.com/v2"
FABRIC_META_API="https://meta.fabricmc.net/v2"
MAVEN_CENTRAL="https://repo1.maven.org/maven2"
USER_AGENT="dystopia-mods-updater/1.0 (github.com/Dystopiko/mods)"

fetch_http() {
    curl -fsSL --retry 3 --retry-delay 2 -A "$USER_AGENT" "$1" || die "HTTP request failed: $1"
}

fetch_modrinth_version() {
    local project_id="$1" mc_ver="$2"
    local json
    json="$(curl -fsSL --retry 3 --retry-delay 2 -A "$USER_AGENT" \
        -G "${MODRINTH_API}/project/${project_id}/version" \
        --data-urlencode "loaders=[\"fabric\"]" \
        --data-urlencode "game_versions=[\"${mc_ver}\"]" \
        --data-urlencode "include_changelog=false" \
      )" || die "Modrinth API failed for project: $project_id"

    local count
    count="$(echo "$json" | jq 'length')"
    if [[ "$count" -eq 0 ]]; then
        echo "null"
        return
    fi
    echo "$json" | jq -r '.[0].version_number'
}

fetch_modrinth_file_url() {
    local project_id="$1" mc_ver="$2"
    local json
    json="$(curl -fsSL --retry 3 --retry-delay 2 -A "$USER_AGENT" \
        -G "${MODRINTH_API}/project/${project_id}/version" \
        --data-urlencode "loaders=[\"fabric\"]" \
        --data-urlencode "game_versions=[\"${mc_ver}\"]" \
        --data-urlencode "include_changelog=false" \
      )" || die "Modrinth API failed for project: $project_id"

    local count
    count="$(echo "$json" | jq 'length')"
    if [[ "$count" -eq 0 ]]; then
        echo "null"
        return
    fi
    echo "$json" | jq -r '.[0].files[] | select(.primary == true).url'
}

fetch_github_asset_url() {
    local repo="$1" pattern="$2"
    local json
    json="$(curl -fsSL --retry 3 --retry-delay 2 -A "$USER_AGENT" \
        "https://api.github.com/repos/${repo}/releases/latest" \
      )" || die "GitHub API failed for repo: $repo"

    local url
    url="$(echo "$json" | jq -r --arg pat "$pattern" '
      .assets[]? | select(.name | test($pat)) | .browser_download_url
    ' | head -1)"

    if [[ -z "$url" || "$url" == "null" ]]; then
        echo "null"
        return
    fi
    echo "$url"
}

fetch_maven_release() {
    local group="$1" artifact="$2"
    local path="${group//.//}/${artifact}/maven-metadata.xml"
    local xml
    xml="$(fetch_http "${MAVEN_CENTRAL}/${path}")"

    python3 -c '
import sys, re
xml_content = sys.argv[1]
match = re.search(r"<release>(.*?)</release>", xml_content) or re.search(r"<latest>(.*?)</latest>", xml_content)
print(match.group(1) if match else "")
' "$xml"
}

fetch_polymer_version() {
    local mc_ver="$1"
    local xml
    xml="$(fetch_http "https://maven.nucleoid.xyz/eu/pb4/polymer-core/maven-metadata.xml")"

    python3 -c '
import sys, re
mc, xml_content = sys.argv[1], sys.argv[2]
versions = re.findall(r"<version>(.*?)</version>", xml_content)

def matches(v):
    if "+" not in v:
        return False
    suffix = v.split("+")[-1]
    low = v.lower()
    if any(tag in low for tag in ("pre", "beta", "rc", "alpha", "snapshot")):
        return False
    return suffix == mc or suffix.startswith(mc + ".") or mc.startswith(suffix + ".")

candidates = [v for v in versions if matches(v)]
print(candidates[-1] if candidates else "")
' "$mc_ver" "$xml"
}
