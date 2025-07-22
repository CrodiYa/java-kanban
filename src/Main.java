import managers.*;
import model.*;

public class Main {

    public static void main(String[] args) {
        TaskManager inMemoryTaskManager = Managers.getDefault();

        for (int i = 0; i < 10; i++) {
            inMemoryTaskManager.addEpic(new Epic((i + 1) + "", (char) ('A' + i) + "", Status.NEW));
            inMemoryTaskManager.getEpic(i + 1);
        }


        System.out.println(inMemoryTaskManager.getHistory());

    }
}
