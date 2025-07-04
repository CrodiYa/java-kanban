package model;

public class SubTask extends Task {
    private final int epic_parent_id;

    public SubTask(String subTask, String description, int epic_id, Status status) {
        super(subTask, description, status);
        this.epic_parent_id = epic_id;
    }

    public int get_epic_parent_id() {
        return epic_parent_id;
    }
}
