# Nguồn tham khảo kỹ thuật nội bộ

## Vai trò

Space Shooter phát triển từ prototype Fando trên GitHub bằng Java/LibGDX.
StarShift là dự án riêng của cùng tác giả, dùng nội bộ để đối chiếu gameplay,
dữ liệu và tài nguyên đã thực hiện. Source Android được build là Space.
Tài liệu này sở hữu mapping giữa hai codebase; docs domain của Space giữ thiết kế
và số liệu hệ thống, không mô tả quy trình chuyển đổi engine.

## Mốc khảo sát nguồn

- Space trước mốc nền: commit ff38f3f, source Java/LibGDX và assets/data/waves.json.
- Fando: commit 324243d8f13f7d8cdf9c801d352d3d424bd81112, checkout riêng được ignore.
- StarShift: repository kế bên ../starshift, commit
  b07a26b19cd1ad758a298baabec8e5bf0de5f053, Unity root app/.
- Chỉ đọc source/config/asset production; không sửa, mở build hoặc build Android
  cho StarShift trong công việc nền của Space.

StarShift có Mission01–08 qua hai region. Target design 12 level chưa được coi
là content phải làm thêm. 186 PNG, 44 WAV, 119 C# runtime scripts và 217 data
assets là kiểm kê thư mục đầu vào, chưa chứng minh tất cả được production dùng.

## Mapping trách nhiệm

| Nguồn tham khảo | Hướng tích hợp vào Space Java/LibGDX |
|---|---|
| Scripts/Player và Combat | Input intent, gate, lives, weapon/thermal; tách simulation khỏi presentation |
| Scripts/Encounters, Gameplay, Enemies | Encounter/flight/formation/camera, enemy và boss; config tách runtime clock/state |
| Scripts/Rewards, Persistence | Reward/progression/profile/save; chốt migration trước khi thay Preferences |
| Scripts/UI, Audio và presentation components | Scene2D shell/HUD, audio controller và hiệu ứng pooled |
| Data/*.asset và GUID references | Export config theo stable ID; không đưa ScriptableObject vào runtime Java |
| Art, UI/Fonts, Audio | Chuyển artwork/clip/frame/pivot cùng provenance/license; atlas và cache offline |
| Prefab/scene/material/procedural VFX | Đọc wiring/timing rồi tái hiện bằng Java/rendering phù hợp; không copy Unity runtime |

Phạm vi đã duyệt là tiếp nhận toàn bộ nội dung production hiện có cần cho trải
nghiệm đã chốt, không chỉ lấy skin. Kiểm kê references trước khi chuyển để không
bỏ sót asset cần dùng hoặc ship cả Unity cache.

## Những ranh giới phải giữ

Tài liệu source có thể mô tả ý định khác với code; đối chiếu C#/serialized data
thật trước khi lấy thông số. Không tự đóng dấu provisional thành locked. Config
không thành save, presentation không viết damage/save/clock. Quyết định của Space
được cập nhật vào owner của Space, không sửa docs nguồn để hợp thức hóa sai lệch.

Tham khảo StarShift không có nghĩa bê nguyên kiến trúc Unity: giữ Gradle modules,
Java/LibGDX, Android launcher và nhánh prototype có thể build độc lập. Kết quả
build/runtime của Unity không chứng minh app Space chạy đúng.

## Bằng chứng tài nguyên chuẩn bị

Art có manifest PixelLab/Aseprite/procedural tools tại
`tools/assets/m8/sources/manifest.json` trong nguồn tham khảo. Audio có
`tools/audio/sources.json` và `tools/audio/licenses/`. Khi dùng file trong Space,
đưa bằng chứng cần thiết vào cùng asset và cập nhật owner tài nguyên.

- Artwork và ba air cues synth do tác giả thực hiện bằng công cụ tương ứng.
- Music Andrew Sitkov giữ license PDF và clip được dùng.
- Kenney Sci-Fi/Digital/Interface SFX giữ license CC0 và credit nguồn.
- Little Robot Sound Factory Sci-Fi SFX giữ CC BY 3.0, tác giả, URL và sửa đổi.
- Font giữ OFL/license đúng với file thực tế.

Đây là kiểm kê bộ chuẩn bị; license/credit runtime hiện tại thuộc
[tài nguyên của Space](assets.md).

## Hợp đồng export cho giai đoạn game

Chỉ đọc filesystem, không mở hoặc build Unity. Bắt đầu từ scene production,
catalog mission và prefab/config được tham chiếu; inventory toàn bộ dependencies
nhưng export lát Mission01 trước, mở rộng theo content được tích hợp.

Dùng PyYAML SafeLoader đang có cho exporter offline; xử lý YAML tags/documents
của Unity mà không thực thi object constructor. Resolve theo (GUID, fileID),
bao gồm sprite subasset IDs và local scene/prefab references. IDs số lớn giữ dạng
chuỗi ở manifest để không mất precision. Mảng int hex giải mã đúng little-endian
và kiểu field, không đọc như một chuỗi hoặc tự đoán float.

Serialized overrides trong scene/prefab được ưu tiên hơn default trong C#; hỗ trợ
đúng các component production cần dùng, không viết Unity deserializer tổng quát.
Thiếu component/reference/field bắt buộc thì báo đường dẫn cụ thể; không lấp bằng
sprite hoặc balance mặc định. Giữ rect/pivot/PPU, collider/muzzle anchors, timing,
curve/formation và audio event bindings. Đổi origin/UV ở biên render, không sửa
lại tọa độ gameplay để chữa lỗi hình.

TextureAtlas được tạo offline bằng TexturePacker cùng phiên bản LibGDX; tool
dependency tách khỏi Android runtime. Runtime chỉ tải asset/config đã export;
không phụ thuộc repo tham khảo hoặc parse YAML khi chơi. Provenance/license cho
file dùng thật được copy cùng asset, tài nguyên không dùng được ghi vào inventory
thay vì đóng cả Unity cache vào APK.

Giá trị đã kiểm tra để định hướng slice: PlayerLives khởi đầu 3, default trần 5;
WeaponLoadout.MaximumLevel=5. Giá trị serialized thực tế vẫn phải được đối chiếu
trước khi export; không lấy default làm bằng chứng prefab/scene không override.
