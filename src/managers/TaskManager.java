package managers;

import model.*;

import java.util.ArrayList;
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

        updateEpicStatus(epic); // проверяем статус на случай невалидного статуса

        epics.put(epic.getTaskID(), epic);
    }

    public void addSubTask(SubTask subTask) {
        subTask.setTaskID(++idCount);
        subtasks.put(subTask.getTaskID(), subTask);

        Epic epic = epics.get(subTask.getEpicID());
        epic.addSubTask(subTask);
        updateEpicStatus(epic);
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

    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    public ArrayList<SubTask> getSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    public ArrayList<SubTask> getSubTasksFromEpic(int id) {
        Epic epic = epics.get(id);
        if(epic == null){
            return new ArrayList<>();
        }
        ArrayList<SubTask> subTasksList= new ArrayList<>();

        for (Integer subtaskID : epic.getSubtasksIDs()){
            subTasksList.add(subtasks.get(subtaskID));
        }

        return subTasksList;
    }


    public void updateTask(Task task) {
        tasks.put(task.getTaskID(), task);
    }

    public void updateEpic(Epic epic) {
        int id = epic.getTaskID();
        Epic oldEpic = epics.get(id);

        oldEpic.setTitle(epic.getTitle());
        oldEpic.setDescription(epic.getDescription());

        updateEpicStatus(oldEpic);
        /*
         oldEpic ссылается на тот же объект,
         поэтому добавлять его обратно необязательно,
         */
    }


    public void updateSubTask(SubTask subTask) {
        int id = subTask.getTaskID();
        SubTask oldSubTask = subtasks.get(id);

        oldSubTask.setTitle(subTask.getTitle());
        oldSubTask.setDescription(subTask.getDescription());
        /*
         oldSubTask ссылается на тот же объект,
         поэтому добавлять его обратно необязательно,
         */

    }

    public void updateEpicStatus(Epic epic) {
        ArrayList<Integer> epicChildren = epic.getSubtasksIDs();
        if (epicChildren.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        int subTasksDone = 0;
        int subTasksNotNew = 0;

        for (Integer id : epicChildren) {
            Status status = subtasks.get(id).getStatus();
            if (status != Status.NEW) {
                subTasksNotNew++;
            }
            if (status == Status.DONE) {
                subTasksDone++;
            }
        }

        if (subTasksNotNew != 0) {
            epic.setStatus(Status.IN_PROGRESS);
        } else {
            epic.setStatus(Status.NEW);
            return;
        }

        if (subTasksDone != 0 && subTasksDone == epicChildren.size()) {
            epic.setStatus(Status.DONE);
        }
    }

    public void deleteTask(int id) {
        if (!tasks.containsKey(id)) {
            return;
        }
        tasks.remove(id);
    }

    public void clearTasks() {
        tasks.clear();
    }

    public void deleteEpic(int id) {
        if (!epics.containsKey(id)) {
            return;
        }

        Epic epic = epics.get(id);

        for (Integer subtaskID : epic.getSubtasksIDs()) { // удаляем из основной таблицы подзадач каждую подзадачу эпика
            subtasks.remove(subtaskID);
        }
        epics.remove(id);
    }

    public void clearEpics() {
        epics.clear();
        subtasks.clear();
    }

    public void deleteSubTask(int id) {
        if (!subtasks.containsKey(id)) {
            return;
        }

        int epicParentID = subtasks.get(id).getEpicID();
        // удаляем подзадачу из листа эпика, пересчитываем статус
        Epic epic = epics.get(epicParentID);
        epic.deleteSubTask(id);
        updateEpicStatus(epic);

        subtasks.remove(id);
    }

    public void clearSubTasks() {
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks(); // статус эпика обновляется внутри метода
        }
    }

}
