package persistence.sql.entity;

public class Session implements EntityManager {
    private final EntityPersister entityPersister;
    private final EntityLoader entityLoader;

    public Session(final EntityPersister entityPersister, final EntityLoader entityLoader) {
        this.entityPersister = entityPersister;
        this.entityLoader = entityLoader;
    }

    @Override
    public void persist(final Object entity){
        entityPersister.insert(entity);
    }

    @Override
    public <T> T find(final Class<T> clazz, final Long id) {
        return entityLoader.select(clazz, id);
    }

    @Override
    public void remove(final Object entity) {
        entityPersister.delete(entity);
    }

    @Override
    public void update(final Object entity) {
        entityPersister.update(entity);
    }
}
