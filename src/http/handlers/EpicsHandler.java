package http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import model.Epic;
import model.Task;
import util.enums.Endpoint;
import util.exceptions.TaskNotFound;
import util.http.JsonBuilder;
import util.http.RequestSegments;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;

/**
 * Обработчик HTTP-запросов для управления эпиками (Epic) и их подзадачами.
 * <p>
 * Расширяет функциональность {@link TaskHandler} для работы с эпиками.
 * Обеспечивает REST API для операций CRUD над эпиками и их подзадачами.
 * </p>
 *
 * <p><b>Поддерживаемые эндпоинты для эпиков:</b></p>
 * <table border="1">
 *   <tr><th>Метод</th><th>Путь</th><th>Действие</th></tr>
 *   <tr><td>HEAD</td><td>любой путь</td><td>Получить заголовки</td></tr>
 *   <tr><td>GET</td><td>/epics</td><td>Получить все эпики</td></tr>
 *   <tr><td>GET</td><td>/epics/{id}</td><td>Получить эпик по Id</td></tr>
 *   <tr><td>GET</td><td>/epics/{id}/subtasks</td><td>Получить все подзадачи эпика</td></tr>
 *   <tr><td>POST</td><td>/epics</td><td>Создать новый эпик</td></tr>
 *   <tr><td>POST</td><td>/epics/{id}</td><td><b>Запрещено</b> - метод не разрешен</td></tr>
 *   <tr><td>POST</td><td>/epics/{id}/subtasks</td><td><b>Запрещено</b> - метод не разрешен</td></tr>
 *   <tr><td>DELETE</td><td>/epics</td><td>Удалить все эпики</td></tr>
 *   <tr><td>DELETE</td><td>/epics/{id}</td><td>Удалить эпик по Id</td></tr>
 *   <tr><td>DELETE</td><td>/epics/{id}/subtasks</td><td>Удалить все подзадачи эпика</td></tr>
 *   <tr><td>OPTIONS</td><td>любой путь</td><td>Получить разрешенные методы</td></tr>
 * </table>
 *
 * <p><b>Формат JSON для эпика:</b></p>
 * <pre>
 * {
 *   "taskId": 0,           // 0 для создания, >0 для обновления
 *   "title": "string",     // обязательное поле
 *   "description": "string", // обязательное поле
 *   "status": "NEW|IN_PROGRESS|DONE", // (только для чтения)
 *   "subtasks": [1, 2, 3], // (только для чтения)
 *   "duration": 10,        // (только для чтения)
 *   "startTime": "1970-01-01T00:00:00.000" //(только для чтения)
 * }
 * </pre>
 *
 * <p><b>Обработка ошибок:</b></p>
 * <ul>
 *   <li>400 - Неверный формат JSON или отсутствуют обязательные поля</li>
 *   <li>404 - Эпик не найден или неверный путь</li>
 *   <li>405 - Метод не разрешен для ресурса</li>
 *   <li>500 - Внутренняя ошибка сервера</li>
 * </ul>
 */
public class EpicsHandler extends TaskHandler {

    public EpicsHandler(TaskManager manager, Gson gson, JsonBuilder jsonBuilder) {
        super(manager, gson, jsonBuilder);
    }

    /**
     * Обрабатывает HTTP-метод OPTIONS для эпиков.
     * <p>
     * Возвращает разрешенные методы в зависимости от контекста:
     * </p>
     * <ul>
     *   <li>Для /epics: GET, POST, DELETE, OPTIONS, HEAD</li>
     *   <li>Для /epics/{id} и /epics/{id}/subtasks: GET, DELETE, OPTIONS, HEAD</li>
     * </ul>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @throws IOException если возникает ошибка при отправке ответа
     */
    @Override
    protected void handleOptions(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (segments.id() == 0 && segments.subResource().isEmpty()) {
            exchange.getResponseHeaders().add("Allow", "GET, POST, DELETE, OPTIONS, HEAD");
        } else {
            exchange.getResponseHeaders().add("Allow", "GET, DELETE, OPTIONS, HEAD");
        }
        exchange.sendResponseHeaders(204, -1);
    }

    /**
     * Обрабатывает HTTP-метод GET для эпиков.
     * <p>
     * В зависимости от пути запроса:
     * </p>
     * <ul>
     *   <li>/epics - возвращает список всех эпиков</li>
     *   <li>/epics/{id} - возвращает конкретный эпик</li>
     *   <li>/epics/{id}/subtasks - возвращает все подзадачи указанного эпика</li>
     * </ul>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @throws IOException если возникает ошибка при отправке ответа
     */
    @Override
    protected void handleGet(HttpExchange exchange, RequestSegments segments) throws IOException {
        int id = segments.id();

        if (segments.subResource().isPresent()) {
            if (manager.getEpicWithoutHistory(id) != null) {
                sendText(exchange, gson.toJson(manager.getSubTasksFromEpic(id)), 200);
            } else {
                sendNotFound(exchange, jsonBuilder.notFound("Epic with id '" + id + "'not found"));
            }
        } else if (id == 0) {
            sendText(exchange, gson.toJson(manager.getEpics()), 200);
        } else {
            Epic epic = manager.getEpic(id);
            if (epic != null) {
                sendText(exchange, gson.toJson(epic), 200);
            } else {
                sendNotFound(exchange, jsonBuilder.notFound("Epic with id '" + id + "'not found"));
            }
        }
    }

