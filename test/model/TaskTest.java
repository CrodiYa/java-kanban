package model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TaskTest {

    @Test
    public void shouldBeEqualWhenIdEqual() {
        Task task1 = new Task("test1", "test1", Status.NEW);
        Task task2 = new Task("test2", "test2", Status.NEW);

        task1.setTaskId(1); // устанавливаем одинаковые id
        task2.setTaskId(1);

        Assertions.assertEquals(task1.getTaskId(), task2.getTaskId(), "ids are not equal");
    }

}