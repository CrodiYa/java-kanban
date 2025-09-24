package model;

import util.enums.Status;
import util.enums.Type;

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
                   long durationInMinutes,
                   LocalDateTime startTime) {
        super(title, description, status, durationInMinutes, startTime);
        this.epicId = epicId;
    }

    public SubTask(String title, String description, Status status, int epicId, long durationInMinutes) {
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
        return String.format("%s{id=%d, title=%s, description=%s, status=%s, epicId=%d,\n    startTime=[%s], duration=[%s], endTime=[%s]}\n",
                this.getClass().getName(),
                this.taskId,
                this.title,
                this.description,
                this.status,
                this.epicId,
                formatDateTime(startTime),
                duration,
                formatDateTime(getEndTime())
        );
    }
}
