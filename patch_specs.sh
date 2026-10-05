#!/bin/bash
find src/main/java/org/enterprise -name "*Specification.java" -exec sed -i '' '/try {/,/    }/ {
    /predicates.add(cb.like(cb.lower(root.get("id").as(String.class)), likePattern));/ {
        # Delete the try block
        c\
            // Id search removed because PostgreSQL doesn'\''t support lower(bigint)
        # We need to skip the rest of the block (catch)
        N
        N
        N
        d
    }
}' {} +
