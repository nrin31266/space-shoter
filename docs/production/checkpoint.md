# Checkpoint dự án

## Trạng thái

2026-10-23, Linux: **đã hoàn tất và đóng mốc setup codebase/docs**. Đang triển khai G1–G7 theo yêu cầu chạy end-to-end. G1 đã export/pack asset;
G2/G3 đã có bản Android chạy encounter đầu. G4 đang nối directed attacks, hazards
và boss; G5–G7 chưa nghiệm thu. Không coi lịch test damage là gameplay tương đương.

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
Mốc setup đã đóng. Đã lập kế hoạch G1–G7 theo yêu cầu mới, ưu tiên Claude/Gemini
qua 9Router và chỉ test tại điểm quan trọng. Một job phân tích không tool qua
Gemini đã trả kết quả thành công trong lượt lập kế hoạch này; parent giữ Android
workflow, không nhận đề xuất mở desktop target.
Bắt đầu code khi được yêu cầu triển khai [kế hoạch phát triển](development-plan.md).

## Phiên triển khai game đang mở

- Export offline chỉ đọc nguồn b07a26b; 186 PNG, 44 WAV, 3 TTF cùng font/license
  và provenance. Sprite IDs giữ chuỗi; mảng hex int32 và metadata Single/Multiple
  được xử lý riêng. Catalog bao gồm inventory, chưa cắt hết fixture khỏi APK.
- Runtime mới: campaign catalog, atlas loader, prefab pose rendering, lives/heat/
  branch levels, projectile pool/swept collision, reward quota, save file riêng,
  menu/HUD/input relative drag và Android landscape. Legacy Preferences không đổi.
- Claude/Gemini qua 9Router đã tạo save, flight math, combat, reward rules,
  renderer và directed skill; Codex kiểm tra/sửa contract và tích hợp. Job tool
  nhiều lượt không hiệu quả; dùng output code theo module, route flash thường.
- `:core:checkCampaign` pass: save backup/schema/corruption; lives/shield/heat;
  formation/curve/numeric enums; swept collision; 8 missions/21 encounters bằng
  test damage, checkpoint/cleanup. Không chứng minh cân bằng hoặc boss skills.
- `:android:assembleDebug` pass. Emulator-5554 load menu, bắt đầu/Continue,
  encounter đầu và game-over; lỗi callback dọn shot trong collision đã sửa,
  regression riêng pass. Chưa xác nhận pause/audio nghe trực tiếp.
- Tiếp theo: tích hợp DirectedSkill/BossCombat; hazard/optional spawn; hoàn thiện
  campaign/profile/replay và shell/VFX; gom một lượt Android E2E sau tích hợp.
- Không commit hoặc push trong phiên triển khai này.

## Tiếp tục trực tiếp ngày 2026-10-23

- Đã đọc chat bàn giao. Người dùng yêu cầu port sát gameplay/hình ảnh/bố trí,
  tiến hành theo cụm lớn; dừng Gemini/9Router theo chỉ thị mới. Không khởi chạy
  job Gemini tiếp. Hai job tool trước dừng max-turns không có edits; job trả code
  đã dừng. Không đổi cấu hình model toàn máy.
- Đã bổ sung EntryBeats/optional schedule, giữ pickup qua handoff, phát floor
  trước grace; camera transition + playable viewport/footprint theo profile.
- Sửa cadence không backlog sau thả tay; muzzle/color projectile, homing forward/
  launch arc và target shield node. Directed strike không bị flight ghi đè pose;
  boss beam theo hướng ngang, cancel khi grace/retreat. Boss death dùng anchors.
- Bổ sung cleared Continue, mission score, Shard high-water qua Retry, pending
  save retry và cosmetic ownership/equip. Giữ Preferences cũ, không pm clear.
- Menu/hangar/store/help, preview và cosmetic lúc chơi; HUD bố trí theo source
  (nhiệt trên trái, lives dưới trái, missile/weapon dọc dưới phải). Intro và region
  travel là bản tái hiện Java, chưa phải cinematic tương đương hoàn toàn.
