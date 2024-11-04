package persistence.sql.entity;

import java.util.Objects;

record CacheKey(String className, Long id) {
    CacheKey {
        Objects.requireNonNull(className, "className must not be null");
        Objects.requireNonNull(id, "id must not be null");
    }
}
