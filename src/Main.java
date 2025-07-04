import managers.*;
import model.*;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        taskManager.addTask(new Task("name", "description", Status.NEW));
    }
}
