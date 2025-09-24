package http.handlers;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import managers.TaskManager;
import util.http.JsonBuilder;
import util.http.RequestSegments;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

import static util.enums.Endpoint.*;
import static util.http.RequestSegments.getRequestSegments;

/**
 * Абстрактный базовый класс для обработки HTTP-запросов.
 * <p>
 * Предоставляет общую функциональность для обработки HTTP-запросов, включая:
 * </p>
 * <ul>
 *   <li>Парсинг и валидацию сегментов URI</li>
 *   <li>Маршрутизацию запросов по HTTP-методам</li>
 *   <li>Стандартизированную отправку ответов</li>
 *   <li>Обработку ошибок и исключений</li>
 * </ul>
 *
 * <p><b>Жизненный цикл обработки запроса:</b></p>
 * <ol>
 *   <li>Парсинг сегментов пути из URI</li>
 *   <li>Валидация сегментов и HTTP-метода</li>
 *   <li>Проверка ресурсов (абстрактный метод)</li>
 *   <li>Маршрутизация на соответствующий обработчик метода</li>
 *   <li>Обработка исключений и отправка ошибок</li>
 * </ol>
 *
 * <p><b>Поддерживаемые HTTP-методы:</b> GET, POST, DELETE, OPTIONS, HEAD</p>
 *
 * <p><b>Особенности обработки:</b></p>
 * <ul>
 *   <li>Метод HEAD обрабатывается как GET, но без отправки тела ответа</li>
 *   <li>OPTIONS возвращает разрешенные методы для ресурса</li>
 *   <li>Некорректные запросы возвращают соответствующие HTTP-статусы</li>
 *   <li>Все ответы отправляются в формате JSON с UTF-8 кодировкой</li>
 * </ul>
 *
 * <p><b>Наследование:</b> Классы-наследники должны реализовать абстрактные методы
 * для конкретной логики обработки ресурсов.</p>
 */
public abstract class BaseHttpHandler implements HttpHandler {
    private final Charset defaultCharset = StandardCharsets.UTF_8;
    protected final TaskManager manager;

    protected Gson gson;
    protected JsonBuilder jsonBuilder;

    public BaseHttpHandler(TaskManager manager, Gson gson, JsonBuilder jsonBuilder) {
        this.manager = manager;
        this.gson = gson;

        this.jsonBuilder = jsonBuilder;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        try {
            RequestSegments segments = getPreparedSegments(exchange);

            if (segments == null || validateResources(exchange, segments)) {
                return;
            }

            mapEndpoints(exchange, segments);

        } catch (JsonSyntaxException e) {
            sendText(exchange, jsonBuilder.badRequest("Invalid Json"), 400);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            try {
                sendServerError(exchange);
                e.printStackTrace();
            } catch (IOException ex) {
                System.err.println("Failed to send response");
                ex.printStackTrace();
            }
        }
    }

    /**
     * Подготавливает и валидирует сегменты запроса.
     *
     * @param exchange HTTP-обмен для анализа
     * @return подготовленные сегменты запроса или {@code null} если валидация не пройдена
     * @throws IOException если возникает ошибка при обработке запроса
     */
    private RequestSegments getPreparedSegments(HttpExchange exchange) throws IOException {
        RequestSegments segments = getRequestSegments(exchange);

        jsonBuilder.setSegments(segments);

        if (!isSegmentsValid(exchange, segments)) {
            return null;
        }

        return segments;
    }

    /**
     * Маршрутизирует запрос на соответствующий обработчик метода.
     *
     * @param exchange HTTP-обмен для обработки
     * @param segments разобранные сегменты запроса
     * @throws IOException если возникает ошибка при обработке запроса
     */
    private void mapEndpoints(HttpExchange exchange, RequestSegments segments) throws IOException {
        switch (segments.endpoint()) {
            case OPTIONS -> handleOptions(exchange, segments);
            case GET -> handleGet(exchange, segments);
            case POST -> handlePost(exchange, segments);
            case DELETE -> handleDelete(exchange, segments);
            default -> sendText(exchange, jsonBuilder.badRequest("No such endpoint"), 400);
        }
    }

