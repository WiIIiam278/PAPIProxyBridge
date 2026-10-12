import copy
from pathlib import Path
import tempfile
import unittest

import yaml

from check_fabric_publication import ROOT, check_artifacts, fabric_targets, java_version, validate_workflow


class PublicationTests(unittest.TestCase):
    def setUp(self):
        self.expression = "${{ env.version_name }}"
        self.versions = {"1.20.1", "26.3"}
        ordered = sorted(self.versions)
        self.document = {"jobs": {"build": {"steps": [
            {"uses": "WiIIiam278/bones-publish-action@v1", "with": {
                "distro-names": "\n".join("fabric-" + v for v in ordered),
                "distro-groups": "fabric\nfabric",
                "distro-descriptions": "\n".join("Fabric " + v for v in ordered),
                "files": "\n".join(self.jar(v) for v in ordered),
            }},
            *[{"uses": "WiIIiam278/mc-publish@hangar", "with": {
                "loaders": "fabric", "game-versions": v,
                "files-primary": self.jar(v), "java": java_version(v),
            }} for v in ordered],
        ]}}}

    def jar(self, version):
        return f"target/PAPIProxyBridge-Fabric-{self.expression}+mc.{version}.jar"

    def check(self, document=None):
        validate_workflow(document or self.document, self.versions, self.expression)

    def test_complete_publication_passes(self):
        self.check()

    def test_missing_bones_target_fails(self):
        fields = self.document["jobs"]["build"]["steps"][0]["with"]
        for key in fields:
            fields[key] = fields[key].splitlines()[0]
        with self.assertRaisesRegex(ValueError, "distributions differ"):
            self.check()

    def test_misaligned_bones_columns_fail(self):
        self.document["jobs"]["build"]["steps"][0]["with"]["distro-groups"] = "fabric"
        with self.assertRaisesRegex(ValueError, "different lengths"):
            self.check()

    def test_wrong_bones_filename_fails(self):
        fields = self.document["jobs"]["build"]["steps"][0]["with"]
        fields["files"] = "\n".join(reversed(fields["files"].splitlines()))
        with self.assertRaisesRegex(ValueError, "metadata"):
            self.check()

    def test_missing_modrinth_target_fails(self):
        self.document["jobs"]["build"]["steps"].pop()
        with self.assertRaisesRegex(ValueError, "publishers differ"):
            self.check()

    def test_duplicate_modrinth_target_fails(self):
        steps = self.document["jobs"]["build"]["steps"]
        steps.append(copy.deepcopy(steps[-1]))
        with self.assertRaisesRegex(ValueError, "publishers differ"):
            self.check()

    def test_wrong_modrinth_filename_fails(self):
        self.document["jobs"]["build"]["steps"][-1]["with"]["files-primary"] = self.jar("26.2")
        with self.assertRaisesRegex(ValueError, "Modrinth JAR"):
            self.check()

    def test_wrong_java_version_fails(self):
        self.document["jobs"]["build"]["steps"][-1]["with"]["java"] = 21
        with self.assertRaisesRegex(ValueError, "Java requirement"):
            self.check()

    def test_extra_game_version_fails(self):
        self.document["jobs"]["build"]["steps"][-1]["with"]["game-versions"] = "26.3\n26.2"
        with self.assertRaisesRegex(ValueError, "ambiguous"):
            self.check()

    def test_java_policy_preserves_legacy_targets(self):
        for version, java in [("1.20.1", 17), ("1.21.10", 21), ("1.21.11", 21),
                              ("26.1.2", 25), ("26.2", 25), ("26.3", 25)]:
            with self.subTest(version=version):
                self.assertEqual(java_version(version), java)

    def test_repository_workflows_cover_all_targets(self):
        versions = fabric_targets(ROOT)
        for name, expression in [("ci.yml", "${{ env.version_name }}"),
                                 ("release.yml", "${{ github.event.release.tag_name }}")]:
            with self.subTest(workflow=name):
                document = yaml.safe_load((ROOT / ".github/workflows" / name).read_text())
                validate_workflow(document, versions, expression)


class ArtifactTests(unittest.TestCase):
    def setUp(self):
        self.temporary = tempfile.TemporaryDirectory()
        self.addCleanup(self.temporary.cleanup)
        self.root = Path(self.temporary.name)
        (self.root / "target").mkdir()
        self.jar = self.root / "target/PAPIProxyBridge-Fabric-test+mc.26.3.jar"

    def test_missing_artifact_fails(self):
        with self.assertRaisesRegex(ValueError, "exactly one non-empty"):
            check_artifacts(self.root, {"26.3"})

    def test_empty_artifact_fails(self):
        self.jar.touch()
        with self.assertRaisesRegex(ValueError, "exactly one non-empty"):
            check_artifacts(self.root, {"26.3"})

    def test_duplicate_artifacts_fail(self):
        self.jar.write_bytes(b"fixture")
        self.jar.with_name("PAPIProxyBridge-Fabric-other+mc.26.3.jar").write_bytes(b"fixture")
        with self.assertRaisesRegex(ValueError, "exactly one non-empty"):
            check_artifacts(self.root, {"26.3"})

    def test_one_nonempty_artifact_passes(self):
        self.jar.write_bytes(b"fixture")
        check_artifacts(self.root, {"26.3"})


if __name__ == "__main__":
    unittest.main()
