#!/usr/bin/env python3
"""Export dữ liệu/tài nguyên offline; không chạy hoặc thay đổi Unity."""
import argparse
import hashlib
import json
import re
import shutil
import struct
import subprocess
from pathlib import Path

import yaml

INTEGER_ARRAYS = {'WeaponLevels', 'weaponLevels', 'PhaseAOrder', 'PhaseBOrder'}


def normalize(value, field=''):
    if isinstance(value, dict):
        return {k: str(v) if k == 'fileID' else normalize(v, k) for k, v in value.items()}
    if isinstance(value, list):
        return [normalize(v) for v in value]
    if field in INTEGER_ARRAYS and isinstance(value, str):
        if len(value) % 8 or not re.fullmatch('[0-9a-fA-F]*', value):
            raise ValueError(f'{field}: invalid int32 hex array')
        raw = bytes.fromhex(value)
        return list(struct.unpack('<' + 'i' * (len(raw) // 4), raw))
    return value


def read_documents(path):
    text = path.read_text(encoding='utf-8-sig')
    # YAML 1.1 nhận chuỗi hex chỉ gồm chữ số thành số bát phân và làm mất dữ liệu.
    fields = '|'.join(sorted(INTEGER_ARRAYS))
    text = re.sub(r'^(\s*(?:' + fields + r'):\s*)([0-9a-fA-F]+)\s*$',
                  lambda m: m[1] + '"' + m[2] + '"', text, flags=re.M)
    headers = list(re.finditer(r'^--- !u!(\d+) &(-?\d+)(?: stripped)?\s*$', text, re.M))
    if not headers:
        raise ValueError(f'{path}: missing Unity document header')
    result = []
    for index, header in enumerate(headers):
        end = headers[index + 1].start() if index + 1 < len(headers) else len(text)
        document = yaml.safe_load(text[header.end():end])
        if not isinstance(document, dict) or len(document) != 1:
            raise ValueError(f'{path}: invalid document {header.group(2)}')
        kind, data = next(iter(document.items()))
        result.append({'fileID': header.group(2), 'type': kind, 'data': normalize(data)})
    return result


def png_size(path):
    header = path.read_bytes()[:24]
    if header[:8] != b'\x89PNG\r\n\x1a\n':
        raise ValueError(f'{path}: invalid PNG')
    return struct.unpack('>II', header[16:24])


def sprite_records(path, meta, target):
    importer = meta['TextureImporter']
    ppu = importer['spritePixelsToUnits']
    if ppu <= 0:
        raise ValueError(f'{path}: invalid PPU')
    guid = meta['guid']
    names = {item['second']: str(next(iter(item['first'].values())))
             for item in importer.get('internalIDToNameTable', [])}
    entries = importer.get('spriteSheet', {}).get('sprites', [])
    result = {}
    if importer['spriteMode'] == 2 and not entries:
        raise ValueError(f'{path}: empty sprite sheet')
    if importer['spriteMode'] == 1 or not entries:
        w, h = png_size(path)
        entry = {'path': target, 'name': path.stem, 'rect': {'x': 0, 'y': 0, 'width': w, 'height': h},
                 'pivot': importer['spritePivot'], 'ppu': ppu}
        result[guid + ':21300000'] = entry
        if importer['spriteMode'] == 1:
            return result
    for sprite in entries:
        name = sprite['name']
        file_id = str(sprite.get('internalID', names.get(name, '')))
        if not file_id or file_id == '0':
            raise ValueError(f'{path}: missing sprite ID {name}')
        result[guid + ':' + file_id] = {'path': target, 'name': name, 'rect': sprite['rect'],
                                      'pivot': sprite['pivot'], 'ppu': ppu}
    return result


def export(source, output):
    source, output = source.resolve(), output.resolve()
    if output == source or source in output.parents:
        raise ValueError('Output must be outside reference repository')
    root = source / 'app/Assets/StarShift'
    if not (root / 'Scenes/02_Gameplay.unity').is_file():
        raise ValueError(f'{root}: missing production scene')
    records, sprites, script_types, files, hashes, resources = {}, {}, {}, [], {}, {}
    for path in sorted(root.rglob('*')):
        if not path.is_file() or path.suffix == '.meta':
            continue
        relative = path.relative_to(root).as_posix()
        if path.suffix in {'.asset', '.prefab'} or relative == 'Scenes/02_Gameplay.unity':
            meta = yaml.safe_load(Path(str(path) + '.meta').read_text())
            records[meta['guid']] = {'path': relative, 'documents': read_documents(path)}
        elif path.suffix == '.cs' and relative.startswith('Scripts/'):
            meta = yaml.safe_load(Path(str(path) + '.meta').read_text())
            script_types[meta['guid']] = path.stem
        target = None
        if relative.startswith('Art/') and path.suffix == '.png':
            target = 'art/' + relative[4:]
            meta = yaml.safe_load(Path(str(path) + '.meta').read_text())
            sprites.update(sprite_records(path, meta, target))
        elif relative.startswith('Audio/') and path.suffix == '.wav':
            target = 'audio/' + relative[6:]
        elif relative.startswith('UI/Fonts/') and path.suffix in {'.ttf', '.txt'}:
            target = 'fonts/' + path.name
        if target:
            metadata = yaml.safe_load(Path(str(path) + '.meta').read_text())
            resources[metadata['guid']] = target
            destination = output / target
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(path, destination)
            files.append(target)
            hashes[target] = hashlib.sha256(path.read_bytes()).hexdigest()
    evidence = [('tools/audio/licenses', 'audio'), ('tools/audio/sources.json', 'audio/sources.json'),
                ('tools/assets/m8/sources/manifest.json', 'artwork-manifest.json')]
    for original, target in evidence:
        path = source / original
        if not path.exists():
            raise ValueError(f'Missing license/provenance: {path}')
        destination = output / 'licenses' / target
        if path.is_dir():
            shutil.copytree(path, destination, dirs_exist_ok=True)
        else:
            destination.parent.mkdir(parents=True, exist_ok=True)
            shutil.copyfile(path, destination)
    commit = subprocess.check_output(['git', '-C', str(source), 'rev-parse', 'HEAD'], text=True).strip()
    catalog = {'schema': 1, 'sourceCommit': commit, 'records': records, 'sprites': sprites,
               'scriptTypes': script_types, 'files': files, 'sha256': hashes, 'resources': resources}
    (output / 'catalog.json').write_text(json.dumps(catalog, ensure_ascii=False, indent=2) + '\n')
    print(f'Export: {len(records)} records, {len(sprites)} sprite IDs, {len(files)} files; source {commit}')
    return catalog


if __name__ == '__main__':
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument('--source', type=Path, default=Path(__file__).resolve().parents[2] / 'starshift')
    parser.add_argument('--output', type=Path, default=Path(__file__).resolve().parents[1] / 'assets/campaign')
    args = parser.parse_args()
    export(args.source, args.output)
