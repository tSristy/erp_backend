import re

file_path = "/Volumes/Backup/my-team/erp_backend/src/main/java/org/enterprise/inventory/entity/Product.java"

with open(file_path, "r") as f:
    content = f.read()

def replacement(match):
    prop_name = match.group(1)
    prop_type = match.group(2)
    back_ref = match.group(3)
    
    return f"""    public void set{prop_name.capitalize()}(java.util.List<{prop_type}> {prop_name}) {{
        if ({prop_name} != null) {{
            this.{prop_name}.removeIf(existing -> {prop_name}.stream().noneMatch(incoming -> incoming.getId() != null && incoming.getId().equals(existing.getId())));
            for ({prop_type} item : {prop_name}) {{
                if (item.getId() == null) {{
                    item.{back_ref}(this);
                    this.{prop_name}.add(item);
                }} else {{
                    {prop_type} existing = this.{prop_name}.stream().filter(e -> e.getId().equals(item.getId())).findFirst().orElse(null);
                    if (existing == null) {{
                        item.{back_ref}(this);
                        this.{prop_name}.add(item);
                    }} else {{
                        org.springframework.beans.BeanUtils.copyProperties(item, existing, "id", "{back_ref.replace('set', '').lower()}");
                    }}
                }}
            }}
        }} else {{
            this.{prop_name}.clear();
        }}
    }}"""

# Regex to match the setter pattern
pattern = re.compile(r'    public void set([A-Z][a-zA-Z]+)\(java\.util\.List<([a-zA-Z]+)> \1\) \{\s+if \(\1 != null\) \{\s+this\.\1\.clear\(\);\s+this\.\1\.addAll\(\1\);\s+for \(\2 p : this\.\1\) \{\s+p\.(set[A-Z][a-zA-Z]+)\(this\);\s+\}\s+\} else \{\s+this\.\1\.clear\(\);\s+\}\s+\}', re.MULTILINE)

# We need to manually fix them because the regex might be brittle.
# Actually I'll just write a script that replaces the whole block of setters.

