package model;

public class SubTask extends Task {
    private final int epicID;

    public SubTask(String title, String description, Status status, int epicID) {
        super(title, description, status);
        this.epicID = epicID;
    }

    public int getEpicID() {
        return epicID;
    }

    @Override
    public String toString() {
        return String.format("%s{id=%d, title=%s, description=%s, status=%s, epicID=%d}",
                this.getClass(),
                this.taskID,
                this.title,
                this.description,
                this.status,
                this.epicID
        );
    }
}
