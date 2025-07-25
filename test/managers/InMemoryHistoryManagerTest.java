package managers;

import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class InMemoryHistoryManagerTest {

    private final HistoryManager manager = Managers.getDefaultHistory();

    @Test
    public void shouldAddAnyTask() {
        Task task = new Task("task", "test", Status.NEW);
        task.setTaskId(1);

        Epic epic = new Epic("epic", "test", Status.NEW);
        epic.setTaskId(2);
        SubTask subtask = new SubTask("subtask", "test", Status.NEW, 2);
        subtask.setTaskId(3);

        manager.addTask(task);
        manager.addTask(epic);
        manager.addTask(subtask);

        assertTrue(manager.getHistory().contains(task));
        assertTrue(manager.getHistory().contains(epic));
        assertTrue(manager.getHistory().contains(subtask));
    }

    @Test
    public void shouldDeleteFirstAndAddLastWhenMaxLimit() {
        for (int i = 0; i < 10; i++) {
            manager.addTask(new Task(i+"", i+"", Status.NEW));
        }

        Task first = manager.getHistory().getFirst();

        Task last = new Task(11+"", 11+"", Status.NEW);
        manager.addTask(last);

        assertFalse(manager.getHistory().contains(first));
        assertTrue(manager.getHistory().contains(last));

    }
}