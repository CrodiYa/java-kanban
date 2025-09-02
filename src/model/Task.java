package model;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

public class Task {
    protected int taskId;
    protected String title;
    protected String description;
    protected Status status;
    protected Duration duration;
    protected LocalDateTime startTime;

    public Task(String title, String description, Status status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public Task(String title, String description, Status status, int durationInMinutes, LocalDateTime startTime) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.duration = Duration.ofMinutes(durationInMinutes);
        this.startTime = startTime;
    }

    public Task(String title, String description, Status status, int durationInMinutes) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.duration = Duration.ofMinutes(durationInMinutes);
    }

    public int getTaskId() {
        return taskId;
    }

    public void setTaskId(int taskId) {
        this.taskId = taskId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public Duration getDuration() {
        return duration;
    }

    public void setDuration(Duration duration) {
        this.duration = duration;
    }

    public LocalDateTime getEndTime() {
        if (startTime == null || duration == null) {
            return null;
        }
        return startTime.plus(duration);
    }

    public Type getType() {
        return Type.TASK;
    }

    @Override
    public String toString() {
        return String.format("%s{id=%d, title=%s, description=%s, status=%s}",
                this.getClass(),
                taskId,
                title,
                description,
                status
        );
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        Task copy = (Task) obj;
        return Objects.equals(this.title, copy.title) &&
                Objects.equals(this.description, copy.description) &&
                this.status == copy.status &&
                this.taskId == copy.taskId;
    }

}
