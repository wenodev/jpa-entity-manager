package persistence.sql.entity;

import jakarta.persistence.Id;
import java.lang.reflect.Field;
import java.util.Arrays;

public class IdValue {
    private final Object entity;

    public IdValue(final Object entity) {
        this.entity = entity;
    }

    public Long value() {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .map(this::makeAccessible)
                .map(this::getId)
                .orElseThrow(()-> new RuntimeException("Cannot find id field"));
    }

    private Field makeAccessible(final Field field) {
        field.setAccessible(true);
        return field;
    }

    private Long getId(final Field field) {
        try {
            return (Long) field.get(entity);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Cannot access id field", e);
        }
    }
}
