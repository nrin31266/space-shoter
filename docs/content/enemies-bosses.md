# Enemy và boss

## Runtime campaign

`CampaignWorld` tạo enemy từ prefab/config trong catalog; `FlightMath` xử lý
formation, sequence, repeat motion và hướng chiến đấu. `DirectedSkill` giữ beam/
strike cùng cửa sổ charge/tracking/active/cooldown. Coordinator chung giới hạn số
nguồn bắn, spacing và volley pause; đặc nhiệm được ưu tiên đòn đầu.

`EscortController` sinh reinforcement theo capacity/interval và rút escort khi
owner bị hạ. Hazard dùng lịch seeded, scale/HP/transit theo variant đã export.
`BossCombat` đọc module order, phase threshold/offset/rest, shield nodes, collapse,
beam, cannon/escort và barrage có safe lane. Shield chặn core tới khi node bị hạ
và collapse kết thúc. Death/cancel đóng đòn; renderer không quyết damage.

## Giới hạn nghiệm thu

Logic đã được nối trong Java; source Unity chỉ làm chuẩn đối chiếu. Không suy
balance, telegraph, destruction staging hoặc độ tương đương hình ảnh từ kiểm tra
hạ địch bằng test damage. Kết quả và phần chưa kiểm ở
[checkpoint](../production/checkpoint.md).
