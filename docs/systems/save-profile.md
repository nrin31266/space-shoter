# Save và profile

## Hiện trạng

SaveManager.java dùng LibGDX Preferences `space-shooter-save`; SaveData là nested
class trong SaveManager. Source giữ serialization và fallback key cũ.
AudioManager dùng Preferences `SpaceShooter` cho volume/musicMuted.

Custom và original có applicationId khác nhau nên tách sandbox Android. Script
cài bằng install -r, không uninstall hoặc pm clear. Nếu chữ ký khác thì báo lỗi
cài, không tự gỡ app và làm mất dữ liệu.

## Thiết kế phát triển

Tách CampaignSave, PlayerProfile và Settings; checkpoint ở ranh giới encounter,
không snapshot world đang combat. Validate dữ liệu trước khi khôi phục state;
replay/dev run không ghi nhầm vào campaign/profile.

[TBD] Chốt migration hoặc lưu riêng wave save cũ trước khi triển khai persistence
mới. Giữ nguyên Preferences cũ trong lúc chưa có quyết định; không diễn giải wave
save thành campaign save và không tự xóa dữ liệu.
