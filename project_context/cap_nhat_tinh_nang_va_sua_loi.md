# Báo Cáo Cập Nhật: Tính Năng Mới và Tối Ưu Hóa Game

Tài liệu này tổng hợp toàn bộ các cơ chế mới được thêm vào game, hệ thống màn chơi, và các tinh chỉnh đã thực hiện để tạo ngữ cảnh cho các bản cập nhật mã nguồn trước khi push lên kho lưu trữ (Git).

## 1. Cơ Chế Xếp Đội Hình Quái & Boss (Wave & Boss Mechanics)
- **Hệ thống Đội Hình Đa Dạng:**
  - Lập trình hệ thống "Nhà máy sinh quái" (`EnemyFactory.java`) với khả năng tự động dàn trận theo nhiều hình thù khác nhau: 
    - `GRID`: Đội hình lưới chữ nhật vuông vức.
    - `CHEVRON`: Đội hình chữ V ngược, mũi nhọn chĩa về phía người chơi.
    - `INTERLEAVED_ROWS`: Đội hình lưới so le xen kẽ.
    - `LINE`: Đội hình dàn hàng ngang mỏng.
    - `RANDOM`: Đội hình rải rác ngẫu nhiên.
  - Mỗi đội hình đều được tính toán để các phi thuyền không bị chồng lấp lên nhau khi di chuyển vào vị trí (tính toán dựa trên `screenWidth` và bù trừ `spawnOffset`).
- **Phát Triển Wave (Chiến Dịch):**
  - Mở rộng quy mô từ vài wave cơ bản lên **15 Waves** chi tiết trong file cấu hình (`waves.json`). Các wave được xếp từ dễ đến khó, xen kẽ các đội hình tăng dần độ dồn dập.
- **Cơ Chế Trùm (Boss):**
  - Bổ sung lệnh gọi trực tiếp `"BOSS"` vào hệ thống sinh quái, cho phép Boss khổng lồ (`EnemyBoss`) xuất hiện đúng thời điểm ở cuối các wave quan trọng (như Wave 5) với lượng HP trâu hơn thay vì sinh ra quái nhỏ.

## 2. Cơ Chế Vũ Khí Của Người Chơi (Weapon Progression)
- **Cân bằng Sức Mạnh (Balancing):**
  - Giới hạn cấp độ vũ khí (Weapon Level) tối đa là **5** để tránh người chơi trở nên quá lố (OP) nếu cộng dồn vô hạn.
  - Đồng bộ hoá toàn bộ tia laser thành màu **Vàng (Yellow)**.
- **Tiến trình Nâng cấp (Progression Flow):**
  - **Lv 1:** 1 tia đạn, tốc độ bắn rất chậm (Delay: 200).
  - **Lv 2:** 1 tia đạn, tốc độ bắn nhanh hơn (Delay: 120).
  - **Lv 3:** Tách chùm 3 tia đạn, tốc độ bắn quay về mức chậm (Delay: 200) để cân bằng sát thương diện rộng.
  - **Lv 4:** 3 tia đạn, tốc độ bắn nhanh (Delay: 120).
  - **Lv 5 (Max):** 5 tia đạn chùm tỏa rộng, tốc độ bắn nhanh (Delay: 120), mang lại uy lực tuyệt đối.

## 3. Cơ Chế Rơi Vật Phẩm (Item Physics & Drop Logic)
- **Tách Biệt Trọng Lực (Gravity Separation):**
  - Ngôi Sao (Tăng điểm): Hoạt động với gia tốc rơi lớn, lao nhanh xuống dưới mép màn hình, nảy nhẹ lên vài lần rồi mới biến mất để tránh làm "loạn mắt" khi tiêu diệt hàng loạt quái vật.
  - Item Máu (HP) & Nâng Cấp Đạn: Sở hữu hệ số trọng lực (`gravity`) riêng biệt. Rơi rất êm ái và nhẹ nhàng để người chơi dễ dàng phán đoán và thu thập.
- **Làm Mới Hình Ảnh (Visual Polish):**
  - Item Nâng Cấp Đạn được thiết kế lại thành biểu tượng **Mũi Tên Kép (Double Chevron)** với đường viền Xanh Ngọc (Cyan) phát sáng và lõi Vàng (Yellow), làm nổi bật sự quý giá của vật phẩm này so với đồ họa cũ.

## 4. Tối Ưu Hóa & Sửa Lỗi (Bug Fixes)
- **Khắc Phục Vật Lý Chuyển Động (Movement Fix):**
  - Fix lỗi quái vật bay ra khỏi màn hình do vận tốc không đồng bộ với Frame Rate. Gắn chặt toạ độ với `deltaTime`, giúp chúng bay lượn (hovering) tại chỗ cực kỳ mượt mà.
  - Fix lỗi điểm dừng (`hoverY`) của các đội hình lớn bị tràn khỏi mép trên màn hình khiến người chơi không thể bắn tới (Lỗi không qua được Wave 1).
- **Tối Ưu Âm Thanh (Audio Throttling):**
  - Bổ sung bộ đếm thời gian (limit 150ms) để lọc bớt âm thanh khi có quá nhiều vụ nổ diễn ra cùng lúc, khắc phục triệt để lỗi rè loa và rách tiếng. Đồng thời hỗ trợ lưu cấu hình cài đặt (`Preferences`).

---
> [!NOTE]
> Báo cáo này đại diện cho toàn bộ giá trị cốt lõi của bản cập nhật hiện tại. Mã nguồn đã hoàn thiện và sẵn sàng để `git add .` và `git commit`.
