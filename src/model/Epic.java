package model;

import java.util.ArrayList;

public class Epic extends Task {

    private final ArrayList<Integer> subtaskIds;

    public Epic(String title, String description, Status status) {
        super(title, description, status);
        subtaskIds = new ArrayList<>();
    }

    public void addSubTask(SubTask subTask) {
        subtaskIds.add(subTask.getTaskID());
    }


    public void deleteSubTask(int id) {
        subtaskIds.remove(Integer.valueOf(id));
    }

    public void clearSubtasks() {
        subtaskIds.clear();
        this.setStatus(Status.NEW);
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    @Override
    public String toString() {
        return String.format("%s{id=%d, title=%s, description=%s, status=%s, subtasks=%s}",
                this.getClass(),
                this.getTaskID(),
                this.getTitle(),
                this.getDescription(),
                this.getStatus(),
                this.getSubtaskIds()
        );
    }

}
