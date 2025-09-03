package managers.filedbacked;

import util.Status;
import util.Type;
import util.exceptions.ManagerLoadException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Вспомогательный класс для парсинга данных задач из файла.
 *
 * <p>Предоставляет статические методы для валидации и преобразования
 * строковых значений в соответствующие типы данных:
 * <ul>
 *   <li>Парсинг целых чисел и опциональных целых чисел</li>
 *   <li>Парсинг типов задач {@link Type}</li>
 *   <li>Парсинг статусов задач {@link Status}</li>
 *   <li>Парсинг длительности {@link Duration}</li>
 *   <li>Парсинг даты и времени {@link LocalDateTime}</li>
 * </ul>
 *
 * <p>Все методы выбрасывают {@link ManagerLoadException} при ошибках парсинга.
 *
 * @implSpec Все методы статические
 * @see FileBackedTaskManager
 * @see ManagerLoadException
 */
public class ParserHelper {

    protected static void validateHeader(String header) {
        if (header == null || !header.startsWith("id,type,name,status,description,epic,duration,startTime")) {
            throw new ManagerLoadException("Invalid file format or missing header");
        }
    }

    protected static int parseInteger(String value) throws ManagerLoadException {
        if (value == null || value.isBlank()) {
            throw new ManagerLoadException("Line cannot be empty");
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ManagerLoadException("Invalid id: " + value);
        }
    }

    protected static int parseOptionalInteger(String value) {
        if (value == null || value.equals("null") || value.trim().isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            throw new ManagerLoadException("Invalid optional id: " + value);
        }
    }

    protected static Type parseType(String value) throws ManagerLoadException {
        try {
            return Type.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            throw new ManagerLoadException("Invalid task type: " + value);
        }
    }

    protected static Status parseStatus(String value) throws ManagerLoadException {
        try {
            return Status.valueOf(value.trim());
        } catch (IllegalArgumentException e) {
            throw new ManagerLoadException("Invalid status: " + value);
        }
    }

    protected static Duration parseOptionalDuration(String value) throws ManagerLoadException {
        if (value == null || value.equals("null") || value.trim().isEmpty()) {
            return null;
        }

        try {
            return Duration.parse(value.trim());
        } catch (Exception e) {
            throw new ManagerLoadException("Invalid duration format: " + value);
        }
    }

    protected static LocalDateTime parseOptionalDateTime(String value, DateTimeFormatter formatter) throws ManagerLoadException {
        if (value == null || value.equals("null") || value.trim().isEmpty()) {
            return null;
        }

        try {
            return LocalDateTime.parse(value.trim(), formatter);
        } catch (DateTimeParseException e) {
            throw new ManagerLoadException("Invalid date format: " + value, e);
        }
    }
}
