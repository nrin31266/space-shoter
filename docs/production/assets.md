# Tài nguyên và ghi nhận nguồn

## Quy ước

Artwork custom do Nguyễn Văn Rin thiết kế và thực hiện bằng công cụ AI,
agent/MCP và công cụ chỉnh sửa được ghi nhận theo đúng tác giả và quy trình tạo.
Giữ manifest/hash cần thiết để tái tạo và quản lý tài nguyên.

Chỉ duy trì credit/license của tài nguyên còn dùng. Khi thay asset và xóa toàn bộ
reference cũ, xóa mục ghi nhận cũ cùng thay đổi. Font, music hoặc SFX bên thứ ba
còn dùng giữ đúng tác giả/license của file tương ứng.

## Asset runtime hiện tại

| Nhóm | Ghi nhận |
|---|---|
| Tàu, enemy/boss và sprite custom | Tác giả dự án; không chạy generator ghi đè tranh |
| Đạn, item, nebula sinh offline | Tác giả dự án, chuẩn bị offline |
| Nhạc và identity SFX OGG | Synth do tác giả dự án tạo |
| Legacy SFX MP3 | Kế thừa prototype; chưa có manifest nguồn đầy đủ trong repo |
| Roboto Bold / Font Awesome 6 Free Solid | Font bên thứ ba; chưa có license text đi kèm ở baseline |

Nền source: [Fando/space-shooter](https://github.com/Fando/space-shooter).
Không gán CC0/public domain chung cho toàn bộ file. assets/ATTRIBUTIONS.md trỏ về
đây để giữ một nơi quản lý credit.

## Bộ tài nguyên mới

Artwork mới được ghi nhận là tài nguyên tác giả thực hiện với PixelLab qua MCP,
AI/agent và Aseprite/procedural tools. Bộ này chưa thay asset runtime hiện tại.
Manifest sản xuất và bằng chứng license được đưa vào Space cùng file thực sự dùng.
Danh mục nội bộ phục vụ chuẩn bị nằm trong [nguồn tham khảo](reference-source.md).
Quy trình tích hợp và loại bỏ file cũ thuộc [asset pipeline](asset-pipeline.md).
