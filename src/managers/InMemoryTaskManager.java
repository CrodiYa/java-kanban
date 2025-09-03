package managers;

import managers.history.HistoryManager;
import model.Epic;
import model.SubTask;
import model.Task;
import util.Status;
import util.TaskTimeController;
import util.exceptions.TaskTimeOverlapException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    private int idCount;
    private final HashMap<Integer, Task> tasks = new HashMap<>();
    private final HashMap<Integer, Epic> epics = new HashMap<>();
    private final HashMap<Integer, SubTask> subtasks = new HashMap<>();
    private final HistoryManager historyManager = Managers.getDefaultHistory();
    private final TaskTimeController taskTimeController = new TaskTimeController();

    @Override
    public void addTask(Task task) throws TaskTimeOverlapException {
        if (taskTimeController.isTimeOverlapping(task)) {
            throw new TaskTimeOverlapException("Can`t add task: " + task);
        }
        task.setTaskId(++idCount);
        tasks.put(task.getTaskId(), task);
        taskTimeController.add(task);
    }

    @Override
    public void addEpic(Epic epic) {
        epic.setTaskId(++idCount);
        epics.put(epic.getTaskId(), epic);
    }

    @Override
    public void addSubTask(SubTask subTask) throws TaskTimeOverlapException {
        if (taskTimeController.isTimeOverlapping(subTask)) {
            throw new TaskTimeOverlapException("Can`t add task: " + subTask);
        }
        subTask.setTaskId(++idCount);
        subtasks.put(subTask.getTaskId(), subTask);

        Epic epic = epics.get(subTask.getEpicId());
        epic.addSubTask(subTask);
        updateEpicStatus(epic);

        taskTimeController.updateEpicDurationAndStartTime(epic, subTask);
        taskTimeController.add(subTask);
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
    public List<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public List<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    @Override
    public List<SubTask> getSubTasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public List<SubTask> getSubTasksFromEpic(int id) {
        Epic epic = epics.get(id);
        if (epic == null) {
            return new ArrayList<>();
        }

        return epic.getSubtaskIds().stream()
                .map(subtasks::get)
                .toList();
    }

    @Override
    public List<Task> getPrioritizedTasks() {
        return List.copyOf(taskTimeController.getTimeSortedTasks());
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

    /**
     * Обновляет статус эпика на основе статусов его подзадач.
     * Статус эпика определяется по следующим правилам:
     * <ul>
     *   <li>Если у эпика нет подзадач - статус устанавливается в {@link Status#NEW}</li>
     *   <li>Если все подзадачи имеют статус {@link Status#NEW} - эпик получает статус {@link Status#NEW}</li>
     *   <li>Если все подзадачи имеют статус {@link Status#DONE} - эпик получает статус {@link Status#DONE}</li>
     *   <li>В остальных случаях (смешанные статусы или подзадачи в процессе выполнения) -
     *       эпик получает статус {@link Status#IN_PROGRESS}</li>
     * </ul>
     *
     * @param epic эпик, статус которого следует обновить.
     * @implNote Вызывается при добавлении, удалении или изменении подзадачи эпика.
     */
    private void updateEpicStatus(Epic epic) {
        List<Integer> epicChildren = epic.getSubtaskIds();
        if (epicChildren.isEmpty()) {
            epic.setStatus(Status.NEW);
            return;
        }

        int total = epicChildren.size();
        int doneCount = 0;
        int newCount = 0;

        for (Integer id : epicChildren) {
            Status status = subtasks.get(id).getStatus();
            if (status == Status.NEW) {
                newCount++;
            } else if (status == Status.DONE) {
                doneCount++;
            }
        }

        if (total == newCount) {
            epic.setStatus(Status.NEW);
        } else if (total == doneCount) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    @Override
    public void deleteTask(int id) {
        if (!tasks.containsKey(id)) {
            return;
        }
        historyManager.remove(id);
        taskTimeController.remove(tasks.get(id)); // удаляем объект т.к это быстрее, чем удаление по id
        tasks.remove(id);
    }

    @Override
    public void clearTasks() {
        tasks.forEach((id, task) -> historyManager.remove(id));
        tasks.clear();
        taskTimeController.removeTasks();
    }

    /**
     * Удаляет эпик по идентификатору вместе со всеми его подзадачами.
     *
     * <p>Выполняет следующие операции при удалении эпика:
     * <ol>
     *   <li>Для каждой подзадачи эпика:
     *     <ul>
     *       <li>Удаляет подзадачу из истории просмотров</li>
     *       <li>Удаляет подзадачу из контроллера временных промежутков</li>
     *       <li>Удаляет подзадачу из основной таблицы подзадач</li>
     *     </ul>
     *   </li>
     *   <li>Удаляет эпик из истории просмотров</li>
     *   <li>Удаляет эпик из таблицы эпиков</li>
     * </ol>
     *
     * <p>Если эпик с указанным идентификатором не существует, метод завершается
     * без выполнения каких-либо операций.
     *
     * @param epicId идентификатор эпика для удаления; должен быть положительным числом
     */
    @Override
    public void deleteEpic(int epicId) {
        if (!epics.containsKey(epicId)) {
            return;
        }
        Epic epic = epics.get(epicId);

        for (Integer subtaskId : epic.getSubtaskIds()) {
            historyManager.remove(subtaskId);
            taskTimeController.remove(subtasks.get(subtaskId));
            subtasks.remove(subtaskId);
        }

        historyManager.remove(epicId);
        epics.remove(epicId);
    }

    @Override
    public void clearEpics() {
        epics.forEach((id, epic) -> historyManager.remove(id));
        epics.clear();

        subtasks.forEach((id, subTask) -> historyManager.remove(id));
        taskTimeController.removeSubTasks();
        subtasks.clear();
    }

    /**
     * Удаляет подзадачу по идентификатору и выполняет связанные обновления.
     *
     * <p>Выполняет следующие операции при удалении подзадачи:
     * <ol>
     *   <li>Удаляет подзадачу из списка подзадач эпика</li>
     *   <li>Обновляет статус эпика с учетом оставшихся подзадач</li>
     *   <li>Удаляет подзадачу из истории просмотров</li>
     *   <li>Удаляет подзадачу из контроллера временных интервалов</li>
     *   <li>Обновляет временные параметры эпика (длительность и время начала)</li>
     *   <li>Удаляет подзадачу из основной таблицы подзадач</li>
     * </ol>
     *
     * <p>Если подзадача с указанным идентификатором не существует, метод завершается
     * без выполнения каких-либо операций.
     *
     * @param id идентификатор подзадачи для удаления
     */
    @Override
    public void deleteSubTask(int id) {
        if (!subtasks.containsKey(id)) {
            return;
        }
        SubTask subTask = subtasks.get(id);
        int epicParentId = subTask.getEpicId();
        Epic epic = epics.get(epicParentId);

        epic.deleteSubTask(id);
        updateEpicStatus(epic);

        historyManager.remove(id);
        taskTimeController.remove(subTask);
        taskTimeController.updateEpicDurationAndStartTimeDeletion(epic, subTask);
        subtasks.remove(id);
    }

    @Override
    public void clearSubTasks() {
        subtasks.forEach((id, subTask) -> historyManager.remove(id));

        // статус и временные параметры эпика обновляется внутри метода clearSubtasks()
        epics.forEach((id, epic) -> epic.clearSubtasks());

        taskTimeController.removeSubTasks();

        subtasks.clear();
    }

    public int getIdCount() {
        return idCount;
    }

    public void setIdCount(int id) {
        idCount = id;
    }

    @Override
    public List<Task> getHistory() {
        return historyManager.getHistory();
    }

}
