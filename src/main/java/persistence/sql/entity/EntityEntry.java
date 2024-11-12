package persistence.sql.entity;

class EntityEntry {
    private Status status;

    EntityEntry(final Status status) {
        this.status = status;
    }

    void updateStatus(final Status status) {
        this.status = status;
    }

    boolean isManaged() {
        return status == Status.MANAGED;
    }
}
