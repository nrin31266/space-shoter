# releases/

Thư mục chứa các tệp APK đã đóng gói (build output).

## Cách tạo APK mới

Chạy từ thư mục gốc của project:

```bash
./build_release.sh
```

Script sẽ hỏi:

- **Version** (dạng `x.y.z`, ví dụ `1.2.0`) — bắt buộc nhập.
- **versionCode** — mặc định tự sinh từ version theo công thức
  `major * 10000 + minor * 100 + patch` (ví dụ `1.2.3` → `10203`),
  có thể nhập tay nếu muốn.
- **Loại APK** — `1` = Release (đã ký bằng debug keystore, khuyên dùng)
  hoặc `2` = Debug.

Sau khi build xong, APK sẽ được đặt tại:

```
releases/space-shooter-v<version>-release.apk
releases/space-shooter-v<version>-debug.apk
```

## Ghi chú

- Version được truyền vào Gradle qua `-PversionName` / `-PversionCode`,
  không sửa cứng trong `android/build.gradle`.
- Release build dùng debug keystore (`~/.android/debug.keystore`) để ký,
  phù hợp cho việc cài đặt kiểm thử/bàn giao nội bộ.
  Khi phát hành lên Google Play, cần ký bằng release keystore chính thức.
