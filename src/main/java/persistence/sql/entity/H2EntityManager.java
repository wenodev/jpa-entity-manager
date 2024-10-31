package persistence.sql.entity;

import jdbc.JdbcTemplate;
import persistence.sql.dml.DmlQueryBuilder;

public class H2EntityManager implements EntityManager {
    private final JdbcTemplate jdbcTemplate;
    private final DmlQueryBuilder dmlQueryBuilder;

    public H2EntityManager(final JdbcTemplate jdbcTemplate, final DmlQueryBuilder dmlQueryBuilder) {
        this.jdbcTemplate = jdbcTemplate;
        this.dmlQueryBuilder = dmlQueryBuilder;
    }

    @Override
    public void persist(final Object entity) {
        final String insert = dmlQueryBuilder.insert(entity.getClass(), entity);
        jdbcTemplate.execute(insert);
    }

    @Override
    public <T> T find(final Class<T> clazz, final Long id) {
        final String select = dmlQueryBuilder.select(clazz, id);
        return jdbcTemplate.queryForObject(select, new GenericRowMapper<>(clazz));
    }

    @Override
    public void remove(final Object entity) {
        final EntityId entityId = new EntityId(entity);
        final String delete = dmlQueryBuilder.delete(entity.getClass(), entityId.extractId());
        jdbcTemplate.execute(delete);
    }

    @Override
    public void update(final Object entity) {
        final EntityId entityId = new EntityId(entity);
        final String update = dmlQueryBuilder.update(entity.getClass(), entity, entityId.extractId());
        jdbcTemplate.execute(update);
    }
}
