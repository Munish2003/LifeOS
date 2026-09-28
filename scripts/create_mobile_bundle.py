import os
import zipfile
from pathlib import Path

def create_bundle():
    root_dir = Path(__file__).resolve().parent.parent
    output_zip = root_dir / "LifeOS_Mobile_Project.zip"

    ignore_patterns = {
        ".git", ".idea", ".gradle", "build", ".pytest_cache",
        "__pycache__", ".venv", "venv", ".DS_Store"
    }

    print(f"Creating portable project zip: {output_zip}")
    with zipfile.ZipFile(output_zip, "w", zipfile.ZIP_DEFLATED) as zf:
        for foldername, subfolders, filenames in os.walk(root_dir):
            # Exclude unwanted directories
            subfolders[:] = [d for d in subfolders if d not in ignore_patterns and not d.endswith(".egg-info")]

            rel_folder = os.path.relpath(foldername, root_dir)
            if rel_folder == ".":
                rel_folder = ""

            for filename in filenames:
                if filename == "LifeOS_Mobile_Project.zip" or filename.endswith(".pyc"):
                    continue
                file_path = os.path.join(foldername, filename)
                archive_name = os.path.join(rel_folder, filename)
                zf.write(file_path, archive_name)
                print(f"  Added: {archive_name}")

    print(f"\nSuccessfully created: {output_zip} ({os.path.getsize(output_zip) / (1024 * 1024):.2f} MB)")

if __name__ == "__main__":
    create_bundle()
