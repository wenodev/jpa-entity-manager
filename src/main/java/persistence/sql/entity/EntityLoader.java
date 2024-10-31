package persistence.sql.entity;

import jdbc.JdbcTemplate;
import persistence.sql.dml.DmlQueryBuilder;

class EntityLoader {
    private final JdbcTemplate jdbcTemplate;
    private final DmlQueryBuilder dmlQueryBuilder;

    public EntityLoader(final JdbcTemplate jdbcTemplate, final DmlQueryBuilder dmlQueryBuilder) {
        this.jdbcTemplate = jdbcTemplate;
        this.dmlQueryBuilder = dmlQueryBuilder;
    }

    <T> T select(Class<T> clazz, Long id) {
        final String select = dmlQueryBuilder.select(clazz, id);
        return jdbcTemplate.queryForObject(select, new GenericRowMapper<>(clazz));
    }
}
