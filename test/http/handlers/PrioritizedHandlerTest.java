package http.handlers;

import http.HttpTaskServerTest;
import model.Task;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import util.enums.Status;

import java.io.IOException;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PrioritizedHandlerTest extends HttpTaskServerTest {

    @Nested
    class HistoryGetTest {

        private HttpRequest getRequest(String value) {
            return getMethod("GET", value);
        }

        @Test
        public void shouldBeEqualAndCode200() throws IOException, InterruptedException {
            manager.addTask(new Task("task1", "demo", Status.NEW, 10, epochTime));
            manager.addTask(new Task("task2", "demo", Status.NEW, 10, epochTime.plusMinutes(10)));
            manager.addTask(task);

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/prioritized"),
                    HttpResponse.BodyHandlers.ofString());


            List<Task> list = gson.fromJson(response.body(), new ListTaskTypeToken().getType());
            assertEquals(200, response.statusCode());
            assertEquals(2, list.size());
        }

        @Test
        public void shouldReturn404WithInvalidId() throws IOException, InterruptedException {

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/prioritized/1"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldReturn404WithInvalidIdAndSub() throws IOException, InterruptedException {

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/prioritized/1/sub"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }

        @Test
        public void shouldReturn404WithInvalidResource() throws IOException, InterruptedException {

            HttpResponse<String> response = client.send(
                    getRequest("http://localhost:8080/prioritizedABC"),
                    HttpResponse.BodyHandlers.ofString());

            assertEquals(404, response.statusCode());
        }

    }

}