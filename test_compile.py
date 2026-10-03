import subprocess
try:
    subprocess.run(['javac', '-cp', 'src/main/java', 'src/main/java/org/enterprise/inventory/controller/ProductController.java'], capture_output=True, text=True, check=True)
    print("Compiled successfully!")
except subprocess.CalledProcessError as e:
    print(f"Compilation failed: {e.stderr}")
