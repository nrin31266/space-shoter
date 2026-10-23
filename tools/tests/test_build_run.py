"""Chạy script thật với ADB/Gradle giả; không dùng thiết bị hoặc mạng."""
import json
import os
from pathlib import Path
import shutil
import subprocess
import sys
import tempfile
import unittest

ROOT = Path(__file__).resolve().parents[2]


class BuildRunTests(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.base = Path(self.temp.name)
        self.project = self.base / 'project with spaces'
        (self.project / 'tools').mkdir(parents=True)
        shutil.copyfile(ROOT / 'build_run_spaceshooter.sh', self.project / 'run.sh')
        shutil.copyfile(ROOT / 'tools/android_env.sh', self.project / 'tools/android_env.sh')
        sdk = self.base / 'sdk'
        (sdk / 'platforms/android-34').mkdir(parents=True)
        (sdk / 'platforms/android-34/android.jar').touch()
        jdk = self.base / 'jdk'
        self.executable(jdk / 'bin/java', '#!/bin/bash\necho \'openjdk version "17.0.18"\' >&2\n')
        self.executable(self.project / 'gradlew', '''#!/usr/bin/env python3
import json, os, pathlib, sys
with open(os.environ['TRACE'], 'a') as f: f.write(json.dumps(['gradle', str(pathlib.Path.cwd()), *sys.argv[1:]])+'\\n')
if os.environ.get('FAIL') == 'build': sys.exit(7)
if os.environ.get('FAIL') != 'missing-apk':
 p=pathlib.Path('android/build/outputs/apk/debug/android-debug.apk');p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(b'fixture')
''')
        self.executable(sdk / 'platform-tools/adb', '''#!/usr/bin/env python3
import json, os, sys
args=sys.argv[1:]
with open(os.environ['TRACE'], 'a') as f: f.write(json.dumps(['adb', *args])+'\\n')
if args == ['devices']:
 print('List of devices attached')
 for serial,state in json.loads(os.environ['DEVICES']): print(serial+'\\t'+state)
 sys.exit(3 if os.environ.get('FAIL') == 'devices' else 0)
if args and args[0] == '-s': args=args[2:]
fail=os.environ.get('FAIL')
if args and args[0]=='install' and fail=='install': sys.exit(8)
if args[:3]==['shell','am','force-stop'] and fail=='force-stop': sys.exit(9)
if args[:3]==['shell','am','start']:
 if fail=='launch-exit': sys.exit(10)
 if fail=='launch-error': print('Error type 3: Activity does not exist')
 elif fail=='launch-timeout': print('Status: timeout')
 else: print('Status: ok')
''')
        self.env = dict(os.environ)
        for key in ['ANDROID_SERIAL', 'ANDROID_HOME', 'ANDROID_SDK_ROOT', 'JAVA_HOME']:
            self.env.pop(key, None)
        self.env.update(ANDROID_SDK_ROOT=str(sdk), JAVA_HOME=str(jdk),
                        TRACE=str(self.base / 'trace'), DEVICES=json.dumps([['emu-1', 'device']]))

    def executable(self, path, content):
        path.parent.mkdir(parents=True, exist_ok=True)
        path.write_text(content)
        path.chmod(0o755)

    def run_script(self, *args, stdin='', **overrides):
        trace = Path(self.env['TRACE'])
        trace.unlink(missing_ok=True)
        result = subprocess.run(['bash', str(self.project / 'run.sh'), *args],
                                input=stdin, text=True, capture_output=True, cwd=self.base,
                                env={**self.env, **overrides}, timeout=10)
        self.calls = [json.loads(line) for line in trace.read_text().splitlines()] if trace.exists() else []
        return result

    def assert_not_installed(self):
        self.assertFalse(any('install' in call or 'start' in call for call in self.calls))

    def test_custom_aliases_and_root_from_other_directory(self):
        for flag in ['--custom', '--modern', '-c']:
            with self.subTest(flag=flag):
                result = self.run_script(flag)
                self.assertEqual(result.returncode, 0, result.stderr)
                build = next(call for call in self.calls if call[0] == 'gradle')
                self.assertEqual(build[1], str(self.project))
                self.assertIn(':android:assembleDebug', build)
                self.assertIn('-Pandroid.builder.sdkDownload=false', build)
                self.assertIn(['adb', '-s', 'emu-1', 'install', '-r', str(self.project / 'android/build/outputs/apk/debug/android-debug.apk')], self.calls)
                self.assertIn(['adb', '-s', 'emu-1', 'shell', 'am', 'start', '-W', '-n', 'com.alexei.spaceshooter/com.alexei.spaceshooter.AndroidLauncher'], self.calls)

    def test_no_ready_devices_or_failed_listing_never_builds(self):
        for devices, failure in [([], ''), ([['x', 'offline'], ['y', 'unauthorized']], ''), ([['x', 'device']], 'devices')]:
            with self.subTest(devices=devices, failure=failure):
                result = self.run_script('-c', DEVICES=json.dumps(devices), FAIL=failure)
                self.assertNotEqual(result.returncode, 0)
                self.assertFalse(any(call[0]=='gradle' for call in self.calls))
                self.assert_not_installed()

    def test_multi_device_reprompts_then_uses_selected_serial(self):
        result = self.run_script('-c', stdin='bad\n0\n999999999999999999999\n3\n2\n',
                                 DEVICES=json.dumps([['emu-1', 'device'], ['usb-2', 'device']]))
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn('không hợp lệ', result.stdout)
        self.assertTrue(all(call[2]=='usb-2' for call in self.calls if call[:2]==['adb','-s']))

    def test_android_serial_override_and_rejection(self):
        devices = json.dumps([['emu-1', 'device'], ['usb-2', 'device']])
        result = self.run_script('-c', DEVICES=devices, ANDROID_SERIAL='usb-2')
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertTrue(any(call[:3]==['adb','-s','usb-2'] for call in self.calls))
        result = self.run_script('-c', DEVICES=devices, ANDROID_SERIAL='missing')
        self.assertNotEqual(result.returncode, 0)
        self.assert_not_installed()

    def test_interactive_default_invalid_and_eof(self):
        self.assertEqual(self.run_script(stdin='\n').returncode, 0)
        for value in ['3\n', 'word\n', '']:
            with self.subTest(value=value):
                self.assertNotEqual(self.run_script(stdin=value).returncode, 0)
                self.assert_not_installed()

    def test_unknown_or_extra_flags(self):
        for args in [('--bogus',), ('-c', 'extra')]:
            with self.subTest(args=args):
                self.assertNotEqual(self.run_script(*args).returncode, 0)
                self.assertEqual(self.calls, [])

    def test_original_missing_and_separate_package(self):
        for flag in ['--original', '-o']:
            with self.subTest(flag=flag):
                self.assertNotEqual(self.run_script(flag).returncode, 0)
                self.assert_not_installed()
        original = self.project / 'space-shooter-original'
        original.mkdir()
        shutil.copyfile(self.project / 'gradlew', original / 'gradlew')
        (original / 'gradlew').chmod(0o755)
        (self.project / 'tools/setup_original.py').write_text('import sys\nassert sys.argv[1:] == ["--check"]\n')
        result = self.run_script('-o')
        self.assertEqual(result.returncode, 0, result.stderr)
        self.assertIn(['adb', '-s', 'emu-1', 'shell', 'am', 'start', '-W', '-n', 'com.alexei.spaceshooter.original/com.alexei.spaceshooter.AndroidLauncher'], self.calls)
        self.assertEqual(next(call[1] for call in self.calls if call[0]=='gradle'), str(original))

    def test_failed_build_does_not_install_stale_apk(self):
        apk = self.project / 'android/build/outputs/apk/debug/android-debug.apk'
        apk.parent.mkdir(parents=True)
        apk.write_bytes(b'stale')
        self.assertNotEqual(self.run_script('-c', FAIL='build').returncode, 0)
        self.assert_not_installed()

    def test_successful_build_without_apk_is_failure(self):
        self.assertNotEqual(self.run_script('-c', FAIL='missing-apk').returncode, 0)
        self.assert_not_installed()

    def test_install_failure_stops_before_launch(self):
        self.assertNotEqual(self.run_script('-c', FAIL='install').returncode, 0)
        self.assertFalse(any('start' in call or 'force-stop' in call for call in self.calls))

    def test_force_stop_or_launch_failure_is_not_reported_as_success(self):
        for failure in ['force-stop', 'launch-exit', 'launch-error', 'launch-timeout']:
            with self.subTest(failure=failure):
                result = self.run_script('-c', FAIL=failure)
                self.assertNotEqual(result.returncode, 0)
                self.assertNotIn('Đã build, cài và mở', result.stdout)

    def test_invalid_explicit_toolchain_fails_without_adb(self):
        for overrides in [{'ANDROID_SDK_ROOT': str(self.base/'missing')}, {'JAVA_HOME': str(self.base/'missing')}]:
            with self.subTest(overrides=overrides):
                self.assertNotEqual(self.run_script('-c', **overrides).returncode, 0)
                self.assertEqual(self.calls, [])


if __name__ == '__main__':
    unittest.main()
