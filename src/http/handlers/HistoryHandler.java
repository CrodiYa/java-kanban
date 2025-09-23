package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import util.http.JsonBuilder;
import util.http.RequestSegments;

import java.io.IOException;

/**
 * Обработчик HTTP-запросов для получения истории просмотров задач.
 * <p>
 * Предоставляет доступ к истории последних просмотренных задач.
 * </p>
 *
 * <p><b>Поддерживаемые эндпоинты:</b></p>
 * <table border="1">
 *   <tr><th>Метод</th><th>Путь</th><th>Действие</th></tr>
 *   <tr><td>HEAD</td><td>/history</td><td>Получить заголовки</td></tr>
 *   <tr><td>GET</td><td>/history</td><td>Получить историю просмотров</td></tr>
 *   <tr><td>OPTIONS</td><td>/history</td><td>Получить разрешенные методы</td></tr>
 *   <tr><td>POST</td><td>/history</td><td><b>Запрещено</b> - метод не разрешен</td></tr>
 *   <tr><td>DELETE</td><td>/history</td><td><b>Запрещено</b> - метод не разрешен</td></tr>
 *   <tr><td>GET</td><td>/history/{id}</td><td><b>Запрещено</b> - метод не разрешен</td></tr>
 * </table>
 *
 * <p><b>Формат ответа:</b></p>
 * <pre>
 * [
 *   {
 *     "taskId": 1,
 *     "title": "Название задачи",
 *     "description": "Описание задачи",
 *     "status": "NEW",
 *     "type": "TASK|EPIC|SUBTASK"
 *     // ... другие поля задачи в зависимости от типа
 *   },
 *   {
 *     "taskId": 2,
 *     "title": "Другая задача",
 *     // ...
 *   }
 * ]
 * </pre>
 *
 * <p><b>Обработка ошибок:</b></p>
 * <ul>
 *   <li>404 - Неверный путь</li>
 *   <li>405 - Метод не разрешен для ресурса</li>
 *   <li>500 - Внутренняя ошибка сервера</li>
 * </ul>
 */
public class HistoryHandler extends BaseHttpHandler {

    public HistoryHandler(TaskManager manager, Gson gson, JsonBuilder jsonBuilder) {
        super(manager, gson, jsonBuilder);
    }

    /**
     * Обрабатывает HTTP-метод OPTIONS для истории.
     * <p>
     * Возвращает разрешенные методы для ресурса истории:
     * GET, OPTIONS, HEAD
     * </p>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @throws IOException если возникает ошибка при отправке ответа
     */
    @Override
    protected void handleOptions(HttpExchange exchange, RequestSegments segments) throws IOException {
        exchange.getResponseHeaders().add("Allow", "GET, OPTIONS, HEAD");
        exchange.sendResponseHeaders(204, -1);
    }

    /**
     * Обрабатывает HTTP-метод GET для истории.
     * <p>
     * Возвращает полную историю просмотров задач в формате JSON.
     * </p>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @throws IOException если возникает ошибка при отправке ответа
     */
    @Override
    protected void handleGet(HttpExchange exchange, RequestSegments segments) throws IOException {
        sendText(exchange, gson.toJson(manager.getHistory()), 200);
    }

    /**
     * Обрабатывает HTTP-метод POST для истории или списка приоритетов.
     * <p>
     * Ресурс только для чтения, поэтому POST запросы запрещены.
     * Возвращает статус 405 Method Not Allowed с заголовком Allow.
     * </p>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @throws IOException если возникает ошибка при отправке ответа
     */
    @Override
    protected void handlePost(HttpExchange exchange, RequestSegments segments) throws IOException {
        exchange.getResponseHeaders().add("Allow", "GET, HEAD, OPTIONS");
        sendMethodNotAllowed(exchange);
    }

    /**
     * Обрабатывает HTTP-метод DELETE для истории или списка приоритетов.
     * <p>
     * Ресурс только для чтения, поэтому DELETE запросы запрещены.
     * Возвращает статус 405 Method Not Allowed с заголовком Allow.
     * </p>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @throws IOException если возникает ошибка при отправке ответа
     */
    @Override
    protected void handleDelete(HttpExchange exchange, RequestSegments segments) throws IOException {
        exchange.getResponseHeaders().add("Allow", "GET, HEAD, OPTIONS");
        sendMethodNotAllowed(exchange);
    }

    /**
     * Проверяет корректность ресурса и пути запроса для истории.
     * <p>
     * Выполняет следующие проверки:
     * </p>
     * <ul>
     *   <li>Убеждается, что запрос адресован истории ("history")</li>
     *   <li>Проверяет, что в пути нет идентификатора и подресурсов</li>
     * </ul>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @return true если путь неверен, иначе false
     * @throws IOException если возникает ошибка при отправке ответа 404
     */
    @Override
    protected boolean validateResources(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (!"history".equals(segments.resource())) {
            sendNotFound(exchange, jsonBuilder.resourceNotFound());
            return true;
        } else if (segments.id() != 0) {
            sendNotFound(exchange, jsonBuilder.subresourceNotFound());
            return true;
        }
        return false;
    }
}
