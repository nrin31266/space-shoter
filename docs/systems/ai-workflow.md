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

## Chia việc cho Claude

Codex giữ tích hợp; Claude nhận việc độc lập theo module. Một job cần: mục tiêu,
file được đọc/sửa, hành vi phải giữ, tiêu chí test và đầu ra ngắn. Không để hai bên
sửa một file đồng thời. Không cấp Bash/network/tool không cần chỉ để viết docs.

Ví dụ review gọn, không tool/MCP/session history; prompt đặt trong file tạm ngoài
repo và chỉ chứa phần source cần review:

```bash
claude --bare --model ag/gemini-3.8-flash-high \
  --tools '' --strict-mcp-config --mcp-config '{"mcpServers":{}}' \
  --no-session-persistence --max-turns 1 --output-format json \
  -p < /tmp/space-review-task.txt > /tmp/space-review-result.json
```

`--bare` bỏ memory/hooks/plugin tự động: job cần quy tắc AGENTS thì chỉ rõ nội
dung/ràng buộc cần tuân thủ trong prompt. Job code cần đọc/sửa thì chỉ mở các tool
Read/Glob/Grep/Write/Edit phù hợp và file scope đã chốt; parent kiểm tra diff/tests.
Không dùng dangerously-skip-permissions. Với job cần tiếp tục, bỏ
no-session-persistence, lưu session_id từ JSON và dùng `--resume ID`; không dùng
--continue vì có thể nhặt nhầm job gần nhất. Giới hạn thời gian/số lượt theo việc;
kiểm tra exit status, is_error, terminal_reason và file thật sau mỗi job.

Gemini đã dùng được qua Claude CLI/9Router. Timeout của một job cần được xử lý
bằng giới hạn thời gian, chia nhỏ việc hoặc chạy lại có kiểm soát; không kết luận
route không hoạt động chỉ từ một lần timeout. Kiểm tra diff và kết quả thật trước
khi nhận bàn giao.

## Tiếp tục sau compaction hoặc session mới

Đọc AGENTS → [checkpoint](../production/checkpoint.md) → owner liên quan → diff
và file cần sửa. Checkpoint chỉ giữ quyết định, tiến độ, lệnh/kết quả và bước kế;
không copy toàn transcript, log dài hoặc dump source vào docs. Lưu log/job JSON
ở /tmp hoặc build output được ignore; đưa session_id cần resume vào checkpoint
khi job vẫn còn giá trị. Xong job thì giữ kết quả đã review, không nạp lại cả lịch sử.

Tài liệu tham khảo: [Claude CLI](https://code.claude.com/docs/en/cli-reference),
[9Router/RTK](https://github.com/decolua/9router). Không coi số tiền CLI ước lượng
cho model tùy biến là hóa đơn thực của provider.
