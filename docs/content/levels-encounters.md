# Level và encounter

## Runtime campaign

Catalog `assets/campaign/catalog.json` chứa tám mission, 21 encounter, hai region.
`CampaignWorld` đọc lịch spawn, formation/sequence, EntryBeats và OptionalEvents;
`HazardSchedule` ghép lịch asteroid theo seed. Definition không giữ runtime clock.
Các mốc nhịp đợi minimum gap/remaining threshold/fallback và optional resolution
trước khi mở nhóm tiếp theo. Optional spawn không chặn mục tiêu required.

Camera lấy orthographicSize, TransitionSeconds và playableViewport từ profile.
Region A chiến đấu dọc, region B chiến đấu ngang. `FlightMath` đổi hệ tọa độ ở
biên; giữ pivot/PPU và transform của prefab khi render. Pickup được giữ qua
handoff encounter; floor reward được phát trước reward grace.

Mission intro có briefing → approach → travel → brake → mission title lead.
Chạm hợp lệ trả input ngay ở approach; enemy schedule chỉ chạy sau mission lead.
Encounter title là overlay có timer riêng, không cộng vào entry lead để khóa chơi.

Kết thúc timeline/survive ngừng producer, cho tàu địch retreat (asteroid tiếp tục
bay, sau 8 giây mới buộc retreat), rồi chờ thời gian đạn hostile còn nhìn thấy.
Boss dọn hostile ngay và chờ corpse presentation. Hoàn tất mới phát floor reward;
đợt giữa bỏ reward grace, giữ pickup qua clear pause/handoff. Clear pause trừ thời
gian đã chờ đạn; handoff dùng HandoffTravelSeconds, không cộng HandoffDelay lần nữa.
Đợt cuối chờ nhặt thưởng hoặc hết RewardGraceSeconds trước khi rời world.

Mission cùng vùng tự chuyển qua chuyến bay missionClearSeconds, giữ input/pose.
Sang vùng mới chạy glide/accelerate/turn/decelerate/reveal theo scene config,
chờ tap, rồi approach/travel/brake. Không đổi mission/save sang đích trước brake.
`regionReady` cho Continue khôi phục tại điểm chờ. Bằng chứng simulation và phần
hình ảnh chưa nghiệm thu được ghi riêng ở checkpoint.

## Phần cũ

WaveManager/EnemyFactory, wave data và gameplay legacy không còn đường gọi đã
được xóa khỏi bản custom. Bản original vẫn ở checkout riêng, không thay gameplay. Tiến độ thật ở [checkpoint](../production/checkpoint.md).
