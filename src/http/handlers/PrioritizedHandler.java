package http.handlers;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import managers.TaskManager;
import util.http.JsonBuilder;
import util.http.RequestSegments;

import java.io.IOException;

/**
 * Обработчик HTTP-запросов для получения отсортированного списка задач по приоритету.
 * <p>
 * Расширяет функциональность {@link HistoryHandler} для обработки запрещенных методов (POST, DELETE).
 * </p>
 *
 * <p><b>Поддерживаемые эндпоинты:</b></p>
 * <table border="1">
 *   <tr><th>Метод</th><th>Путь</th><th>Действие</th></tr>
 *   <tr><td>HEAD</td><td>/prioritized</td><td>Получить заголовки</td></tr>
 *   <tr><td>GET</td><td>/prioritized</td><td>Получить отсортированный список задач</td></tr>
 *   <tr><td>OPTIONS</td><td>/prioritized</td><td>Получить разрешенные методы</td></tr>
 *   <tr><td>POST</td><td>/prioritized</td><td><b>Запрещено</b> - метод не разрешен</td></tr>
 *   <tr><td>DELETE</td><td>/prioritized</td><td><b>Запрещено</b> - метод не разрешен</td></tr>
 *   <tr><td>GET</td><td>/prioritized/{id}</td><td><b>Запрещено</b> - метод не разрешен</td></tr>
 * </table>
 *
 * <p><b>Формат ответа:</b></p>
 * <pre>
 * [
 *   {
 *     "taskId": 3,
 *     "title": "Ранняя задача",
 *     "startTime": "1970-01-01T00:00:00.000",
 *     "duration": 10,
 *     // ... другие поля задачи
 *   },
 *   {
 *     "taskId": 1,
 *     "title": "Задача с временем",
 *     "startTime": "2000-01-01T00:00:00.000",
 *     "duration": 10,
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
public class PrioritizedHandler extends HistoryHandler {

    public PrioritizedHandler(TaskManager manager, Gson gson, JsonBuilder jsonBuilder) {
        super(manager, gson, jsonBuilder);
    }

    /**
     * Обрабатывает HTTP-метод GET для приоритизированного списка.
     * <p>
     * Возвращает задачи, отсортированные по времени начала (startTime).
     * </p>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @throws IOException если возникает ошибка при отправке ответа
     * @see util.TaskTimeController#getPrioritizedTasks()
     */
    @Override
    protected void handleGet(HttpExchange exchange, RequestSegments segments) throws IOException {
        sendText(exchange, gson.toJson(manager.getPrioritizedTasks()), 200);
    }

    /**
     * Проверяет корректность ресурса и пути запроса для приоритизированного списка.
     * <p>
     * Выполняет следующие проверки:
     * </p>
     * <ul>
     *   <li>Убеждается, что запрос адресован приоритизированному списку ("prioritized")</li>
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
        if (!"prioritized".equals(segments.resource())) {
            sendNotFound(exchange, jsonBuilder.resourceNotFound());
            return true;
        } else if (segments.id() != 0) {
            sendNotFound(exchange, jsonBuilder.subresourceNotFound());
            return true;
        }
        return false;
    }
}
