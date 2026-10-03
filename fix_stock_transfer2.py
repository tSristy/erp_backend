import re

# Fix Backend Controller
path_backend = '/Volumes/Backup/my-team/erp_backend/src/main/java/org/enterprise/inventory/controller/StockTransferController.java'
with open(path_backend, 'r') as f:
    content = f.read()

content = content.replace('@RequestMapping("/api/stock-transfers")', '@RequestMapping("/api/v1/inventory/stock-transfers")')

with open(path_backend, 'w') as f:
    f.write(content)

# Fix Frontend Service
path_frontend = '/Volumes/Backup/my-team/erp_frontend/src/services/inventory/stock-transfer-service.ts'
with open(path_frontend, 'r') as f:
    content2 = f.read()

content2 = content2.replace('const BASE_PATH = "/api/v1/stock-transfers";', 'const BASE_PATH = "/api/v1/inventory/stock-transfers";')

with open(path_frontend, 'w') as f:
    f.write(content2)

