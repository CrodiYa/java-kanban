package model;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class SubTaskTest {

    @Test
    void shouldBeEqualWhenIdEqual() {
        SubTask subtask1 = new SubTask("test1", "test1", Status.NEW, 2);
        SubTask subTask2 = new SubTask("test2", "test2", Status.NEW, 2);

        subtask1.setTaskId(1); // устанавливаем одинаковые id
        subTask2.setTaskId(1);

        Assertions.assertEquals(subtask1.getTaskId(), subTask2.getTaskId(), "ids are not equal");
    }

}