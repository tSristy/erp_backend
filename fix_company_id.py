import glob, re

modules = ['finance', 'hr', 'inventory', 'crm', 'sales', 'production', 'reportengine', 'pos/retail', 'pos/restaurant']

for mod in modules:
    controllers = glob.glob(f'src/main/java/org/enterprise/{mod}/controller/*.java')
    
    for path in controllers:
        with open(path, 'r') as f:
            content = f.read()
            
        # Replace instances of (companyId, q, pageable)
        content = re.sub(r'\(companyId, q, pageable\)', '(org.enterprise.common.util.TenantContext.getCompanyId(), q, pageable)', content)
        
        with open(path, 'w') as f:
            f.write(content)

