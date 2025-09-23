package http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import model.Task;
import util.exceptions.TaskNotFound;
import util.exceptions.TaskTimeOverlapException;
import util.http.JsonBuilder;
import util.http.RequestSegments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;

/**
 * Обработчик HTTP-запросов для управления задачами (Task).
 * <p>
 * Обеспечивает REST API для операций CRUD над задачами:
 * </p>
 * <ul>
 *   <li>Создание новых задач</li>
 *   <li>Получение задач (всех или по идентификатору)</li>
 *   <li>Обновление существующих задач</li>
 *   <li>Удаление задач (всех или по идентификатору)</li>
 * </ul>
 *
 * <p><b>Поддерживаемые эндпоинты:</b></p>
 * <table>
 *   <tr><th>Метод</th><th>Путь</th><th>Действие</th></tr>
 *   <tr><td>HEAD</td><td>/tasks или /tasks/{id}</td><td>Получить заголовки</td></tr>
 *   <tr><td>GET</td><td>/tasks</td><td>Получить все задачи</td></tr>
 *   <tr><td>GET</td><td>/tasks/{id}</td><td>Получить задачу по Id</td></tr>
 *   <tr><td>POST</td><td>/tasks</td><td>Создать новую задачу</td></tr>
 *   <tr><td>POST</td><td>/tasks/{id}</td><td><b>Запрещено</b> - метод не разрешен</td></tr>
 *   <tr><td>DELETE</td><td>/tasks</td><td>Удалить все задачи</td></tr>
 *   <tr><td>DELETE</td><td>/tasks/{id}</td><td>Удалить задачу по Id</td></tr>
 *   <tr><td>OPTIONS</td><td>/tasks или /tasks/{id}</td><td>Получить разрешенные методы</td></tr>
 * </table>
 *
 * <p><b>Формат JSON для задачи:</b></p>
 * <pre>
 * {
 *   "taskId": 0,           // 0 для создания, >0 для обновления
 *   "title": "string",     // обязательное поле
 *   "description": "string", // обязательное поле
 *   "status": "NEW|IN_PROGRESS|DONE", // обязательное поле
 *   "duration": 10,        // продолжительность в минутах (опционально)
 *   "startTime": "1970-01-01T00:00:00.000" // дата и время ISO (опционально)
 * }
 * </pre>
 *
 * <p><b>Обработка ошибок:</b></p>
 * <ul>
 *   <li>400 - Неверный формат JSON или отсутствуют обязательные поля</li>
 *   <li>404 - Задача не найдена или неверный путь</li>
 *   <li>405 - Метод не разрешен для ресурса</li>
 *   <li>406 - Пересечение временных интервалов с существующими задачами</li>
 *   <li>500 - Внутренняя ошибка сервера</li>
 * </ul>
 *
 * @see java.time.format.DateTimeFormatter#ISO_LOCAL_DATE_TIME
 */
public class TaskHandler extends BaseHttpHandler {

    public TaskHandler(TaskManager manager, Gson gson, JsonBuilder jsonBuilder) {
        super(manager, gson, jsonBuilder);
    }

    /**
     * Обрабатывает HTTP-метод OPTIONS для задач.
     * <p>
     * Возвращает разрешенные методы в зависимости от контекста:
     * </p>
     * <ul>
     *   <li>Для /tasks или /subtasks: GET, POST, DELETE, OPTIONS, HEAD</li>
     *   <li>Для /tasks/{id} или /subtasks/{id}: GET, DELETE, OPTIONS, HEAD</li>
     * </ul>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @throws IOException если возникает ошибка при отправке ответа
     */
    @Override
    protected void handleOptions(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (segments.id() == 0) {
            exchange.getResponseHeaders().add("Allow", "GET, POST, DELETE, OPTIONS, HEAD");
        } else {
            exchange.getResponseHeaders().add("Allow", "GET, DELETE, OPTIONS, HEAD");
        }
        exchange.sendResponseHeaders(204, -1);
    }

