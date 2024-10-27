package persistence.sql.entity;

public class Session implements EntityManager {
    private final EntityPersister entityPersister;

    public Session(final EntityPersister entityPersister) {
        this.entityPersister = entityPersister;
    }

    @Override
    public void persist(final Object entity){
        entityPersister.insert(entity);
    }

    @Override
    public <T> T find(final Class<T> clazz, final Long id) {
        return entityPersister.select(clazz, id);
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
