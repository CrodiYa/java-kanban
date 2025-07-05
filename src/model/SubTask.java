package model;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(String title, String description, Status status, int epicId) {
        super(title, description, status);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    @Override
    public String toString() {
        return String.format("%s{id=%d, title=%s, description=%s, status=%s, epicId=%d}",
                this.getClass(),
                this.taskId,
                this.title,
                this.description,
                this.status,
                this.epicId
        );
    }
}
