package persistence.sql.entity;

import jakarta.persistence.Entity;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

class PersistenceContext {
    private final Map<CacheKey, CacheEntry> entityCache = new HashMap<>();

    Optional<Object> find(final Class<?> entityClass, final Long id) {
        return Optional.ofNullable(id)
                .map(validId -> getCacheKey(entityClass, validId))
                .map(entityCache::get)
                .map(CacheEntry::getEntity);
    }

    void put(final Class<?> entityClass, final Long id, final Object entity) {
        Optional.ofNullable(id)
                .map(validId -> getCacheKey(entityClass, validId))
                .ifPresent(key -> {
                    entityCache.put(key, new CacheEntry(entity));
                });
    }

    void remove(final Class<?> entityClass, final Long id) {
        Optional.ofNullable(id)
                .map(validId -> getCacheKey(entityClass, validId))
                .ifPresent(entityCache::remove);
    }

    Map<CacheKey, Object> getDirtyEntities() {
        return entityCache.entrySet().stream()
                .filter(entry -> entry.getValue().isDirty())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> entry.getValue().getEntity()
                ));
    }

    void clearDirtyEntities() {
        entityCache.values().forEach(CacheEntry::clearDirty);
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
