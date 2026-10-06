path = 'src/main/java/org/enterprise/finance/dto/StatementSetupDTO.java'
with open(path, 'r') as f:
    content = f.read()

content = content.replace('private java.util.List<StatementSetupAccountDTO> accounts;', 'private Boolean bold;\n    private Boolean visible;\n    private Boolean bottomLine;\n    private java.util.List<StatementSetupAccountDTO> accounts;')

with open(path, 'w') as f:
    f.write(content)
