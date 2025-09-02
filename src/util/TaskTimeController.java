package util;

import model.Task;

import java.util.TreeSet;

/**
 * Контроллер для управления временными интервалами задач.
 * Обеспечивает хранение задач в порядке приоритета и проверку пересечений временных интервалов.
 * Приоритет задачи определяется по времени начала выполнения ({@code startTime}).
 *
 * <p>Класс предназначен для использования менеджерами задач для валидации временных промежутков
 * и обеспечения отсутствия конфликтов в расписании. Игнорирует попытки добавить эпики и задачи
 * без установленных временных параметров.
 */
public class TaskTimeController {

    private final TreeSet<Task> timeSortedTasks = new TreeSet<>();

    /**
     * Проверка пересечений временных интервалов с использованием {@code stream API}.
     * Использует метод anyMatch и сравнивает значение элемента со значением {@code task}.
     * Предварительно проверяет присутствие у {@code task} поля времени: {@code duration}, {@code startTime} методом
     * {@link #hasMissingTimeFields(Task)}.
     * Если одно из полей равно {@code null} - возвращает {@code false}.
     *
     * @param task задача для проверки
     * @return {@code true} если есть пересечение, {@code false} если временной промежуток доступен
     */
    public boolean isTimeOverlapping(Task task) {
        if (hasMissingTimeFields(task)) {
            return false;
        }
        return timeSortedTasks.stream()
                .anyMatch((element) ->
                        element.getEndTime().isAfter(task.getStartTime()) &&
                                element.getStartTime().isBefore(task.getEndTime()));
    }

    /**
     * Оптимизированная проверка пересечений временных интервалов с использованием TreeSet.
     * Использует алгоритм поиска соседних элементов {@code floor/ceiling} для O(log n) сложности.
     * Предварительно проверяет присутствие у {@code task} поля времени: {@code duration}, {@code startTime} методом
     * {@link #hasMissingTimeFields(Task)}.
     * Если одно из полей равно {@code null} - возвращает {@code false}.
     *
     * @param task задача для проверки
     * @return {@code true} если есть пересечение, {@code false} если временной промежуток доступен
     * @apiNote Готов к использованию, но в настоящее время не используется по требованию ТЗ (stream API)
     */
    public boolean isTimeOverlappingWithTreeSearch(Task task) {
        if (hasMissingTimeFields(task)) {
            return false;
        }
        Task floor = timeSortedTasks.floor(task);

        if (floor != null && floor.getEndTime().isAfter(task.getStartTime())) {
            return true;
        }

        Task ceiling = timeSortedTasks.ceiling(task);

        if (ceiling != null && ceiling.getStartTime().isBefore(task.getEndTime())) {
            return true;
        }

        return false;
    }

    private boolean hasMissingTimeFields(Task task) {
        return task.getDuration() == null || task.getStartTime() == null;
    }

    /**
     * Добавляет задачу в отсортированную коллекцию, если у нее установлены временные поля.
     * Задачи без времени начала или продолжительности игнорируются без выброса исключения.
     * Эпики игнорируются без выброса исключения.
     *
     * @param task задача для добавления
     * @implNote Метод молча игнорирует эпики и задачи с отсутствующими временными полями.
     */
    public void add(Task task) {
        if (task.getType() == Type.EPIC || hasMissingTimeFields(task)) {
            return;
        }
        timeSortedTasks.add(task);
    }

    public void remove(Task task) {
        timeSortedTasks.remove(task);
    }

    public void remove(int id) {
        timeSortedTasks.removeIf(element -> element.getTaskId() == id);
    }

    /**
     * Удаляет все задачи типа {@link Type#TASK} из коллекции.
     *
     * @apiNote Используется для выборочной очистки только обычных задач
     */
    public void removeTasks() {
        timeSortedTasks.removeIf(element -> element.getType() == Type.TASK);
    }

    /**
     * Удаляет все задачи типа {@link Type#SUBTASK} из коллекции.
     *
     * @apiNote Используется для выборочной очистки только подзадач
     */
    public void removeSubTasks() {
        timeSortedTasks.removeIf(element -> element.getType() == Type.SUBTASK);
    }

    public void clear() {
        timeSortedTasks.clear();
    }

    /**
     * Возвращает неизменяемую копию отсортированного набора задач.
     * Изменения в возвращаемой коллекции не влияют на внутреннее состояние.
     *
     * @return неизменяемый {@code TreeSet<Task>} с задачами, отсортированными по времени
     */
    public TreeSet<Task> getTimeSortedTasks() {
        return new TreeSet<>(timeSortedTasks);
    }
}
