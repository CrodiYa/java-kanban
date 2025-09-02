package model;

import java.time.LocalDateTime;

public class SubTask extends Task {
    private final int epicId;

    public SubTask(String title, String description, Status status, int epicId) {
        super(title, description, status);
        this.epicId = epicId;
    }

    public SubTask(String title,
                   String description,
                   Status status,
                   int epicId,
                   int durationInMinutes,
                   LocalDateTime startTime) {
        super(title, description, status, durationInMinutes, startTime);
        this.epicId = epicId;
    }

    public SubTask(String title, String description, Status status, int epicId, int durationInMinutes) {
        super(title, description, status, durationInMinutes);
        this.epicId = epicId;
    }

    public int getEpicId() {
        return epicId;
    }

    public Type getType() {
        return Type.SUBTASK;
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
