# Nguồn skills

- Repository: https://github.com/gamedev-skills/awesome-gamedev-agent-skills
- Commit: `59b4a4230071028d808ee6623e23fa8e9f64ab67`
- Giấy phép: Apache-2.0, xem [LICENSE](LICENSE).
- Năm thư mục `audio-design`, `game-feel`, `game-ui-ux`, `performance-optimization`, `save-systems`
  và `references/` đi kèm được cài bằng Skills CLI, giữ nguyên nội dung upstream.
- Không cài router, engine skill hoặc dependency skill được nhắc trong ví dụ.
- `.claude/skills` dùng symlink tương đối tới cùng bộ skill của Codex.
- Các ví dụ Godot/Unity không thay thế API LibGDX; tra Context7 trước khi áp dụng.

## Chọn theo phạm vi

Giữ UI/UX, feedback, audio và performance; thêm save-systems cho persistence,
validation và migration khi mở rộng campaign. Không cài input-systems vì phần
lớn nội dung là rebind/gamepad/platformer; relative drag/hold-to-fire được đối
chiếu source hiện có và API LibGDX. Camera/shader/asset-generation skills chỉ xem
lại khi task cần tạo mới hoặc sửa subsystem đó. Không cài Unity/Godot/web skills.

Tra ngày 2026-10-23: input-systems khoảng 6.2K installs, save-systems khoảng 6.4K;
nguồn cùng repository 3K+ stars, commit và Apache-2.0 như trên. Không chọn theo
lượt cài đơn thuần: đọc SKILL.md và kiểm tra độ phù hợp Java/LibGDX trước.