    /**
     * Проверяет валидность сегментов запроса.
     * Отправляет ответ, если проверка не пройдена.
     * <p>
     * Выполняет следующие проверки:
     * </p>
     * <ul>
     *   <li>Поддержка HTTP-метода</li>
     *   <li>Корректность endpoint</li>
     *   <li>Валидность структуры подресурсов</li>
     *   <li>Корректность числового идентификатора</li>
     *   <li>Разрешение подресурсов только для эпиков</li>
     * </ul>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты для валидации
     * @return {@code true} если сегменты валидны, иначе {@code false}
     * @throws IOException если возникает ошибка при отправке ответа об ошибке
     */
    private boolean isSegmentsValid(HttpExchange exchange, RequestSegments segments) throws IOException {

        if (segments.endpoint() == INVALID_METHOD) {
            exchange.getResponseHeaders().add("Allow", "GET, POST, DELETE, HEAD, OPTIONS");
            sendMethodNotAllowed(exchange);
            return false;
        } else if (segments.endpoint() == INVALID) {
            sendText(exchange, jsonBuilder.badRequest("No such endpoint"), 400);
            return false;
        } else if (segments.endpoint() == INVALID_SUBRESOURCE) {
            sendNotFound(exchange, jsonBuilder.tooMuchSubResources());
            return false;
        } else if (segments.id() == -1) {
            sendText(exchange, jsonBuilder.invalidId(), 400);
            return false;
        } else if (segments.subResource().isPresent() && !segments.resource().equals("epics")) {
            sendNotFound(exchange, jsonBuilder.subresourceNotFound());
            return false;
        }

        return true;
    }


    protected void sendText(HttpExchange exchange, String responseString, int responseCode) throws IOException {
        byte[] resp = responseString.getBytes(defaultCharset);
        exchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        exchange.getResponseHeaders().add("Content-Length", String.valueOf(resp.length));

        if (!"HEAD".equals(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(responseCode, resp.length);
            exchange.getResponseBody().write(resp);
        } else {
            exchange.sendResponseHeaders(responseCode, -1);
        }

        exchange.close();
    }

    protected void sendNotFound(HttpExchange exchange, String responseString) throws IOException {
        sendText(exchange, responseString, 404);
    }

    protected void sendHasOverlaps(HttpExchange exchange, String responseString) throws IOException {
        sendText(exchange, responseString, 406);
    }

    protected void sendMethodNotAllowed(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(405, -1);
        exchange.close();
    }

    protected void sendServerError(HttpExchange exchange) throws IOException {
        exchange.sendResponseHeaders(500, -1);
        exchange.close();
    }

    /**
     * Проверяет запрет метода POST для ресурсов с идентификатором.
     * Отправляет ответ, если метод запрещен
     * <p>
     * POST запросы разрешены только для формата пути /resource.
     * Для форматов /resource/id и /resource/id/subresource
     * ресурсов разрешены только GET, DELETE, OPTIONS, HEAD.
     * </p>
     *
     * @param exchange HTTP-обмен
     * @param segments сегменты запроса
     * @return true если операция POST запрещена, false если разрешена
     * @throws IOException если возникает ошибка при отправке ответа об ошибке
     */
    protected boolean isPostForbidden(HttpExchange exchange, RequestSegments segments) throws IOException {
        if (segments.id() != 0) {
            exchange.getResponseHeaders().add("Allow", "GET, DELETE, OPTIONS, HEAD");
            sendMethodNotAllowed(exchange);
            return true;
        }
        return false;
    }

    protected abstract void handleOptions(HttpExchange exchange, RequestSegments segments) throws IOException;

    protected abstract void handleGet(HttpExchange exchange, RequestSegments segments) throws IOException;

    protected abstract void handlePost(HttpExchange exchange, RequestSegments segments) throws IOException;

    protected abstract void handleDelete(HttpExchange exchange, RequestSegments segments) throws IOException;

    protected abstract boolean validateResources(HttpExchange exchange, RequestSegments segments) throws IOException;
}
