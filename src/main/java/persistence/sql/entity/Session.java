package persistence.sql.entity;

import jakarta.persistence.PersistenceException;
import java.util.Map;
import java.util.Optional;

public class Session implements EntityManager {
    private final EntityPersister entityPersister;
    private final EntityLoader entityLoader;
    private final PersistenceContext persistenceContext;

    public Session(final EntityPersister entityPersister,
                   final EntityLoader entityLoader,
                   final PersistenceContext persistenceContext) {
        this.entityPersister = entityPersister;
        this.entityLoader = entityLoader;
        this.persistenceContext = persistenceContext;
    }

    @Override
    public void persist(final Object entity) {
        final CacheKey key = createKey(entity);

        if (persistenceContext.contains(key)) {
            return;
        }

        try {
            final Object primaryKey = entityLoader.getMaxId(entity.getClass());
            persistenceContext.prePersist(entity, primaryKey);
            entityPersister.insert(entity);
            persistenceContext.postPersist(entity, primaryKey);
        } catch (final RuntimeException e) {
            persistenceContext.handlePersistError(key);
            throw new PersistenceException("Entity persist failed", e);
        }
    }

    @Override
    public void remove(final Object entity) {
        final CacheKey key = createKey(entity);

        if (!persistenceContext.containsEntity(entity.getClass(), extractId(entity))) {
            throw new IllegalArgumentException("Entity must be managed to be removed");
        }

        persistenceContext.removeEntity(key);
        entityPersister.delete(entity);
    }

    public <T> T find(final Class<T> entityClass, final Long id) {
        final CacheKey key = new CacheKey(entityClass.getSimpleName(), id);

        final Optional<Object> cachedEntity = persistenceContext.getEntity(key);
        if (cachedEntity.isPresent()) {
            return entityClass.cast(cachedEntity.get());
        }

        final EntityEntry entityEntry = persistenceContext.preLoad(entityClass, id);
        final T loadedEntity = entityLoader.select(entityClass, id);
        persistenceContext.postLoad(loadedEntity, id, entityEntry);
        return loadedEntity;
    }

    public void flush() {
        final Map<CacheKey, Object> dirtyEntities = persistenceContext.getDirtyEntities();

        for (final Map.Entry<CacheKey, Object> entry : dirtyEntities.entrySet()) {
            final Object entity = entry.getValue();
            try {
                entityPersister.update(entity);
            } catch (final Exception e) {
                throw new IllegalStateException("Failed to flush", e);
            }
        }
    }

    public <T> T merge(final T entity) {
        final CacheKey key = createKey(entity);
        final Long id = extractId(entity);

        if (persistenceContext.containsEntity(entity.getClass(), id)) {
            try {
                entityPersister.update(entity);
                persistenceContext.managedEntity(key, entity);
                return entity;
            } catch (final Exception e) {
                throw new IllegalStateException("Failed to merge managed entity: " + key.className(), e);
            }
        }
        try {
            entityPersister.insert(entity);
            persistenceContext.managedEntity(key, entity);
            return entity;
        } catch (final Exception e) {
            throw new IllegalStateException("Failed to merge new entity: " + key.className(), e);
        }
    }

    private CacheKey createKey(final Object entity) {
        return new CacheKey(
                entity.getClass().getSimpleName(),
                extractId(entity)
        );
    }

    private Long extractId(final Object entity) {
        return new EntityId(entity).extractId();
    }
}