- Regression nền + build/cài/mở Android đã pass trước nhóm mới. Ảnh runtime ở
  build/verification. Nhóm mới đang kiểm tra tích hợp: store, math/combat/reward,
  8 mission/21 encounter test damage và sweep attack thực 70 giây/encounter.
  Hai assertion của test mới cần sửa phạm vi (bounds đổi encounter; boss xuất
  hiện lặp khi sweep encounter trước tự advance). Không gọi đó là lỗi gameplay.
- Build dùng JDK17/API34/Build Tools33.0.1 từ Unity 6000.5.9f1 đã cài, vì env
  mặc định trỏ JDK25/SDK khác. Không cài SDK hoặc build Unity.
- Còn phải hoàn tất vòng tích hợp hiện tại, ghi kết quả mới; đối chiếu gần nhất
  cần làm tiếp: cinematic PlanetJourney/LevelFlow, boss skill cue/staged destruction,
  collider/shape chính xác, UI focus và chi tiết presentation. Swept collision hiện
  đã chọn hit gần nhất, thay vì tùy thứ tự enemy trong list.
  Chưa nghiệm thu toàn bộ G1–G7; không claim pixel-perfect hay cân bằng bằng test damage.

## Cổng tích hợp đã pass sau lượt tiếp tục

2026-10-23, Linux, JDK 17.0.18 / API34 / Build Tools33.0.1, working tree chưa commit:

- `./gradlew :core:checkCampaign :android:assembleDebug -Pandroid.builder.sdkDownload=false`
  pass. Gói tích hợp gồm write failure giữ checkpoint, Shard high-water/cosmetic,
  8 mission/21 encounter bằng test damage và sweep 70 giây/encounter để quan sát
  attack thực, hai boss mở phase/core/cancel, Replay không ghi campaign và cleared
  Continue về result. Không suy nghiệm thu balance từ lượt sweep có protection.
- `python3 -m unittest discover -s tools/tests -v`: 23 tests pass.
  `python3 tools/check_docs.py`: pass; `git diff --check`: pass.
- `./build_run_spaceshooter.sh --custom` cài/mở thành công emulator-5554;
  đã quan sát menu preview mới, Continue vào gameplay, projectile/HUD mới.
  Ảnh tại build/verification/menu-current.png và smoke-current.png (không vào Git).
  Log AndroidRuntime/CampaignSave gần lượt này không có lỗi mới.
- Chưa nghe audio trực tiếp, chưa chơi tay trọn campaign/boss, chưa đo thiết bị
  thật hoặc xác minh iOS. G1–G7 chưa đóng nghiệm thu parity hình ảnh/cinematic.

## Ưu tiên mới theo flow chơi — đang làm

Người dùng yêu cầu UI/animation/icon trước, rồi HUD trong Play, rồi toàn flow
bấm tiếp tục → bay/meteor streak → chuyển cảnh → trả input; lấy timing thật từ
code/docs nguồn, không thêm bản gần đúng để gọi hoàn tất.
Đã mở Unity MCP với sự cho phép của người dùng. Instance app@459bcdad, scene
02_Gameplay. Đã quan sát menu, Replay L1 và DevTest boss L4/L8; không build Unity,
không sửa source/scene/asset. Đã trả Editor về Edit Mode ban đầu; git nguồn sạch.
Ảnh tham khảo nằm trong starshift/app/Temp/SpacePortReference (không tracked).

Quan sát live đã xác nhận menu tàu trái/nút phải, title VT323, Canvas1600x900;
combat ẩn planet, background tile theo PPU (36.57×16 units), tốc độ nền .035×3.
Đã sửa phía Java theo đó, HUD đang chuyển kích cỡ/icon/font theo tỉ lệ .8 từ
reference1600x900 sang1280x720. Có preview debug Android isolated qua extras
preview_mission/preview_encounter; release bỏ qua extras, preview không ghi save.

