# Space Shooter — Hướng dẫn agent

## Đọc trước

- Đọc file này → [điều hướng tài liệu](docs/README.md) → owner của domain đang sửa.
- Bắt đầu công việc dài từ [checkpoint](docs/production/checkpoint.md); cập nhật
  checkpoint sau mỗi mốc hoặc trước khi bàn giao/compaction.
- Yêu cầu hiện tại được ưu tiên. Source chứng minh hành vi đang chạy; docs mô tả
  đích đã chốt. Khi khác nhau, ghi rõ khoảng cách, không sửa docs để che lỗi.

## Ranh giới dự án

- Đích là Java/LibGDX, Android-first; `core/` dùng chung, `android/` và `ios/`
  là adapter. Không đưa Unity runtime, C#, URP hoặc prefab vào app LibGDX.
- StarShift ở repository kế bên chỉ là nguồn tham khảo nội bộ. Không sửa nó trong
  công việc của Space nếu chưa có yêu cầu riêng.
- `space-shooter-original/` là Fando đã chốt; chỉ helper chuẩn bị được đổi
  applicationId/tên app. Không port hoặc refactor gameplay của bản gốc.
- Offline tools ở `tools/`, asset runtime ở `assets/`, docs ở `docs/`.
- Không tự commit, push, tạo branch hoặc viết lại lịch sử.

## Tài liệu và phạm vi thay đổi

- Docs chính mô tả Space Shooter phát triển từ prototype Fando. Tách hiện trạng
  đã có trong source với thiết kế sắp triển khai; ghi đúng số liệu của từng phần.
- StarShift là nguồn tham khảo nội bộ cho ý tưởng, logic và tài nguyên tác giả
  đã tạo. Chỉ ghi mapping/chi tiết chuyển đổi trong
  [tài liệu nội bộ](docs/production/reference-source.md), không rải cách kể này
  vào docs gameplay, UI hoặc pipeline của Space.
- Task setup/docs không mở rộng sang gameplay hoặc dựng scaffold hệ thống mới.
  Thay gameplay theo mốc được yêu cầu, cập nhật owner và regression tương ứng.
- Bảo vệ dữ liệu cũ; chốt migration trước khi thay persistence. Không đọc wave
  save như campaign save hoặc tự xóa Preferences.

## Quy tắc code và asset

- Identifiers, filenames kỹ thuật và config IDs dùng English; docs và comment
  giải thích ngắn dùng tiếng Việt. Comment chỉ giải thích lý do không hiển nhiên.
- Tái sử dụng trách nhiệm hiện có; tách config khỏi runtime state, simulation
  khỏi presentation. Không thêm framework, DI, ECS hoặc abstraction chưa cần.
- Đọc asset hiện có trước khi tạo mới. Không chạy generator ghi đè tranh tàu/boss.
- AssetManager sở hữu tài nguyên được load; cache regions/fonts/audio, pool hiệu
  ứng tần suất cao. Không load/tạo Texture/Sound/font trong render hoặc mỗi frame.
- Mobile: kiểm soát allocation, draw calls, texture switching, số particle và SFX
  đồng thời. Đo trước khi tối ưu; không claim FPS thiết bị từ emulator.
- Artwork tác giả tự tạo với AI/agent/MCP ghi đúng tác giả và công cụ, không gọi
  là vẽ tay hoặc artwork game bên thứ ba. Credit/license chỉ giữ cho asset còn
  dùng; asset cũ bị thay thì bỏ cùng mục credit. Clip/font bên thứ ba vẫn ghi đúng
  nguồn theo [asset owner](docs/production/assets.md), không gọi tất cả là CC0.
- Không tự nâng LibGDX/AGP/Gradle/MobiVM hoặc cài công cụ hệ thống. Nêu lỗi/gói cần
  thiết và hỏi người dùng nếu thực sự cần cài thêm. Dependency do Wrapper tải là
  một phần của build; không tự tải thêm Android SDK component.

## Skills và công cụ AI

- Dùng skill theo domain: `game-ui-ux`, `game-feel`, `audio-design`,
  `performance-optimization`, `save-systems`. Chỉ đọc skill liên quan; ví dụ
  engine khác không là yêu cầu đổi kiến trúc. Nguồn được giữ trong `.agents/skills/UPSTREAM.md`.
- Tra Context7 cho API/config thư viện, đối chiếu version đang dùng; fallback tài
  liệu chính thức. Không dùng tài liệu ngoài để tự thay luật game.
- Claude CLI qua 9Router nhận việc có scope/file/tiêu chí rõ. Đọc
  [quy trình AI](docs/systems/ai-workflow.md); không sửa cùng file đồng thời.
- Không sửa model/token toàn máy, không ghi secret hoặc toàn bộ hội thoại vào Git.

## Kiểm chứng và tài liệu

- Một thông tin chi tiết có một owner theo router; file khác tóm tắt và link.
  Headings không đánh số, filenames lowercase kebab-case; README là ngoại lệ.
- Phân biệt hiện trạng, thiết kế mới và kết quả thực sự chạy. Ghi ngày/môi trường,
  lệnh, kết quả và phần chưa kiểm tra ở checkpoint; không lưu log dài trong docs.
- Logic mới có nhánh/loop/script phải có regression nhỏ; sửa text đơn giản không
  cần test riêng. Gom kiểm tra theo phạm vi thay đổi, không lặp full suite vô ích.
- Kiểm tra docs: `python3 tools/check_docs.py`; tooling:
  `python3 -m unittest discover -s tools/tests -v`.
- Build Android: source `tools/android_env.sh`, gọi `space_android_env "$PWD"`,
  rồi `./gradlew :android:assembleDebug -Pandroid.builder.sdkDownload=false`.
- Tự dùng emulator đã mở qua `./build_run_spaceshooter.sh`; không tự khởi động.
  Smoke menu, Continue, bắn, HUD, pause, game-over và audio khi có thiết bị.
- Không claim build/runtime/audio/save hoặc iOS đã pass nếu chưa chạy tương ứng.
