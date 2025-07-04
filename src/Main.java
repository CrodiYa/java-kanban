import managers.*;
import model.*;

public class Main {

    public static void main(String[] args) {
        TaskManager taskManager = new TaskManager();
        taskManager.addEpic(new Epic("epicID1", "demo", Status.NEW));
        System.out.println(taskManager.getEpics()); // статус NEW

        taskManager.addSubTask(new SubTask("subtaskID2", "demo", Status.NEW, 1));
        taskManager.addSubTask(new SubTask("subtaskID3", "demo", Status.DONE, 1));
        System.out.println(taskManager.getEpics()); // статус IN_PROGRESS


        taskManager.deleteSubTask(2); // удаляем subtask с id=2 status=NEW
        System.out.println(taskManager.getEpics()); // статус DONE

        taskManager.addSubTask(new SubTask("subtaskID4", "demo", Status.NEW, 1));
        System.out.println(taskManager.getEpics()); // статус снова IN_PROGRESS

        taskManager.deleteSubTask(3); // удаляем subtask с id=3 status=DONE
        System.out.println(taskManager.getEpics()); // статус NEW

    }
}
