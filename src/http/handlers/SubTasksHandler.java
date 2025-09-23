package http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import model.SubTask;
import model.Task;
import util.exceptions.TaskNotFound;
import util.exceptions.TaskTimeOverlapException;
import util.http.JsonBuilder;
import util.http.RequestSegments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;

/**
 * Обработчик HTTP-запросов для управления подзадачами (SubTask).
 * <p>
 * Расширяет функциональность {@link TaskHandler} для работы с подзадачами.
 * Обеспечивает REST API для операций CRUD над подзадачами.
 * </p>
 *
 * <p><b>Поддерживаемые эндпоинты для подзадач:</b></p>
 * <table border="1">
 *   <tr><th>Метод</th><th>Путь</th><th>Действие</th></tr>
 *   <tr><td>HEAD</td><td>/subtasks или /subtasks/{id}</td><td>Получить заголовки</td></tr>
 *   <tr><td>GET</td><td>/subtasks</td><td>Получить все подзадачи</td></tr>
 *   <tr><td>GET</td><td>/subtasks/{id}</td><td>Получить подзадачу по Id</td></tr>
 *   <tr><td>POST</td><td>/subtasks</td><td>Создать новую подзадачу</td></tr>
 *   <tr><td>POST</td><td>/subtasks/{id}</td><td><b>Запрещено</b> - метод не разрешен</td></tr>
 *   <tr><td>DELETE</td><td>/subtasks</td><td>Удалить все подзадачи</td></tr>
 *   <tr><td>DELETE</td><td>/subtasks/{id}</td><td>Удалить подзадачу по Id</td></tr>
 * </table>
 *
 * <p><b>Формат JSON для подзадачи:</b></p>
 * <pre>
 * {
 *   "taskId": 0,           // 0 для создания, >0 для обновления
 *   "title": "string",     // обязательное поле
 *   "description": "string", // обязательное поле
 *   "status": "NEW|IN_PROGRESS|DONE", // обязательное поле
 *   "epicId": 123,         // обязательное поле - ID родительского эпика
 *   "duration": 10,        // продолжительность в минутах (опционально)
 *   "startTime": "1970-01-01T00:00:00.000" // дата и время ISO (опционально)
 * }
 * </pre>
 *
 * <p><b>Обработка ошибок:</b></p>
 * <ul>
 *   <li>400 - Неверный формат JSON или отсутствуют обязательные поля</li>
 *   <li>404 - Подзадача или родительский эпик не найдены или неверный путь</li>
 *   <li>405 - Метод не разрешен для ресурса</li>
 *   <li>406 - Пересечение временных интервалов с существующими задачами</li>
 *   <li>500 - Внутренняя ошибка сервера</li>
 * </ul>
 */
public class SubTasksHandler extends TaskHandler {

    public SubTasksHandler(TaskManager manager, Gson gson, JsonBuilder jsonBuilder) {
        super(manager, gson, jsonBuilder);
    }

    /**
     * Обрабатывает HTTP-метод GET для подзадач.
     * <p>
     * В зависимости от наличия идентификатора:
     * </p>
     * <ul>
     *   <li>Без ID: возвращает список всех подзадач</li>
     *   <li>С ID: возвращает конкретную подзадачу</li>
     * </ul>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @throws IOException если возникает ошибка при отправке ответа
     */
    @Override
    protected void handleGet(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (segments.id() == 0) {
            sendText(exchange, gson.toJson(manager.getSubTasks()), 200);
        } else {
            SubTask subtask = manager.getSubTask(segments.id());
            if (subtask != null) {
                sendText(exchange, gson.toJson(subtask), 200);
            } else {
                sendNotFound(exchange, jsonBuilder.notFound("SubTask with id '" + segments.id() + "'not found"));
            }
        }
    }

