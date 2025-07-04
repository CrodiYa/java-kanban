package model;

import java.util.ArrayList;

public class Epic extends Task {

    private final ArrayList<Integer> subtasksIDs;

    public Epic(String title, String description, Status status) {
        super(title, description, status);
        subtasksIDs = new ArrayList<>();
    }

    public void addSubTask(SubTask subTask) {
        subtasksIDs.add(subTask.getTaskID());
    }


    public void deleteSubTask(int id) {
        subtasksIDs.remove(Integer.valueOf(id));
    }

    public void clearSubtasks() {
        subtasksIDs.clear();
        this.setStatus(Status.NEW);
    }

    public ArrayList<Integer> getSubtasksIDs(){
        return subtasksIDs;
    }

    @Override
    public String toString() {
        return String.format("%s{id=%d, title=%s, description=%s, status=%s, subtasks=%s}",
                this.getClass(),
                this.getTaskID(),
                this.getTitle(),
                this.getDescription(),
                this.getStatus(),
                this.getSubtasksIDs()
        );
    }

}
