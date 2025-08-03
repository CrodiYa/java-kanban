import managers.InMemoryTaskManager;
import managers.TaskManager;
import model.Epic;
import model.Status;
import model.SubTask;
import model.Task;

public class Main {

    public static void main(String[] args) {
        TaskManager manager = new InMemoryTaskManager();
        for (int i = 1; i <= 3; i++) {
            manager.addEpic(new Epic(i + "", "d", Status.NEW));
        }
        manager.addSubTask(new SubTask(4 + "", "d", Status.NEW,1));

        manager.getEpic(2);
        manager.getEpic(1);
        manager.getEpic(3);
        manager.getEpic(1);
        manager.getEpic(2);
        manager.getEpic(1);
        manager.getEpic(3);
        manager.getSubTask(4);

        manager.clearSubTasks();
        System.out.println(manager.getHistory());;
    }
}