    /**
     * Обрабатывает HTTP-метод GET для задач.
     * <p>
     * В зависимости от наличия идентификатора:
     * </p>
     * <ul>
     *   <li>Без Id: возвращает список всех задач</li>
     *   <li>С Id: возвращает конкретную задачу</li>
     * </ul>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @throws IOException если возникает ошибка при отправке ответа
     */
    @Override
    protected void handleGet(HttpExchange exchange, RequestSegments segments) throws IOException {

        if (segments.id() == 0) {
            sendText(exchange, gson.toJson(manager.getTasks()), 200);
        } else {
            Task task = manager.getTask(segments.id());
            if (task != null) {
                sendText(exchange, gson.toJson(task), 200);
            } else {
                sendNotFound(exchange, jsonBuilder.notFound("Task with id '" + segments.id() + "'not found"));
            }
        }
    }

    /**
     * Обрабатывает HTTP-метод POST для задач.
     * <p>
     * Создает новую задачу или обновляет существующую в зависимости от taskId:
     * </p>
     * <ul>
     *   <li>taskId = 0: создание новой задачи</li>
     *   <li>taskId > 0: обновление существующей задачи</li>
     * </ul>
     * <p><b>Возвращает задачу в теле ответа</b></p>
     *
     * <p><b>Валидация:</b></p>
     * <ul>
     *   <li>Проверка </li>
     *   <li>Проверка наличия обязательных полей</li>
     *   <li>Проверка формата даты и времени</li>
     *   <li>Проверка на пересечение временных интервалов</li>
     * </ul>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @throws IOException если возникает ошибка при отправке ответа
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
            Task task = gson.fromJson(body, Task.class);
            isTaskValid(task);

            if (task.getTaskId() == 0) {
                manager.addTask(task);
            } else {
                manager.updateTask(task);
            }
            sendText(exchange, gson.toJson(manager.getTaskWithoutHistory(task.getTaskId())), 201);
        } catch (TaskNotFound e) {
            sendNotFound(exchange, jsonBuilder.notFound(e.getMessage()));
        } catch (DateTimeParseException e) {
            sendText(exchange, jsonBuilder.badRequest("Invalid date format"), 400);
        } catch (TaskTimeOverlapException e) {
            sendHasOverlaps(exchange, jsonBuilder.hasOverlaps());
        }
    }

    /**
     * Обрабатывает HTTP-метод DELETE для задач.
     * <p>
     * В зависимости от наличия идентификатора:
     * </p>
     * <ul>
     *   <li>Без ID: удаляет все задачи</li>
     *   <li>С ID: удаляет конкретную задачу</li>
     * </ul>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @throws IOException если возникает ошибка при отправке ответа
     */
    @Override
    protected void handleDelete(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (segments.id() == 0) {
            manager.clearTasks();
            sendText(exchange, jsonBuilder.message("Tasks were deleted"), 200);
        } else {
            manager.deleteTask(segments.id());
            sendText(exchange, jsonBuilder.message("Task: '" + segments.id() + "' was deleted"), 200);
        }
    }

    /**
     * Проверяет корректность ресурса в запросе.
     * <p>
     * Убеждается, что запрос адресован именно задачам ("tasks").
     * </p>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @return true если ресурс неверен (не "tasks"), false если корректно
     * @throws IOException если возникает ошибка при отправке ответа 404
     */
    @Override
    protected boolean validateResources(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (!"tasks".equals(segments.resource())) {
            sendNotFound(exchange, jsonBuilder.resourceNotFound());
            return true;
        }
        return false;
    }

    /**
     * Проверяет валидность объекта задачи.
     * <p>
     * Проверяет наличие обязательных полей:
     * </p>
     * <ul>
     *   <li>title - заголовок задачи</li>
     *   <li>description - описание задачи</li>
     *   <li>status - статус задачи</li>
     * </ul>
     *
     * @param task задача для валидации
     * @throws JsonSyntaxException если отсутствуют обязательные поля
     */
    protected void isTaskValid(Task task) {
        if (task.getTitle() == null || task.getDescription() == null || task.getStatus() == null) {
            throw new JsonSyntaxException("Task has invalid fields");
        }
    }
}
