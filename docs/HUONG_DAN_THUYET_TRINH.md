# HƯỚNG DẪN & KỊCH BẢN THUYẾT TRÌNH BÁO CÁO THỰC TẬP

> **Đề tài:** Nghiên cứu và tùy biến game mã nguồn mở trên nền tảng Android bằng Android Studio và công cụ AI  
> **Sinh viên thực hiện:** Nguyễn Văn Rin – 23IT231 (Lớp: 23SE2)  
> **Đơn vị thực tập:** Công ty TNHH MTV Dịch vụ Phần mềm SafeHorizons  
> **Người hướng dẫn doanh nghiệp:** Lê Văn Minh | **Giảng viên hướng dẫn:** ThS. Nguyễn Thanh Cẩm  
> **Thời lượng gợi ý:** 10 – 15 phút (bao gồm Demo & Q&A)

---

## MỤC LỤC KỊCH BẢN

1. [Cấu trúc & Phân bổ thời gian](#1-cấu-trúc--phân-bổ-thời-gian)
2. [Phần 1: Mở đầu & Khảo sát mã nguồn gốc (2 phút)](#phần-1-mở-đầu--khảo-sát-mã-nguồn-gốc-2-phút)
3. [Phần 2: Live Demo & Phân tích hạn chế của Bản gốc (3 phút)](#phần-2-live-demo--phân-tích-hạn-chế-của-bản-gốc-3-phút)
4. [Phần 3: Live Demo & Trình diễn Bản tùy biến hoàn thiện (5 phút)](#phần-3-live-demo--trình-diễn-bản-tùy-biến-hoàn-thiện-5-phút)
5. [Phần 4: Ứng dụng AI, Kiến trúc AGENTS.md & Chứng minh Bản quyền (3 phút)](#phần-4-ứng-dụng-ai-kiến-trúc-agentsmd--chứng-minh-bản-quyền-3-phút)
6. [Phần 5: Kết luận & Bộ câu hỏi phản biện Q&A (2 phút)](#phần-5-kết-luận--bộ-câu-hỏi-phản-biện-qa-2-phút)

---

## 1. CẤU TRÚC & PHÂN BỔ THỜI GIAN

| STT | Nội dung | Hành động trên máy tính | Thời lượng |
|---|---|---|---|
| **P1** | **Mở đầu & Giới thiệu mã nguồn gốc** | Mở trình duyệt Web xem GitHub repo gốc | ~2 phút |
| **P2** | **Demo & Đánh giá bản gốc (Critique)** | Chạy `./build_run_spaceshooter.sh -o` | ~3 phút |
| **P3** | **Demo & Trình diễn bản tùy biến** | Chạy `./build_run_spaceshooter.sh -c` | ~5 phút |
| **P4** | **Báo cáo về AI, AGENTS.md & Bản quyền** | Mở IDE show file `AGENTS.md`, `ATTRIBUTIONS.md` & `tools/` | ~3 phút |
| **P5** | **Kết luận & Trả lời phản biện** | Tổng kết và sẵn sàng cho Q&A | ~2 phút |

---

## PHẦN 1: MỞ ĐẦU & KHẢO SÁT MÃ NGUỒN GỐC (2 PHÚT)

### 🎯 Mục tiêu
Giới thiệu bản thân, ngữ cảnh thực tập tại SafeHorizons, triết lý tiếp cận phần mềm mã nguồn mở (OSS) và link GitHub tác giả gốc.

### 🖥️ Hành động
* Mở slide / trình duyệt web hiển thị trang GitHub của dự án gốc.

### 🎙️ Lời thoại mẫu
> *"Kính thưa quý Thầy/Cô trong Hội đồng và các bạn!*
>
> *Em tên là **Nguyễn Văn Rin**, sinh viên lớp 23SE2. Hôm nay em xin phép được báo cáo kết quả thực tập doanh nghiệp tại **Công ty TNHH MTV Dịch vụ Phần mềm SafeHorizons** với đề tài: **'Nghiên cứu và tùy biến game mã nguồn mở trên nền tảng Android bằng Android Studio và công cụ AI'** dưới sự hướng dẫn của thầy ThS. Nguyễn Thanh Cẩm và anh Lê Văn Minh tại doanh nghiệp.*
>
> *Trong ngành công nghiệp phần mềm hiện đại, việc tiếp nhận, đọc hiểu và tái cấu trúc (refactor/modernize) một dự án mã nguồn mở có sẵn là kỹ năng cực kỳ quan trọng đối với một lập trình viên. Thay vì xây dựng từ con số không, em đã chọn nghiên cứu một dự án game mã nguồn mở 2D bắn phi thuyền cuộn dọc (Space Shooter) viết trên nền tảng **LibGDX (Java)**.*
>
> *(Chỉ vào màn hình GitHub)* *Như thầy cô thấy trên trang repo gốc của tác giả, đây là một bản prototype được xây dựng nhằm thử nghiệm framework LibGDX trên Android. Tuy nhiên, dự án gốc mới chỉ dừng ở mức thử nghiệm sơ khai, trong README tác giả còn để dở rất nhiều hạng mục TODO như hệ thống wave, kẻ địch, vũ khí, boss và đồ họa. Nhiệm vụ của em trong đợt thực tập là nghiên cứu toàn bộ kiến trúc và nâng cấp toàn diện dự án này thành một game mobile hoàn chỉnh, hiện đại."*

---

## PHẦN 2: LIVE DEMO & PHÂN TÍCH HẠN CHẾ CỦA BẢN GỐC (3 PHÚT)

### 🎯 Mục tiêu
Build trực tiếp bản gốc lên máy ảo/thiết bị, trải nghiệm 1–2 phút và chỉ ra thẳng thắn các nhược điểm kỹ thuật / gameplay / visual.

### 🖥️ Hành động
Chạy lệnh trong terminal:
```bash
./build_run_spaceshooter.sh --original
```

### 🎙️ Lời thoại mẫu
> *"Để quý Thầy/Cô có cái nhìn trực quan nhất về điểm xuất phát, em xin phép khởi chạy bản mã nguồn gốc lên thiết bị.*
>
> *(Vừa chơi vừa thuyết minh)*:
> *Khi trải nghiệm bản gốc, chúng ta có thể dễ dàng nhận thấy những điểm còn hạn chế rất lớn:*
> 1. **Về đồ họa:** Tàu và đối tượng chủ yếu vẽ bằng hình khối cơ bản hoặc đồ họa placeholder thô sơ qua `ShapeRenderer`, chưa có phong cách nghệ thuật đồng nhất.
> 2. **Về vũ khí:** Chỉ có 2 loại rất cơ bản (Laser đơn và Rocket đuổi), không hề có hệ thống cấp độ (level) hay cơ chế nâng cấp chiều sâu.
> 3. **Về kẻ địch & Màn chơi:** Chỉ có đúng 2 loại kẻ địch (con màu hồng 1 hit chết và con màu xanh bắn cầu năng lượng). Không có Boss, không có kịch bản màn chơi rõ ràng mà các đợt lặp lại khá nhàm chán.
> 4. **Về hệ thống Lưu trữ (Save System):** Hoàn toàn chưa có hệ thống lưu tiến trình. Người chơi thoát ra là mất hết mọi dữ liệu.
> 5. **Về âm thanh & Bản quyền:** Âm thanh dùng lại file nhạc từ game khác (nhạc Unreal Tournament), hiệu ứng âm thanh dễ bị rè (clipping) khi có nhiều sự kiện cùng lúc.*
>
> *Từ những hạn chế thực tế trên, em đã đặt ra mục tiêu tái cấu trúc và hiện đại hóa toàn diện trò chơi."*

---

## PHẦN 3: LIVE DEMO & TRÌNH DIỄN BẢN TÙY BIẾN HOÀN THIỆN (5 PHÚT)

### 🎯 Mục tiêu
Chạy bản tùy biến mới, phô diễn các tính năng vượt trội: Đồ họa Neon, 3 vũ khí 7 level, 6 quái + Boss, 20 Wave JSON, Save/Load Preferences, UI VisUI và Audio DSP.

### 🖥️ Hành động
Chạy lệnh trong terminal:
```bash
./build_run_spaceshooter.sh --custom
```

### 🎙️ Lời thoại mẫu
> *"Và bây giờ, em xin phép khởi chạy phiên bản **Space Shooter Modernized** đã được tùy biến và mở rộng hoàn chỉnh.*
>
> *(Game khởi động lên Main Menu)*:
> *Ngay từ màn hình **Main Menu**, thầy cô có thể thấy sự thay đổi vượt bậc:*
> - *Giao diện hiện đại sử dụng **VisUI**, hiển thị rõ ràng **High Score** và **Total Stars** tích lũy qua các ván chơi.*
> - *Nền không gian **Starfield đa tầng** kết hợp ảnh mây tinh vân (Nebula) tạo chiều sâu 3D.*
> - *Có nút **CONTINUE** để tiếp tục ván chơi dở nhờ hệ thống **SaveManager** lưu trạng thái vào Preferences.*
>
> *(Bấm NEW GAME và vào ván đấu)*:
> *Khi bước vào Gameplay:*
> 1. **Hệ thống HUD 3 vùng responsive:** Phía trên hiển thị lượng máu dạng trái tim, biểu tượng vũ khí hiện tại, số Wave ở giữa và Điểm số + Sao thu thập bên phải. Font chữ render động qua FreeType sắc nét ở mọi độ phân giải.
> 2. **Hệ thống 3 nhánh Vũ khí chuyên sâu (7 Cấp độ):**
>    - **Laser:** Bắn chùm tia hình quạt từ 1 đến 5 tia, quét sạch diện rộng.
>    - **Blast:** Bắn cầu năng lượng xuyên phá đa mục tiêu.
>    - **Homing:** Tên lửa phi tiêu tự động truy đuổi mục tiêu gần nhất.
>    - *Đặc biệt, có cơ chế **Stockpile**: Khi nhặt vũ khí cùng loại, tàu sẽ tích trữ lượt đỡ đạn, khi bị trúng đòn chỉ trừ stockpile chứ không bị rớt cấp độ ngay lập tức.*
> 3. **Hệ thống Kẻ địch & Màn chơi Data-driven:**
>    - Em đã phát triển **6 loại kẻ địch (Enemy A đến F)**, mỗi loại có hành vi di chuyển, đội hình bay (Diamond, Checkerboard...) và loại đạn riêng (Sniper beam, Twin purple, Gold spread...).
>    - Hệ thống **20 Waves** được cấu hình hoàn toàn bằng file JSON (`waves.json`), sau Wave 20 game tự động chuyển sang chế độ lặp vô tận (**Endless loop**) với hệ số scale HP và tốc độ.
> 4. **Trận chiến với Trùm cuối (Boss - Dreadnought):**
>    *(Nếu có thể, dùng DebugConfig hoặc show ảnh Boss)*
>    - Boss xuất hiện định kỳ mỗi 5 wave với 3 nòng pháo hoạt động song song (Spread shot, Aimed laser, Sniper beam) và khi bị hạ gục sẽ tung ra 'cơn mưa' 20 Stars cùng 4 hộp trang bị.
> 5. **Cơ chế Game Feel & Âm thanh:**
>    - Khi trúng đạn có hiệu ứng chớp sáng (Flash), tia lửa (Sparks) và khiên bất tử tạm thời 1.5s nhấp nháy để tránh sốc sát thương.
>    - Âm thanh được xử lý qua cơ chế **Throttle chống chồng tiếng**, không bị rè dù nổ nhiều đối tượng cùng lúc."*

---

## PHẦN 4: ỨNG DỤNG AI, KIẾN TRÚC AGENTS.MD & CHỨNG MINH BẢN QUYỀN (3 PHÚT)

### 🎯 Mục tiêu
Làm rõ phương pháp làm việc hiện đại cùng AI (AGY Pro), kiến trúc quy chuẩn `AGENTS.md`, các Agent Skills, và chứng minh tính hợp pháp 100% không vi phạm bản quyền của Asset.

### 🖥️ Hành động
Mở Android Studio / VS Code show các file:
- `AGENTS.md`
- `assets/ATTRIBUTIONS.md`
- Thư mục `tools/gen_sprites.py`, `tools/gen_audio.py`
- Thư mục `.agents/skills/`

### 🎙️ Lời thoại mẫu
> *"Kính thưa Thầy/Cô, một trong những nội dung trọng tâm của đề tài là: **Ứng dụng công cụ AI trong quy trình phát triển phần mềm doanh nghiệp**.*
>
> *Trong dự án này, em đã sử dụng **Antigravity (AGY)** cùng mô hình **Google Pro (Student)** với vai trò là một cộng sự lập trình thông minh (Pair-Programming Assistant).*
>
> *Tuy nhiên, để AI làm việc hiệu quả và không phá vỡ logic game, em đã áp dụng phương pháp luận quản lý AI chuẩn mực:*
>
> 1. **Hiến pháp phát triển `AGENTS.md`:**
>    - *(Mở file `AGENTS.md`)* Đây là tài liệu quy định 'luật bất khả xâm phạm' cho AI:
>      + **Bảo vệ Gameplay cốt lõi:** Tuyệt đối không được làm gãy game loop, va chạm AABB, schema lưu save.
>      + **Tái cấu trúc Presentation:** Cho phép thay thế toàn bộ đồ họa và âm thanh cũ bằng phong cách Neon hiện đại.
>      + **Ràng buộc hiệu năng Mobile:** Nghiêm cấm cấp phát bộ nhớ (allocation) hoặc load file trong hàm `render()`, bắt buộc dùng SpriteBatch gộp draw call và caching texture.
>
> 2. **Hệ thống Agent Skills chuyên môn hóa (`.agents/skills`):**
>    - Dự án tích hợp các gói kỹ năng chuyên biệt như `audio-design`, `game-feel`, `create-game-assets`, `performance-optimization`, `save-systems`. Nhờ đó, AI đóng vai trò như các chuyên gia phụ trách từng mảng trong đội ngũ.
>
> 3. **CHỨNG MINH KHÔNG VI PHẠM BẢN QUYỀN TÀI NGUYÊN (Copyright Compliance):**
>    *(Mở `assets/ATTRIBUTIONS.md` và thư mục `tools/`)*
>    *Đây là điểm em đặc biệt chú trọng để sản phẩm đạt chuẩn thương mại:*
>    - **Về Hình ảnh (Sprites):** Toàn bộ đạn (projectiles), vật phẩm (items), nền tinh vân (nebula) được sinh tự động bằng script Python (`tools/gen_sprites.py`, `gen_projectiles_items.py`) sử dụng thư viện toán học `numpy` và `Pillow` với kỹ thuật siêu lấy mẫu (4x Supersampling) và lọc Lanczos. Silhouette tàu và Boss là hình ảnh nguyên bản thuộc quyền tác giả dự án.
>    - **Về Âm thanh (Audio & SFX):** 100% nhạc nền synthwave và hiệu ứng âm thanh (tiếng nhặt đồ, nâng cấp, nổ, còi boss) được **tổng hợp số trực tiếp bằng thuật toán DSP (Digital Signal Processing)** trong `tools/gen_audio.py`. Âm thanh được tạo từ các bộ dao động sóng sin, sóng răng cưa và hàm bao ADSR thuần túy bằng code Python — **hoàn toàn không sử dụng bất kỳ mẫu âm thanh (sample) của bên thứ ba nào**.
>    - Toàn bộ nguồn gốc và giấy phép đều được minh bạch tại [`assets/ATTRIBUTIONS.md`](file:///home/nrin31266/IdeaProjects/space-shooter/assets/ATTRIBUTIONS.md)."*

---

## PHẦN 5: KẾT LUẬN & BỘ CÂU HỎI PHẢN BIỆN Q&A (2 PHÚT)

### 🎯 Mục tiêu
Tóm tắt ngắn gọn thành quả đạt được, định hướng phát triển và mở đầu phần hỏi đáp.

### 🎙️ Lời thoại mẫu
> *"Tóm lại, sau quá trình thực tập tại SafeHorizons, em đã đạt được 3 kết quả chính:  
> 1. Đọc hiểu và tái cấu trúc thành công một dự án mã nguồn mở đa nền tảng bằng LibGDX và Android Studio.  
> 2. Nâng cấp toàn diện gameplay, đồ họa Neon, âm thanh DSP, hệ thống 20 wave data-driven và hệ thống lưu trữ hoàn chỉnh.  
> 3. Nắm vững kỹ năng phối hợp cùng AI Coding Agent thông qua quy chuẩn `AGENTS.md` và pipeline tài nguyên tự động.  
>
> Sản phẩm đã được đóng gói thành công dưới dạng file APK release đã ký số và sẵn sàng cài đặt.  
> Em xin chân thành cảm ơn quý Thầy/Cô đã lắng nghe và em rất mong nhận được những góp ý, câu hỏi từ Hội đồng ạ!"*

---

## 💡 BỘ CÂU HỎI PHẢN BIỆN THƯỜNG GẶP & CÁCH TRẢ LỜI

### ❓ Câu 1: Em đã dùng AI như thế nào? AI có tự viết 100% code không?
* **Trả lời:** *"Dạ thưa Thầy/Cô, AI đóng vai trò như một **Cộng sự lập trình (Pair-programmer)**. Toàn bộ kiến trúc, thiết kế Use Case, định nghĩa luật trong `AGENTS.md` và việc kiểm tra chất lượng mã nguồn (code review) đều do em trực tiếp chỉ đạo và kiểm soát. Em dùng AI để tăng tốc quá trình đọc hiểu mã nguồn cũ, hỗ trợ sinh các script phụ trợ như DSP Audio/Sprite Generator và hỗ trợ tìm lỗi (debug). Mọi quyết định kỹ thuật cuối cùng đều do em kiểm chứng và thực thi."*

### ❓ Câu 2: Tại sao em lại chọn tự sinh tài nguyên (Offline Asset Pipeline) bằng Python thay vì tải ảnh trên mạng về?
* **Trả lời:** *"Dạ, việc tự viết script sinh tài nguyên bằng Python mang lại 3 lợi ích lớn:  
  1. **Bản quyền an toàn 100%:** Vì âm thanh tạo từ thuật toán DSP và hình ảnh tạo từ toán học vector nên không sợ vi phạm bản quyền hay tranh chấp pháp lý.  
  2. **Đồng bộ phong cách đồ họa (Neon Arcade):** Script tự động áp dụng cùng một bảng màu, độ sáng viền và tỉ lệ supersampling giúp game có tính nhất quán cao.  
  3. **Tối ưu dung lượng & Hiệu năng:** Script loại bỏ hoàn toàn các pixel rác (ghost pixels) và nén tối ưu trước khi đóng gói vào APK."*

### ❓ Câu 3: Làm thế nào em đảm bảo game không bị giật lag (drop FPS) trên các thiết bị Android cấu hình yếu?
* **Trả lời:** *"Dạ, em đã tuân thủ nghiêm ngặt các nguyên tắc tối ưu trong LibGDX:  
  1. **Nạp tài nguyên một lần:** Sử dụng `AssetManager` và `TextureRegistry` nạp toàn bộ texture vào RAM từ lúc LoadingScreen, không load file trong vòng lặp game loop.  
  2. **Gộp Draw Call (Batching):** Nền không gian Starfield và các sprite đạn đều vẽ qua một `SpriteBatch` chung.  
  3. **Hạn chế Garbage Collection (GC Spikes):** Tái sử dụng các Vector và đối tượng toán học, tránh lệnh `new` liên tục trong hàm `render()`."*

### ❓ Câu 4: Dự án này có thể mở rộng tiếp theo hướng nào?
* **Trả lời:** *"Dạ, hướng phát triển tiếp theo gồm có:  
  1. Hoàn thiện và build thử nghiệm module iOS trên môi trường macOS/Xcode.  
  2. Bổ sung Cloud Save và Leaderboard thông qua Google Play Games Services.  
  3. Xây dựng bộ kiểm thử tự động (Unit Test / Automated UI Test) cho các logic Manager."*
