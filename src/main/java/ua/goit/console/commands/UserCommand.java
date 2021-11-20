package ua.goit.console.commands;

import ua.goit.console.Command;
import ua.goit.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

public class UserCommand implements Command {

    private static final Map<String, User> users = User.getExitingUsers();

    private static final String[] petParameters = {
            "id",
            "username",
            "firstName",
            "lastName",
            "email",
            "password",
            "phone",
            "userStatus"
    };

    @Override
    public void handle(String params, Consumer<Command> setActive) {
        String command = params.split(" ")[0];

        String subParams = params.replace(command + " ", "");
        switch (command) {
            case "get" -> get(subParams).ifPresent(System.out::println);
            case "login" -> login(subParams);
            case "logout" -> logout();
            case "create" -> create();
            case "post" -> post(subParams);
            case "postWithList" -> postWithList();
            case "postWithArray" -> postWithArray(subParams);
            case "update" -> put(subParams);
            case "delete" -> delete(subParams);
        }
    }

    private Optional<User> get(String params) {
        try {
            String username = params.split(" ")[0];

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%suser/%s", URL, username)))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            switch (response.statusCode()) {
                case 200 -> {
                    User user = GSON.fromJson(response.body(), User.class);

                    users.put(user.getUsername(), user);
                    return Optional.of(user);
                }
                case 400 -> System.out.println("Invalid username supplied.");
                case 404 -> System.out.printf("User with USERNAME='%s' not found.%n", username);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private void login(String params) {
        try {
            String[] paramsArray = params.split(" ");

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%suser/login?username=%s&password=%s", URL, paramsArray[0], paramsArray[1])))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            switch (response.statusCode()) {
                case 200 -> System.out.println(GSON.fromJson(response.body(), ApiResponse.class).getMessage());
                case 400 -> System.out.println("Invalid username/password supplied.");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void logout() {
        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%suser/logout", URL)))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
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
            User user = new User();

            System.out.println("Enter parameters:");
            System.out.println("Print 'next' to skip the parameter.");
            for (int i = 0; i < petParameters.length; ) {
                String param = petParameters[i];

                System.out.println("Current parameter: " + param);

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
                            user.setId(Long.parseLong(line));
                            i++;
                        }
                        case "username" -> {
                            if (users.get(line) == null) {
                                user.setUsername(line);
                                i++;
                            } else {
                                System.out.printf("User with USERNAME='%s' already exist.%n", line);
                            }
                        }
                        case "firstName" -> {
                            user.setFirstName(line);
                            i++;
                        }
                        case "lastName" -> {
                            user.setLastName(line);
                            i++;
                        }
                        case "email" -> {
                            user.setEmail(line);
                            i++;
                        }
                        case "password" -> {
                            user.setPassword(line);
                            i++;
                        }
                        case "phone" -> {
                            user.setPhone(line);
                            i++;
                        }
                        case "userStatus" -> {
                            user.setUserStatus(Integer.parseInt(line));
                            i++;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format. Try again.");
                }
            }

            System.out.printf("Created entity: %s%n", user);

            System.out.println("Post to the server? 'yes' or 'no'");
            boolean push = reader.readLine().equals("yes");

            if (push) {
                post(user.getUsername());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void post(String params) {
        try {
            User user = users.get(params);

            if (user == null) {
                System.out.printf("User with USERNAME='%s' not found.%n", params);
                return;
            }

            final HttpRequest request = HttpRequest
                    .newBuilder(URI.create(String.format("%suser", URL)))
                    .headers("Accept", "application/json",
                            "Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(user)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("The operation is successful.");
            } else {
                System.out.println("Oops, something went wrong. Status code: " + response.statusCode());
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void put(String params) {
        try {
            User user = users.get(params.split(" ")[0]);

            if (user == null) {
                Optional<User> byParam = get(params);
                if (byParam.isPresent()) {
                    user = byParam.get();
                } else {
                    System.out.printf("User with USERNAME='%s' not found.%n", params);
                    return;
                }
            }

            users.remove(user.getUsername());

            System.out.println("Enter parameters:");
            System.out.println("Print 'next' to skip the parameter.");
            for (int i = 0; i < petParameters.length; ) {
                String param = petParameters[i];

                System.out.println("Current parameter: " + param);

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
                            user.setId(Long.parseLong(line));
                            i++;
                        }
                        case "username" -> {
                            if (users.get(line) == null) {
                                user.setUsername(line);
                                i++;
                            } else {
                                System.out.printf("User with USERNAME='%s' already exist.%n", line);
                            }
                        }
                        case "firstName" -> {
                            user.setFirstName(line);
                            i++;
                        }
                        case "lastName" -> {
                            user.setLastName(line);
                            i++;
                        }
                        case "email" -> {
                            user.setEmail(line);
                            i++;
                        }
                        case "password" -> {
                            user.setPassword(line);
                            i++;
                        }
                        case "phone" -> {
                            user.setPhone(line);
                            i++;
                        }
                        case "userStatus" -> {
                            user.setUserStatus(Integer.parseInt(line));
                            i++;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format. Try again.");
                }
            }

            String newUsername = user.getUsername();
            if (newUsername != null) {
                users.put(newUsername, user);
            } else {
                while ((newUsername = reader.readLine()) != null) {
                    if (users.get(newUsername) == null) {
                        users.put(newUsername, user);
                        break;
                    } else {
                        System.out.println("This username already exist. Try another.");
                    }
                }
            }

            System.out.printf("Created entity: %s%n", user);


            final HttpRequest request = HttpRequest
                    .newBuilder(URI.create(String.format("%suser/%s", URL, user.getUsername())))
                    .headers("accept", "application/json",
                            "Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(user)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            switch (response.statusCode()) {
                case 200 -> System.out.println("User was updated.");
                case 400 -> System.out.println("Invalid 'USERNAME' supplied.");
                case 404 -> System.out.printf("User with USERNAME=%s not found.%n", params);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void delete(String params) {
        try {
            String userName = params.split("\\W")[0];
            final HttpRequest request = HttpRequest
                    .newBuilder(URI.create(String.format("%suser/%s", URL, userName)))
                    .header("accept", "application/json")
                    .DELETE()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            switch (response.statusCode()) {
                case 200 -> {
                    System.out.println("User was deleted.");
                    users.remove(userName);
                }
                case 400 -> System.out.println("Invalid 'USERNAME' supplied.");
                case 404 -> System.out.printf("User with USERNAME=%s not found.%n", params);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void postWithList() {
        try {
            System.out.println("*** Create some users ***");
            System.out.println("*** Print 'exit' when you're ended ***");

            List<User> userList = new ArrayList<>();

            System.out.println("Enter parameters:");
            System.out.println("Print 'next' to skip the parameter.");

            outer: while (true) {
                User user = new User();

                for (int i = 0; i < petParameters.length; ) {
                    String param = petParameters[i];

                    System.out.println("Current parameter: " + param);

                    String line = reader.readLine();
                    if (line.equals("exit")) {
                        break outer;
                    } else if (line.equals("next")) {
                        i++;
                        continue;
                    }


                    try {
                        switch (param) {
                            case "id" -> {
                                user.setId(Long.parseLong(line));
                                i++;
                            }
                            case "username" -> {
                                if (users.get(line) == null) {
                                    user.setUsername(line);
                                    i++;
                                } else {
                                    System.out.printf("User with USERNAME='%s' already exist.%n", line);
                                }
                            }
                            case "firstName" -> {
                                user.setFirstName(line);
                                i++;
                            }
                            case "lastName" -> {
                                user.setLastName(line);
                                i++;
                            }
                            case "email" -> {
                                user.setEmail(line);
                                i++;
                            }
                            case "password" -> {
                                user.setPassword(line);
                                i++;
                            }
                            case "phone" -> {
                                user.setPhone(line);
                                i++;
                            }
                            case "userStatus" -> {
                                user.setUserStatus(Integer.parseInt(line));
                                i++;
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number format. Try again.");
                    }
                }

                System.out.printf("Created entity: %s%n", user);

                users.put(user.getUsername(), user);
                userList.add(user);
            }


            final HttpRequest request = HttpRequest
                    .newBuilder(URI.create(String.format("%suser/createWithList", URL)))
                    .headers("Accept", "application/json",
                            "Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(userList)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("The operation is successful.");
            } else {
                System.out.println("Oops, something went wrong. Status code: " + response.statusCode());
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void postWithArray(String params) {
        try {
            int usersCount = Integer.parseInt(params.split(" ")[0]);
            User[] userArray = new User[usersCount];
            System.out.println("Created Users array with length=" + usersCount);

            System.out.printf("*** Create %d users ***%n", usersCount);

            System.out.println("Enter parameters:");
            System.out.println("Print 'next' to skip the parameter.");

            for (int userIndex = 0; userIndex < usersCount; userIndex++) {
                if (userIndex > 0) System.out.printf("*** %d more users left to create. ***%n", (usersCount - userIndex));
                User user = new User();

                for (int i = 0; i < petParameters.length; ) {
                    String param = petParameters[i];

                    System.out.println("Current parameter: " + param);

                    String line = reader.readLine();
                    if (line.equals("exit")) {
                        System.out.println("You are not finished.");
                        System.out.println("Print 'yes' if really want interrupt the operation.");
                        if (reader.readLine().equalsIgnoreCase("yes")) {
                            System.out.println("The operation is interrupted.");
                            return;
                        } else {
                            System.out.println("Ok go on.");
                            continue;
                        }
                    } else if (line.equals("next")) {
                        i++;
                        continue;
                    }


                    try {
                        switch (param) {
                            case "id" -> {
                                user.setId(Long.parseLong(line));
                                i++;
                            }
                            case "username" -> {
                                if (users.get(line) == null) {
                                    user.setUsername(line);
                                    i++;
                                } else {
                                    System.out.printf("User with USERNAME='%s' already exist.%n", line);
                                }
                            }
                            case "firstName" -> {
                                user.setFirstName(line);
                                i++;
                            }
                            case "lastName" -> {
                                user.setLastName(line);
                                i++;
                            }
                            case "email" -> {
                                user.setEmail(line);
                                i++;
                            }
                            case "password" -> {
                                user.setPassword(line);
                                i++;
                            }
                            case "phone" -> {
                                user.setPhone(line);
                                i++;
                            }
                            case "userStatus" -> {
                                user.setUserStatus(Integer.parseInt(line));
                                i++;
                            }
                        }
                    } catch (NumberFormatException e) {
                        System.out.println("Invalid number format. Try again.");
                    }
                }

                System.out.printf("Created entity: %s%n", user);

                users.put(user.getUsername(), user);
                userArray[userIndex] = user;
            }


            final HttpRequest request = HttpRequest
                    .newBuilder(URI.create(String.format("%suser/createWithArray", URL)))
                    .headers("Accept", "application/json",
                            "Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(userArray)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("The operation is successful.");
            } else {
                System.out.println("Oops, something went wrong. Status code: " + response.statusCode());
            }
        } catch (NumberFormatException e) {
            System.out.println("Number format exception.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void printActiveMenu() {
        System.out.println("------User menu------");
        System.out.println("Commands:");
        System.out.println("\t*get {username}");
        System.out.println("\t*login {username} {password}");
        System.out.println("\t*logout");
        System.out.println("\t*create");
        System.out.println("\t*post {username}");
        System.out.println("\t*postWithList");
        System.out.println("\t*postWithArray {users_count}");
        System.out.println("\t*update {username}");
        System.out.println("\t*delete {username}");
    }
}
