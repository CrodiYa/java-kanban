package util.gsonadapters;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

import java.io.IOException;
import java.time.Duration;

public class DurationAdapter extends TypeAdapter<Duration> {

    @Override
    public void write(JsonWriter jsonWriter, Duration duration) throws IOException {
        jsonWriter.value(duration == null ? null : duration.toMinutes());
    }

    @Override
    public Duration read(JsonReader jsonReader) throws IOException {
        String string = jsonReader.nextString();
        return Duration.ofMinutes(Long.parseLong(string));
    }
}
