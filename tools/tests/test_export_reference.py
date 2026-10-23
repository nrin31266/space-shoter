import importlib.util
import struct
import tempfile
import unittest
from pathlib import Path

spec = importlib.util.spec_from_file_location('export_reference', Path(__file__).parents[1] / 'export_reference.py')
exporter = importlib.util.module_from_spec(spec)
spec.loader.exec_module(exporter)


class ExportReferenceTest(unittest.TestCase):
    def test_documents_and_large_reference(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'sample.prefab'
            path.write_text('%YAML 1.1\n%TAG !u! tag:unity3d.com,2011:\n'
                            '--- !u!114 &-9007199254740993\nMonoBehaviour:\n'
                            '  ref: {fileID: 9007199254740999, guid: abc}\n'
                            '  WeaponLevels: 010000000000000000000000\n'
                            '  PhaseAOrder: 0300000002000000\n'
                            '--- !u!1 &2 stripped\nGameObject:\n  m_Name: Test\n')
            result = exporter.read_documents(path)
            self.assertEqual(result[0]['fileID'], '-9007199254740993')
            self.assertEqual(result[0]['data']['ref']['fileID'], '9007199254740999')
            self.assertEqual(result[1]['type'], 'GameObject')
            self.assertEqual(result[0]['data']['WeaponLevels'], [1, 0, 0])
            self.assertEqual(result[0]['data']['PhaseAOrder'], [3, 2])

    def test_hex_only_for_known_integer_array(self):
        raw = struct.pack('<iii', 1, 0, -2).hex()
        self.assertEqual(exporter.normalize({'WeaponLevels': raw})['WeaponLevels'], [1, 0, -2])
        self.assertEqual(exporter.normalize({'Id': raw})['Id'], raw)
        with self.assertRaises(ValueError):
            exporter.normalize({'WeaponLevels': '01'})

    def test_sprite_metadata_has_signed_ids_and_bottom_origin(self):
        meta = {'guid': 'abc', 'TextureImporter': {'spritePixelsToUnits': 56.25,
                'spriteMode': 2, 'spriteSheet': {'sprites': [{'name': 'frame', 'internalID': -9007199254740999,
                'rect': {'x': 16, 'y': 32, 'width': 48, 'height': 64}, 'pivot': {'x': .3, 'y': .8}}]}}}
        entry = exporter.sprite_records(Path('sheet.png'), meta, 'art/sheet.png')['abc:-9007199254740999']
        self.assertEqual(entry['rect']['y'], 32)
        self.assertEqual(entry['pivot'], {'x': .3, 'y': .8})
        self.assertEqual(entry['ppu'], 56.25)
        meta['TextureImporter']['spritePixelsToUnits'] = 0
        with self.assertRaises(ValueError):
            exporter.sprite_records(Path('sheet.png'), meta, 'art/sheet.png')

    def test_single_sprite_ignores_stale_sheet_metadata(self):
        with tempfile.TemporaryDirectory() as directory:
            path = Path(directory) / 'single.png'
            path.write_bytes(b'\x89PNG\r\n\x1a\n' + b'\x00' * 8 + struct.pack('>II', 128, 64))
            meta = {'guid': 'abc', 'TextureImporter': {'spritePixelsToUnits': 21, 'spriteMode': 1,
                    'spritePivot': {'x': .5, 'y': .5}, 'spriteSheet': {'sprites': [
                    {'name': 'stale', 'internalID': 3, 'rect': {'x': 3, 'y': 5, 'width': 121, 'height': 118},
                     'pivot': {'x': 0, 'y': 0}}]}}}
            records = exporter.sprite_records(path, meta, 'art/single.png')
            self.assertEqual(set(records), {'abc:21300000'})
            self.assertEqual(records['abc:21300000']['rect']['height'], 64)


if __name__ == '__main__':
    unittest.main()
