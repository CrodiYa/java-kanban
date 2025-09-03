package model;

import util.Status;
import util.Type;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Epic extends Task {

    private final ArrayList<Integer> subtaskIds;

    public Epic(String title, String description, Status status) {
        super(title, description, status);
        subtaskIds = new ArrayList<>();
    }

    public void addSubTask(SubTask subTask) {
        subtaskIds.add(subTask.getTaskId());
    }

    public void deleteSubTask(int id) {
        subtaskIds.remove(Integer.valueOf(id));
    }

    public void clearSubtasks() {
        subtaskIds.clear();
        this.setStatus(Status.NEW);
        this.duration = null;
        this.startTime = null;
    }

    public List<Integer> getSubtaskIds() {
        return List.copyOf(subtaskIds);
    }

    public Type getType() {
        return Type.EPIC;
    }

    /**
     * Устанавливает или добавляет длительность эпика в минутах.
     *
     * <p>Если текущая длительность эпика равна {@code null}, устанавливает длительность
     * равной указанному количеству минут. Если длительность уже установлена, добавляет
     * указанное количество минут к существующей длительности.
     *
     * @param durationInMinutes длительность в минутах для добавления к эпику;
     */
    public void setEpicDuration(long durationInMinutes) {
        this.duration = this.duration == null ? Duration.ofMinutes(durationInMinutes) : this.duration.plusMinutes(durationInMinutes);
    }

    /**
     * Устанавливает или добавляет длительность эпика.
     *
     * <p>Если текущая длительность эпика равна {@code null}, устанавливает длительность
     * равной указанной длительности. Если длительность уже установлена, добавляет
     * указанную длительность к существующей.
     *
     * @param duration длительность для добавления к эпику
     * @apiNote Предпочтительный метод для работы с временными интервалами
     */
    public void setEpicDuration(Duration duration) {
        this.duration = this.duration == null ? duration : this.duration.plus(duration);
    }

    /**
     * Устанавливает или обновляет время начала эпика.
     *
     * <p>Устанавливает время начала эпика, если оно еще не установлено ({@code null}).
     * Если время начала уже установлено, обновляет его только если новое время начала
     * раньше текущего (минимальное время среди всех подзадач).
     *
     * @param startTime время начала для установки или сравнения;
     */
    public void setEpicStartTime(LocalDateTime startTime) {
        if (this.startTime == null || this.startTime.isAfter(startTime)) {
            this.startTime = startTime;
        }
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
