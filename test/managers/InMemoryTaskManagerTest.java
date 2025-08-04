package managers;

import model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class InMemoryTaskManagerTest {

    private final TaskManager manager = Managers.getDefault();
    private Task task;
    private Epic epic;
    private SubTask subtask1;
    private SubTask subtask2;

    private final int invalidId = 42;
    private final int taskId = 1;
    private final int epicId = 2;
    private final int subTaskId1 = 3;
    private final int subTaskId2 = 4;

    @BeforeEach
    public void BeforeEach() {
        task = new Task("task", "task", Status.NEW);
        epic = new Epic("epic", "epic", Status.NEW);

        manager.addTask(task); //id=1
        manager.addEpic(epic); //id=2

        subtask1 = new SubTask("subtask1", "subtask1", Status.NEW, epic.getTaskId());
        subtask2 = new SubTask("subtask2", "subtask2", Status.NEW, epic.getTaskId());

        manager.addSubTask(subtask1); //id=3
        manager.addSubTask(subtask2); //id=4
    }

    // search tests
    @Test
    public void shouldBeNullWithInvalidId() {
        // ищем несуществующие таски
        assertNull(manager.getTask(epicId)); // looking for task with epicId
        assertNull(manager.getEpic(subTaskId1)); // looking for epic with subtaskId
        assertNull(manager.getSubTask(taskId)); // looking for subtask1 with taskId

        assertNull(manager.getTask(invalidId));
        assertNull(manager.getEpic(invalidId));
        assertNull(manager.getSubTask(invalidId));
    }

    @Test
    public void shouldBeEqualWithSize() {
        // проверяем, действительно ли добавились
        assertEquals(1, manager.getTasks().size());
        assertEquals(1, manager.getEpics().size());
        assertEquals(2, manager.getSubTasks().size());
    }

    @Test
    public void shouldBeEqual() {
        // сравниваем таск в менеджере с оригинальным таском
        assertEquals(task, manager.getTask(taskId));
        assertEquals(epic, manager.getEpic(epicId));
        assertEquals(subtask1, manager.getSubTask(subTaskId1));
        assertEquals(subtask2, manager.getSubTask(subTaskId2));
    }

    // clear tests
    @Test
    public void shouldBeEmptyWhenTasksClear() {
        manager.clearTasks();
        assertTrue(manager.getTasks().isEmpty());
    }

    @Test
    public void shouldBeEmptyWhenEpicsClear() {
        manager.clearEpics();
        assertTrue(manager.getEpics().isEmpty());
    }

    @Test
    public void shouldBeEmptyWhenSubTasksClear() {
        manager.clearSubTasks();
        assertTrue(manager.getSubTasks().isEmpty());
    }

    @Test
    public void shouldBeEmptyWhenSubtasksEmpty() {
        // проверяем, что сабтаски удалились и из эпика
        manager.clearSubTasks();
        assertTrue(manager.getSubTasksFromEpic(epicId).isEmpty());
        assertTrue(manager.getEpic(epicId).getSubtaskIds().isEmpty());

    }

    // delete tests
    @Test
    public void shouldBeNullWhenSearchForTaskAfterDelete() {
        manager.deleteTask(taskId);
        assertNull(manager.getTask(taskId));
    }

    @Test
    public void shouldBeNullWhenSearchForEpicAfterDelete() {
        manager.deleteEpic(epicId);
        assertNull(manager.getEpic(epicId));
    }

    @Test
    public void shouldBeNullWhenSearchForSubTaskAfterDelete() {
        manager.deleteSubTask(subTaskId1);
        assertNull(manager.getSubTask(subTaskId1));
    }

    // epic got subtasks tests
    @Test
    public void shouldContainSubtaskIdInEpic() {
        // проверяем, что эпик получил айди сабтасков
        assertTrue(epic.getSubtaskIds().contains(subtask1.getTaskId()));
        assertTrue(epic.getSubtaskIds().contains(subtask2.getTaskId()));
    }

    @Test
    public void subTasksSizeInEpicShouldBeEqualWithSize() {
        // проверяем, что количество сабтасков в эпике равно добавленным сабтаскам
        assertEquals(2, manager.getSubTasksFromEpic(epicId).size());
        assertEquals(2, manager.getEpic(epicId).getSubtaskIds().size());
    }

    @Test
    public void shouldContainSubtasksInEpic() {
        // проверяем, что эпик получил именно те сабтаски
        assertTrue(manager.getSubTasksFromEpic(epicId).contains(subtask1));
        assertTrue(manager.getSubTasksFromEpic(epicId).contains(subtask2));
    }

    @Test
    public void shouldBeEmptyWhenEpicIsNull() {
        // проверяем, что сабтасков не существует, если эпика не существует
        assertTrue(manager.getSubTasksFromEpic(invalidId).isEmpty());
    }


    // update tests
    @Test
    public void shouldBeEqualAfterTaskUpdate() {
        Task updTask = new Task("updatedTask", "test", Status.IN_PROGRESS);
        updTask.setTaskId(taskId);

        manager.updateTask(updTask);

        assertEquals("updatedTask", manager.getTask(taskId).getTitle());
        assertEquals("test", manager.getTask(taskId).getDescription());
        assertEquals(Status.IN_PROGRESS, manager.getTask(taskId).getStatus());

    }

    @Test
    public void shouldBeEqualAfterSubTaskUpdate() {
        SubTask updTask = new SubTask("updatedTask", "test", Status.IN_PROGRESS, epicId);
        updTask.setTaskId(subTaskId1);

        manager.updateSubTask(updTask);

        assertEquals("updatedTask", manager.getSubTask(subTaskId1).getTitle());
        assertEquals("test", manager.getSubTask(subTaskId1).getDescription());
        assertEquals(Status.IN_PROGRESS, manager.getSubTask(subTaskId1).getStatus());
    }

    // epic update
    @Test
    public void shouldBeEqualAfterEpicUpdate() {
        // проверяем только изменения названия и описания
        Epic updTask = new Epic("updatedEpic", "test", Status.NEW);
        updTask.setTaskId(epicId);

        manager.updateEpic(updTask);

        assertEquals("updatedEpic", manager.getEpic(epicId).getTitle());
        assertEquals("test", manager.getEpic(epicId).getDescription());
    }

    @Test
    public void statusShouldNotBeEqualAfterEpicUpdate() {
        // проверяем, что статус эпика не изменится
        Epic updTask = new Epic("updatedEpic", "test", Status.IN_PROGRESS);
        updTask.setTaskId(epicId);

        manager.updateEpic(updTask);

        assertNotEquals(Status.IN_PROGRESS, manager.getEpic(epicId).getStatus());
    }

    @Test
    public void childrenShouldBeEqualAfterEpicUpdate() {
        // проверяем, что эпик не потерял свои сабтаски
        Epic updTask = new Epic("updatedEpic", "test", Status.NEW);
        updTask.setTaskId(epicId);

        manager.updateEpic(updTask);

        assertEquals(epic.getSubtaskIds(), manager.getEpic(epicId).getSubtaskIds());
    }

    private void updateSubtasks(Status status) {
        // метод помощник, чтобы избежать множественных повторений
        SubTask updTask = new SubTask("updatedTask", "test", status, epicId);

        updTask.setTaskId(subTaskId1);
        manager.updateSubTask(updTask);

        updTask.setTaskId(subTaskId2);
        manager.updateSubTask(updTask);
    }

    @Test
    public void epicStatusShouldChangeAfterSubTaskUpdate() {
        // проверяем, что статус эпика меняется при изменении статуса сабтаска
        SubTask updTask = new SubTask("updatedTask", "test", Status.IN_PROGRESS, epicId);

        updTask.setTaskId(subTaskId1);
        manager.updateSubTask(updTask);

        assertEquals(epic.getStatus(), manager.getSubTask(subTaskId1).getStatus());
    }


    @Test
    public void epicStatusShouldStayWhenOnlyOneChildIsNotDone() {
        // проверяем, что статус эпика не поменяется на DONE, если есть сабтаск со статусом отличным от DONE
        SubTask updTask = new SubTask("updatedTask", "test", Status.DONE, epicId);

        updTask.setTaskId(subTaskId1);
        manager.updateSubTask(updTask);

        // subTaskId2 не был изменен и его статус NEW, статус эпика не должен поменяться
        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    public void epicStatusShouldBeDoneWhenAllChildrenAreDone() {
        // проверяем, что эпик становится DONE, когда все сабтаски DONE
        updateSubtasks(Status.DONE);

        assertEquals(Status.DONE, epic.getStatus());
    }

    @Test
    public void epicStatusShouldBeInProgressWhenNewChildAdded() {
        // проверяем, что эпик изменяется на IN_PROGRESS, когда добавляется новый сабтаск
        updateSubtasks(Status.DONE);
        manager.addSubTask(new SubTask("subtask3", "test", Status.NEW, epicId));

        assertEquals(Status.IN_PROGRESS, epic.getStatus());
    }

    @Test
    public void epicStatusShouldBeNewWhenSubTasksClear() {
        // проверяем, что эпик изменяется на NEW, когда пустой
        updateSubtasks(Status.IN_PROGRESS);

        manager.clearSubTasks();

        assertEquals(Status.NEW, epic.getStatus());
    }


}