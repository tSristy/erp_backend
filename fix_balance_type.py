path = 'src/main/java/org/enterprise/finance/enums/BalanceType.java'
with open(path, 'r') as f:
    content = f.read()

content = content.replace('CREDIT', 'CREDIT,\n    NET')

with open(path, 'w') as f:
    f.write(content)
