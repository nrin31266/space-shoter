import copy
import importlib.util
import json
import tempfile
import unittest
from pathlib import Path

ROOT = Path(__file__).resolve().parents[2]
spec = importlib.util.spec_from_file_location('asset_audit', ROOT / 'tools/audit_runtime_assets.py')
module = importlib.util.module_from_spec(spec)
spec.loader.exec_module(module)


class AssetAuditTests(unittest.TestCase):
    def test_current_catalog(self):
        result = module.audit(ROOT)
        self.assertEqual((result['images'], result['audio'], len(result['cue_ids'])), (166, 44, 41))

    def invalid(self, mutate):
        catalog = json.loads((ROOT / 'assets/campaign/catalog.json').read_text())
        mutate(catalog)
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root / 'assets/campaign').mkdir(parents=True)
            (root / 'core/src').mkdir(parents=True)
            (root / 'core/src/Direct.java').write_text('VFX/beam-energy.png VFX/ion-curtain.png')
            (root / 'assets/campaign/catalog.json').write_text(json.dumps(catalog))
            with self.assertRaises(ValueError):
                module.audit(root)

    def test_orphan_rejected(self):
        self.invalid(lambda c: c['files'].append('art/orphan.png'))

    def test_duplicate_cue_rejected(self):
        def duplicate(c):
            palette = next(r for r in c['records'].values() if r['path'].endswith('AudioPalette.asset'))
            data = next(d['data'] for d in palette['documents'] if 'Cues' in d['data'])
            data['Cues'].append(copy.deepcopy(data['Cues'][0]))
        self.invalid(duplicate)
