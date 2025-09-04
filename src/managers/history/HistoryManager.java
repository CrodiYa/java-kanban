package managers.history;

import model.Task;

import java.util.List;

/**
 * Интерфейс менеджера истории просмотров задач.
 *
 * @implSpec Реализации должны обеспечивать эффективное удаление из середины списка
 */
public interface HistoryManager {

    void addTask(Task task);

    void remove(int id);

    List<Task> getHistory();

}