    /**
     * Обрабатывает HTTP-метод POST для подзадач.
     * <p>
     * Создает новую подзадачу или обновляет существующую в зависимости от taskId:
     * </p>
     * <ul>
     *   <li>taskId = 0: создание новой подзадачи</li>
     *   <li>taskId > 0: обновление существующей подзадачи</li>
     * </ul>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @throws IOException              если возникает ошибка при отправке ответа
     * @throws TaskNotFound             если родительский эпик или подзадача для обновления не существует
     * @throws TaskTimeOverlapException если обнаружено пересечение временных интервалов
     */
    @Override
    protected void handlePost(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (isPostForbidden(exchange, segments)) {
            return;
        }
        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);

        if (body.isBlank()) {
            sendText(exchange, jsonBuilder.badRequest("Body is required"), 400);
        }

        try {
            SubTask subTask = gson.fromJson(body, SubTask.class);
            isTaskValid(subTask);

            if (subTask.getTaskId() == 0) {
                manager.addSubTask(subTask);
            } else {
                manager.updateSubTask(subTask);
            }
            sendText(exchange, gson.toJson(manager.getSubTaskWithoutHistory(subTask.getTaskId())), 201);
        } catch (TaskNotFound e) {
            sendNotFound(exchange, jsonBuilder.notFound(e.getMessage()));
        } catch (DateTimeParseException e) {
            sendText(exchange, jsonBuilder.badRequest("Invalid date format"), 400);
        } catch (TaskTimeOverlapException e) {
            sendHasOverlaps(exchange, jsonBuilder.hasOverlaps());
        }

    }

    /**
     * Обрабатывает HTTP-метод DELETE для подзадач.
     * <p>
     * В зависимости от наличия идентификатора:
     * </p>
     * <ul>
     *   <li>Без ID: удаляет все подзадачи</li>
     *   <li>С ID: удаляет конкретную подзадачу</li>
     * </ul>
     *
     * <p><b>Удаление подзадачи автоматически обновляет статус и поля времени родительского эпика.</b></p>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @throws IOException если возникает ошибка при отправке ответа
     */
    @Override
    protected void handleDelete(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (segments.id() == 0) {
            manager.clearSubTasks();
            sendText(exchange, jsonBuilder.message("Subtasks were deleted"), 200);
        } else {
            manager.deleteSubTask(segments.id());
            sendText(exchange, jsonBuilder.message("Subtask: '" + segments.id() + "' was deleted"), 200);
        }
    }

    /**
     * Проверяет корректность ресурса в запросе.
     * <p>
     * Убеждается, что запрос адресован именно подзадачам ("subtasks").
     * </p>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @return true если ресурс неверен (не "subtasks"), иначе false
     * @throws IOException если возникает ошибка при отправке ответа 404
     */
    @Override
    protected boolean validateResources(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (!"subtasks".equals(segments.resource())) {
            sendNotFound(exchange, jsonBuilder.resourceNotFound());
            return true;
        }
        return false;
    }

    /**
     * Проверяет валидность объекта подзадачи.
     * <p>
     * Проверяет наличие обязательных полей для подзадачи:
     * </p>
     * <ul>
     *   <li>title - заголовок подзадачи</li>
     *   <li>description - описание подзадачи</li>
     *   <li>status - статус подзадачи</li>
     *   <li>epicId - идентификатор родительского эпика (должен быть > 0)</li>
     * </ul>
     *
     * <p><b>Отличие от TaskHandler:</b> для подзадач дополнительно проверяется наличие epicId.</p>
     *
     * @param task подзадача для валидации (приводится к SubTask)
     * @throws JsonSyntaxException если отсутствуют обязательные поля или epicId = 0
     */
    @Override
    protected void isTaskValid(Task task) {
        SubTask subTask = (SubTask) task;
        if (subTask.getTitle() == null ||
                subTask.getDescription() == null ||
                subTask.getStatus() == null ||
                subTask.getEpicId() == 0) {
            throw new JsonSyntaxException("Subtask has invalid fields");
        }
    }

}
