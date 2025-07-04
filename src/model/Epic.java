package model;

import java.util.HashMap;
import java.util.Set;

public class Epic extends Task {

    private HashMap<Integer, SubTask> subTasks;

    public Epic(String epic, String description, Status status) {
        super(epic, description, status);
        subTasks = new HashMap<>();
        updateStatus(); // пересчитываем статус вне зависимости от указаний конструктора
    }


    public void addSubTask(SubTask subTask) {
        subTasks.put(subTask.get_task_id(), subTask);
        updateStatus();
    }

    public void printSubTasks(Status status) {
        // Метод печатает подзадачи для каждого статуса + название эпика
        if (subTasks.isEmpty()) {
            if (status == Status.NEW) System.out.println(this.getTask());
            return;
        }
        boolean flag = true;

        for (SubTask subTask : subTasks.values()) {
            if (status.equals(subTask.getStatus())) {
                if (flag) {
                    System.out.println(this.getTask());
                    flag = false;
                }
                System.out.println(" -" + subTask.getTask());
            }
        }
    }

    public void printSubTasks() {
        // метод печатает каждую подзадачу метода
        System.out.printf("Эпик: %s\n", this.getTask());
        if (subTasks.isEmpty()) {
            System.out.println("Подзадач нет");
            return;
        }

        for (SubTask subTask : subTasks.values()) {
            System.out.printf("Подзадача: %s Статус: %s\n",
                    subTask.getTask(),
                    subTask.getStatus()
            );
        }
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

    public void deleteSubTask(int task_id) {
        subTasks.remove(task_id);
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

}
