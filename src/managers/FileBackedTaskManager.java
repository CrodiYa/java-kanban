package managers;

import managers.exceptions.ManagerSaveException;
import model.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static model.Type.*;

public class FileBackedTaskManager extends InMemoryTaskManager {

    /*первая служебная строка файла*/
    private final static String FIRST_LINE = "id,type,name,status,description,epic";

    private final File FILE;

    public FileBackedTaskManager(File file) {
        this.FILE = file;
    }

    public static FileBackedTaskManager loadFromFile(File file) {

        if (!file.exists()) {
            /* Если файла не существует, то возвращается пустой менеджер
             *  с возможностью создать файл и записывать в него*/
            return new FileBackedTaskManager(file);
        }

        FileBackedTaskManager manager = new FileBackedTaskManager(file);

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            reader.readLine(); // пропуск первой служебной строки

            /*конечный id, который будет присвоен idCount(далее счетчик) в manager*/
            int maxId = 0;

            while (reader.ready()) {
                String line = reader.readLine();
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",");

                int id = Integer.parseInt(parts[0]);

                maxId = Math.max(maxId, id); // сохраняется максимальный id

                /* менеджер сам присваивает id,
                 *  но не знает верного счетчика для каждого таска,
                 *  поэтому счетчик устанавливается на id - 1 (то есть предыдущее значение),
                 *  а уже в методе добавления счетчик увеличивается на один, устанавливая верное значение */
                manager.setIdCount(id - 1);

                Type type = Type.valueOf(parts[1]);
                String title = parts[2];
                Status status = Status.valueOf(parts[3]);
                String description = parts[4];

                if (type == TASK) {
                    manager.addTask(new Task(title, description, status));
                } else if (type == EPIC) {
                    manager.addEpic(new Epic(title, description, status));
                } else if (type == SUBTASK) {
                    manager.addSubTask(new SubTask(title, description, status, Integer.parseInt(parts[5])));
                } else {
                    System.out.println("Такой формат не существует");
                }
            }
            /*счетчик устанавливается на максимальный найденный id,
             * теперь отсчет id всех новых тасков будет от этого значения*/
            manager.setIdCount(maxId);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return manager;
    }

    private void save() {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE, StandardCharsets.UTF_8))) {
            bw.write(FIRST_LINE);
            bw.newLine();

            writeTasks(bw, super.getTasks()); // записываются таски
            writeTasks(bw, super.getEpics()); // записываются эпики
            writeSubTasks(bw, super.getSubTasks()); // записываются сабтаски

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения файла", e);
        }
    }

    private void writeTasks(BufferedWriter bw, ArrayList<? extends Task> tasks) throws IOException {
        /* Для записи в файл таска или эпика требуются методы,
         *  которые реализованы в родительском классе Task,
         *  следовательно, можно воспользоваться дженериком
         *  и не создавать два одинаковых метода для тасков и эпиков*/

        for (Task task : tasks) {
            String line = String.format("%s,%s,%s,%s,%s",
                    task.getTaskId(),
                    task.getType(),
                    task.getTitle(),
                    task.getStatus(),
                    task.getDescription()
            );

            bw.write(line);
            bw.newLine();
        }
    }

    private void writeSubTasks(BufferedWriter bw, ArrayList<SubTask> subTasks) throws IOException {
        /* Для записи сабтаска нам требуется метод getEpicId,
         *  поэтому метод writeSubTask отдельный*/
        for (SubTask subTask : subTasks) {
            String line = String.format("%s,%s,%s,%s,%s,%s",
                    subTask.getTaskId(),
                    subTask.getType(),
                    subTask.getTitle(),
                    subTask.getStatus(),
                    subTask.getDescription(),
                    subTask.getEpicId()
            );

            bw.write(line);
            bw.newLine();
        }
    }

    @Override
    public void addTask(Task task) {
        super.addTask(task);
        save();
    }

    @Override
    public void addEpic(Epic epic) {
        super.addEpic(epic);
        save();
    }

    @Override
    public void addSubTask(SubTask subTask) {
        super.addSubTask(subTask);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateEpic(Epic epic) {
        super.updateTask(epic);
        save();
    }

    @Override
    public void updateSubTask(SubTask subTask) {
        super.updateSubTask(subTask);
        save();
    }

    @Override
    public void deleteTask(int id) {
        super.deleteTask(id);
        save();
    }

    @Override
    public void deleteEpic(int id) {
        super.deleteEpic(id);
        save();
    }

    @Override
    public void deleteSubTask(int id) {
        super.deleteSubTask(id);
        save();
    }

    @Override
    public void clearTasks() {
        super.clearTasks();
        save();
    }

    @Override
    public void clearEpics() {
        super.clearEpics();
        save();
    }

    @Override
    public void clearSubTasks() {
        super.clearSubTasks();
        save();
    }
}