Sửa logic vừa đối chiếu: death giữ active attacks/projectiles, game-over sau .6s,
respawn chọn safe anchors rồi mới trả input, drag gom vào fixed step, zoom giữ
world position quanh tâm camera. Đã thêm CampaignParityCheck; core aggregate +
Android build PASS sau nhóm đó (9 giây). Sau đó đang thêm cache transient renderers,
armor/death fragments/skill cue và UI sửa theo live; chưa build lại nhóm này.
Ưu tiên tiếp ngay: ShipPreview/UiButtonMotion/ShellStarfield → menu/hangar/store,
rồi HUD và LevelFlow/MissionIntroPresentation/PlanetJourney timing từng pha.
Không chạy lại full suite trước mỗi chỉnh UI; gom tại cổng flow có thay đổi.


## Mốc flow theo source — tiếp tục cùng ngày

Đã bỏ cách gom chờ của runtime trước. Đối chiếu trực tiếp EncounterRunner,
LevelFlow, RegionCampaignFlow, PlanetJourney, MissionIntroPresentation,
GameplayHud và ShipPreview/UiButtonMotion; scene serialized là chuẩn kích cỡ,
không lấy mặc định C# khi scene override (planet scale .8 → 1.03).

- UI preview dùng cached sprite động cơ/ribbon, sockets và nhịp thật; nút có
  hover/press motion; hangar/store chuyển sang thẻ preview trước khi mua/equip.
- Flow tách exit drain / projectile drain / boss presentation / reward grace /
  clear pause / handoff. Giữ pickup, không cộng TitleSeconds vào entry lead,
  không cộng HandoffDelay vào ClearPauseSeconds. Timeline cutoff trước spawn.
- Tap trả input tại approach; HUD hiện trở lại trong lúc cinematic vẫn chạy.
  Chuyến mission cùng vùng tự nối, giữ input. Chuyến vùng có đủ 5 pha, chờ tap,
  rồi 3 pha tiếp cận; thêm optional v1 regionReady, snapshot cũ default false.
- Thêm CampaignFlowCheck: input/fire khi approach nhưng chưa spawn, title lead,
  visible projectile drain, clear/handoff, Continue chờ vùng. Aggregate 8 mission/
  21 encounter và Android build PASS sau nhóm flow đầu (5 giây). Sau đó đang
  sửa thẻ UI/briefing tap và required kill counter; cần cổng build cuối.
- Đã cài và quan sát menu trên emulator; ảnh build/verification/menu-flow-current.png.
  Unity quan sát thêm hangar và briefing bằng DevTest, không ghi profile nguồn;
  Editor hiện Play Mode và cần trả Stop sau đối chiếu. Source scene dirty false.
- Còn đối chiếu UI settings/focus, HUD transient icons/heat vent, projectile shape/
  trail và chuyển cảnh trên emulator sau nhóm cuối. Không coi aggregate simulation
  là nghiệm thu hình ảnh, nghe audio hoặc cân bằng chơi tay.

## Hoàn thiện cổng kiểm chứng gameplay và đồng bộ trên thiết bị

- Đã xóa toàn bộ tài nguyên legacy cũ ngoài `assets/campaign/` theo chỉ thị.
- Đã khắc phục lỗi tương thích `Drawable` trong `CampaignUi.java`, chuẩn hóa
  serialize `JsonWriter.OutputType.json` và căn chỉnh phạm vi clamp player trong
  `CampaignGoldCheck.java`.
- Tự động hóa phát hiện môi trường Android API 34 và JDK 17 trong `tools/android_env.sh`.
- Toàn bộ 11 test suites của `:core:checkCampaign` pass 100% (store, flight math,
  combat, gold physics/drops, hazards, 8 missions/21 encounters, parity, flow).
- Build Android `:android:assembleDebug` pass.
- Đã cài đặt và kiểm chứng trực tiếp trên emulator-5554 qua `./build_run_spaceshooter.sh --custom`:
  - Main Menu: preview tàu hoạt họa, hệ thống nút tương tác.
  - Xưởng & Cửa hàng: duyệt thẻ tàu/sơn/vệt, mua tàu Ưng Kích và trang bị thành công.
  - Cài đặt: điều chỉnh thanh trượt âm lượng và công tắc rung.
  - Nhiệm vụ: giao diện 8 màn phân vùng Khu vực A và B, hiển thị kỷ lục, chơi lại.
  - Chiến đấu: dòng sự kiện tiếp cận hành tinh (Astra), vệt sao tốc độ cao,
    điều khiển vuốt/bắn liên tục, hệ thống nhiệt, thanh boss Vanguard và Warden
    (hướng bay ngang), đạn chùm/laser directed beam và modal kết thúc lượt/thử lại.
