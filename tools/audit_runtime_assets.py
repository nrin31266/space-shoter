#!/usr/bin/env python3
"""Liệt kê tham chiếu từng PNG/WAV; không đánh đồng với runtime draw/play coverage."""
import json
from pathlib import Path


def audit(root):
    catalog = json.loads((root / 'assets/campaign/catalog.json').read_text())
    references = {path: [] for path in catalog['files'] if path.endswith(('.png', '.wav'))}
    resources = catalog['resources']

    def walk(value, owner, field):
        if isinstance(value, dict):
            path = resources.get(value.get('guid'))
            if path in references:
                references[path].append(f'{owner} / {field}')
            for key, child in value.items():
                walk(child, owner, f'{field}.{key}')
        elif isinstance(value, list):
            for index, child in enumerate(value):
                walk(child, owner, f'{field}[{index}]')

    for record in catalog['records'].values():
        for doc in record['documents']:
            data = doc['data']
            owner = record['path'] + ' / ' + (data.get('m_EditorClassIdentifier') or doc['type'])
            walk(data, owner, doc['fileID'])
    java = '\n'.join(path.read_text() for path in (root / 'core/src').rglob('*.java'))
    for path in references:
        if path.startswith('art/') and path[4:] in java:
            references[path].append('Java trực tiếp / ' + path[4:])
    missing = [path for path, refs in references.items() if not refs]
    if missing:
        raise ValueError('Không có tham chiếu: ' + ', '.join(missing))
    palette = next(record for record in catalog['records'].values() if record['path'].endswith('AudioPalette.asset'))
    data = next(doc['data'] for doc in palette['documents'] if 'Cues' in doc['data'])
    ids = {cue['Id'] for cue in data['Cues']}
    if len(data['Cues']) != 41 or ids != set(range(41)):
        raise ValueError('Thiếu/duplicate AudioCue IDs')
    for path in references:
        if not (root / 'assets/campaign' / path).is_file():
            raise ValueError('Thiếu file: ' + path)
    return {'evidence': 'Configured references, not runtime activation coverage',
            'images': sum(path.endswith('.png') for path in references),
            'audio': sum(path.endswith('.wav') for path in references),
            'cue_ids': sorted(ids), 'resources': {path: sorted(set(refs)) for path, refs in references.items()}}


if __name__ == '__main__':
    root = Path(__file__).resolve().parents[1]
    result = audit(root)
    target = root / 'build/verification/asset-references.json'
    target.parent.mkdir(parents=True, exist_ok=True)
    target.write_text(json.dumps(result, ensure_ascii=False, indent=2) + '\n')
    print(f"PASS: {result['images']} PNG, {result['audio']} WAV, 41 cue IDs có tham chiếu; {target}")
