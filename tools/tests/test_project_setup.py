"""Kiểm tra symlink/config và helper bản gốc bằng Git fixture cục bộ."""
import contextlib
import importlib.util
import io
import json
from pathlib import Path
import subprocess
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]


def load_tool(name):
    spec = importlib.util.spec_from_file_location(name, ROOT/'tools'/f'{name}.py')
    module = importlib.util.module_from_spec(spec)
    spec.loader.exec_module(module)
    return module


class ProjectSetupTests(unittest.TestCase):
    def test_relative_symlinks_and_exact_skill_set(self):
        for link, target in [('CLAUDE.md','AGENTS.md'), ('.claude/skills','.agents/skills')]:
            path = ROOT/link
            self.assertTrue(path.is_symlink())
            self.assertFalse(Path(path.readlink()).is_absolute())
            self.assertEqual(path.resolve(), (ROOT/target).resolve())
        selected = {'game-ui-ux','game-feel','audio-design','performance-optimization','save-systems'}
        skills = ROOT/'.agents/skills'
        self.assertEqual({p.name for p in skills.iterdir() if p.is_dir()}, selected)
        lock = json.loads((ROOT/'skills-lock.json').read_text())
        self.assertEqual(set(lock['skills']), selected)
        for name in selected:
            self.assertTrue((skills/name/'SKILL.md').is_file())
            self.assertTrue(any((skills/name/'references').glob('*.md')))
            self.assertEqual(lock['skills'][name]['ref'], '59b4a4230071028d808ee6623e23fa8e9f64ab67')
        self.assertIn('Apache License', (skills/'LICENSE').read_text())

    def test_mcp_configs_are_project_scoped_without_secrets(self):
        import tomllib
        codex = tomllib.loads((ROOT/'.codex/config.toml').read_text())
        claude = json.loads((ROOT/'.mcp.json').read_text())
        url = 'https://mcp.context7.com/mcp'
        self.assertEqual(codex, {'mcp_servers':{'context7':{'url':url}}})
        self.assertEqual(claude, {'mcpServers':{'context7':{'type':'http','url':url}}})

    def test_docs_checker_catches_missing_links_and_unicode_anchors(self):
        checker = load_tool('check_docs')
        with tempfile.TemporaryDirectory() as directory:
            root = Path(directory)
            (root/'README.md').write_text('[đúng](docs/topic.md#điều-khiển)\n')
            (root/'docs').mkdir()
            topic = root/'docs/topic.md'
            topic.write_text('# Điều khiển\n\nNội dung.\n')
            self.assertEqual(checker.check_documents(root)[0], [])
            topic.write_text('# Điều khiển\n\n[sai](missing.md)\n')
            self.assertTrue(any('missing local target' in error for error in checker.check_documents(root)[0]))


class OriginalSetupTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.module = load_tool('setup_original')
        self.repo = Path(self.temp.name)/'original'
        self.repo.mkdir()
        self.module.ORIGINAL = self.repo
        self.run_git('init', '-q')
        self.run_git('remote','add','origin',self.module.SOURCE)
        for relative,(before,_) in self.module.PATCHES.items():
            path = self.repo/relative
            path.parent.mkdir(parents=True,exist_ok=True)
            path.write_text(before+'\n')
        (self.repo/'game.java').write_text('protected gameplay\n')
        (self.repo/'.gitignore').write_text('build/\n')
        self.run_git('add','.')
        self.run_git('-c','user.name=Fixture','-c','user.email=fixture@example.invalid','commit','-qm','fixture')
        self.module.COMMIT = self.run_git('rev-parse','HEAD')

    def run_git(self,*args):
        return subprocess.check_output(['git','-C',str(self.repo),*args],text=True,stderr=subprocess.DEVNULL).strip()

    def prepare(self,check=False):
        with contextlib.redirect_stdout(io.StringIO()):
            self.module.prepare(check)

    def test_check_does_not_apply_patch_and_prepare_is_idempotent(self):
        with self.assertRaises(ValueError):
            self.prepare(check=True)
        self.assertEqual(self.run_git('diff'), '')
        self.prepare()
        first = self.run_git('diff')
        self.prepare()
        self.prepare(check=True)
        self.assertEqual(self.run_git('diff'), first)
        self.assertEqual(set(self.run_git('diff','--name-only').splitlines()),set(self.module.PATCHES))
        self.assertEqual((self.repo/'game.java').read_text(),'protected gameplay\n')

    def test_unexpected_source_changes_are_rejected_before_packaging(self):
        for path in ['game.java','extra.java']:
            with self.subTest(path=path):
                target = self.repo/path
                before = target.read_text() if target.exists() else None
                target.write_text('user changes\n')
                with self.assertRaises(ValueError):
                    self.prepare()
                self.assertEqual((self.repo/'android/build.gradle').read_text(),self.module.PATCHES['android/build.gradle'][0]+'\n')
                if before is None:target.unlink()
                else:target.write_text(before)

    def test_unexpected_packaging_change_is_preserved(self):
        target = self.repo/'android/build.gradle'
        target.write_text('custom user packaging\n')
        with self.assertRaises(ValueError):
            self.prepare()
        self.assertEqual(target.read_text(),'custom user packaging\n')

    def test_wrong_commit_or_origin_is_rejected(self):
        original_commit = self.module.COMMIT
        self.module.COMMIT = '0'*40
        with self.assertRaises(ValueError):self.prepare()
        self.module.COMMIT = original_commit
        self.run_git('remote','set-url','origin','https://example.invalid/other.git')
        with self.assertRaises(ValueError):self.prepare()


if __name__ == '__main__':
    unittest.main()
