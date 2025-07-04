package model;

import java.util.Objects;

public class Task {
    private int taskID;
    private final String title;
    private final String description;
    private Status status;


    public Task(String title, String description, Status status) {
        this.title = title;
        this.description = description;
        this.status = status;
    }

    public int getTaskID() {
        return taskID;
    }

    public void setTaskID(int taskID) {
        this.taskID = taskID;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }


    @Override
    public String toString() {
        return String.format("%s{id=%d, title=%s, description=%s, status=%s}",
                this.getClass(),
                taskID,
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
                this.taskID == copy.taskID;
    }

}
