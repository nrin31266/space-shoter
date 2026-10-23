# Quy trình AI và tiết kiệm context

## Cấu hình theo dự án

Codex đọc AGENTS.md; Claude đọc CLAUDE.md là symlink tới cùng file. Năm skill được
lưu thật trong .agents/skills; .claude/skills trỏ tương đối tới đó. Nội dung và
references upstream giữ English, docs dự án dùng tiếng Việt. Nguồn/license/commit
của skill ở [.agents/skills/UPSTREAM.md](../../.agents/skills/UPSTREAM.md).

Context7 HTTP nằm trong .codex/config.toml và .mcp.json. Server hiện cho phép tra
không có key trong quota hiện tại; nếu cần hạn mức cao hơn, giữ key ngoài Git.
Đã resolve `/libgdx/libgdx.github.io` và query AssetManager/TextureAtlas. Chỉ gửi
câu hỏi API, không gửi source riêng/secret; đối chiếu LibGDX 1.12 trong dự án.
MCP config mới cần client trust/approve trước khi nạp tool. Kiểm tra hiện tại:
Claude báo Pending approval cho context7; Codex CLI chưa load config dự án vì
checkout chưa được trust trong CLI. Mở client và xác nhận trust/Context7 của dự
án khi được hỏi; không sửa cấu hình global để né bước này. Kết nối HTTP trực tiếp
đã thành công, khác với việc client đã nạp MCP.

