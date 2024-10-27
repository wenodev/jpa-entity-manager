package persistence.sql.entity;

import jdbc.JdbcTemplate;
import persistence.sql.dml.DmlQueryBuilder;

public class EntityPersister {
    private final JdbcTemplate jdbcTemplate;
    private final DmlQueryBuilder dmlQueryBuilder;

    public EntityPersister(final JdbcTemplate jdbcTemplate, final DmlQueryBuilder dmlQueryBuilder) {
        this.jdbcTemplate = jdbcTemplate;
        this.dmlQueryBuilder = dmlQueryBuilder;
    }

    public void insert(final Object entity) {
        final String insert = dmlQueryBuilder.insert(entity.getClass(), entity);
        jdbcTemplate.execute(insert);
    }

    public void update(Object entity) {
        final IdValue idValue = new IdValue(entity);
        final String update = dmlQueryBuilder.update(entity.getClass(), entity, idValue.value());
        jdbcTemplate.execute(update);
    }

    public void delete(Object entity) {
        final IdValue idValue = new IdValue(entity);
        final String delete = dmlQueryBuilder.delete(entity.getClass(), idValue.value());
        jdbcTemplate.execute(delete);
    }

    public <T> T select(Class<T> clazz, Long id) {
        final String select = dmlQueryBuilder.select(clazz, id);
        return jdbcTemplate.queryForObject(select, new GenericRowMapper<>(clazz));
    }
}
