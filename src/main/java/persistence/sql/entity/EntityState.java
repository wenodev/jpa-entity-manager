package persistence.sql.entity;

class EntityState {
    private final CacheEntry cacheEntry;
    private Status status;

    EntityState(final Object entity, final Status status) {
        this.cacheEntry = new CacheEntry(entity);
        this.status = status;
    }

    Object getEntity() {
        return cacheEntry.getEntity();
    }

    boolean isDirty() {
        return status == Status.MANAGED && cacheEntry.isDirty();
    }

    void updateStatus(final Status newStatus) {
        this.status = newStatus;
    }

    Status getStatus() {
        return status;
    }
}