Nguồn chính thức: [Context7](https://github.com/upstash/context7),
[Codex MCP](https://learn.chatgpt.com/docs/extend/mcp),
[Claude MCP](https://code.claude.com/docs/en/mcp).

## 9Router hiện có

Claude CLI lấy cấu hình auth/base URL đã có của người dùng, không copy token vào
project. Router local: http://127.0.0.1:20128/v1. Chỉ tái sử dụng instance đang
chạy; nếu chưa chạy, mở 9router theo cấu hình của người dùng, không reset provider.

| Route | Trạng thái đã thử |
|---|---|
| `ag/gemini-3.8-flash-high` | Claude CLI nhận ROUTER_OK qua proxy |
| `oc/deepseek-v4.1-flash-free` | Provider trả 429 FreeUsageLimitError |

Đây là tên route do router báo, không phải chứng nhận model thực phía provider.
Không tự đổi global model/alias hoặc thêm `[1m]` để giả bảo đảm context 1M.
Khi DeepSeek hết quota dùng Gemini; khi cả hai lỗi, báo lỗi và Codex tiếp tục
phần tự làm được. Không tự đăng ký gói trả phí hoặc chuyển provider ngoài yêu cầu.

CLI API có xác thực nội bộ của 9Router xác nhận RTK=true, Headroom=false. RTK
nén tool_result trước khi gửi model; không kéo dài vô hạn context chat Codex và
không thay checkpoint. Theo dõi Dashboard Token Saver/request usage trước và sau
job đại diện; chưa có số đo tiết kiệm riêng cho Space. Không bật Headroom hoặc
cài RTK CLI khác chỉ vì quảng cáo phần trăm tiết kiệm.

## Giao việc tối đa qua 9Router

Ưu tiên Claude/Gemini thực hiện các phần có thể tách rõ: inventory/exporter,
DTO/validator, logic combat/reward/flight, content batches, Scene2D screens, audio/
VFX và targeted tests. Codex chốt contract, giữ file tích hợp chung, review diff,
chạy cổng kiểm tra và xử lý xung đột. Không giới hạn Claude ở việc hỏi đáp/review.

Mặc định tối đa hai job độc lập chạy đồng thời khi file scope và interface đã
chốt. Job phụ thuộc kết quả nhau chạy tuần tự. Mỗi file chỉ có một bên sở hữu
trong lúc sửa; các file MainGame/GamePlayScreen, Gradle và catalog/schema chung
do Codex tích hợp. Không tự tạo branch hoặc worktree nếu chưa được yêu cầu.

Một job nhận đúng mục tiêu, file được đọc/sửa, contract và tiêu chí hoàn tất.
Chỉ gửi đoạn source/canonical owner cần thiết, không cả repository hoặc transcript.
Đầu ra gồm file đã đổi, hành vi, kiểm tra đã/chưa chạy, rủi ro còn lại; Codex đọc
diff thay vì yêu cầu model kể lại code. Tóm tắt ngắn được giữ ở checkpoint.

## Lệnh và vòng đời job

Dùng claude -p, --output-format json, --max-turns theo độ lớn job và model route
Gemini đã cấu hình. --bare giảm nạp memory/hooks/plugin/MCP tự động; khi dùng,
job phải được cấp các quy tắc AGENTS và context liên quan một cách tường minh.
Chỉ mở Read/Glob/Grep/Write/Edit cho job code; Bash/build chỉ giao khi thật sự
cần và được phân công riêng để tránh nhiều bên chạy cùng check.

Ví dụ job code có file scope ghi trong /tmp/space-task.txt:

```bash
claude --bare --model ag/gemini-3.8-flash-high \
  --tools Read,Glob,Grep,Write,Edit --allowedTools Read Glob Grep Write Edit \
  --strict-mcp-config --mcp-config '{"mcpServers":{}}' \
  --permission-mode acceptEdits --max-turns 12 --output-format json \
  -p < /tmp/space-task.txt > /tmp/space-task-result.json
```

Tool allowlist không thay thế file scope/review: luôn kiểm tra diff và không cấp
Bash hoặc MCP của dự án khác khi job không cần. Job chỉ phân tích dùng --tools ''
và --max-turns 1. Không dùng dangerously-skip-permissions.

Lưu session_id từ JSON. Chỉ dùng --resume ID cho việc còn liên quan của cùng
module; module mới bắt đầu session mới để tránh context phình. Không dùng
--continue vì có thể tiếp tục nhầm job gần nhất. Giới hạn thời gian ở tiến trình
cha, kiểm tra exit status/is_error/terminal_reason và file thực tế sau mỗi job.
Job timeout/quota: lưu phần đã làm, thử lại có giới hạn hoặc thu nhỏ việc; không
chạy vòng lặp retry vô hạn và không kết luận Gemini không hỗ trợ chỉ từ timeout.

Giữ RTK đang bật; theo dõi token/latency của job thật, không chạy health-check
lặp lại trước từng việc. DeepSeek chỉ dùng lại khi quota sẵn; không liên tục thử
route đã báo hết quota. Không bật thêm Headroom hoặc mở model khác ngoài scope.

## Kiểm tra tập trung

Claude không tự build APK hoặc chạy full suite sau mỗi chỉnh sửa. Codex gom các
job cùng subsystem rồi chạy targeted check một lần; build/smoke tại các cổng của
[kế hoạch phát triển](../production/development-plan.md). Kết quả pass được giữ
cùng revision/config; chỉ chạy lại nếu dependency liên quan thay đổi. Lỗi cục bộ
thì chạy lại check cục bộ, không quay về test toàn bộ game cũ.

## Tiếp tục sau compaction hoặc session mới

Đọc AGENTS → [checkpoint](../production/checkpoint.md) → owner liên quan → diff
và file cần sửa. Checkpoint chỉ giữ quyết định, tiến độ, lệnh/kết quả và bước kế;
không copy toàn transcript, log dài hoặc dump source vào docs. Lưu log/job JSON
ở /tmp hoặc build output được ignore; đưa session_id cần resume vào checkpoint
khi job vẫn còn giá trị. Xong job thì giữ kết quả đã review, không nạp lại cả lịch sử.

Tài liệu tham khảo: [chạy Claude theo chương trình](https://code.claude.com/docs/en/headless),
[giảm context/token](https://code.claude.com/docs/en/costs), [Claude CLI](https://code.claude.com/docs/en/cli-reference),
[9Router/RTK](https://github.com/decolua/9router). Không coi số tiền CLI ước lượng
cho model tùy biến là hóa đơn thực của provider.
