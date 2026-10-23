# Đối chiếu cơ chế và tham số

## Phạm vi và bằng chứng

Ngày 2026-10-24. Đối chiếu canonical docs và code/serialized data tại nguồn nội bộ
commit `b07a26b19cd1ad758a298baabec8e5bf0de5f053`. Không sửa repository nguồn.
Routing/ownership theo [nguồn tham khảo](reference-source.md); kết quả phiên chạy
ở [checkpoint](checkpoint.md). Không nâng `[PROVISIONAL]`/`[TUNABLE]` thành locked.

Fresh export vào thư mục tạm, áp cùng pruning recipe rồi so dictionary: **216 records,
447 sprite IDs, 218 file resources** khớp toàn bộ trường/config/pose với source.
169 PNG có trong atlas, 44 WAV giữ hash. Đây là kiểm tra data đầy đủ; không suy
mọi cơ chế runtime đúng chỉ vì config/asset được copy đúng.

## Cơ chế đã đọc và xử lý

| Nhóm | Tham số/contract đối chiếu | Kết quả và kiểm tra |
|---|---|---|
| Input | Relative drag theo screen delta; hold-to-fire; UI tách pointer; không kéo bù delta sau block | Bỏ clear held khi chết; giữ intent, bỏ pending delta khi bị khóa; mất focus vẫn hủy pointer. Parity và respawn assertions |
| Lives | Khởi đầu 3, cap 5 provisional; shield một hit; protection trước shield | Shield absorb không downgrade/life loss; collect lại vẫn một charge. CombatStateCheck + contract regression |
| Death/respawn | Death .6 s, entrance .55 s, protection 2.5 s, fire grace 1 s | Không xóa projectile đã bay; giữ intent; safe anchors; Game Over freeze sau death, không tự Retry. Parity/terminal assertions |
| Heat | Capacity 100; branch 9/11/8.5 heat/s; delay .30; cool 18, overheat 65; chung heat khi đổi súng | Cấu hình nguyên source; combat tests giữ shared heat và latch. HUD có caption/status/fill/vent, không dùng HUD làm authority |
| Weapon | Ba branch, Lv1–5; count/cadence/spread/spacing/damage/speed/lifetime/homing/splash từ definition | So toàn bộ serialized fields; Pulse/Plasma/Homing impacts đúng bank; drone giữ branch Pulse. Không chỉnh balance bằng cảm tính |
| Projectile collision | Box collider riêng, không lấy visual scale/bán kính chung; nearest sweep; bỏ target chưa damage-ready | Sửa Pulse .08×.4 và curtain 1.9×.45 theo prefab; xoay collider khi bay ngang; animate curtain sheet. Regression điểm trúng + curtain cả hai hướng |
| Beam/strike | Sweep tương đối giữa beam/player; chỉ nối trong active window; strike committed target và overshoot .12 viewport | Thêm BeamContact, reset khi cancel/charge; strike linear ra đích, smooth return. Drag xuyên tia/moving-beam assertions |
| Asteroid | Seed, hướng, cone, interval, variants/HP/transit; continuous origin cố định | Sửa nhảy origin sau .65 s; dùng RNG cùng thuật toán System.Random; damage stages lấy config, không tự lấy .5/.25. Chạy trajectory assertions vượt mốc entry |
| Camera/bounds | CameraOverride ưu tiên; current viewport, footprint, zoom; combat Up/Right | Giữ data source. Hint chỉ restricted edge, 2px; source hold/color/segment. Kiểm full-screen và combat view |
| Spawn/flow | EntryBeats, optional, producer cutoff, projectile drain, boss death, grace/clear/handoff | Chạy 8 mission/21 encounter và attack sweep; không spawn mới sau cutoff. Title không cộng vào entry lead |
| Missile reserve | Start 1/cap3; cooldown .8; flight .32; protection .7; normal/large60%/boss10% | Chỉ Running/Drain; clear hostile đúng một lần lúc detonate; không clear khi launch/flash, giữ friendly; round-to-even theo source. Regression trước/sau detonation |
| Drop | Normal/Elite odds, quota/deadline/weights, low-life pity, clutter6, guaranteed overflow | Dùng definition + seeded quota; refresh shield/overdrive/drone đúng receiver source. Gold tests: cap, sweep, bounce/eject/direction/lifetime |
| Boss rewards | Staged death xong mới emit pair/gold; không thưởng module/minion | Dời award đến corpse completion; Victory một lần; assertion không pickup trước death completion rồi đúng bundle sau đó |
| Score/save | Mission-local stats; campaign score carry; Retry checkpoint; Shard high-water; record/entry transaction | Same-region clear commit incoming entry với record trong một write; travel runtime không cộng đôi. Store/flow/replay regressions |
| Replay/dev | Không ghi campaign/profile; settings là exception explicit | Isolated simulation listeners reject writes; runtime smoke dùng Dev/replay. Không xóa/seed save thật |
| Cosmetic | Chassis/skin/trail riêng; default Không Vệt; Ion/Rainbow chỉ phát khi chuyển động, life ≤.16 s, width .09/.10 | Thay sprite rời bằng ribbon nối sample; pause không sinh điểm, đứng yên tự hết. Dùng profile đang trang bị để kiểm hình |
| HUD | Hierarchy Counter 590px; weapon/missile cách cạnh8px source; font38/40/42/32; heat caption/status | Resolve RectTransform từ scene thay anchor trên full screen; bỏ inset dư ở cụm phải; text/icon đúng family; layout assertions 16:9/20:9 |
| Audio | 41 cues/3 BGM; palette gains/interval/variation; 16 voices theo 6 nhóm; crossfade .85 | Pool riêng cho danger/critical/weapon/common/pickup/UI, budgets/duck theo source; giữ UI tail qua đổi màn, không ignition lại ở tap vùng mới. Pool/cap/duck/reset assertions; listening chưa nghiệm thu |

