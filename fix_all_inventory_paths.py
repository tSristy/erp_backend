import os
import re

backend_dir = '/Volumes/Backup/my-team/erp_backend/src/main/java/org/enterprise/inventory/controller/'
frontend_dir = '/Volumes/Backup/my-team/erp_frontend/src/services/inventory/'

# 1. Patch Backend
for filename in os.listdir(backend_dir):
    if not filename.endswith('.java'): continue
    path = os.path.join(backend_dir, filename)
    with open(path, 'r') as f:
        content = f.read()

    # Look for @RequestMapping("/api/...") or @RequestMapping({"/api/...", ...})
    # If it lacks /v1/inventory, inject it
    
    # We will do a generic regex substitution for simple mappings:
    # @RequestMapping("/api/xyz") -> @RequestMapping("/api/v1/inventory/xyz")
    # Exception: if it already has /api/v1/inventory, ignore it.
    
    def repl_mapping(match):
        raw_path = match.group(1)
        if '/v1/inventory/' in raw_path:
            return match.group(0)
        
        # strip /api/
        if raw_path.startswith('/api/'):
            clean_name = raw_path[5:]
        elif raw_path.startswith('/inventory/'):
            clean_name = raw_path[11:]
        else:
            return match.group(0)
            
        return f'@RequestMapping("/api/v1/inventory/{clean_name}")'

    # Single string mappings
    new_content = re.sub(r'@RequestMapping\("([^"]+)"\)', repl_mapping, content)
    
    # For InventoryReportController which has {"/api/inventory/reports", "/inventory/reports"}
    if filename == 'InventoryReportController.java':
        new_content = re.sub(r'@RequestMapping\(\{[^\}]+\}\)', '@RequestMapping("/api/v1/inventory/reports")', new_content)
        
    if new_content != content:
        with open(path, 'w') as f:
            f.write(new_content)
            print(f"Patched {filename}")

# 2. Patch Frontend
for filename in os.listdir(frontend_dir):
    if not filename.endswith('.ts'): continue
    path = os.path.join(frontend_dir, filename)
    with open(path, 'r') as f:
        content = f.read()
        
    def repl_basepath(match):
        raw_path = match.group(1)
        if '/v1/inventory/' in raw_path:
            return match.group(0)
            
        if raw_path.startswith('/api/v1/'):
            clean_name = raw_path[8:]
        elif raw_path.startswith('/api/'):
            clean_name = raw_path[5:]
        else:
            return match.group(0)
            
        return f'const BASE_PATH = "/api/v1/inventory/{clean_name}";'
        
    new_content = re.sub(r'const BASE_PATH = "([^"]+)";', repl_basepath, content)
    
    if new_content != content:
        with open(path, 'w') as f:
            f.write(new_content)
            print(f"Patched {filename}")

