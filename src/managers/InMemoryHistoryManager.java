package managers;

import model.Task;

import java.util.LinkedList;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {

    private final LinkedList<Task> history = new LinkedList<>();
    private final int MAX_HISTORY_CAPACITY = 10;

    @Override
    public void addTask(Task task) {

        if (history.size() == MAX_HISTORY_CAPACITY) {
            history.removeFirst();
        }
        history.add(task);
    }

    @Override
    public List<Task> getHistory() {
        return history;
    }
}
