package persistence.sql.entity;

import jdbc.RowMapper;

import jakarta.persistence.Column;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

class GenericRowMapper<T> implements RowMapper<T> {
    private final Class<T> clazz;
    private final Map<String, Field> fieldsMap;
    private final Constructor<T> constructor;

    GenericRowMapper(final Class<T> clazz) {
        this.clazz = clazz;
        this.constructor = initializeConstructor(clazz);
        this.fieldsMap = initializeFieldsMap(clazz);
    }

    private Map initializeFieldsMap(final Class<T> clazz) {
        final Map<String, Field> fieldsMap = new HashMap<>();
        for (final Field field : clazz.getDeclaredFields()) {
            field.setAccessible(true);
            final Column column = field.getAnnotation(Column.class);
            final String columnName = column != null ? column.name() : field.getName();
            fieldsMap.put(columnName.toLowerCase(), field);
            fieldsMap.put(field.getName().toLowerCase(), field);
        }
        return fieldsMap;
    }

    private Constructor<T> initializeConstructor(final Class<T> clazz) {
        try {
            final Constructor<T> declaredConstructor = clazz.getDeclaredConstructor();
            declaredConstructor.setAccessible(true);
            return declaredConstructor;
        } catch (final NoSuchMethodException e) {
            throw new IllegalArgumentException("""
                    Class %s must have a no-args constructor
                    """.formatted(clazz.getName()), e);
        }
    }

    @Override
    public T mapRow(final ResultSet resultSet) throws SQLException {
        final ResultSetMetaData metaData = resultSet.getMetaData();
        final T instance = createInstance();
        mapAllColumns(resultSet, metaData, instance);
        return instance;
    }

    private void mapAllColumns(final ResultSet resultSet, final ResultSetMetaData metaData, final T instance) throws SQLException {
        for (int i = metaData.getColumnCount(); i > 0; i--) {
            final String columnName = metaData.getColumnLabel(i).toLowerCase();
            final Field field = fieldsMap.get(columnName);
            mapColumnToField(resultSet, instance, field, i);
        }
    }

    private void mapColumnToField(final ResultSet resultSet, final T instance, final Field field, final int i) throws SQLException {
        if (field != null) {
            final Object value = getValueByFieldType(resultSet, i, field.getType());
            setFieldValue(field, instance, value);
        }
    }

    private T createInstance() {
        try {
            return constructor.newInstance();
        } catch (final Exception e) {
            throw new RuntimeException("Cannot create instance of " + clazz.getName(), e);
        }
    }

    private void setFieldValue(final Field field, final T instance, final Object value) {
        try {
            field.set(instance, value);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Cannot set value to field " + field.getName(), e);
        }
    }

    private Object getValueByFieldType(final ResultSet resultSet, final int index, final Class<?> fieldType) throws SQLException {
        if (resultSet.getObject(index) == null) {
            return null;
        }

        if (fieldType.equals(String.class)) {
            return resultSet.getString(index);
        } else if (fieldType.equals(Long.class) || fieldType.equals(long.class)) {
            return resultSet.getLong(index);
        } else if (fieldType.equals(Integer.class) || fieldType.equals(int.class)) {
            return resultSet.getInt(index);
        } else if (fieldType.equals(Double.class) || fieldType.equals(double.class)) {
            return resultSet.getDouble(index);
        } else if (fieldType.equals(Boolean.class) || fieldType.equals(boolean.class)) {
            return resultSet.getBoolean(index);
        } else if (fieldType.equals(LocalDateTime.class)) {
            final Timestamp timestamp = resultSet.getTimestamp(index);
            return timestamp != null ? timestamp.toLocalDateTime() : null;
        } else if (fieldType.equals(Timestamp.class)) {
            return resultSet.getTimestamp(index);
        }
        return resultSet.getObject(index);
    }
}