- Mã nguồn và tài nguyên đã được commit vào nhánh `main`. Tiếp tục bám sát gameplay
  chuẩn cho tới khi đạt độ tương đương hoàn toàn.

## Sửa presentation sau bàn giao — 2026-10-24

- Tiếp tục working tree đang có; không sửa repository Unity nguồn, không commit/push.
  Dùng hướng dẫn game-feel/audio-design để đối chiếu event, không thêm generator asset.
- Sao menu/combat dùng texture speed-streak có sẵn, giảm footprint và mật độ overlay;
  combat cuộn theo heading. Muzzle bám socket tàu, giữ nhịp .1 s; bỏ thanh trắng
  giả vệt projectile. Impact Pulse/Plasma/Homing/Rock dùng bank/scale riêng tại điểm
  swept collision; đạn drone vẫn giữ branch Pulse khi player đang cầm Homing.
- Thiên thạch dùng bốn bộ size/damage/critical đã author, không phóng thêm scale
  lên artwork vốn đã có kích thước riêng. Pickup dùng .14 s/frame, alpha expiry;
  collection bám player. Bổ sung smoke, survival frames, strike launch tại origin;
  sửa khóa sprite support để không crash khi buff drone xuất hiện.
- Reserve splash về .5–2.4 và alpha 1–t; vòng impact mỏng theo source. Boss field
  dùng ion-curtain alpha riêng, beam có lõi và warning mảnh. Cue charge đọc size
  serialized. Audio thêm Heavy charge/shot, Homing impact, boss explosion beats,
  WarpPass/TransitionWind và journey phase cues.
- Boundary hint đọc màu/hold/segment serialized; độ dày 2 pixel theo framebuffer,
  chỉ ở cạnh viewport bị giới hạn. Bỏ việc clamp tự tạo pressure chỉ vì tàu gần mép;
  pressure bắt đầu từ thao tác kéo vượt biên, reset khi thả tay hoặc vùng mở rộng.
- `:core:checkCampaignPresentation :android:assembleDebug -Pandroid.builder.sdkDownload=false`
  PASS với JDK17/API34. Regression gồm ba impact banks/điểm trúng, drone branch,
  12 asteroid states, restricted/full-screen boundary và nearby parity death/respawn/
  zoom/fire backlog. Không chạy lại sweep toàn chiến dịch hoặc tooling không liên quan.
- Toàn bộ 186 PNG nằm trong atlas, 44 WAV và các font khớp SHA-256 catalog;
  đối chiếu thêm 235 file export với source hiện tại không có khác biệt. Đây là
  bằng chứng đóng gói và giữ nguyên asset, không phải bằng chứng tất cả đã xuất hiện
  đúng sự kiện khi chơi. Chưa nghiệm thu 100% runtime asset usage hoặc nghe audio mix.
- APK debug cuối đã cài emulator-5554; đã quan sát menu, briefing, giữ bắn/nhiệt,
  boundary mảnh đúng mép và reserve missile giảm kho/nổ. Log AndroidRuntime/libgdx
  theo PID lượt chạy mới không có lỗi. Ảnh tại build/verification/presentation-*.png.
  Preview là replay isolated, không ghi campaign/profile. Chưa kiểm thiết bị thật/iOS.

## Chốt audit và phản hồi bổ sung — 2026-10-24

Chi tiết và giới hạn ở [port audit](port-audit.md). Đã sửa continuous asteroid transit,
beam swept contact, collider projectile, boss part hitboxes, reward sau staged death,
held-input/terminal freeze, checkpoint incoming mission và Planet 2 restore/timing.
HUD resolve đúng hierarchy nguồn; sao lớn nhẹ, ribbon gradient theo Unity và warning rõ hơn.

