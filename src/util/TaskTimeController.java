package util;

import model.Epic;
import model.SubTask;
import model.Task;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

/**
 * Контроллер для управления временными интервалами задач.
 * Обеспечивает хранение задач в порядке приоритета и проверку пересечений временных интервалов.
 * Приоритет задачи определяется по времени начала выполнения ({@code startTime}).
 *
 * <p>Класс предназначен для использования менеджерами задач для валидации временных промежутков
 * и обеспечения отсутствия конфликтов в расписании. Игнорирует попытки добавить эпики и задачи
 * без установленных временных параметров. Управляет временем начала и длительностью эпиков.
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

        return ceiling != null && ceiling.getStartTime().isBefore(task.getEndTime());
    }

    /**
     * Обновляет временные параметры эпика при добавлении подзадачи.
     *
     * <p>Выполняет следующие операции:
     * <ul>
     *   <li><b>Длительность:</b> Добавляет длительность подзадачи к общей длительности эпика
     *       с помощью {@link Epic#setEpicDuration(java.time.Duration)}. Если текущая длительность
     *       равна {@code null}, устанавливается длительность подзадачи.</li>
     *   <li><b>Время начала:</b> Устанавливает время начала эпика равным времени начала подзадачи
     *       с помощью {@link Epic#setEpicStartTime(LocalDateTime)}, если оно раньше текущего
     *       или если время начала эпика не установлено.</li>
     *   <li><b>Время окончания:</b> Устанавливает время окончания эпика равным времени окончания подзадачи
     *       с помощью {@link Epic#setEpicEndTime(LocalDateTime)}, если оно позже текущего
     *       или если время окончания эпика не установлено.</li>
     * </ul>
     * <p>Метод игнорирует подзадачи с отсутствующими временными параметрами ({@code null}).
     *
     * @param epic    эпик, параметры которого следует обновить
     * @param subTask подзадача, на основе которой обновляются параметры эпика
     * @implNote Метод вызывается при добавлении подзадачи
     */
    public void updateEpicTimeParams(Epic epic, SubTask subTask) {
        if (hasMissingTimeFields(subTask)) {
            return;
        }

        epic.setEpicDuration(subTask.getDuration());
        epic.setEpicStartTime(subTask.getStartTime());
        epic.setEpicEndTime(subTask.getEndTime());
    }

    /**
     * Обновляет временные параметры эпика после удаления подзадачи.
     *
     * <p>Выполняет следующие операции:
     * <ul>
     *   <li><b>Длительность:</b> Уменьшает общую длительность эпика на длительность удаляемой подзадачи
     *       с помощью {@link Epic#setEpicDuration(long)}.</li>
     *   <li><b>Время начала:</b> Находит самое раннее время начала среди валидных оставшихся подзадач
     *       (игнорируя задачи без временных параметров) и устанавливает его как время начала эпика.</li>
     *   <li><b>Время окончания:</b> Находит самое позднее время окончания среди валидных оставшихся подзадач
     *       и устанавливает его как время окончания эпика.</li>
     *   <li><b>Очистка параметров:</b> Если не осталось валидных подзадач с временными параметрами,
     *       сбрасывает все временные параметры эпика в {@code null}.</li>
     * </ul>
     *
     * @param epic     эпик, параметры которого следует обновить
     * @param subtask  удаляемая подзадача, длительность которой следует вычесть
     * @param subtasks список всех оставшихся подзадач эпика после удаления подзадачи
     * @implSpec Метод вызывается <b>после</b> удаления подзадачи
     * @apiNote Игнорирует подзадачи без установленных временных параметров ({@code duration} или {@code startTime})
     * @implNote Время начала ищется по {@code startTime}, время окончания - по {@code endTime}
     */
    public void updateEpicTimeParamsDeletion(Epic epic, SubTask subtask, List<SubTask> subtasks) {
        if (hasMissingTimeFields(subtask)) return;

        epic.setEpicDuration(-subtask.getDuration().toMinutes()); // в любом случае удаляем

        if (subtasks.isEmpty()) {
            epic.setStartTime(null);
            epic.setDuration(null);
            epic.setEndTime(null);
            return;
        }

        List<SubTask> validSubtasks = subtasks.stream()
                .filter(subTask -> !hasMissingTimeFields(subTask))
                .toList();

        if (!validSubtasks.isEmpty()) {
            validSubtasks.stream()
                    .max(Comparator.comparing(Task::getEndTime))
                    .ifPresent(
                            subTask -> epic.setEndTime(subTask.getEndTime()));

            validSubtasks.stream()
                    .min(Comparator.comparing(Task::getStartTime))
                    .ifPresent(
                            subTask -> epic.setStartTime(subTask.getStartTime()));

        } else {
            epic.setStartTime(null);
            epic.setDuration(null);
            epic.setEndTime(null);
        }


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
        if (hasMissingTimeFields(task)) {
            return;
        }
        timeSortedTasks.remove(task);
    }

    /**
     * Удаляет задачу или подзадачу из отсортированной коллекции по идентификатору.
     *
     * <p>Метод выполняет поиск элемента с указанным идентификатором и удаляет его
     * из внутренней отсортированной коллекции временных интервалов, если у него есть поля времени.
     *
     * <p><b>Не рекомендуется для общего использования</b> - метод следует использовать только
     * если нет доступа к объекту задачи и известен только {@code id}.
     *
     * @param id идентификатор задачи для удаления
     */
    public void remove(int id) {
        timeSortedTasks.removeIf(element -> element.getTaskId() == id && !hasMissingTimeFields(element));
    }

    /**
     * Удаляет все задачи типа {@link Type#TASK} из коллекции.
     *
     * @apiNote Используется для выборочной очистки обычных задач
     */
    public void removeTasks() {
        timeSortedTasks.removeIf(element -> element.getType() == Type.TASK);
    }

    /**
     * Удаляет все задачи типа {@link Type#SUBTASK} из коллекции.
     *
     * @apiNote Используется для выборочной очистки подзадач
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
     * @return неизменяемый {@code List<Task>} с задачами, отсортированными по времени
     */
    public List<Task> getPrioritizedTasks() {
        return List.copyOf(timeSortedTasks);
    }
}
