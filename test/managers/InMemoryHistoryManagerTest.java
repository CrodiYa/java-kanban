package managers;

import managers.history.HistoryManager;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {

    private final HistoryManager historyManager = Managers.getDefaultHistory();

    @Test
    public void shouldAddAnyTask() {
        Task task = new Task("task", "test", Status.NEW);
        task.setTaskId(1);

        Epic epic = new Epic("epic", "test", Status.NEW);
        epic.setTaskId(2);
        SubTask subtask = new SubTask("subtask", "test", Status.NEW, 2);
        subtask.setTaskId(3);

        historyManager.addTask(task);
        historyManager.addTask(epic);
        historyManager.addTask(subtask);

        assertTrue(historyManager.getHistory().contains(task));
        assertTrue(historyManager.getHistory().contains(epic));
        assertTrue(historyManager.getHistory().contains(subtask));
    }

    @Test
    public void shouldBeUniqueInHistoryWhenWasAddedAgain() {
        for (int i = 1; i <= 3; i++) {
            Task task = new Task(i + "", "test", Status.NEW);
            task.setTaskId(i);
            historyManager.addTask(task);
        }

        int id = 2;
        Task task = new Task(id + "", "again", Status.NEW);
        task.setTaskId(id);
        historyManager.addTask(task);

        int expected = 1;
        int actual = 0;
        for (Task t : historyManager.getHistory()) {
            if (t.getTaskId() == id) {
                actual++;
            }
        }
        assertEquals(expected, actual);
    }

    @Test
    public void shouldBeLastInHistoryWhenWasAddedAgain() {
        for (int i = 1; i <= 3; i++) {
            Task task = new Task(i + "", "test", Status.NEW);
            task.setTaskId(i);
            historyManager.addTask(task);
        }

        Task task = new Task(1 + "", "again", Status.NEW);
        task.setTaskId(1);
        historyManager.addTask(task);

        assertEquals(historyManager.getHistory().getLast(), task);
    }

    @Test
    public void shouldNotContainWhenDeletedFromTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        Task task = new Task("title", "demo", Status.NEW);
        taskManager.addTask(task);
        taskManager.getTask(1);

        taskManager.deleteTask(1);

        assertFalse(taskManager.getHistory().contains(task));

    }

    @Test
    public void shouldBeEmptyWhenTasksClearedFromTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        for (int i = 1; i <= 10; i++) {
            taskManager.addTask(new Task("title", "demo", Status.NEW));
            taskManager.getTask(i);
        }
        taskManager.clearTasks();
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    public void shouldBeEmptyWhenEpicsClearedFromTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        for (int i = 1; i <= 10; i++) {
            taskManager.addEpic(new Epic("title", "demo", Status.NEW));
            taskManager.getEpic(i);
        }
        taskManager.clearEpics();
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    public void shouldBeEmptyWhenSubtasksClearedFromTaskManager() {
        TaskManager taskManager = Managers.getDefault();
        taskManager.addEpic(new Epic("epic", "demo", Status.NEW));

        for (int i = 2; i <= 10; i++) {
            taskManager.addSubTask(new SubTask("title", "demo", Status.NEW, 1));
            taskManager.getSubTask(i);
        }

        taskManager.clearSubTasks();
        assertTrue(taskManager.getHistory().isEmpty());
    }

    @Test
    public void shouldBeEmptyWhenEpicsClearedFromTaskManagerAndHistoryContainsSubtasks() {
        TaskManager taskManager = Managers.getDefault();
        for (int i = 1; i <= 10; i++) {
            taskManager.addEpic(new Epic("title", "demo", Status.NEW));
        }
        for (int i = 11; i <= 20; i++) {
            taskManager.addSubTask(new SubTask("title", "demo", Status.NEW, i - 10));
            taskManager.getSubTask(i);
        }

        taskManager.clearEpics();
        assertTrue(taskManager.getHistory().isEmpty());
    }


}