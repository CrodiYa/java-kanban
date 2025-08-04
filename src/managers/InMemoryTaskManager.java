package managers;

import managers.history.HistoryManager;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private int idCount;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, SubTask> subtasks = new HashMap<>();

    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Override
    public void addTask(Task task) {
        task.setTaskId(++idCount);
        tasks.put(task.getTaskId(), task);
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setTaskId(++idCount);
        epics.put(epic.getTaskId(), epic);
    }

    @Override
    public void addSubTask(SubTask subTask) {
        subTask.setTaskId(++idCount);
        subtasks.put(subTask.getTaskId(), subTask);

        Epic epic = epics.get(subTask.getEpicId());
        epic.addSubTask(subTask);
        updateEpicStatus(epic);
    }

    @Override
    public Task getTask(int id) {
        historyManager.addTask(tasks.get(id));
        return tasks.get(id);
    }

    @Override
    public Epic getEpic(int id) {
        historyManager.addTask(epics.get(id));
        return epics.get(id);
    }

    @Override
    public SubTask getSubTask(int id) {
        historyManager.addTask(subtasks.get(id));
        return subtasks.get(id);
    }

    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public ArrayList<SubTask> getSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<SubTask> getSubTasksFromEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return new ArrayList<>();
        }
        ArrayList<SubTask> subTasksList = new ArrayList<>();

        for (Integer subtaskId : epic.getSubtaskIds()) {
            subTasksList.add(subtasks.get(subtaskId));
        }

        return subTasksList;
    }

    @Override
    public void updateTask(Task task) {
        tasks.put(task.getTaskId(), task);
    }

    @Override
    public void updateEpic(Epic epic) {
        int id = epic.getTaskId();
        Epic oldEpic = epics.get(id);

        oldEpic.setTitle(epic.getTitle());
        oldEpic.setDescription(epic.getDescription());
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        int id = subTask.getTaskId();
        SubTask oldSubTask = subtasks.get(id);

        oldSubTask.setTitle(subTask.getTitle());
        oldSubTask.setDescription(subTask.getDescription());
        oldSubTask.setStatus(subTask.getStatus());

        updateEpicStatus(epics.get(oldSubTask.getEpicId())); //обновляем статус эпика
    }

    private void updateEpicStatus(Epic epic) {
        ArrayList<Integer> epicChildren = epic.getSubtaskIds();
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

    @Override
    public void deleteTask(int id) {
        if (!tasks.containsKey(id)) {
            return;
        }
        historyManager.remove(id); // удаляем из истории
        tasks.remove(id);
    }

    @Override
    public void clearTasks() {
        clearHistoryTasks();
        tasks.clear();
    }

    @Override
    public void deleteEpic(int id) {
        if (!epics.containsKey(id)) {
            return;
        }
        Epic epic = epics.get(id);

        for (Integer subtaskId : epic.getSubtaskIds()) { // удаляем из основной таблицы подзадач каждую подзадачу эпика
            subtasks.remove(subtaskId);
            historyManager.remove(subtaskId); // удаляем из истории
        }

        historyManager.remove(id); // удаляем из истории
        epics.remove(id);

    }

    @Override
    public void clearEpics() {
        clearHistoryEpics();
        epics.clear();

        clearHistorySubTasks();
        subtasks.clear();
    }

    @Override
    public void deleteSubTask(int id) {
        if (!subtasks.containsKey(id)) {
            return;
        }

        int epicParentId = subtasks.get(id).getEpicId();
        // удаляем подзадачу из листа эпика, пересчитываем статус
        Epic epic = epics.get(epicParentId);
        epic.deleteSubTask(id);
        updateEpicStatus(epic);

        historyManager.remove(id); // удаляем из истории
        subtasks.remove(id);
    }

    @Override
    public void clearSubTasks() {
        clearHistorySubTasks();
        subtasks.clear();
        for (Epic epic : epics.values()) {
            epic.clearSubtasks(); // статус эпика обновляется внутри метода
        }
    }

    private void clearHistoryTasks() {
        for (Integer id : tasks.keySet()) {
            historyManager.remove(id);
        }
    }

    private void clearHistoryEpics() {
        for (Integer id : epics.keySet()) {
            historyManager.remove(id);
        }
    }

    private void clearHistorySubTasks() {
        for (Integer id : subtasks.keySet()) {
            historyManager.remove(id);
        }
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

}
