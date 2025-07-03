import java.util.Objects;

public class Task {
    private int task_id;
    private final String task;
    private final String description;
    private Status status;


    public Task(String task, String description, Status status) {
        this.task = task;
        this.description = description;
        this.status = status;
    }

    public int get_task_id() {
        return task_id;
    }

    public void set_task_id(int task_id) {
        this.task_id = task_id;
    }

    public String getTask() {
        return task;
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

        String result = String.format("%s{id=%d, task=%s, description=%s, status=%s",
                this.getClass(),
                task_id,
                task,
                description,
                status
        );

        if (this instanceof Epic) result += ", subtasks=" + ((Epic) this).getSubtasksString();

        if (this instanceof SubTask) result += ", epic_parent_id=" + ((SubTask) this).get_epic_parent_id();

        return result + "}";
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || this.getClass() != obj.getClass()) return false;
        Task copy = (Task) obj;
        return Objects.equals(this.task, copy.task) &&
                Objects.equals(this.description, copy.description) &&
                this.status == copy.status &&
                this.task_id == copy.task_id;
    }

}
