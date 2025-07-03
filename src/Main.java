public class Main {

    public static void main(String[] args) {
        /*
        файл существует лишь для удобства тестирования и лучшего понимания работы программы
        цифра в имени каждой задачи отображает id задачи
        при удалении задачи в консоли дублируется оповещение об этом т.к метод также уведомляет об удалении
        */

        TaskManager taskManager = new TaskManager();

        Task task1 = new Task("task1", "demo", Status.NEW);
        Task task2 = new Task("task2", "demo", Status.NEW);

        taskManager.addTask(task1);
        taskManager.addTask(task2);

        System.out.println("\nДобавлено 2 задачи");
        taskManager.printTasksDev();

        Epic epic3 = new Epic("epic", "demo",Status.DONE);
        //намеренно указываем статус DONE, он все равно пересчитывается в требуемый
        taskManager.addEpic(epic3);

        System.out.println("\nДобавлен Эпик");
        taskManager.printTasksDev();
        /*
        пользователь не может знать id эпика,
        программа будет получать его благодаря клику на объект
        */
        SubTask subTask4 = new SubTask("subtask4", "demo", 3,Status.NEW);
        SubTask subTask5 = new SubTask("subtask5", "demo", 3,Status.NEW);

        taskManager.addSubTask(subTask4);
        taskManager.addSubTask(subTask5);

        System.out.println("\nДобавлены 2 подзадачи в эпик 3");
        taskManager.printTasksDev();

        SubTask replaceSubTask4 = new SubTask("replaceSubTask4", "demo", 3, Status.IN_PROGRESS);
        /*
        Предполагается, что мы обновляем уже существующий объект и знаем его id
        в данной ситуации приходится выставлять id вручную
        то же самое касается и установки статуса, все задачи создаются как новые,
         но при перезаписи у пользователя будет выбор установки статуса,
         при этом статус эпика поменять будет невозможно
        */
        replaceSubTask4.set_task_id(4);
        taskManager.updateSubTask(replaceSubTask4);

        System.out.println("\nОбновлена подзадача id=4");
        taskManager.printTasksDev();

        taskManager.deleteSubTask(5); // удаляем подзадачу с id 5

        System.out.println("\nПодзадача id=5 удалена");
        taskManager.printTasksDev();

        replaceSubTask4 = new SubTask("replaceSubTask4", "demo", 3,Status.DONE);
        replaceSubTask4.set_task_id(4);

        taskManager.updateSubTask(replaceSubTask4);

        System.out.println("\nОбновлена подзадача id=4");
        taskManager.printTasksDev();
        /*
        эпик получает новую подзадачу со статусом NEW
        поэтому он пересчитывает свой статус до IN_PROGRESS (одна подзадача NEW, одна DONE)
        */
        SubTask subTask6 = new SubTask("subtask6", "demo", 3,Status.NEW);
        taskManager.addSubTask(subTask6);

        System.out.println("\nДобавлена подзадача id=6 в эпик 3");
        taskManager.printTasksDev();

        System.out.println("\nПечатаются Эпики");
        taskManager.printEpics();
        System.out.println("\nПечатаются подзадачи эпика id=3");
        taskManager.printSubTasksFromEpic(3);

        taskManager.deleteEpic(3);

        System.out.println("\nЭпик id=3 и его подзадачи удалены");
        taskManager.printTasksDev();


    }
}