- `:core:checkCampaign :android:assembleDebug` PASS; 8 missions/21 encounters,
  save/replay, collision/flow/presentation/HUD regressions. Tooling: 24 Python tests PASS.
- Cleanup: 59 Java legacy, 17 PNG superseded, 22 unreachable records đã bỏ.
  Export hiện tại 216 records/447 sprites/218 resources; 169 PNG, 44 WAV trong APK.
- Fresh pruned export khớp toàn bộ dữ liệu/resources/hash với catalog hiện tại.
- APK cài một lần cuối, mở menu thường. Dev smoke Vanguard + kéo ribbon + HUD;
  ảnh `build/verification/final-menu.png`, `final-trail-hud.png`, `final-boss.png`.
  Save trước/sau Dev smoke không đổi; log lượt mới không có AndroidRuntime/libgdx error.
- Không tìm thấy auto-retry loop; thêm terminal freeze và tiêu thụ preview intent một lần.
  Các lần restart do build/install trước đó không được tính là bằng chứng app tự crash.
- Chưa nghiệm thu chơi tay toàn campaign, nghe mix trên thiết bị thật, FPS thật hoặc iOS.
  Không claim mọi asset đã kích hoạt đúng event hay 100% parity.

## Dev UI và chạm tiếp tục Planet 2 — 2026-10-24

- DevPanel chung menu/pause: dropdown màn, đợt, route, branch/level; God, skip
  intro và fake shots. Route boss/phase B/victory, chuyển mission, full Planet 2
  journey hoặc vào thẳng chờ chạm. Selection có nền checked, underline và marker.
- Fake shots giữ collider/impact nhưng không direct/splash damage; pause Dev
  có áp dụng loadout rồi chơi tiếp, đạn địch thử, supplies và kill enemies.
  Controls runtime chỉ có trong replay; route launch luôn copy state, không ghi save.
- Sửa overlay theo awaitingContinue đổi ngay trong REGION_TRAVEL. Chỉ có lời nhắc
  "Chạm để tiếp tục" và chạm màn hình như Planet 1; không có nút riêng theo yêu cầu.
  Direct/restored ready route giữ title, pose và background vùng đến.
- `:core:checkCampaign :android:assembleDebug` PASS cho thay đổi cơ chế/UI chung;
  sau sửa nhỏ final, `:core:checkCampaignContracts :android:assembleDebug` PASS.
  Dev regressions gồm invalid route, full journey/tap, phase B, fake impact/splash,
  apply loadout và campaign guard. Docs checker và diff whitespace PASS.
- Emulator: đã nhìn dropdown selection, Dev pause, full flight → waiting → M5;
  final tap-only ready → M5. Ảnh `build/verification/dev-planet2-tap-final.png`,
  `dev-planet2-gameplay-final.png`, `dev-pause-final.png`. Save byte-for-byte không đổi.
- Không kiểm thiết bị thật/iOS hoặc toàn bộ tổ hợp Dev; không claim toàn bộ
  Inspector/debug visualization của Unity đã được chuyển.

## Cổng cuối trước PR — 2026-10-24

- Chạy lại `:core:checkCampaign :android:assembleDebug`: PASS. Bao phủ 8 missions /
  21 encounters, hai boss, actual attack scheduling/phase/cancel, save/replay,
  transitions/Planet 2 tap, input/death, reward, HUD/assets và Dev controls.
- 24 Python tooling tests PASS; docs checker PASS và diff whitespace sạch.
- Giữ nguyên save trên emulator; đã kiểm trực tiếp Dev pause, full journey và
  tap-only tới M5 ở mốc trên. Các kiểm tra này không chứng minh không còn mọi
  sai lệch hình ảnh/audio/balance trên thiết bị thật; giữ giới hạn trong port audit.
- Tách toàn bộ 4 commit local cùng các sửa tiếp theo sang `codex/campaign-custom`.
  `codex/old-custom` giữ remote main ban đầu `ff38f3f`; local main trở về mốc đó.
  Không rewrite remote main hoặc force-push; tích hợp qua pull request.
