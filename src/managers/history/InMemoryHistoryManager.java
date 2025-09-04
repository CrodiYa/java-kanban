package managers.history;

import model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Менеджер истории просмотров задач, реализованный на двусвязном списке.
 * Данные хранятся в оперативной памяти.
 *
 * <p>Обеспечивает хранение истории просмотров задач с соблюдением порядка просмотра
 * и быстрым доступом к элементам через хэш-таблицу.
 *
 * <p>Реализация использует комбинацию HashMap для быстрого поиска узлов
 * и двусвязного списка для поддержания порядка элементов.
 *
 * @apiNote Все операции выполняются за время O(1), кроме {@link #getHistory()} - O(n)
 */
public class InMemoryHistoryManager implements HistoryManager {
    private Node head;
    private Node tail;

    private final HashMap<Integer, Node> history = new HashMap<>();

    @Override
    public void addTask(Task task) {
        if (task == null) {
            return;
        }
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

    /**
     * Добавляет задачу в конец двусвязного списка.
     *
     * <p>Создает новый узел для задачи и добавляет его в хвост списка.
     *
     * @param task задача для добавления в список
     * @return созданный узел, содержащий задачу
     */
    private Node linkLast(Task task) {
        Node newNode = new Node(tail, task, null);

        if (tail == null) {
            // инициализируем голову, теперь она будет хранить всю последовательность
            head = newNode;
        } else {
            // добавляем новый узел к текущему хвосту
            tail.setNext(newNode);
        }
        tail = newNode; // переопределяем хвост

        return newNode;
    }

    /**
     * Удаляет узел из двусвязного списка.
     * <p>Обрабатывает все возможные случаи:
     * <ul>
     *   <li>Удаление головного узла</li>
     *   <li>Удаление хвостового узла</li>
     *   <li>Удаление узла из середины списка</li>
     * </ul>
     *
     * @param node узел для удаления
     */
    private void removeNode(Node node) {
        Node next = node.getNext();
        Node prev = node.getPrev();

        if (prev == null) {
            // переопределяем голову на второй элемент последовательности
            head = next;
        } else {
            // привязываем предыдущий узел к последующему (минуя текущий)
            prev.setNext(next);
        }

        if (next == null) {
            // переопределяем хвост на предпоследний элемент последовательности
            tail = prev;
        } else {
            // привязываем к последующему узлу предыдущий (минуя текущий)
            next.setPrev(prev);
        }

    }

    /**
     * Возвращает список задач в порядке их просмотра.
     *
     * <p>Выполняет обход двусвязного списка от головы к хвосту и собирает все задачи
     * в список. Порядок элементов соответствует порядку просмотра (от старых к новым).
     *
     * @return неизменяемый список задач в порядке просмотра
     * @apiNote Возвращаемый список является копией, изменения не влияют на внутреннее состояние
     */
    private List<Task> getTasks() {
        List<Task> tasks = new ArrayList<>();
        Node current = head; // копия головы, чтобы избежать потери

        while (current != null) {
            tasks.add(current.getTask());
            current = current.getNext();
        }
        return tasks;
    }
}
