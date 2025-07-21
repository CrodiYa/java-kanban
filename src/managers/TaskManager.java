package managers;

import model.Epic;
import model.SubTask;
import model.Task;

import java.util.ArrayList;
import java.util.List;

public interface TaskManager {
    void addTask(Task task);

    void addEpic(Epic epic);

    void addSubTask(SubTask subTask);

    Task getTask(int id);

    Epic getEpic(int id);

    SubTask getSubTask(int id);

    ArrayList<Task> getTasks();

    ArrayList<Epic> getEpics();

    ArrayList<SubTask> getSubTasks();

    ArrayList<SubTask> getSubTasksFromEpic(int id);

    void updateTask(Task task);

    void updateEpic(Epic epic);

    void updateSubTask(SubTask subTask);

    void deleteTask(int id);

    void clearTasks();

    void deleteEpic(int id);

    void clearEpics();

    void deleteSubTask(int id);

    void clearSubTasks();

    void updateHistory(Task task);

    List<Task> getHistory();
}
