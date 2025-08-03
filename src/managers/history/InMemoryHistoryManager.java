package managers.history;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryHistoryManager implements HistoryManager {
    private Node head;
    private Node tail;

    private final HashMap<Integer, Node> history = new HashMap<>();

    @Override
    public void addTask(Task task) {
        int id = task.getTaskId();
        remove(id);
        history.put(id, linkLast(task));
    }


    @Override
    public void remove(int id) {
        Node node = history.remove(id);
        if (node == null) {
            return;
        }
        removeNode(node);
    }

    @Override
    public List<Task> getHistory() {
        return getTasks();
    }

    private Node linkLast(Task task) {
        Node newNode = new Node(tail, task, null);

        if (tail == null) {
            // инициализируем голову, теперь она будет хранить всю последовательность
            head = newNode;
        } else {
            // добавляем новый узел к текущему хвосту
            tail.next = newNode;
        }
        tail = newNode; // переопределяем хвост

        return newNode;
    }

    private void removeNode(Node node) {
        Node next = node.next;
        Node prev = node.prev;

        if (prev == null) {
            // переопределяем голову на второй элемент последовательности
            head = next;
        } else {
            // привязываем предыдущий узел к последующему (минуя текущий)
            prev.next = next;
        }

        if (next == null) {
            // переопределяем хвост на предпоследний элемент последовательности
            tail = prev;
        } else {
            // привязываем к последующему узлу предыдущий (минуя текущий)
            next.prev = prev;
        }

    }

    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = head; // копия головы, чтобы избежать потери головы

        // проходим через всю последовательность и добавляем задачу в список
        while (current != null) {
            tasks.add(current.task);
            current = current.next;
        }

        return tasks;
    }
}
