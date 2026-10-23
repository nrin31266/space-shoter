# Checkpoint dự án

## Trạng thái

2026-10-23, Linux: **đã hoàn tất và đóng mốc setup codebase/docs**. Source gameplay,
debug config và save schema giữ nguyên. Các mốc asset/gameplay mới chưa bắt đầu.

## Đã hoàn tất

- Docs tiếng Việt theo domain, mô tả phát triển Space từ prototype Fando;
  số liệu hiện trạng lấy từ source Java/config. Thiết kế mới được ghi riêng.
- README/AGENTS, symlink tương đối cho Claude, năm skill có lockfile/license,
  Context7 project config và quy trình Claude CLI/9Router.
- Script build/run hai bản trên ADB đã mở; bản original cài song song bằng
  applicationId riêng. Bỏ script mở emulator và đường JDK máy cũ.
- Bỏ báo cáo/thuyết trình cũ, file lock, ảnh hướng dẫn/screenshot cũ, ảnh template,
  script chỉnh wave một lần, ba generator cũ, tám SFX không được load, Python
  cache và APK lịch sử ở root không còn dùng.
- Phần đối chiếu dự án tham khảo tập trung trong tài liệu nội bộ; docs gameplay,
  presentation và asset pipeline mô tả hệ thống Space.

## Kiểm chứng

- Hai APK Java/LibGDX đã build thành công, cài và mở qua script trên emulator-5554;
  package/label và UID khác nhau. JDK 17.0.18, API 34, Build Tools 33.0.1.
- Smoke custom: menu, New Game Wave 19, HUD/boss/projectile, pause và game-over.
  Original đã mở màn prototype. Không tiếp tục test sâu game cũ theo yêu cầu.
- Tooling: 19 tests pass; Bash syntax, docs links/anchors và git diff whitespace
  đã kiểm tra. Bản original kiểm tra đúng commit và chỉ đổi hai file đóng gói.
- Sau đợt dọn cuối: Android assembleDebug pass; 23 docs/65 relative links hợp lệ.
  Kiểm tra APK không còn ảnh tài liệu cũ và AudioManager không thiếu file tải.
- Context7 HTTP truy vấn thành công; 9Router RTK bật, Gemini qua Claude CLI đã
  trả ROUTER_OK. Client cần trust/approve project config để nạp Context7.

## Giới hạn và điểm dừng

Chưa xác minh đầy đủ Wave 1–5, save/Continue, audio mix, nhiều tỉ lệ màn hình,
FPS thiết bị thật hoặc iOS. Không suy các kết quả này từ smoke/build.
Đã đóng mốc hiện tại, không tự bắt đầu asset hoặc gameplay.
Khi có yêu cầu tiếp, đọc [kế hoạch phát triển](development-plan.md).
