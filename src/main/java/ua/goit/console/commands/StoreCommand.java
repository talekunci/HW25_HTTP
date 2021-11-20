package ua.goit.console.commands;

import com.google.gson.reflect.TypeToken;
import ua.goit.console.Command;
import ua.goit.model.*;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.Consumer;

public class StoreCommand implements Command {

    private static final String[] orderParameters = {
            "id",
            "petId",
            "quantity",
            "status" + ". Possible values: " + Arrays.toString(OrderStatus.values()),
            "complete" + ". 'true' or 'false'"
    };

    @Override
    public void handle(String params, Consumer<Command> setActive) {
        String command = params.split(" ")[0];

        String subParams = params.replace(command + " ", "");
        switch (command) {
            case "inventory" -> inventory();
            case "create" -> create();
            case "get" -> get(subParams).ifPresent(System.out::println);
            case "delete" -> delete(subParams);
        }
    }

    private void inventory() {
        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL + "store/inventory"))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            Map<String, Integer> map = GSON.fromJson(response.body(), new TypeToken<Map<String, Integer>>() {
            }.getType());

            if (response.statusCode() == 200) {
                if (map.isEmpty()) {
                    System.out.println("Response is empty.");
                } else {
                    map.forEach((k, v) -> System.out.printf("Item name: [%s]\tItem count: %d%n", k, v));
                }
                System.out.println("The operation is successful.");
            } else {
                System.out.println("Oops, something went wrong. Status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void create() {
        try {
            Order order = new Order();

            order.setShipDate(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SZ").format(new Date()).replace(" ", "T"));

            System.out.println("Enter parameters:");
            System.out.println("Print 'next' to skip the parameter.");
            for (int i = 0; i < orderParameters.length; ) {
                String param = orderParameters[i].split("\\W")[0];

                System.out.println("Current parameter: " + orderParameters[i]);

                String line = reader.readLine();
                if (line.equals("exit")) {
                    break;
                } else if (line.equals("next")) {
                    i++;
                    continue;
                }


                try {
                    switch (param) {
                        case "id" -> {
                            order.setId(Long.parseLong(line));
                            i++;
                        }
                        case "petId" -> {
                            order.setPetId(Long.parseLong(line));
                            i++;
                        }
                        case "quantity" -> {
                            order.setQuantity(Integer.parseInt(line));
                            i++;
                        }
                        case "status" -> {
                            switch (line.toLowerCase()) {
                                case "placed" -> {
                                    order.setStatus("placed");
                                    i++;
                                }
                                case "approved" -> {
                                    order.setStatus("approved");
                                    i++;
                                }
                                case "delivered" -> {
                                    order.setStatus("delivered");
                                    i++;
                                }
                                default -> System.out.println("Invalid value. Try again.");
                            }
                        }
                        case "complete" -> {
                            switch (line.toLowerCase()) {
                                case "true" -> {
                                    order.setComplete(true);
                                    i++;
                                }
                                case "false" -> {
                                    order.setComplete(false);
                                    i++;
                                }
                                default -> System.out.println("Invalid value. Try again.");
                            }
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format. Try again.");
                }
            }

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(URL + "store/order"))
                    .headers("Accept", "application/json",
                            "Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(order)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            switch (response.statusCode()) {
                case 200 -> System.out.println("Created order: " + GSON.fromJson(response.body(), Order.class));
                case 400 -> System.out.println("Invalid Order.");
                default -> System.out.printf("%nCode: %d%nBody: %s%n", response.statusCode(), response.body());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Optional<Order> get(String params) {
        try {
            int id = Integer.parseInt(params.split("\\D")[0]);

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%sstore/order/%d", URL, id)))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            switch (response.statusCode()) {
                case 200 -> {
                    return Optional.of(GSON.fromJson(response.body(), Order.class));
                }
                case 400 -> System.out.println("Invalid ID supplied.");
                case 404 -> System.out.println("Order not found.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private void delete(String params) {
        try {
            int id = Integer.parseInt(params.split("\\D")[0]);

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%sstore/order/%d", URL, id)))
                    .header("Accept", "application/json")
                    .DELETE()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            switch (response.statusCode()) {
                case 200 -> System.out.printf("Order with ID=%d was deleted.%n", id);
                case 400 -> System.out.println("Invalid ID supplied.");
                case 404 -> System.out.println("Order not found.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void printActiveMenu() {
        System.out.println("------Store menu------");
        System.out.println("Commands:");
        System.out.println("\t*inventory");
        System.out.println("\t*create");
        System.out.println("\t*get {id}");
        System.out.println("\t*delete {id}");
    }
}
