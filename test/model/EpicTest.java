package model;

import org.junit.jupiter.api.Test;
import util.Status;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

public class EpicTest {

    private final long tenMinutes = 10;
    private final Duration durationTenMinutes = Duration.ofMinutes(tenMinutes);
    private final LocalDateTime epochTime =
            LocalDateTime.of(1970, 1, 1, 0, 0, 0);

    @Test
    public void shouldBeEqualWhenIdEqual() {

        Epic epic1 = new Epic("test1", "test1", Status.NEW);
        Epic epic2 = new Epic("test2", "test2", Status.NEW);

        epic1.setTaskId(1); // устанавливаем одинаковые id
        epic2.setTaskId(1);

        assertEquals(epic1.getTaskId(), epic2.getTaskId(), "ids are not equal");
    }

    @Test
    public void shouldBeEqualWhenOverLoad() {
        Epic epic1 = new Epic("test1", "test1", Status.NEW);
        Epic epic2 = new Epic("test1", "test1", Status.NEW);

        epic1.setEpicDuration(durationTenMinutes);
        epic2.setEpicDuration(tenMinutes);

        assertEquals(epic1.getDuration(), epic2.getDuration());
    }

    @Test
    public void durationShouldBeEqualAfterAddition() {
        Duration expected = Duration.ofMinutes(20);
        Epic epic1 = new Epic("test1", "test1", Status.NEW);

        epic1.setEpicDuration(durationTenMinutes);
        assertNotNull(epic1.getDuration());

        epic1.setEpicDuration(durationTenMinutes);
        assertEquals(expected, epic1.getDuration());

    }

    @Test
    public void startTimeShouldBeSetWhenDateIsBefore() {
        LocalDateTime expected = epochTime.plusMinutes(-tenMinutes);
        Epic epic1 = new Epic("test1", "test1", Status.NEW);
        epic1.setStartTime(epochTime);

        epic1.setEpicStartTime(expected);

        assertEquals(expected, epic1.getStartTime());
    }

    @Test
    public void startTimeShouldNotBeSetWhenDateIsAfter() {
        LocalDateTime expected = epochTime.plusMinutes(tenMinutes);
        Epic epic1 = new Epic("test1", "test1", Status.NEW);
        epic1.setStartTime(epochTime);

        epic1.setEpicStartTime(expected);

        assertNotEquals(expected, epic1.getStartTime());
    }

    @Test
    public void endTimeShouldBeSetWhenDateIsAfter() {
        LocalDateTime expected = epochTime.plusMinutes(tenMinutes);
        Epic epic1 = new Epic("test1", "test1", Status.NEW);
        epic1.setEndTime(epochTime);

        epic1.setEpicEndTime(expected);

        assertEquals(expected, epic1.getEndTime());
    }

    @Test
    public void endTimeShouldNotBeSetWhenDateIsBefore() {
        LocalDateTime expected = epochTime.plusMinutes(-tenMinutes);
        Epic epic1 = new Epic("test1", "test1", Status.NEW);
        epic1.setEndTime(epochTime);

        epic1.setEpicEndTime(expected);

        assertNotEquals(expected, epic1.getStartTime());
    }
}