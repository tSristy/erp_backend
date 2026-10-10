import os
import re

entity_path = 'src/main/java/org/enterprise/production/entity/RoutingOperation.java'
dto_path = 'src/main/java/org/enterprise/production/dto/RoutingOperationDto.java'

with open(entity_path, 'r') as f:
    content = f.read()
content = content.replace('private String operationName;', '@Enumerated(EnumType.STRING)\n    private org.enterprise.production.enums.OperationType operationName;')
with open(entity_path, 'w') as f:
    f.write(content)

with open(dto_path, 'r') as f:
    content = f.read()
content = content.replace('private String operationName;', 'private org.enterprise.production.enums.OperationType operationName;')
with open(dto_path, 'w') as f:
    f.write(content)
