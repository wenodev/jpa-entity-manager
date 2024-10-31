package persistence.sql.entity;

import jakarta.persistence.Id;
import java.lang.reflect.Field;

public class IdValue {
    private final Object entity;

    public IdValue(final Object entity) {
        this.entity = entity;
    }

    public Long value() {
        final Field[] fields = entity.getClass().getDeclaredFields();
        for (final Field field : fields) {
            if (field.isAnnotationPresent(Id.class)) {
                field.setAccessible(true);
                try {
                    return (Long) field.get(entity);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("Cannot access id field", e);
                }
            }
        }
        throw new RuntimeException("Cannot find id field");
    }
}