    /**
     * Обрабатывает HTTP-метод POST для эпиков.
     * <p>
     * Создает новый эпик или обновляет существующий:
     * </p>
     * <ul>
     *   <li>taskId = 0: создает новый эпик через конструктор</li>
     *   <li>taskId > 0: обновляет существующий эпик</li>
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
            Epic epic = gson.fromJson(body, Epic.class);
            isTaskValid(epic);

            if (epic.getTaskId() == 0) {
                //новый эпик для безопасного создания
                epic = new Epic(epic.getTitle(), epic.getDescription(), epic.getStatus());
                manager.addEpic(epic);
            } else {
                manager.updateEpic(epic);
            }
            sendText(exchange, gson.toJson(manager.getEpicWithoutHistory(epic.getTaskId())), 201);
        } catch (DateTimeParseException e) {
            sendText(exchange, jsonBuilder.badRequest("Invalid date format"), 400);
        } catch (TaskNotFound e) {
            sendNotFound(exchange, jsonBuilder.notFound(e.getMessage()));
        }
    }

    /**
     * Обрабатывает HTTP-метод DELETE для эпиков.
     * <p>
     * В зависимости от пути запроса:
     * </p>
     * <ul>
     *   <li>/epics - удаляет все эпики</li>
     *   <li>/epics/{id} - удаляет конкретный эпик</li>
     *   <li>/epics/{id}/subtasks - удаляет все подзадачи указанного эпика</li>
     * </ul>
     *
     * <p><b>Удаление эпика автоматически удаляет все его подзадачи.</b></p>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @throws IOException если возникает ошибка при отправке ответа
     */
    @Override
    protected void handleDelete(HttpExchange exchange, RequestSegments segments) throws IOException {

        if (segments.subResource().isPresent()) {
            try {
                manager.clearSubTasksFromEpic(segments.id());
                sendText(exchange,
                        jsonBuilder.message("Subtasks from epic: '" + segments.id() + "' were deleted"),
                        200);
            } catch (TaskNotFound e) {
                sendNotFound(exchange, jsonBuilder.notFound(e.getMessage()));
            }
        } else if (segments.id() == 0) {
            manager.clearEpics();
            sendText(exchange, jsonBuilder.message("Epics were deleted"), 200);
        } else {
            manager.deleteEpic(segments.id());
            sendText(exchange, jsonBuilder.message("Epic: '" + segments.id() + "' was deleted"), 200);
        }
    }

    /**
     * Проверяет корректность ресурса и подресурсов в запросе.
     * <p>
     * Выполняет следующие проверки:
     * </p>
     * <ul>
     *   <li>Убеждается, что запрос адресован эпикам ("epics")</li>
     *   <li>Проверяет валидность подресурса (только "subtasks")</li>
     *   <li>Запрещает POST запросы для подресурсов</li>
     * </ul>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @return true если ресурс неверен или запрос запрещен, иначе false
     * @throws IOException если возникает ошибка при отправке ответа об ошибке
     */
    @Override
    protected boolean validateResources(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (!"epics".equals(segments.resource())) {
            sendNotFound(exchange, jsonBuilder.resourceNotFound());
            return true;
        }

        if (segments.subResource().isPresent()) {
            if (!segments.subResource().get().equals("subtasks")) {
                sendNotFound(exchange, jsonBuilder.subresourceNotFound());
                return true;
            }
            if (segments.endpoint() == Endpoint.POST) {
                exchange.getResponseHeaders().add("Allow", "GET, DELETE, HEAD, OPTIONS");
                sendMethodNotAllowed(exchange);
                return true;
            }
        }
        return false;
    }

    /**
     * Проверяет валидность объекта эпика.
     * <p>
     * Проверяет наличие обязательных полей:
     * </p>
     * <ul>
     *   <li>title - заголовок эпика</li>
     *   <li>description - описание эпика</li>
     * </ul>
     *
     * <p><b>Отличие от TaskHandler:</b> для эпиков статус не является обязательным полем,
     * так как он рассчитывается автоматически на основе подзадач.</p>
     *
     * @param task эпик для валидации
     * @throws JsonSyntaxException если отсутствуют обязательные поля
     */
    @Override
    protected void isTaskValid(Task task) {
        if (task.getTitle() == null || task.getDescription() == null) {
            throw new JsonSyntaxException("Epic has invalid fields");
        }
    }
}
