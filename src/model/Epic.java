package model;

import util.Status;
import util.Type;

import java.time.LocalDateTime;
import java.util.ArrayList;

public class Epic extends Task {

    private final ArrayList<Integer> subtaskIds;

    public Epic(String title, String description, Status status) {
        super(title, description, status);
        subtaskIds = new ArrayList<>();
    }

    public void addSubTask(SubTask subTask) {

        setDurationAndStartTime(subTask);
        subtaskIds.add(subTask.getTaskId());
    }

    /**
     * Обновляет длительность и время начала эпика на основе добавляемой подзадачи.
     *
     * <p><b>Длительность:</b> Суммируется длительность всех подзадач эпика.
     * Если текущая длительность равна {@code null}, устанавливается длительность подзадачи.
     *
     * <p><b>Время начала:</b> Устанавливается самое раннее время начала среди всех подзадач.
     * Если время начала подзадачи раньше текущего времени начала эпика или время начала эпика
     * не установлено, время начала эпика обновляется.
     *
     * <p>Метод игнорирует подзадачи с отсутствующими временными параметрами ({@code null}).
     *
     * @param subTask подзадача, на основе которой обновляются параметры эпика
     * @implNote Метод вызывается при добавлении каждой новой подзадачи к эпику
     */
    private void setDurationAndStartTime(SubTask subTask) {

        if (subTask.getDuration() == null) {
            return;
        }

        duration = duration == null ? subTask.getDuration() : duration.plus(subTask.getDuration());

        if (subTask.getStartTime() == null) {
            return;
        }

        LocalDateTime subtaskStartTime = subTask.getStartTime();

        if (startTime == null || startTime.isAfter(subtaskStartTime)) {
            // Обновляем время начала, если оно еще не установлено
            // или текущее начало позже начала подзадачи
            startTime = subtaskStartTime;
        }
    }

    public void deleteSubTask(int id) {
        subtaskIds.remove(Integer.valueOf(id));
    }

    public void clearSubtasks() {
        subtaskIds.clear();
        this.setStatus(Status.NEW);
    }

    public ArrayList<Integer> getSubtaskIds() {
        return subtaskIds;
    }

    public Type getType() {
        return Type.EPIC;
    }

    @Override
    public String toString() {
        return String.format("%s{id=%d, title=%s, description=%s, status=%s, subtasks=%s,\n    startTime=[%s], duration=[%s], endTime=[%s]}\n",
                this.getClass().getName(),
                this.taskId,
                this.title,
                this.description,
                this.status,
                this.subtaskIds,
                formatDateTime(startTime),
                duration,
                formatDateTime(getEndTime())
        );
    }

}
