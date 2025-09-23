package http;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sun.net.httpserver.HttpServer;
import http.handlers.*;
import managers.TaskManager;
import managers.filedbacked.FileBackedTaskManager;
import util.gsonadapters.DurationAdapter;
import util.gsonadapters.LocalDateTimeAdapter;
import util.http.JsonBuilder;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Scanner;

public class HttpTaskServer {
    private final int PORT = 8080;
    private HttpServer httpServer;
    private final TaskManager manager;

    private final Gson gson;
    private final JsonBuilder jsonBuilder;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String cmd = "";
        HttpTaskServer server = new HttpTaskServer(new FileBackedTaskManager(new File("resources/httpTasks.csv")));
        server.start();

        System.out.println("Commands to stop: c, stop");
        while (!cmd.equals("c") && !cmd.equals("stop")) {
            cmd = scanner.next();
        }
        server.stop();
    }

    public HttpTaskServer(TaskManager manager) {
        this.manager = manager;
        this.gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(Duration.class, new DurationAdapter())
                .create();
        this.jsonBuilder = new JsonBuilder(gson);
        createServer();
    }

    private void createServer() {
        try {
            httpServer = HttpServer.create(new InetSocketAddress(PORT), 0);
            httpServer.createContext("/tasks", new TaskHandler(manager, gson, jsonBuilder));
            httpServer.createContext("/epics", new EpicsHandler(manager, gson, jsonBuilder));
            httpServer.createContext("/subtasks", new SubTasksHandler(manager, gson, jsonBuilder));
            httpServer.createContext("/history", new HistoryHandler(manager, gson, jsonBuilder));
            httpServer.createContext("/prioritized", new PrioritizedHandler(manager, gson, jsonBuilder));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        try {
            httpServer.start();
            System.out.println("Server is running: " + httpServer.getAddress().getPort());
        } catch (IllegalStateException e) {
            System.err.println("Server is already running");
        }
    }

    public void stop() {
        httpServer.stop(0);
        System.out.println("Server has been stopped");
    }
}
