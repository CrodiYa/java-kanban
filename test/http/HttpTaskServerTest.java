package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import managers.InMemoryTaskManager;
import managers.TaskManager;
import model.Epic;
import model.SubTask;
import model.Task;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import util.enums.Status;
import util.gsonadapters.DurationAdapter;
import util.gsonadapters.LocalDateTimeAdapter;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HttpTaskServerTest {

    protected HttpClient client;
    protected HttpTaskServer server;
    protected TaskManager manager;
    protected final Gson gson = new GsonBuilder()
            .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
            .registerTypeAdapter(Duration.class, new DurationAdapter())
            .create();

    protected final LocalDateTime epochTime =
            LocalDateTime.of(1970, 1, 1, 0, 0, 0);

    protected Task task;
    protected Epic epic;
    protected SubTask subTask;

    @BeforeEach
    public void initServer() {
        client = HttpClient.newHttpClient();
        manager = new InMemoryTaskManager();
        server = new HttpTaskServer(manager);

        task = new Task("task1", "demo", Status.NEW);
        epic = new Epic("epic1", "demo", Status.NEW);
        subTask = new SubTask("subtask1", "demo", Status.NEW, 1);
        server.start();
    }

    @AfterEach
    public void stopServer() {
        server.stop();
    }

    protected HttpRequest getMethod(String method, String path) {
        URI url = URI.create(path);

        HttpRequest.Builder builder = HttpRequest.newBuilder().uri(url);

        switch (method) {
            case "HEAD" -> builder.HEAD();
            case "OPTIONS" -> builder.method("OPTIONS", HttpRequest.BodyPublishers.noBody());
            case "GET" -> builder.GET();
            case "DELETE" -> builder.DELETE();
        }

        return builder.build();
    }

    protected HttpRequest getPost(String value, String taskJson) {
        URI url = URI.create(value);

        return HttpRequest
                .newBuilder()
                .POST(HttpRequest.BodyPublishers.ofString(taskJson))
                .uri(url)
                .build();
    }

    @Nested
    class BaseHandlerTest {
        private HttpRequest getInvalidMethod() {
            URI url = URI.create("http://localhost:8080/tasks");

            return HttpRequest
                    .newBuilder()
                    .PUT(HttpRequest.BodyPublishers.ofString(""))
                    .uri(url)
                    .build();
        }

        @Test
        public void shouldReturn405WhenForbiddenMethod() throws IOException, InterruptedException {

            HttpResponse<String> response = client.send(
                    getInvalidMethod(),
                    HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(405, response.statusCode());
        }

        @Test
        public void shouldReturn404WhenTooManySubs() throws IOException, InterruptedException {

            HttpResponse<String> response = client.send(
                    getMethod("GET", "http://localhost:8080/tasks/1/sub/sub"),
                    HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldReturn405WhenInvalidMethod() throws IOException, InterruptedException {
            HttpRequest request = HttpRequest
                    .newBuilder()
                    .method("BlaBlaBla", HttpRequest.BodyPublishers.noBody())
                    .uri(URI.create("http://localhost:8080/tasks"))
                    .build();


            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            assertEquals(405, response.statusCode());
        }
    }

    public class ListTaskTypeToken extends TypeToken<List<Task>> {
    }

    public class ListEpicTypeToken extends TypeToken<List<Epic>> {
    }

    public class ListSubTaskTypeToken extends TypeToken<List<SubTask>> {
    }
}


