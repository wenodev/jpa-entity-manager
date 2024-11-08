package persistence.sql.entity;

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

    @Override
    public void update(final Object entity) {
        entityPersister.update(entity);
        final EntityId entityId = new EntityId(entity);
        final Long id = entityId.extractId();
        persistenceContext.put(entity.getClass(), id, entity);
    }

    public void flush() {
        final Map<CacheKey, Object> dirtyEntities = persistenceContext.getDirtyEntities();
        for (final Map.Entry<CacheKey, Object> entry : dirtyEntities.entrySet()) {
            final Object entity = entry.getValue();
            entityPersister.update(entity);
        }
        persistenceContext.clearDirtyEntities();
    }
}
