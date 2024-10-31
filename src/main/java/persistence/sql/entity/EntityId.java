package persistence.sql.entity;

import jakarta.persistence.Id;
import java.lang.reflect.Field;
import java.util.Arrays;

class EntityId {
    private final Object entity;

    EntityId(final Object entity) {
        this.entity = entity;
    }

    Long extractId() {
        return Arrays.stream(entity.getClass().getDeclaredFields())
                .filter(field -> field.isAnnotationPresent(Id.class))
                .findFirst()
                .map(this::makeAccessible)
                .map(this::getIdValue)
                .orElseThrow(()-> new RuntimeException("Cannot find id field"));
    }

    private Field makeAccessible(final Field field) {
        field.setAccessible(true);
        return field;
    }

    private Long getIdValue(final Field field) {
        try {
            return (Long) field.get(entity);
        } catch (final IllegalAccessException e) {
            throw new RuntimeException("Cannot access id field", e);
        }
    }
}
