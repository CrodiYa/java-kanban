package util.gsonadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class LocalDateTimeAdapter extends TypeAdapter<LocalDateTime> {
    private static final List<DateTimeFormatter> formats =
            List.of(DateTimeFormatter.ISO_LOCAL_DATE_TIME,
                    DateTimeFormatter.ISO_DATE_TIME,
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"),
                    DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
                    DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss")
            );

    @Override
    public void write(JsonWriter jsonWriter, LocalDateTime localDateTime) throws IOException {
        jsonWriter.value(localDateTime == null ? null : localDateTime.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
    }

    @Override
    public LocalDateTime read(JsonReader jsonReader) throws IOException {
        String date = jsonReader.nextString();
        for (DateTimeFormatter format : formats) {
            try {
                return LocalDateTime.parse(date, format);
            } catch (DateTimeParseException e) {
                continue;
            }
        }

        throw new DateTimeParseException("Cannot parse date: " + date, date, 0);
    }
}
