package persistence.sql.entity;

import jakarta.persistence.Entity;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

class PersistenceContext {
    private final Map<String, Map<Long, Object>> firstLevelCache = new HashMap<>();

    Optional<Object> find(final Class<?> clazz, final Long id) {
        validateEntity(clazz);
        if (id == null) {
            return Optional.empty();
        }
        final Map<Long, Object> entityMap = firstLevelCache.get(clazz.getSimpleName());
        return Optional.ofNullable(entityMap != null ? entityMap.get(id) : null);
    }

    void put(final Class<?> clazz, final Long id, final Object entity) {
        validateEntity(clazz);
        if (id == null) {
            return;
        }

        firstLevelCache.computeIfAbsent(clazz.getSimpleName(), k -> new HashMap<>())
                .put(id, entity);
    }

    void remove(final Class<?> clazz, final Long id) {
        validateEntity(clazz);
        if (id == null) {
            return;
        }

        final Map<Long, Object> entityMap = firstLevelCache.get(clazz.getSimpleName());
        if (entityMap != null) {
            entityMap.remove(id);
        }
    }

    private void validateEntity(final Class<?> entityClass) {
        if (!entityClass.isAnnotationPresent(Entity.class)) {
            throw new IllegalArgumentException("Class must be annotated with @Entity");
        }
    }
}
