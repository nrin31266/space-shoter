# Tài nguyên và ghi nhận nguồn

## Runtime campaign

Bộ `assets/campaign` đang được runtime custom dùng: catalog, atlas, 169 PNG nguồn,
44 WAV và 3 TTF đã export. Có 447 sprite IDs và 216 dependency records production;
22 records không được dùng và 17 ảnh tĩnh thay bằng sheet đã bị loại.
Art nguồn được giữ để tái pack, atlas giữ rect/pivot/PPU qua loader.

| Nhóm | Ghi nhận và bằng chứng |
|---|---|
| Artwork tàu, enemy/boss, UI, VFX | Nguyễn Văn Rin, thực hiện với PixelLab/MCP, AI/agent, Aseprite/procedural tools; `campaign/licenses/artwork-manifest.json` |
| Music | Andrew Sitkov; license thương mại trong `campaign/licenses/audio` |
| SFX | Kenney CC0 và Little Robot Sound Factory CC BY 3.0; nguồn/license trong `campaign/licenses/audio` |
| Synth cues | Tác giả dự án; manifest audio ghi từng clip |
| Chakra Petch, VT323 | OFL và license đi cùng `campaign/fonts` |

Nền source: [Fando/space-shooter](https://github.com/Fando/space-shooter).
Không gọi artwork AI là vẽ tay, không gán CC0 chung cho cả bộ.

## Tài nguyên legacy

Các PNG, music/sounds và font legacy ngoài `campaign/` đã được dọn sạch khỏi cây
`assets/` của dự án custom. Bản original dùng checkout riêng tại `space-shooter-original/`.

Chi tiết đối chiếu nội bộ ở [nguồn tham khảo](reference-source.md),
quy trình ở [asset pipeline](asset-pipeline.md).
