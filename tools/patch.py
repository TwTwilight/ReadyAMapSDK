import argparse
import os
import pathlib
import shutil
import subprocess
import sys
import tempfile
import zipfile

CLASS_REL = "com/amap/api/col/sl3/ip.class"


def find_android_jar(explicit):
    if explicit:
        return explicit
    roots = [
        os.environ.get("ANDROID_HOME"),
        os.environ.get("ANDROID_SDK_ROOT"),
        str(pathlib.Path(os.environ.get("LOCALAPPDATA", "")) / "Android" / "Sdk"),
    ]
    for root in roots:
        if not root or not os.path.isdir(root):
            continue
        platforms = pathlib.Path(root) / "platforms"
        if not platforms.is_dir():
            continue
        for d in sorted(platforms.iterdir(), reverse=True):
            cand = d / "android.jar"
            if cand.is_file():
                return str(cand)
    raise SystemExit("android.jar not found, pass --android-jar")


def compile_ip(tools_dir, android_jar, workdir):
    src = tools_dir / "ip.java"
    if not src.is_file():
        raise SystemExit("ip.java not found: %s" % src)
    proc = subprocess.run(
        ["javac", "--release", "8", "-nowarn", "-encoding", "UTF-8", "-classpath", android_jar, "-d", workdir, str(src)],
        capture_output=True,
        text=True,
    )
    if proc.returncode != 0:
        raise SystemExit("javac failed:\n%s\n%s" % (proc.stdout, proc.stderr))
    compiled = pathlib.Path(workdir) / CLASS_REL
    if not compiled.is_file():
        raise SystemExit("class not produced: %s" % compiled)
    return compiled


def replace_class(jar_path, compiled):
    jar_path = pathlib.Path(jar_path)
    tmp = jar_path.with_suffix(".tmp")
    payload = compiled.read_bytes()
    with zipfile.ZipFile(jar_path) as src, zipfile.ZipFile(tmp, "w", zipfile.ZIP_DEFLATED) as dst:
        for item in src.infolist():
            if item.filename == CLASS_REL:
                continue
            dst.writestr(item, src.read(item.filename))
        info = zipfile.ZipInfo(CLASS_REL, date_time=(2020, 1, 1, 0, 0, 0))
        info.compress_type = zipfile.ZIP_DEFLATED
        dst.writestr(info, payload)
    shutil.move(str(tmp), str(jar_path))
    return payload


def verify(jar_path, payload):
    if b"amap_keys.json" not in payload:
        raise SystemExit("patched ip.class does not reference amap_keys.json")
    with zipfile.ZipFile(jar_path) as z:
        names = set(z.namelist())
        data = z.read(CLASS_REL)
    if CLASS_REL not in names:
        raise SystemExit("patched jar has no ip.class")
    if b"currentKey" not in data or b"loadPool" not in data:
        raise SystemExit("patched ip.class looks wrong")
    return len(names)


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--jar", default="")
    parser.add_argument("--android-jar", default="")
    args = parser.parse_args()

    tools_dir = pathlib.Path(__file__).resolve().parent
    repo = tools_dir.parent
    jar = pathlib.Path(args.jar) if args.jar else repo / "libs" / "amap-3dmap.jar"
    if not jar.is_file():
        raise SystemExit("jar not found: %s" % jar)

    android_jar = find_android_jar(args.android_jar)
    workdir = tempfile.mkdtemp(prefix="amapsdk-patch-")
    try:
        compiled = compile_ip(tools_dir, android_jar, workdir)
        payload = replace_class(jar, compiled)
    finally:
        shutil.rmtree(workdir, ignore_errors=True)

    total = verify(jar, payload)
    print("patched %s (entries=%d, android.jar=%s)" % (jar, total, android_jar))


if __name__ == "__main__":
    sys.exit(main())
