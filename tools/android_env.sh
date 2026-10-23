#!/usr/bin/env bash
# Dùng chung cho build/run; không cài SDK hoặc thay Java mặc định của máy.
space_android_env() {
  local root="$1" sdk="${ANDROID_SDK_ROOT:-${ANDROID_HOME:-}}" candidate
  local -a players=("$HOME"/Unity/Hub/Editor/*/Editor/Data/PlaybackEngines/AndroidPlayer)
  if [[ -z "$sdk" && -f "$root/local.properties" ]]; then
    sdk="$(sed -n 's/^sdk\.dir=//p' "$root/local.properties" | tr -d '\r')"
  fi
  if [[ -z "$sdk" ]]; then
    for candidate in "$HOME/Android/Sdk" "${players[@]/%//SDK}"; do
      if [[ -f "$candidate/platforms/android-34/android.jar" ]]; then
        sdk="$candidate"
        break
      fi
    done
  fi
  [[ -f "$sdk/platforms/android-34/android.jar" ]] || {
    echo "Thiếu Android API 34. Đặt ANDROID_SDK_ROOT tới SDK có platforms/android-34." >&2
    return 1
  }
  if [[ -z "${JAVA_HOME:-}" ]]; then
    for candidate in "${players[@]/%//OpenJDK}" /usr/lib/jvm/java-17*; do
      if [[ -x "$candidate/bin/java" ]]; then
        JAVA_HOME="$candidate"
        break
      fi
    done
  fi
  [[ -x "${JAVA_HOME:-}/bin/java" ]] && "$JAVA_HOME/bin/java" -version 2>&1 | head -1 | grep -q '"17\.' || {
    echo "Build cần JDK 17. Đặt JAVA_HOME tới JDK 17 đã cài." >&2
    return 1
  }
  export JAVA_HOME ANDROID_SDK_ROOT="$sdk" ANDROID_HOME="$sdk"
  export PATH="$JAVA_HOME/bin:$sdk/platform-tools:$PATH"
}
