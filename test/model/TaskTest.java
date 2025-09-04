package model;

import org.junit.jupiter.api.Test;
import util.Status;
import util.Type;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TaskTest {

    private final long tenMinutes = 10;
    private final Duration durationTenMinutes = Duration.ofMinutes(tenMinutes);
    private final LocalDateTime epochTime =
            LocalDateTime.of(1970, 1, 1, 0, 0, 0);


    @Test
    public void shouldBeEqualWhenIdEqual() {
        Task task1 = new Task("test1", "test1", Status.NEW);
        Task task2 = new Task("test2", "test2", Status.NEW);

        task1.setTaskId(1); // устанавливаем одинаковые id
        task2.setTaskId(1);

        assertEquals(task1.getTaskId(), task2.getTaskId(), "ids are not equal");
    }

    @Test
    public void shouldReturnCorrectType() {
        Type expectedType = Type.TASK;
        Task task1 = new Task("test1", "test1", Status.NEW);

        assertEquals(expectedType, task1.getType());
    }

    @Test
    public void shouldBeEqualWhenDurationSet() {
        Task task1 = new Task("task1", "demo", Status.NEW);
        Task task2 = new Task("task2", "demo", Status.NEW);

        task1.setDuration(durationTenMinutes);
        task2.setDuration(tenMinutes);

        assertEquals(task1.getDuration(), task2.getDuration());
    }

    @Test
    public void shouldBeEqualWhenReturnEndTime() {
        LocalDateTime expectedEndTime = epochTime.plus(durationTenMinutes);

        Task task1 = new Task("task1", "demo", Status.NEW, tenMinutes, epochTime);

        assertEquals(expectedEndTime, task1.getEndTime());
    }

    @Test
    public void shouldBeNullWhenNoDurationOrStartTime() {
        LocalDateTime expectedEndTime = epochTime.plus(durationTenMinutes);

        Task task1 = new Task("task1", "demo", Status.NEW, tenMinutes);
        Task task2 = new Task("task2", "demo", Status.NEW);

        assertNull(task1.getEndTime());
        assertNull(task2.getEndTime());
    }

}