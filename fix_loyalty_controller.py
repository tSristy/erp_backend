import re
path = 'src/main/java/org/enterprise/crm/controller/LoyaltyController.java'
with open(path, 'r') as f:
    content = f.read()

content = content.replace('@GetMapping("/profiles")', '@GetMapping')
content = content.replace('@org.springframework.web.bind.annotation.PostMapping("/profiles")', '@org.springframework.web.bind.annotation.PostMapping')
content = content.replace('@org.springframework.web.bind.annotation.PutMapping("/profiles/{id}")', '@org.springframework.web.bind.annotation.PutMapping("/{id}")')
content = content.replace('@org.springframework.web.bind.annotation.DeleteMapping("/profiles/{id}")', '@org.springframework.web.bind.annotation.DeleteMapping("/{id}")')

with open(path, 'w') as f:
    f.write(content)
