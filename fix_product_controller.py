path = 'src/main/java/org/enterprise/inventory/controller/ProductController.java'
with open(path, 'r') as f:
    content = f.read()

content = content.replace('@RequestMapping({"/api/products", "/inventory/products"})', '@RequestMapping({"/api/v1/products", "/api/v1/inventory/products"})')

with open(path, 'w') as f:
    f.write(content)
