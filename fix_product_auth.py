path = 'src/main/java/org/enterprise/inventory/controller/ProductController.java'
with open(path, 'r') as f:
    content = f.read()

# Add import
if 'org.springframework.security.access.prepost.PreAuthorize' not in content:
    content = content.replace('import org.springframework.web.bind.annotation.*;', 'import org.springframework.web.bind.annotation.*;\nimport org.springframework.security.access.prepost.PreAuthorize;')

# Add to search (if missing)
content = content.replace('@org.springframework.web.bind.annotation.GetMapping("/search")\n    public Page<Product> search', '@org.springframework.web.bind.annotation.GetMapping("/search")\n    @PreAuthorize("hasAuthority(\'INVENTORY_READ\') or hasAuthority(\'INVENTORY_WRITE\')")\n    public Page<Product> search')

# Add to getAll
content = content.replace('@GetMapping\n    public List<Product> getAll()', '@GetMapping\n    @PreAuthorize("hasAuthority(\'INVENTORY_READ\') or hasAuthority(\'INVENTORY_WRITE\')")\n    public List<Product> getAll()')

# Add to getById
content = content.replace('@GetMapping("/{id}")\n    public Product getById', '@GetMapping("/{id}")\n    @PreAuthorize("hasAuthority(\'INVENTORY_READ\') or hasAuthority(\'INVENTORY_WRITE\')")\n    public Product getById')

# Add to create
content = content.replace('@PostMapping\n    public Product create', '@PostMapping\n    @PreAuthorize("hasAuthority(\'INVENTORY_WRITE\')")\n    public Product create')

# Add to update
content = content.replace('@PutMapping("/{id}")\n    public Product update', '@PutMapping("/{id}")\n    @PreAuthorize("hasAuthority(\'INVENTORY_WRITE\')")\n    public Product update')

# Add to delete
content = content.replace('@DeleteMapping("/{id}")\n    public void delete', '@DeleteMapping("/{id}")\n    @PreAuthorize("hasAuthority(\'INVENTORY_WRITE\')")\n    public void delete')

# Add to types
content = content.replace('@GetMapping("/types")\n    public List', '@GetMapping("/types")\n    @PreAuthorize("hasAuthority(\'INVENTORY_READ\') or hasAuthority(\'INVENTORY_WRITE\')")\n    public List')

# Add to costing-methods
content = content.replace('@GetMapping("/costing-methods")\n    public List', '@GetMapping("/costing-methods")\n    @PreAuthorize("hasAuthority(\'INVENTORY_READ\') or hasAuthority(\'INVENTORY_WRITE\')")\n    public List')

with open(path, 'w') as f:
    f.write(content)
