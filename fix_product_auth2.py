path = 'src/main/java/org/enterprise/inventory/controller/ProductController.java'
with open(path, 'r') as f:
    content = f.read()

content = content.replace('@GetMapping("/types")\n    public ProductType[]', '@GetMapping("/types")\n    @PreAuthorize("hasAuthority(\'INVENTORY_READ\') or hasAuthority(\'INVENTORY_WRITE\')")\n    public ProductType[]')
content = content.replace('@GetMapping("/costing-methods")\n    public CostingMethod[]', '@GetMapping("/costing-methods")\n    @PreAuthorize("hasAuthority(\'INVENTORY_READ\') or hasAuthority(\'INVENTORY_WRITE\')")\n    public CostingMethod[]')

with open(path, 'w') as f:
    f.write(content)
