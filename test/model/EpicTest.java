package model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class EpicTest {

    @Test
    public void shouldBeEqualWhenIdEqual() {

        Epic epic1 = new Epic("test1", "test1", Status.NEW);
        Epic epic2 = new Epic("test2", "test2", Status.NEW);

        epic1.setTaskId(1); // устанавливаем одинаковые id
        epic2.setTaskId(1);

        Assertions.assertEquals(epic1.getTaskId(), epic2.getTaskId(), "ids are not equal");
    }

}