package persistence.sql.entity;

import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

class CacheEntry {
    private final Map<String, Object> snapshot;
    private final Object entity;
    private boolean dirty;

    CacheEntry(final Object entity) {
        this.snapshot = captureFieldValues(entity);
        this.entity = entity;
        this.dirty = false;
    }

    Map<String, Object> captureFieldValues(final Object entity) {
        final Map<String, Object> values = new HashMap<>();
        for (final Field field : entity.getClass().getDeclaredFields()) {
            if (isPersistentField(field)) {
                field.setAccessible(true);
                put(entity, field, values);
            }
        }
        return values;
    }

    private boolean isPersistentField(final Field field) {
        return !field.isAnnotationPresent(Id.class) &&
               !field.isAnnotationPresent(Transient.class);
    }

    private void put(final Object entity, final Field field, final Map<String, Object> values) {
        try {
            values.put(field.getName(), field.get(entity));
        } catch (final IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    boolean isDirty() {
        return dirty;
    }

    Object getEntity() {
        return entity;
    }

    void clearDirty() {
        this.dirty = false;
    }

    Map<String, Object> getSnapshot() {
        return snapshot;
    }
}
