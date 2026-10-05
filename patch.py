import re

with open("src/main/java/org/enterprise/inventory/mapper/InventoryMapperImpl.java", "r") as f:
    content = f.read()

# Remove setShortName, setType, setCapacity, setOperatingHours, setManagerId, setCompanyId, setBranchId, setCity, setCountry, setZipCode, setPhone, setEmail, setActive from Warehouse and Location
props_to_remove = ["setShortName", "setType", "setCapacity", "setOperatingHours", "setManagerId", "setCompanyId", "setBranchId", "setCity", "setCountry", "setZipCode", "setPhone", "setEmail", "setActive", "setPickable", "setReceivable", "setDispatchable", "setQuarantine", "setDamageLocation", "setReturnLocation", "setSequenceNo", "setMaxWeight", "setMaxVolume", "setCurrentCapacity", "setRemarks"]

lines = content.split('\n')
new_lines = []
for line in lines:
    if not any(prop in line for prop in props_to_remove):
        new_lines.append(line)

with open("src/main/java/org/enterprise/inventory/mapper/InventoryMapperImpl.java", "w") as f:
    f.write('\n'.join(new_lines))
