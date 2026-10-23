#!/usr/bin/env python3
"""Khôi phục Fando cố định; chỉ đổi applicationId/tên để cài song song."""

import argparse
from pathlib import Path
import subprocess
import sys


ROOT = Path(__file__).resolve().parent.parent
ORIGINAL = ROOT / "space-shooter-original"
SOURCE = "https://github.com/Fando/space-shooter.git"
COMMIT = "324243d8f13f7d8cdf9c801d352d3d424bd81112"
PATCHES = {
    "android/build.gradle": (
        'applicationId "com.alexei.spaceshooter"',
        'applicationId "com.alexei.spaceshooter.original"',
    ),
    "android/res/values/strings.xml": (
        '<string name="app_name">Space Shooter</string>',
        '<string name="app_name">Space Shooter Original</string>',
    ),
}


def git(*args):
    return subprocess.check_output(
        ["git", "-C", str(ORIGINAL), *args], text=True
    ).strip()


def prepare(check=False):
    if not ORIGINAL.exists():
        if check:
            raise ValueError("Thiếu bản gốc; chạy python3 tools/setup_original.py.")
        subprocess.run(["git", "clone", "--no-checkout", SOURCE, str(ORIGINAL)], check=True)
        subprocess.run(["git", "-C", str(ORIGINAL), "checkout", "--detach", COMMIT], check=True)
    if git("rev-parse", "HEAD") != COMMIT:
        raise ValueError("Bản gốc không ở commit đã chốt; không tự reset checkout hiện có.")
    if git("remote", "get-url", "origin") != SOURCE:
        raise ValueError("Origin bản gốc khác nguồn Fando đã chốt.")
    pending = []
    for relative, (before, after) in PATCHES.items():
        baseline = git("show", f"{COMMIT}:{relative}")
        if baseline.count(before) != 1:
            raise ValueError(f"Không tìm thấy đúng một vị trí đóng gói trong {relative}.")
        expected = baseline.replace(before, after) + "\n"
        path = ORIGINAL / relative
        current = path.read_text()
        if current.strip() == expected.strip():
            continue
        if check or current.strip() != baseline.strip():
            raise ValueError(f"{relative} có thay đổi ngoài bản đóng gói đã chốt.")
        pending.append((path, expected))
    changed = git("diff", "--name-only", "HEAD").splitlines()
    if set(changed) - PATCHES.keys():
        raise ValueError("Bản gốc có thay đổi source ngoài đóng gói; không tự ghi đè.")
    if git("ls-files", "--others", "--exclude-standard"):
        raise ValueError("Bản gốc có file ngoài Git; kiểm tra trước khi build bản upstream.")
    for path, content in pending:
        path.write_text(content)
    print(f"PASS: Fando {COMMIT[:12]}, applicationId .original, gameplay upstream.")


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--check", action="store_true", help="Chỉ kiểm tra, không tải/sửa file")
    args = parser.parse_args()
    try:
        prepare(args.check)
    except (ValueError, OSError, subprocess.CalledProcessError) as error:
        print(f"Lỗi chuẩn bị bản gốc: {error}", file=sys.stderr)
        sys.exit(1)
