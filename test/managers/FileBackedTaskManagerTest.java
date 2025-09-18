package managers;

import managers.filedbacked.FileBackedTaskManager;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import util.enums.Status;
import util.exceptions.ManagerLoadException;
import util.exceptions.ManagerSaveException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.*;

public class FileBackedTaskManagerTest {
    private File tempFile;
    private FileBackedTaskManager manager;

    @BeforeEach
    public void beforeEach() throws IOException {
        tempFile = Files.createTempFile("task", ".csv").toFile();
        manager = new FileBackedTaskManager(tempFile);
    }

    @Test
    public void shouldSaveAndLoad() {

        Task task1 = new Task("task1", "demo1", Status.NEW); // id=1
        manager.addTask(task1);

        Epic epic1 = new Epic("epic1", "demo1", Status.NEW); // id=2
        manager.addEpic(epic1);
        Epic epic2 = new Epic("epic2", "demo2", Status.NEW); // id=3
        manager.addEpic(epic2);

        SubTask subTask1 = new SubTask("subtask1", "demo1", Status.NEW, 2); //id=4
        manager.addSubTask(subTask1);

        Task task2 = new Task("task2", "demo2", Status.NEW); // id=5
        manager.addTask(task2);

        manager.deleteTask(1);

        FileBackedTaskManager manager1 = FileBackedTaskManager.loadFromFile(tempFile);

        assertNull(manager1.getTask(1));
        assertEquals(epic1, manager1.getEpic(2));
        assertEquals(epic2, manager1.getEpic(3));
        assertEquals(subTask1, manager1.getSubTask(4));
        assertEquals(subTask1, manager1.getSubTasksFromEpic(2).getFirst());
        assertEquals(task2, manager1.getTask(5));
        assertEquals(manager.getIdCount(), manager1.getIdCount());
    }

    @Test
    public void shouldThrowManagerSaveException() {
        manager = new FileBackedTaskManager(new File("blablabla/test.txt"));

        assertThrows(ManagerSaveException.class,
                () -> manager.addTask(new Task("title", "demo", Status.NEW)));
    }

    @Test
    public void shouldReturnEmptyManager() {
        FileBackedTaskManager manager1 = FileBackedTaskManager.loadFromFile(new File("resources/test.txt"));

        assertEquals(0, manager1.getIdCount());
        assertEquals(0, manager1.getTasks().size());
        assertEquals(0, manager1.getEpics().size());
        assertEquals(0, manager1.getSubTasks().size());
    }

    @Test
    public void shouldSupportUTF8() {
        String[] words = {
                "Hello", "World",
                "Привет", "Мир",
                "Selam", "Dünya",
                "你好", "世界",
                "👋", "🌎"
        };

        for (int i = 0; i < words.length; i += 2) {
            manager.addTask(new Task(words[i], words[i + 1], Status.NEW));
        }

        FileBackedTaskManager manager1 = FileBackedTaskManager.loadFromFile(tempFile);
        int id = 1;
        for (int i = 0; i < words.length; i += 2) {
            Task task = manager1.getTask(id++);
            assertEquals(words[i], task.getTitle());
            assertEquals(words[i + 1], task.getDescription());
        }
    }

    private void writeToTempFile(String line) {
        String header = "id,type,name,status,description,epic,duration,startTime";
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(tempFile))) {
            writer.write(header);
            writer.newLine();
            writer.write(line);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void shouldSkipWhenInvalidHeader() throws IOException {
        File tempfile1 = File.createTempFile("task1", "csv");
        assertThrows(ManagerLoadException.class, () -> {
            FileBackedTaskManager.loadFromFile(tempfile1);
        });
    }

    @Test
    public void shouldSkipWhenInvalidId() {
        writeToTempFile("INVALID,TASK,task1,NEW,demo,null,PT9M,1970-01-01 00:00:00.000");
        FileBackedTaskManager manager1 = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(0, manager1.getTasks().size());
    }

    @Test
    public void shouldSkipWhenEmptyId() {
        writeToTempFile("  ,TASK,task1,NEW,demo,null,PT9M,1970-01-01 00:00:00.000");
        FileBackedTaskManager manager1 = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(0, manager1.getTasks().size());
    }

    @Test
    public void shouldSkipWhenInvalidType() {
        writeToTempFile("1,INVALID,task1,NEW,demo,null,PT9M,1970-01-01 00:00:00.000");
        FileBackedTaskManager manager1 = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(0, manager1.getTasks().size());
    }

    @Test
    public void shouldSkipWhenInvalidStatus() {
        writeToTempFile("1,TASK,task1,INVALID,demo,null,PT9M,1970-01-01 00:00:00.000");
        FileBackedTaskManager manager1 = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(0, manager1.getTasks().size());
    }

    @Test
    public void shouldSkipWhenInvalidEpicId() {
        writeToTempFile("1,TASK,task1,NEW,demo,INVALID,PT9M,1970-01-01 00:00:00.000");
        FileBackedTaskManager manager1 = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(0, manager1.getTasks().size());
    }

    @Test
    public void shouldSkipWhenInvalidDuration() {
        writeToTempFile("1,TASK,task1,NEW,demo,null,INVALID,1970-01-01 00:00:00.000");
        FileBackedTaskManager manager1 = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(0, manager1.getTasks().size());
    }

    @Test
    public void shouldSkipWhenInvalidDateTime() {
        writeToTempFile("1,TASK,task1,NEW,demo,null,PT9M,INVALID");
        FileBackedTaskManager manager1 = FileBackedTaskManager.loadFromFile(tempFile);
        assertEquals(0, manager1.getTasks().size());
    }
}