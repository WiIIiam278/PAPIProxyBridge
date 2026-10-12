#!/usr/bin/env python3
"""Fail CI when a Fabric build target is omitted or mislabelled in publication."""
import argparse
from collections import Counter
from pathlib import Path
import re
import sys

import yaml


VERSION = re.compile(r"\d+\.\d+(?:\.\d+)?")
ROOT = Path(__file__).resolve().parents[1]


def require(condition, message):
    if not condition:
        raise ValueError(message)


def fabric_targets(root):
    versions = {p.name for p in (root / "fabric").iterdir()
                if p.is_dir() and VERSION.fullmatch(p.name)}
    nodes = re.findall(r'createNode\("([^"]+)"',
                       (root / "fabric/root.gradle").read_text())
    require(versions and Counter(nodes) == Counter(versions),
            "Fabric directories and preprocessing nodes differ")
    return versions


def java_version(version):
    # Keep aligned with the Fabric compileJava release policy in build.gradle.
    return 17 if version == "1.20.1" else 25 if int(version.split(".")[0]) >= 26 else 21


def validate_workflow(document, versions, version_expression):
    steps = document["jobs"]["build"]["steps"]
    bones = [s["with"] for s in steps
             if s.get("uses", "").startswith("WiIIiam278/bones-publish-action@")]
    require(len(bones) == 1, "Expected one William278.net publisher")
    keys = ("distro-names", "distro-groups", "distro-descriptions", "files")
    columns = [bones[0][key].splitlines() for key in keys]
    require(len({len(c) for c in columns}) == 1, "Distribution lists have different lengths")
    rows = [row for row in zip(*columns) if row[0].startswith("fabric-")]
    require(Counter(row[0][7:] for row in rows) == Counter(versions),
            "William278.net Fabric distributions differ from build targets")
    for name, group, description, filename in rows:
        version = name[7:]
        expected = f"target/PAPIProxyBridge-Fabric-{version_expression}+mc.{version}.jar"
        require((group, description, filename) == ("fabric", f"Fabric {version}", expected),
                f"Incorrect William278.net metadata for {version}")

    publishers = [s["with"] for s in steps
                  if s.get("uses", "").startswith("WiIIiam278/mc-publish@")
                  and s.get("with", {}).get("loaders", "").splitlines() == ["fabric"]]
    found = []
    for publisher in publishers:
        game_versions = publisher["game-versions"].splitlines()
        require(len(game_versions) == 1 and game_versions[0] in versions,
                "Fabric Modrinth publisher has an unknown or ambiguous game version")
        version = game_versions[0]
        found.append(version)
        expected = f"target/PAPIProxyBridge-Fabric-{version_expression}+mc.{version}.jar"
        require(publisher["files-primary"] == expected, f"Incorrect Modrinth JAR for {version}")
        require(str(publisher["java"]) == str(java_version(version)),
                f"Incorrect Modrinth Java requirement for {version}")
    require(Counter(found) == Counter(versions),
            "Modrinth Fabric publishers differ from build targets")


def check_artifacts(root, versions):
    for version in sorted(versions):
        jars = list((root / "target").glob(f"PAPIProxyBridge-Fabric-*+mc.{version}.jar"))
        require(len(jars) == 1 and jars[0].is_file() and jars[0].stat().st_size > 0,
                f"Expected exactly one non-empty Fabric artifact for {version}")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--artifacts", action="store_true", help="Also require every built Fabric JAR")
    args = parser.parse_args()
    try:
        versions = fabric_targets(ROOT)
        for name, expression in (("ci.yml", "${{ env.version_name }}"),
                                 ("release.yml", "${{ github.event.release.tag_name }}")):
            document = yaml.safe_load((ROOT / ".github/workflows" / name).read_text())
            validate_workflow(document, versions, expression)
            print(f"{name}: all {len(versions)} Fabric targets covered")
        if args.artifacts:
            check_artifacts(ROOT, versions)
            print(f"Verified {len(versions)} Fabric artifacts")
    except (ValueError, KeyError, TypeError, OSError, yaml.YAMLError) as error:
        print(f"Fabric publication check failed: {error}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
