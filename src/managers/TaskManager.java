package managers;

import model.*;
import java.util.HashMap;

public class TaskManager {
    private int idCount;

    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, SubTask> subtasks = new HashMap<>();


    public void addTask(Task task) {
        task.setTaskID(++idCount);
        tasks.put(task.getTaskID(), task);
    }

    public void addEpic(Epic epic) {
        epic.setTaskID(++idCount);
        epics.put(epic.getTaskID(), epic);
    }

    public void addSubTask(SubTask subTask) {
        subTask.setTaskID(++idCount);
        subtasks.put(subTask.getTaskID(), subTask);

        Epic epic = epics.get(subTask.getEpicID());
        epic.addSubTask(subTask); // проверка статуса происходит внутри метода

    }

    public Task getTask(int id) {
        return tasks.get(id);
    }

    public Epic getEpic(int id) {
        return epics.get(id);
    }

    public SubTask getSubTask(int id) {
        return subtasks.get(id);
    }

    public Task updateTask(Task task) {
        tasks.put(task.getTaskID(), task);
        return task;
    }

    public Epic updateEpic(Epic epic) {
        int id = epic.getTaskID();
        /*
        мы обновляем эпик, сохраняя лишь его id,
        соответственно все подзадачи эпика должны быть удалены
        */
        for (SubTask subTask : subtasks.values()) {
            if (subTask.getEpicID() == id) {
                subtasks.remove(subTask.getTaskID());
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
        epics.get(subTask.getEpicID()).addSubTask(subTask); //перезаписывает объект внутри эпика

        subtasks.put(subTask.getTaskID(), subTask);
        return subTask;
    }
    

    public void deleteTask(int id) {
        if (!tasks.containsKey(id)) return;
        tasks.remove(id);
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void deleteEpic(int id) {
        if (!epics.containsKey(id)) return;
        Epic epic = epics.get(id);

        for (Integer key : epic.getSubTasksKeys()) { // удаляем из основной таблицы подзадач каждую подзадачу эпика
            subtasks.remove(key);
        }
        epics.remove(id);
    }

    public void clearEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteSubTask(int id) {
        if (!subtasks.containsKey(id)) return;

        int epicParent = subtasks.get(id).getEpicID();

        Epic epic = epics.get(epicParent);
        epic.deleteSubTask(id); // удаляем подзадачу из таблицы эпика, статус эпика уточняется внутри метода

        subtasks.remove(id);
    }

    public void clearSubTasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks(); // статус уточняется внутри метода
        }
    }

}
