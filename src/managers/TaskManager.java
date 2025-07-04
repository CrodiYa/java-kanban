package managers;

import model.*;
import java.util.HashMap;

public class TaskManager {
    private int idCount;

    HashMap<Integer, Task> tasks = new HashMap<>();
    HashMap<Integer, Epic> epics = new HashMap<>();
    HashMap<Integer, SubTask> subtasks = new HashMap<>();


    public Task addTask(Task task) {
        task.set_task_id(++idCount);
        tasks.put(task.get_task_id(), task);
        return task;
    }

    public Epic addEpic(Epic epic) {
        epic.set_task_id(++idCount);
        epics.put(epic.get_task_id(), epic);
        return epic;
    }

    public SubTask addSubTask(SubTask subTask) {
        subTask.set_task_id(++idCount);
        subtasks.put(subTask.get_task_id(), subTask);

        Epic epic = epics.get(subTask.get_epic_parent_id());
        epic.addSubTask(subTask); // проверка статуса происходит внутри метода

        return subTask;
    }

    public Task getTask(int task_id) {
        return tasks.get(task_id);
    }

    public Epic getEpic(int epic_id) {
        return epics.get(epic_id);
    }

    public SubTask getSubTask(int subtask_id) {
        return subtasks.get(subtask_id);
    }

    public Task updateTask(Task task) {
        tasks.put(task.get_task_id(), task);
        return task;
    }

    public Epic updateEpic(Epic epic) {
        int id = epic.get_task_id();
        /*
        мы обновляем эпик, сохраняя лишь его id,
        соответственно все подзадачи эпика должны быть удалены
        */
        for (SubTask subTask : subtasks.values()) {
            if (subTask.get_epic_parent_id() == id) {
                subtasks.remove(subTask.get_task_id());
            }
        }
        epics.put(id, epic);
        epic.updateStatus();
        return epic;
    }


    public SubTask updateSubTask(SubTask subTask) {
        /*
        ТЗ не объясняет как конкретно обновляется задача.
        Метод работает с условием, что входящая подзадача не меняет своего эпика
        в ином случае требуется удалить старый объект из эпика
         и добавить новый объект в другой эпик(при условии, что он существует)
        */
        epics.get(subTask.get_epic_parent_id()).addSubTask(subTask); //перезаписывает объект внутри эпика

        subtasks.put(subTask.get_task_id(), subTask);
        return subTask;
    }


    public void clearAllTasks() {
        tasks.clear();
        epics.clear();
        subtasks.clear();
        System.out.println("Все задачи очищены");
    }

    public void deleteTask(int task_id) {
        if (!tasks.containsKey(task_id)) return;
        tasks.remove(task_id);
        System.out.printf("Задача %d удалена\n", task_id);
    }

    public void clearTasks() {
        tasks.clear();
        System.out.println("Все задачи удалены");
    }

    public void deleteEpic(int epic_id) {
        if (!epics.containsKey(epic_id)) return;
        Epic epic = epics.get(epic_id);

        for (Integer key : epic.getSubTasksKeys()) { // удаляем из основной таблицы подзадач каждую подзадачу эпика
            subtasks.remove(key);
        }
        epics.remove(epic_id);
        System.out.printf("Задача %d удалена\n", epic_id);
    }

    public void clearEpics() {
        epics.clear();
        subtasks.clear();
        System.out.println("Все эпики удалены");
    }

    public void deleteSubTask(int subtask_id) {
        if (!subtasks.containsKey(subtask_id)) return;

        int epicParent = subtasks.get(subtask_id).get_epic_parent_id();

        Epic epic = epics.get(epicParent);
        epic.deleteSubTask(subtask_id); // удаляем подзадачу из таблицы эпика, статус эпика уточняется внутри метода

        subtasks.remove(subtask_id);
        System.out.printf("Задача %d удалена\n", subtask_id);
    }

    public void clearSubTasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks(); // статус уточняется внутри метода
        }
        System.out.println("Все подзадачи удалены");
    }


    public void printTasks() {
        if (tasks.isEmpty()) {
            System.out.println("Задач нет");
            return;
        }
        for (Task task : tasks.values()) {
            System.out.println(task.toString());
        }
    }

    public void printEpics() {
        if (epics.isEmpty()) {
            System.out.println("Эпиков нет");
            return;
        }
        for (Epic epic : epics.values()) {
            System.out.println(epic.toString());
        }
    }

    public void printSubTasksFromEpic(int epic_id) {
        epics.get(epic_id).printSubTasks();
    }

    public void printSubTasks() {
        if (subtasks.isEmpty()) {
            System.out.println("Подзадач нет");
            return;
        }
        for (SubTask subtask : subtasks.values()) {
            System.out.println(subtask.toString());
        }
    }


    public void printAllTasks() {
        /*
        Этот метод печатает все задачи по их статусу,
        учитывая, что эпик может быть сразу в нескольких категориях
        */
        System.out.println("*".repeat(20));
        System.out.println("TODO");
        printAllTasksHelper(tasks, Status.NEW);
        printAllEpicsHelper(epics, Status.NEW);
        System.out.println("*".repeat(20));
        System.out.println("In Progress");
        printAllTasksHelper(tasks, Status.IN_PROGRESS);
        printAllEpicsHelper(epics, Status.IN_PROGRESS);
        System.out.println("*".repeat(20));
        System.out.println("Done");
        printAllTasksHelper(tasks, Status.DONE);
        printAllEpicsHelper(epics, Status.DONE);
        System.out.println("*".repeat(20));
    }

    private void printAllTasksHelper(HashMap<Integer, Task> tasks, Status status) {
        if (tasks.isEmpty()) return;

        for (Task task : tasks.values()) {
            if (status.equals(task.getStatus())) {
                System.out.println(task.getTask());
            }
        }
    }

    private void printAllEpicsHelper(HashMap<Integer, Epic> tasks, Status status) {
        if (tasks.isEmpty()) return;

        for (Epic task : tasks.values()) {
            task.printSubTasks(status); // проверка на статус и вывод названия epic`а происходит внутри метода
        }
    }


    //DevTools
    public void printTasksDev() { //печатает подробную информацию о всех задачах
        if (tasks.isEmpty() && epics.isEmpty() && subtasks.isEmpty()) System.out.println("Пусто");
        System.out.println("Tasks:");
        for (Task t : tasks.values()) {
            System.out.println(t.toString());
        }
        System.out.println("Epics:");
        for (Task t : epics.values()) {
            System.out.println(t.toString());
        }
        System.out.println("SubTasks:");
        for (Task t : subtasks.values()) {
            System.out.println(t.toString());
        }
    }

}
