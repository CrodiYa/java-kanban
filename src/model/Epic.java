package model;

import java.util.HashMap;
import java.util.Set;

public class Epic extends Task {

    private final HashMap<Integer, SubTask> subTasks;

    public Epic(String title, String description, Status status) {
        super(title, description, status);
        subTasks = new HashMap<>();
        updateStatus(); // пересчитываем статус вне зависимости от указаний конструктора
    }


    public void addSubTask(SubTask subTask) {
        subTasks.put(subTask.getTaskID(), subTask);
        updateStatus();
    }

    public void updateStatus() {
        if (subTasks.isEmpty()) {
            this.setStatus(Status.NEW);
            return;
        }

        int subTasksDone = 0;
        int subTasksNotNew = 0;

        for (SubTask subTask : subTasks.values()) {
            Status status = subTask.getStatus();

            if (status != Status.NEW) subTasksNotNew++;
            if (status == Status.DONE) subTasksDone++;
        }

        if (subTasksNotNew != 0) {
            this.setStatus(Status.IN_PROGRESS);
        } else {
            this.setStatus(Status.NEW);
            return;
        }

        if (subTasksDone != 0 && subTasksDone == subTasks.size()) this.setStatus(Status.DONE);
    }

    public void deleteSubTask(int id) {
        subTasks.remove(id);
        updateStatus();
    }

    public void clearSubtasks() {
        subTasks.clear();
        updateStatus();
    }

    public Set<Integer> getSubTasksKeys() { // для удаления каждой подзадачи в таблице подзадач taskManager`а
        return subTasks.keySet();
    }

    public String getSubtasksString() { //возвращает таблицу в виде строки для подробного изучения
        return subTasks.toString();
    }

    @Override
    public String toString() {
        return String.format("%s{id=%d, title=%s, description=%s, status=%s, subtasks=%s}",
                this.getClass(),
                this.getTaskID(),
                this.getTitle(),
                this.getDescription(),
                this.getStatus(),
                this.getSubtasksString()
        );
    }

}
