package persistence.sql.entity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class Session implements EntityManager {
    private final EntityPersister entityPersister;
    private final EntityLoader entityLoader;
    private final PersistenceContext persistenceContext;

    public Session(final EntityPersister entityPersister, final EntityLoader entityLoader, final PersistenceContext persistenceContext) {
        this.entityPersister = entityPersister;
        this.entityLoader = entityLoader;
        this.persistenceContext = persistenceContext;
    }

    @Override
    public void persist(final Object entity) {
        entityPersister.insert(entity);
        final EntityId entityId = new EntityId(entity);
        final Long id = entityId.extractId();
        persistenceContext.put(entity.getClass(), id, entity);
    }

    public <T> T find(final Class<T> clazz, final Long id) {
        final Optional<Object> cachedEntity = persistenceContext.find(clazz, id);
        if (cachedEntity.isPresent()) {
            return clazz.cast(cachedEntity.get());
        }

        final T loadedEntity = entityLoader.select(clazz, id);
        persistenceContext.put(clazz, id, loadedEntity);
        return loadedEntity;
    }

    @Override
    public void remove(final Object entity) {
        entityPersister.delete(entity);
        final EntityId entityId = new EntityId(entity);
        final Long id = entityId.extractId();
        persistenceContext.remove(entity.getClass(), id);
    }

    public void flush() {
        final Map<CacheKey, Object> dirtyEntities = persistenceContext.getDirtyEntities();
        for (final Map.Entry<CacheKey, Object> entry : dirtyEntities.entrySet()) {
            final Object entity = entry.getValue();
            final EntityId entityId = new EntityId(entity);
            final Long id = entityId.extractId();
            final CacheEntry cacheEntry = persistenceContext.find(entity.getClass(), id)
                    .map(e -> (CacheEntry) e)
                    .orElseThrow(() -> new IllegalStateException("Entity not found in persistence context"));
            final Map<String, Object> snapshot = cacheEntry.getSnapshot();
            final Map<String, Object> currentValues = cacheEntry.captureFieldValues(entity);
            final Map<String, Object> diff = getDifference(snapshot, currentValues);
            if (!diff.isEmpty()) {
                entityPersister.update(entity);
            }
        }
        persistenceContext.clearDirtyEntities();
    }

    private Map<String, Object> getDifference(final Map<String, Object> snapshot, final Map<String, Object> currentValues) {
        final Map<String, Object> diff = new HashMap<>();
        for (final String fieldName : snapshot.keySet()) {
            final Object snapshotValue = snapshot.get(fieldName);
            final Object currentValue = currentValues.get(fieldName);
            if (!snapshotValue.equals(currentValue)) {
                diff.put(fieldName, currentValue);
            }
        }
        return diff;
    }

    public <T> T merge(final T entity) {
        final EntityId entityId = new EntityId(entity);
        final Long id = entityId.extractId();
        final Class<?> entityClass = entity.getClass();

        if (isPersisted(entityClass, id)) {
            update(entity, entityClass, id);
            return entity;
        }
        insertAndUpdate(entity, entityClass, id);
        return entity;
    }

    private <T> void insertAndUpdate(final T entity, final Class<?> entityClass, final Long id) {
        entityPersister.insert(entity);
        persistenceContext.put(entityClass, id, entity);
    }

    private <T> void update(final T entity, final Class<?> entityClass, final Long id) {
        persistenceContext.put(entityClass, id, entity);
    }

    private boolean isPersisted(final Class<?> entityClass, final Long id) {
        return persistenceContext.find(entityClass, id).isPresent();
    }
}
