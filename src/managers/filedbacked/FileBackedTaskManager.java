package managers.filedbacked;

import managers.InMemoryTaskManager;
import model.Epic;
import model.SubTask;
import model.Task;
import util.enums.Status;
import util.enums.Type;
import util.exceptions.ManagerLoadException;
import util.exceptions.ManagerSaveException;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static managers.filedbacked.ParserHelper.*;
import static util.enums.CsvField.*;
import static util.enums.Type.*;

/**
 * Менеджер задач с сохранением состояния в файл типа csv.
 *
 * <p>Расширяет функциональность {@link InMemoryTaskManager}, добавляя возможность
 * сохранения и восстановления состояния задач из файла. Автоматически сохраняет
 * изменения после каждой операции модификации данных.
 *
 * <p>Формат файла данных:
 * <ul>
 *   <li>Первая строка: заголовок с названиями полей</li>
 *   <li>Последующие строки: данные задач в CSV-формате</li>
 *   <li>Кодировка: UTF-8</li>
 * </ul>
 *
 * <p>Поддерживаемые операции:
 * <ul>
 *   <li>Автоматическое сохранение при изменении данных</li>
 *   <li>Восстановление состояния из файла при запуске</li>
 *   <li>Создание нового файла если он не существует</li>
 *   <li>Обработка ошибок ввода-вывода через {@link ManagerSaveException}</li>
 * </ul>
 *
 * @implSpec Все операции модификации данных автоматически вызывают сохранение
 * @see InMemoryTaskManager
 * @see ManagerSaveException
 * @see ParserHelper
 */
public class FileBackedTaskManager extends InMemoryTaskManager {

    private final File filename;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");

    public FileBackedTaskManager(File filename) {
        this.filename = filename;
    }

    /**
     * Восстанавливает состояние менеджера задач из файла.
     *
     * <p>Если файл не существует, возвращает пустой менеджер с возможностью
     * последующего сохранения данных в указанный файл.
     *
     * <p>Процесс восстановления:
     * <ol>
     *   <li>Проверяет существование файла</li>
     *   <li>Парсит каждую строку с данными задачи</li>
     *   <li>Восстанавливает задачи, эпики и подзадачи</li>
     *   <li>Устанавливает корректный счетчик идентификаторов</li>
     * </ol>
     *
     * @param filename файл для восстановления данных; должен быть валидным файлом
     * @return восстановленный менеджер задач или новый пустой менеджер
     * @throws ManagerLoadException если файл содержит некорректные данные
     * @apiNote Автоматически восстанавливает корректный счетчик идентификаторов
     * @implNote Использует {@link ParserHelper} для парсинга данных
     */
    public static FileBackedTaskManager loadFromFile(File filename) throws ManagerLoadException {

        if (!filename.exists()) {
            /* Если файла не существует, то возвращается пустой менеджер
             *  с возможностью создать файл и записывать в него*/
            return new FileBackedTaskManager(filename);
        }

        FileBackedTaskManager manager = new FileBackedTaskManager(filename);

        try (BufferedReader reader = new BufferedReader(new FileReader(filename, StandardCharsets.UTF_8))) {

            String header = reader.readLine();
            validateHeader(header);
            int maxId = 0; // будет присвоен счетчику менеджера

            while (reader.ready()) {
                String line = reader.readLine();
                if (line.isBlank()) {
                    continue;
                }
                String[] parts = line.split(",");
                try {
                    int id = parseInteger(parts[ID.get()]);
                    Type type = parseType(parts[TYPE.get()]);
                    String title = parts[TITLE.get()];
                    Status status = parseStatus(parts[STATUS.get()]);
                    String description = parts[DESCRIPTION.get()];
                    int epicId = parseOptionalInteger(parts[EPIC_ID.get()]);
                    long maybeDuration = parseOptionalDuration(parts[DURATION.get()])
                            .map(Duration::toMinutes)
                            .orElse(-1L);
                    LocalDateTime startTime = parseOptionalDateTime(parts[START_TIME.get()], formatter);

                    maxId = Math.max(maxId, id);
                    manager.setIdCount(id - 1); // менеджер сам присвоит id, устанавливаем счетчик на предыдущий

                    if (type == TASK) {
                        manager.addTask(new Task(title, description, status, maybeDuration, startTime));
                    } else if (type == EPIC) {
                        manager.addEpic(new Epic(title, description, status));
                    } else if (type == SUBTASK) {
                        manager.addSubTask(new SubTask(title, description, status, epicId, maybeDuration, startTime));
                    }
                } catch (ManagerLoadException e) {
                    e.printStackTrace();
                }
            }

            manager.setIdCount(maxId);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return manager;
    }

    /**
     * Сохраняет текущее состояние менеджера в файл.
     *
     * <p>Формат сохранения:
     * <ol>
     *   <li>Записывает заголовок с названиями полей</li>
     *   <li>Записывает все задачи в CSV формате</li>
     *   <li>Записывает все эпики в CSV формате</li>
     *   <li>Записывает все подзадачи в CSV формате</li>
     * </ol>
     *
     * @throws ManagerSaveException если произошла ошибка записи в файл
     * @implNote Метод вызывается автоматически после каждой операции модификации
     * @see #writeTasks(BufferedWriter, List)
     * @see #writeSubTasks(BufferedWriter, List)
     */
    private void save() {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(filename, StandardCharsets.UTF_8))) {
            bw.write("id,type,name,status,description,epic,duration,startTime"); //первая служебная строка файла
            bw.newLine();

            writeTasks(bw, super.getTasks()); // записываются таски
            writeTasks(bw, super.getEpics()); // записываются эпики
            writeSubTasks(bw, super.getSubTasks()); // записываются подзадачи

        } catch (IOException e) {
            throw new ManagerSaveException("Ошибка сохранения файла", e);
        }
    }

    private void writeTasks(BufferedWriter bw, List<? extends Task> tasks) throws IOException {

        for (Task task : tasks) {
            String startTime = task.getStartTime() == null ? "null" : task.getStartTime().format(formatter);

            String line = String.format("%s,%s,%s,%s,%s,null,%s,%s",
                    task.getTaskId(),
                    task.getType(),
                    task.getTitle(),
                    task.getStatus(),
                    task.getDescription(),
                    task.getDuration(),
                    startTime
            );

            bw.write(line);
            bw.newLine();
        }
    }

    private void writeSubTasks(BufferedWriter bw, List<SubTask> subTasks) throws IOException {

        for (SubTask subTask : subTasks) {
            String startTime = subTask.getStartTime() == null ? "null" : subTask.getStartTime().format(formatter);

            String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s",
                    subTask.getTaskId(),
                    subTask.getType(),
                    subTask.getTitle(),
                    subTask.getStatus(),
                    subTask.getDescription(),
                    subTask.getEpicId(),
                    subTask.getDuration(),
                    startTime
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
        super.updateEpic(epic);
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

    @Override
    public void clearSubTasksFromEpic(int id) {
        super.clearSubTasks();
        save();
    }
}
