package persistence.sql.entity;

import jakarta.persistence.Entity;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class PersistenceContext {
    private final Map<CacheKey, Object> firstLevelCache = new HashMap<>();

    Optional<Object> find(final Class<?> entityClass, final Long id) {
        return Optional.ofNullable(id)
                .map(validId -> getCacheKey(entityClass, validId))
                .map(firstLevelCache::get);
    }

    void put(final Class<?> entityClass, final Long id, final Object entity) {
        Optional.ofNullable(id)
                .map(validId -> getCacheKey(entityClass, validId))
                .ifPresent(key -> firstLevelCache.put(key, entity));
    }

    void remove(final Class<?> entityClass, final Long id) {
        Optional.ofNullable(id)
                .map(validId -> getCacheKey(entityClass, validId))
                .ifPresent(firstLevelCache::remove);
    }

    private CacheKey getCacheKey(final Class<?> entityClass, final Long id) {
        validateEntity(entityClass);
        return new CacheKey(entityClass.getSimpleName(), id);
    }

    private void validateEntity(final Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(Entity.class))
            throw new IllegalArgumentException(
                    String.format("Class %s must be annotated with @Entity", entityClass.getSimpleName())
            );
    }
}
