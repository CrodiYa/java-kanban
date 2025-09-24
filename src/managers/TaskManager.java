package managers;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.List;

/**
 * Интерфейс менеджера задач для управления задачами, эпиками и подзадачами.
 *
 * @apiNote Реализации могут добавлять дополнительные проверки (например, временные пересечения)
 */
public interface TaskManager {
    void addTask(Task task);

    void addEpic(Epic epic);

    void addSubTask(SubTask subTask);

    Task getTask(int id);

    Epic getEpic(int id);

    SubTask getSubTask(int id);

    Task getTaskWithoutHistory(int id);

    Epic getEpicWithoutHistory(int id);

    SubTask getSubTaskWithoutHistory(int id);

    List<Task> getTasks();

    List<Epic> getEpics();

    List<SubTask> getSubTasks();

    List<SubTask> getSubTasksFromEpic(int id);

    List<Task> getPrioritizedTasks();

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubTask(SubTask subTask);

    void deleteTask(int id);

    void clearTasks();

    void deleteEpic(int id);

    void clearEpics();

    void deleteSubTask(int id);

    void clearSubTasks();

    void clearSubTasksFromEpic(int id);

    List<Task> getHistory();
}
