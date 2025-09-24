package util.http;

import com.sun.net.httpserver.HttpExchange;
import util.enums.Endpoint;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;

/**
 * Класс для парсинга сегментов HTTP-запроса.
 * <p>
 * Разбирает URI пути запроса на составные части: endpoint, ресурс,
 * идентификатор и опциональный подресурс. Также выполняет валидацию HTTP-метода.
 * </p>
 *
 * <p><b>Формат пути:</b></p>
 * <ul>
 *   <li>{@code /resource} - ресурс без идентификатора</li>
 *   <li>{@code /resource/123} - ресурс с числовым идентификатором</li>
 *   <li>{@code /resource/123/subresource} - ресурс с идентификатором и подресурсом</li>
 * </ul>
 *
 * <p><b>Поддерживаемые HTTP-методы:</b> GET, POST, DELETE, HEAD, OPTIONS</p>
 *
 * <p><b>Особенности обработки:</b></p>
 * <ul>
 *   <li>Метод HEAD обрабатывается как GET</li>
 *   <li>Некорректные идентификаторы преобразуются в -1</li>
 *   <li>Неподдерживаемые методы возвращают соответствующие
 *       значения Endpoint.INVALID_METHOD</li>
 *   <li>Ошбика парсинга Endpoint возвращает Enpoint.INVALID</li>
 * </ul>
 *
 * @param endpoint    конечная точка запроса (на основе HTTP-метода)
 * @param resource    название основного ресурса (первый сегмент пути после /)
 * @param id          числовой идентификатор ресурса (второй сегмент пути)
 * @param subResource опциональный подресурс (третий сегмент пути)
 */
public record RequestSegments(
        Endpoint endpoint,
        String resource,
        int id,
        Optional<String> subResource) {

    /**
     * Создает экземпляр RequestSegments на основе HttpExchange.
     * <p>
     * Извлекает метод запроса и путь из переданного HttpExchange и делегирует
     * парсинг методу {@link #parse(String, String)}.
     * </p>
     *
     * @param exchange HTTP-обмен для парсинга
     * @return новый экземпляр RequestSegments с разобранными сегментами пути
     * @see #parse(String, String)
     */
    public static RequestSegments getRequestSegments(HttpExchange exchange) {
        String path = exchange.getRequestURI().getPath();
        return RequestSegments.parse(exchange.getRequestMethod(), path);
    }

    /**
     * Парсит HTTP-метод и путь на составляющие сегменты.
     * <p>
     * В зависимости от количества сегментов пути создает соответствующий экземпляр:
     * </p>
     * <table>
     *   <tr><th>Сегментов</th><th>Формат</th><th>Результат</th></tr>
     *   <tr><td>2</td><td>/resource</td><td>resource, id=0, subResource=empty</td></tr>
     *   <tr><td>3</td><td>/resource/123</td><td>resource, id=123, subResource=empty</td></tr>
     *   <tr><td>4</td><td>/resource/123/sub</td><td>resource, id=123, subResource=sub</td></tr>
     *   <tr><td>другое</td><td>любой</td><td>Endpoint.INVALID_SUBRESOURCE</td></tr>
     * </table>
     *
     * @param method HTTP-метод запроса
     * @param path   путь URI запроса
     * @return экземпляр RequestSegments с разобранными сегментами
     */
    private static RequestSegments parse(String method, String path) {
        String[] parts = path.split("/");

        return switch (parts.length) {
            case 2 -> new RequestSegments(
                    parseEndpoint(method),
                    parts[1],
                    0,
                    Optional.empty());
            case 3 -> new RequestSegments(
                    parseEndpoint(method),
                    parts[1],
                    parseInt(parts[2]),
                    Optional.empty());
            case 4 -> new RequestSegments(
                    parseEndpoint(method),
                    parts[1],
                    parseInt(parts[2]),
                    Optional.of(parts[3]));
            default -> new RequestSegments(
                    Endpoint.INVALID_SUBRESOURCE,
                    parts[1],
                    0,
                    Optional.empty());
        };
    }

    private static int parseInt(String idString) {
        try {
            int id = Integer.parseInt(idString);
            if (id < 0) {
                throw new NumberFormatException();
            } else {
                return id;
            }
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static Endpoint parseEndpoint(String method) {
        HashSet<String> validMethods = new HashSet<>(List.of(
                "GET",
                "POST",
                "DELETE",
                "HEAD",
                "OPTIONS")
        );
        if (!validMethods.contains(method)) {
            return Endpoint.INVALID_METHOD;
        }

        Endpoint endpoint;

        if (method.equals("HEAD")) {
            method = "GET";
        }

        try {
            endpoint = Endpoint.valueOf(method);
        } catch (IllegalArgumentException e) {
            endpoint = Endpoint.INVALID;
        }

        return endpoint;
    }
}
