package model;

import java.util.ArrayList;

public class Epic extends Task {

    private final ArrayList<Integer> subtaskIds;

    public Epic(String title, String description, Status status) {
        super(title, description, status);
        subtaskIds = new ArrayList<>();
    }

    public void addSubTask(SubTask subTask) {

        setDurationAndStartTime(subTask);
        subtaskIds.add(subTask.getTaskId());
    }

    private void setDurationAndStartTime(SubTask subTask) {
        if (subTask.getDuration() == null) {
            return;
        }

        duration = duration == null ? subTask.getDuration() : duration.plus(subTask.getDuration());

        if (subTask.getStartTime() == null) {
            return;
        }

        if (startTime == null) {
            startTime = subTask.getStartTime();
        } else {
            startTime = startTime.isAfter(subTask.getStartTime()) ? subTask.getStartTime() : startTime;
        }
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

    public Type getType() {
        return Type.EPIC;
    }

    @Override
    public String toString() {
        return String.format("%s{id=%d, title=%s, description=%s, status=%s, subtasks=%s}",
                this.getClass(),
                this.taskId,
                this.title,
                this.description,
                this.status,
                this.subtaskIds
        );
    }

}