## Dọn file và tái tạo

- Xóa 59 Java legacy classes không reachable từ MainGame hoặc adapter/tests.
  Giữ nguyên checkout original và save/Preferences cũ của người dùng.
- Bỏ 22 records fixture/đường bay không nằm trong graph production.
- Thay placeholder tĩnh bằng first frame của sheet đang bind; bỏ 17 PNG cũ,
  gồm legacy crate và các ảnh tĩnh đã có sheet. Giữ PNG nguồn đang dùng để repack.
- Exporter áp cùng pruning khi chạy lại; atlas-input được xóa trước repack để
  không tái đóng ảnh cũ. Unit test kiểm dependency, material trực tiếp và idempotency.
- Provenance/license vẫn được giữ cho clip/font/art đang dùng.

## Giới hạn đọc kết quả

Source có các bảng lịch sử M2/M4/M5 và target Region C `[FUTURE]`; không áp baseline
cũ đè serialized production L1–L8. Ví dụ bảng camera M5 cũ ghi full viewport trong
khi player owner mới và camera assets đã dùng restricted combat. Lượt này giữ
production override và invariant mới, không sửa source để che khác biệt.

Save Java là format riêng của bản port; không đọc/ghi JSON Unity hoặc migrate
Campaign/Profile nguồn. Các kiểm tra save ở đây là semantic/isolated, không phải
binary-compatible với Unity schema.

Automated trajectory/collision/config tests và ảnh Android xác minh các case đã
chạy. Chưa có nghiệm thu chơi tay trọn campaign, nghe mix trên thiết bị thật,
FPS thật hoặc iOS; không gắn nhãn “100% parity” hay “mọi asset đã được kích hoạt”.

## Bổ sung sau phản hồi cuối

- Boss projectile narrow-phase dùng các collider enabled trên bộ phận/thân,
  bỏ disabled flight envelope; regression tại tâm và góc trống của Vanguard.
- Planet 2 approach/travel/brake dùng .7/1.8/.8 s như PlanetJourney;
  restore region-ready đặt tàu tại (.18,.5), không về checkpoint chiến đấu cũ.
  Flow regression kiểm thời lượng và pose restore.
- Tuning sao/warning và ribbon nằm ở [presentation owner](../presentation/audio-vfx.md).
- Android smoke: menu, Vanguard, ribbon đang kéo, HUD sát cạnh; save byte-for-byte
  không đổi trong Dev replay, log AndroidRuntime/libgdx không có lỗi lượt mới.
