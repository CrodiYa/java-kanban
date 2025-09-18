package util;

import managers.Managers;
import managers.TaskManager;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.Test;
import util.enums.Status;
import util.enums.Type;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class TaskTimeControllerTest {

    private final TaskTimeController ttController = new TaskTimeController();
    private final long tenMinutes = 10;
    private final LocalDateTime epochTime =
            LocalDateTime.of(1970, 1, 1, 0, 0, 0);


    @Test
    public void shouldBeTrueWhenOverlapSameTime() {
        Task task1 = new Task("task1", "demo", Status.NEW, tenMinutes, epochTime);
        ttController.add(task1);

        Task task2 = new Task("task2", "demo", Status.NEW, tenMinutes, epochTime);

        assertTrue(ttController.isTimeOverlapping(task2));
    }

    @Test
    public void shouldBeTrueWhenOverlapStartBeforeEnd() {
        Task task1 = new Task("task1", "demo", Status.NEW, tenMinutes, epochTime);
        ttController.add(task1);

        LocalDateTime wrongStart = epochTime.plusMinutes(8); // за 2 минут до конца таска1
        Task task2 = new Task("task2", "demo", Status.NEW, tenMinutes, wrongStart);

        assertTrue(ttController.isTimeOverlapping(task2));
    }

    @Test
    public void shouldBeTrueWhenOverlapEndAfterStart() {
        Task task1 = new Task("task1", "demo", Status.NEW, tenMinutes, epochTime);
        ttController.add(task1);

        long fiveMinutes = 5;
        Task task2 = new Task("task2", "demo", Status.NEW, fiveMinutes, epochTime.plusMinutes(-1));

        assertTrue(ttController.isTimeOverlapping(task2));
    }

    @Test
    public void shouldBeTrueWhenOverlapInside() {
        Task task1 = new Task("task1", "demo", Status.NEW, tenMinutes, epochTime);
        ttController.add(task1);

        LocalDateTime wrongStart = epochTime.plusMinutes(1); // за 2 минут до конца таска1
        long fiveMinutes = 5;
        Task task2 = new Task("task2", "demo", Status.NEW, fiveMinutes, wrongStart);

        assertTrue(ttController.isTimeOverlapping(task2));
    }

    @Test
    public void shouldBeNoOverlapIfNoFields() {
        Task task1 = new Task("task1", "demo", Status.NEW, tenMinutes);
        Task task2 = new Task("task2", "demo", Status.NEW);

        ttController.add(task1);
        ttController.add(task1);

        assertFalse(ttController.isTimeOverlapping(task1));
        assertFalse(ttController.isTimeOverlapping(task2));
    }

    @Test
    public void shouldBeNoOverlap() {
        Task task1 = new Task("task1", "demo", Status.NEW, tenMinutes,
                epochTime);
        Task task2 = new Task("task2", "demo", Status.NEW, tenMinutes,
                epochTime.plusMinutes(tenMinutes));
        Task task3 = new Task("task3", "demo", Status.NEW, tenMinutes,
                epochTime.plusMinutes(-tenMinutes));

        assertFalse(ttController.isTimeOverlapping(task1));
        assertFalse(ttController.isTimeOverlapping(task2));
        assertFalse(ttController.isTimeOverlapping(task3));
    }

    @Test
    public void shouldNotAddEpic() {
        Epic epic = new Epic("epic", "demo", Status.NEW);

        ttController.add(epic);

        assertEquals(0, ttController.getPrioritizedTasks().size());
    }

    @Test
    public void shouldNotAddIfMissingTimeFields() {
        Task task1 = new Task("task1", "demo", Status.NEW);
        Task task2 = new Task("task2", "demo", Status.NEW, tenMinutes);

        ttController.add(task1);
        ttController.add(task2);

        assertEquals(0, ttController.getPrioritizedTasks().size());
    }

    @Test
    public void shouldAddInPriorityOrder() {
        Task[] tasks = {
                new Task("task5", "demo", Status.NEW, tenMinutes,
                        epochTime.plusMinutes(10000)),
                new Task("task4", "demo", Status.NEW, tenMinutes,
                        epochTime.plusMinutes(1000)),
                new Task("task3", "demo", Status.NEW, tenMinutes,
                        epochTime.plusMinutes(100)),
                new Task("task2", "demo", Status.NEW, tenMinutes,
                        epochTime.plusMinutes(10)),
                new Task("task1", "demo", Status.NEW, tenMinutes,
                        epochTime),
        };

        for (Task task : tasks) {
            ttController.add(task);
        }

        List<Task> priorityTasks = ttController.getPrioritizedTasks();

        for (int i = 0; i < priorityTasks.size(); i++) {
            assertEquals(tasks[tasks.length - i - 1], priorityTasks.get(i));
        }
    }

    @Test
    public void shouldSumDurationAndCalculateEndTimeWhenAddSubtasks() {
        TaskManager manager = Managers.getDefault();

        int epicId = 1;
        Epic epic = new Epic("epic", "demo", Status.NEW);
        manager.addEpic(epic);


        SubTask subTask1 = new SubTask("subtask1", "demo", Status.NEW, epicId, tenMinutes,
                epochTime);
        SubTask subTask2 = new SubTask("subtask2", "demo", Status.NEW, epicId, tenMinutes,
                epochTime.plusMinutes(tenMinutes));
        SubTask subTask3 = new SubTask("subtask3", "demo", Status.NEW, epicId, tenMinutes,
                epochTime.plusMinutes(tenMinutes * 2));

        Duration expectedDuration = Duration.ofMinutes(tenMinutes * 3);
        LocalDateTime expectedEndTime = epochTime.plus(expectedDuration);

        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2);
        manager.addSubTask(subTask3);

        assertEquals(expectedDuration, epic.getDuration());
        assertEquals(expectedEndTime, epic.getEndTime());
    }

    @Test
    public void shouldRecalculateDurationAndEndTimeWhenDeleteLastSubTask() {
        TaskManager manager = Managers.getDefault();

        int epicId = 1;
        Epic epic = new Epic("epic", "demo", Status.NEW);
        manager.addEpic(epic);


        SubTask subTask1 = new SubTask("subtask1", "demo", Status.NEW, epicId, tenMinutes,
                epochTime);
        SubTask subTask2 = new SubTask("subtask2", "demo", Status.NEW, epicId, tenMinutes,
                epochTime.plusMinutes(tenMinutes * 5));
        SubTask subTask3 = new SubTask("subtask3", "demo", Status.NEW, epicId, tenMinutes,
                epochTime.plusMinutes(tenMinutes * 10));

        Duration expectedDuration = Duration.ofMinutes(tenMinutes * 2);
        LocalDateTime expectedEndTime = epochTime.plusMinutes(tenMinutes * 5).plusMinutes(tenMinutes);

        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2);
        manager.addSubTask(subTask3);

        manager.deleteSubTask(4); //subtask3

        assertEquals(expectedDuration, epic.getDuration());
        assertEquals(expectedEndTime, epic.getEndTime());
    }

    @Test
    public void shouldRecalculateDurationAndEndTimeWhenDeleteFirstSubTask() {
        TaskManager manager = Managers.getDefault();

        int epicId = 1;
        Epic epic = new Epic("epic", "demo", Status.NEW);
        manager.addEpic(epic);


        SubTask subTask1 = new SubTask("subtask1", "demo", Status.NEW, epicId, tenMinutes,
                epochTime);
        SubTask subTask2 = new SubTask("subtask2", "demo", Status.NEW, epicId, tenMinutes,
                epochTime.plusMinutes(tenMinutes * 2));
        SubTask subTask3 = new SubTask("subtask3", "demo", Status.NEW, epicId, tenMinutes,
                epochTime.plusMinutes(tenMinutes));

        Duration expectedDuration = Duration.ofMinutes(tenMinutes * 2);
        LocalDateTime expectedStartTime = epochTime.plusMinutes(tenMinutes);

        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2);
        manager.addSubTask(subTask3);

        manager.deleteSubTask(2); //subtask1

        assertEquals(expectedDuration, epic.getDuration());
        assertEquals(expectedStartTime, epic.getStartTime());
    }

    @Test
    public void shouldBeNullWhenAllDeleted() {
        TaskManager manager = Managers.getDefault();

        int epicId = 1;
        Epic epic = new Epic("epic", "demo", Status.NEW);
        manager.addEpic(epic);


        SubTask subTask1 = new SubTask("subtask1", "demo", Status.NEW, epicId, tenMinutes,
                epochTime);
        SubTask subTask2 = new SubTask("subtask2", "demo", Status.NEW, epicId, tenMinutes,
                epochTime.plusMinutes(tenMinutes));
        SubTask subTask3 = new SubTask("subtask3", "demo", Status.NEW, epicId, tenMinutes,
                epochTime.plusMinutes(tenMinutes * 2));

        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2);
        manager.addSubTask(subTask3);

        manager.deleteSubTask(2); //subtask1
        manager.deleteSubTask(3); //subtask2
        manager.deleteSubTask(4); //subtask3

        assertNull(epic.getStartTime());
        assertNull(epic.getDuration());
        assertNull(epic.getEndTime());
    }

    @Test
    public void shouldFindSubTaskFromRightEpic() {
        TaskManager manager = Managers.getDefault();

        int epicId1 = 1;
        Epic epic = new Epic("epic1", "demo", Status.NEW);
        manager.addEpic(epic);

        int epicId2 = 2;
        Epic epic2 = new Epic("epic2", "demo", Status.NEW);
        manager.addEpic(epic2);


        SubTask subTask1 = new SubTask("subtask1", "demo", Status.NEW, epicId1, tenMinutes,
                epochTime);
        SubTask subTask2 = new SubTask("subtask2", "demo", Status.NEW, epicId1, tenMinutes,
                epochTime.plusMinutes(tenMinutes));
        SubTask subTask3 = new SubTask("subtask3", "demo", Status.NEW, epicId1, tenMinutes,
                epochTime.plusMinutes(tenMinutes * 2));

        manager.addSubTask(subTask1);
        manager.addSubTask(subTask2);
        manager.addSubTask(subTask3);

        SubTask subTask4 = new SubTask("subtask4", "demo", Status.NEW, epicId2, tenMinutes,
                epochTime.plusMinutes(tenMinutes * 3));
        SubTask subTask5 = new SubTask("subtask5", "demo", Status.NEW, epicId2, tenMinutes,
                epochTime.plusMinutes(tenMinutes * 4));
        SubTask subTask6 = new SubTask("subtask6", "demo", Status.NEW, epicId2, tenMinutes,
                epochTime.plusMinutes(tenMinutes * 5));

        manager.addSubTask(subTask4);
        manager.addSubTask(subTask5);
        manager.addSubTask(subTask6);

        manager.deleteSubTask(6); //subtask4

        assertEquals(subTask5.getStartTime(), epic2.getStartTime());
    }


    @Test
    public void shouldRemoveByObject() {
        Task task = new Task("task1", "demo", Status.NEW);
        task.setTaskId(1);

        ttController.add(task);

        ttController.remove(task);

        assertEquals(0, ttController.getPrioritizedTasks().size());
    }

    @Test
    public void shouldRemoveById() {
        Task task = new Task("task1", "demo", Status.NEW);
        task.setTaskId(1);

        ttController.add(task);

        ttController.remove(1);

        assertEquals(0, ttController.getPrioritizedTasks().size());
    }

    @Test
    public void shouldRemoveAll() {
        Task task = new Task("task1", "demo", Status.NEW, tenMinutes, epochTime);
        task.setTaskId(1);

        Task task1 = new Task("task2", "demo", Status.NEW, tenMinutes, epochTime.plusMinutes(tenMinutes));
        task.setTaskId(2);

        ttController.add(task);
        ttController.add(task1);

        ttController.clear();

        assertEquals(0, ttController.getPrioritizedTasks().size());
    }

    @Test
    public void shouldRemoveOnlyTasks() {
        Task task1 = new Task("task1", "demo", Status.NEW, tenMinutes,
                epochTime);
        Task task2 = new Task("task2", "demo", Status.NEW, tenMinutes,
                epochTime.plusMinutes(tenMinutes));

        int epicId = 3;
        Epic epic = new Epic("epic", "demo", Status.NEW);
        epic.setTaskId(epicId);

        SubTask subTask1 = new SubTask("subtask1", "demo", Status.NEW, epicId, tenMinutes,
                epochTime.plusMinutes(100));
        SubTask subTask2 = new SubTask("subtask2", "demo", Status.NEW, epicId, tenMinutes,
                epochTime.plusMinutes(1000));
        SubTask subTask3 = new SubTask("subtask3", "demo", Status.NEW, epicId, tenMinutes,
                epochTime.plusMinutes(10000));

        ttController.add(task1);
        ttController.add(task2);

        ttController.add(subTask1);
        ttController.add(subTask2);
        ttController.add(subTask3);

        ttController.removeTasks();

        int actualAmount = 0;
        for (Task task : ttController.getPrioritizedTasks()) {
            if (task.getType() == Type.TASK) {
                actualAmount++;
            }
        }

        assertEquals(0, actualAmount);
    }

    @Test
    public void shouldRemoveOnlySubTasks() {
        Task task1 = new Task("task1", "demo", Status.NEW, tenMinutes,
                epochTime);
        Task task2 = new Task("task2", "demo", Status.NEW, tenMinutes,
                epochTime.plusMinutes(tenMinutes));

        int epicId = 3;
        Epic epic = new Epic("epic", "demo", Status.NEW);
        epic.setTaskId(epicId);

        SubTask subTask1 = new SubTask("subtask1", "demo", Status.NEW, epicId, tenMinutes,
                epochTime.plusMinutes(100));
        SubTask subTask2 = new SubTask("subtask2", "demo", Status.NEW, epicId, tenMinutes,
                epochTime.plusMinutes(1000));
        SubTask subTask3 = new SubTask("subtask3", "demo", Status.NEW, epicId, tenMinutes,
                epochTime.plusMinutes(10000));

        ttController.add(task1);
        ttController.add(task2);

        ttController.add(subTask1);
        ttController.add(subTask2);
        ttController.add(subTask3);

        ttController.removeSubTasks();

        int actualAmount = 0;
        for (Task task : ttController.getPrioritizedTasks()) {
            if (task.getType() == Type.SUBTASK) {
                actualAmount++;
            }
        }

        assertEquals(0, actualAmount);
    }


    @Test
    public void shouldBeTrueWhenOverlapSameTimeTreeSearch() {
        Task task1 = new Task("task1", "demo", Status.NEW, tenMinutes, epochTime);
        ttController.add(task1);

        Task task2 = new Task("task2", "demo", Status.NEW, tenMinutes, epochTime);

        assertTrue(ttController.isTimeOverlappingWithTreeSearch(task2));
    }

    @Test
    public void shouldBeTrueWhenOverlapStartBeforeEndTreeSearch() {
        Task task1 = new Task("task1", "demo", Status.NEW, tenMinutes, epochTime);
        ttController.add(task1);

        LocalDateTime wrongStart = epochTime.plusMinutes(8); // за 2 минуты до конца таска1
        Task task2 = new Task("task2", "demo", Status.NEW, tenMinutes, wrongStart);

        assertTrue(ttController.isTimeOverlappingWithTreeSearch(task2));
    }

    @Test
    public void shouldBeTrueWhenOverlapEndAfterStartTreeSearch() {
        Task task1 = new Task("task1", "demo", Status.NEW, tenMinutes, epochTime);
        ttController.add(task1);

        long fiveMinutes = 5;
        Task task2 = new Task("task2", "demo", Status.NEW, fiveMinutes, epochTime.plusMinutes(-1));

        assertTrue(ttController.isTimeOverlappingWithTreeSearch(task2));
    }

    @Test
    public void shouldBeTrueWhenOverlapInsideTreeSearch() {
        Task task1 = new Task("task1", "demo", Status.NEW, tenMinutes, epochTime);
        ttController.add(task1);

        LocalDateTime wrongStart = epochTime.plusMinutes(1); // за 2 минуты до конца таска1
        long fiveMinutes = 5;
        Task task2 = new Task("task2", "demo", Status.NEW, fiveMinutes, wrongStart);

        assertTrue(ttController.isTimeOverlappingWithTreeSearch(task2));
    }

    @Test
    public void shouldBeNoOverlapIfNoFieldsTreeSearch() {
        Task task1 = new Task("task1", "demo", Status.NEW, tenMinutes);
        Task task2 = new Task("task2", "demo", Status.NEW);

        ttController.add(task1);
        ttController.add(task1);

        assertFalse(ttController.isTimeOverlappingWithTreeSearch(task1));
        assertFalse(ttController.isTimeOverlappingWithTreeSearch(task2));
    }

    @Test
    public void shouldBeNoOverlapTreeSearch() {
        Task task1 = new Task("task1", "demo", Status.NEW, tenMinutes,
                epochTime);
        Task task2 = new Task("task2", "demo", Status.NEW, tenMinutes,
                epochTime.plusMinutes(tenMinutes));
        Task task3 = new Task("task3", "demo", Status.NEW, tenMinutes,
                epochTime.plusMinutes(-tenMinutes));

        assertFalse(ttController.isTimeOverlappingWithTreeSearch(task1));
        assertFalse(ttController.isTimeOverlappingWithTreeSearch(task2));
        assertFalse(ttController.isTimeOverlappingWithTreeSearch(task3));
    }

}