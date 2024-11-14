package persistence.sql.entity;

import jakarta.persistence.Entity;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collector;
import java.util.stream.Collectors;

class PersistenceContext {
    private final Map<CacheKey, CacheEntry> entityCache = new HashMap<>();
    private final Map<CacheKey, EntityEntry> entityEntries = new HashMap<>();

    Optional<Object> getEntity(final CacheKey key) {
        final EntityEntry entityEntry = entityEntries.get(key);
        entityEntry.updateStatus(Status.LOADING);
        final CacheEntry value = entityCache.get(key);
        return Optional.ofNullable(value)
                .map(CacheEntry::getEntity);
    }

    void addEntity(final CacheKey key, final Object entity) {
        final CacheEntry cacheEntry = new CacheEntry(entity);
        entityCache.put(key, cacheEntry);
        entityEntries.put(key, new EntityEntry(Status.MANAGED));
    }

    void managedEntity(final CacheKey key, final Object entity) {
        final CacheEntry cacheEntry = new CacheEntry(entity);
        entityCache.put(key, cacheEntry);
        entityEntries.put(key, new EntityEntry(Status.MANAGED));
    }

    void removeEntity(final CacheKey key) {
        if (entityEntries.containsKey(key)) {
            final EntityEntry entry = entityEntries.get(key);
            entry.updateStatus(Status.DELETED);
            entityCache.remove(key);
        }
    }

    boolean containsEntity(final Class<?> entityClass, final Long id) {
        final CacheKey key = createCacheKey(entityClass, id);
        return entityCache.containsKey(key);
    }

    Map<CacheKey, Object> getDirtyEntities() {
        return entityCache.entrySet().stream()
                .filter(this::isDirtyAndManagedEntity)
                .collect(toCacheKeyObjectMap());
    }

    EntityEntry preLoad(final Class<?> entity, final Object primaryKey) {
        final EntityEntry entry = new EntityEntry(Status.LOADING);
        final CacheKey cacheKey = createCacheKey(entity, (Long) primaryKey);
        entityEntries.put(cacheKey, entry);
        entityCache.put(cacheKey, new CacheEntry(entity));
        return entry;
    }

    void postLoad(final Object entity, final Long id, final EntityEntry entityEntry) {
        entityEntry.updateStatus(Status.MANAGED);
        final CacheKey cacheKey = createCacheKey(entity.getClass(), id);
        entityEntries.put(cacheKey, entityEntry);
        entityCache.put(cacheKey, new CacheEntry(entity));
    }

    private Collector<Map.Entry<CacheKey, CacheEntry>, ?, Map<CacheKey, Object>> toCacheKeyObjectMap() {
        return Collectors.toMap(
                Map.Entry::getKey,
                entry -> entry.getValue().getEntity());
    }

    private boolean isDirtyAndManagedEntity(final Map.Entry<CacheKey, CacheEntry> entry) {
        final EntityEntry entityEntry = entityEntries.get(entry.getKey());
        return entityEntry.isDirtyAndManagedEntity(entry.getValue());
    }

    private CacheKey createCacheKey(final Class<?> entityClass, final Long id) {
        validateEntity(entityClass);
        return new CacheKey(entityClass.getSimpleName(), id);
    }

    private void validateEntity(final Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Class must be annotated with @Entity");
        }
    }
}
