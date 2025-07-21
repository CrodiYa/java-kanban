import managers.*;
import model.*;

public class Main {

    public static void main(String[] args) {
        InMemoryTaskManager inMemoryTaskManager = new InMemoryTaskManager();
        inMemoryTaskManager.addEpic(new Epic("epicID1", "demo", Status.NEW));
        System.out.println(inMemoryTaskManager.getEpics()); // статус NEW

        inMemoryTaskManager.addSubTask(new SubTask("subtaskID2", "demo", Status.NEW, 1));
        inMemoryTaskManager.addSubTask(new SubTask("subtaskID3", "demo", Status.DONE, 1));
        System.out.println(inMemoryTaskManager.getEpics()); // статус IN_PROGRESS


        inMemoryTaskManager.deleteSubTask(2); // удаляем subtask с id=2 status=NEW
        System.out.println(inMemoryTaskManager.getEpics()); // статус DONE

        inMemoryTaskManager.addSubTask(new SubTask("subtaskID4", "demo", Status.NEW, 1));
        System.out.println(inMemoryTaskManager.getEpics()); // статус снова IN_PROGRESS

        inMemoryTaskManager.deleteSubTask(3); // удаляем subtask с id=3 status=DONE
        System.out.println(inMemoryTaskManager.getEpics()); // статус NEW

    }
}
