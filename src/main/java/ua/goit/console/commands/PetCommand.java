package ua.goit.console.commands;

import com.google.gson.reflect.TypeToken;
import ua.goit.console.Command;
import ua.goit.model.Category;
import ua.goit.model.*;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.function.Consumer;

public class PetCommand implements Command {

    private static final Map<Long, Pet> pets = Pet.getExitingPets();
    private static final Map<Long, Category> categories = Category.getExitingCategories();
    private static final Map<Long, Tag> tags = Tag.getExitingTags();

    private static final String[] petParameters = {
            "ID",
            "category ID",
            "name",
            "photoUrsl[]",
            "tags[] ID",
            "status" + ". Possible values: " + Arrays.toString(PetStatus.values())
    };

    @Override
    public void handle(String params, Consumer<Command> setActive) {
        String command = params.split(" ")[0];

        String subParams = params.replace(command + " ", "");
        switch (command) {
            case "create" -> create();
            case "getById" -> getById(subParams).ifPresent(System.out::println);
            case "getByStatus" -> getByStatus(subParams);
            case "getAll" -> pets.forEach((k, v) -> {
                if (v != null) System.out.println(v);
            });
            case "post" -> post(subParams);
            case "postPetImage" -> postImage(subParams);
            case "put" -> put(subParams);
            case "update" -> update(subParams);
            case "delete" -> delete(subParams);
        }
    }

    private Optional<Pet> getById(String params) {
        try {
            Long id = Long.parseLong(params);

            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%spet/%d", URL, id)))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            switch (response.statusCode()) {
                case 200 -> {
                    Pet pet = GSON.fromJson(response.body(), Pet.class);

                    pets.put(pet.getId(), pet);
                    return Optional.of(pet);
                }
                case 400 -> System.out.println("Invalid ID supplied.");
                case 404 -> System.out.printf("Pet with ID=%d not found.%n", id);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private void update(String params) {
        try {
            String[] s = params.split(" ");

            Pet pet = pets.get(Long.parseLong(s[0]));

            if (pet == null) {
                Optional<Pet> byId = getById(s[0]);
                if (byId.isPresent()) {
                    pet = byId.get();
                } else {
                    System.out.printf("Pet with ID=%s not found.%n", s[0]);
                    return;
                }
            }

            String status = s[2];

            while (true) {
                boolean hasMatches = false;
                for (PetStatus v : PetStatus.values()) {
                    if (v.toString().equalsIgnoreCase(status)) {
                        hasMatches = true;
                        break;
                    }
                }

                if (!hasMatches) {
                    System.out.printf("Unknown status. Possible values: %s. Try again.%n", Arrays.toString(PetStatus.values()));
                    status = reader.readLine();
                } else {
                    break;
                }
            }

            String body = String.format("name=%s$status=%s", s[1], status);
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%spet/%d", URL, pet.getId())))
                    .headers("Accept", "application/json",
                            "Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            switch (response.statusCode()) {
                case 200 -> System.out.println("The operation is successful.");
                case 405 -> System.out.println("Invalid input");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void put(String params) {
        try {
            Pet updatedPet = pets.get(Long.parseLong(params.split(" ")[0]));

            if (updatedPet == null) {
                Optional<Pet> byId = getById(params);
                if (byId.isPresent()) {
                    updatedPet = byId.get();
                } else {
                    System.out.printf("Pet with ID=%s not found.%n", params);
                    return;
                }
            }

            System.out.println("Enter parameters:");
            System.out.println("Print 'next' to skip the parameter.");
            for (int i = 1; i < petParameters.length; ) {
                String param = petParameters[i].split("\\W")[0];

                System.out.println("Current parameter: " + petParameters[i]);

                String line = reader.readLine();
                if (line.equals("exit")) {
                    System.out.println("Method was returned.");
                    return;
                } else if (line.equals("next")) {
                    i++;
                    continue;
                }


                try {
                    switch (param) {
                        case "category" -> {
                            long id = Long.parseLong(line);
                            Category category = categories.get(id);
                            if (category == null) {
                                System.out.println("Enter category name.");
                                category = new Category();
                                category.setName(reader.readLine());
                            }
                            updatedPet.setCategory(category);
                            i++;
                        }
                        case "name" -> {
                            updatedPet.setName(line);
                            i++;
                        }
                        case "photoUrsl" -> {
                            List<String> urls = new ArrayList<>();
                            while (!line.equals("next")) {
                                urls.add(line);
                                line = reader.readLine();
                            }
                            i++;

                            updatedPet.setPhotoUrls(urls.toArray(String[]::new));
                        }
                        case "tags" -> {
                            List<Tag> tagList = new ArrayList<>();

                            while (!line.equals("next")) {
                                long id = Long.parseLong(line);
                                Tag tag = tags.get(id);

                                if (tag == null) {
                                    tag = new Tag();
                                    System.out.println("Enter category name.");
                                    tag.setName(reader.readLine());
                                }
                                tagList.add(tag);

                                line = reader.readLine();
                            }
                            i++;

                            updatedPet.setTags(tagList.toArray(Tag[]::new));
                        }

                        case "status" -> {
                            switch (line.toLowerCase()) {
                                case "available" -> updatedPet.setStatus("available");
                                case "pending" -> updatedPet.setStatus("pending");
                                case "sold" -> updatedPet.setStatus("sold");
                                default -> {
                                    System.out.println("Unknown status.");
                                    continue;
                                }
                            }
                            i++;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format. Try again.");
                }
            }

            System.out.printf("Updated entity: %s%n", updatedPet);


            final HttpRequest request = HttpRequest
                    .newBuilder(URI.create(String.format("%spet", URL)))
                    .header("Content-Type", "application/json")
                    .PUT(HttpRequest.BodyPublishers.ofString(GSON.toJson(updatedPet)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            switch (response.statusCode()) {
                case 200 -> {
                    Pet pet = GSON.fromJson(response.body(), Pet.class);
                    System.out.println("Returned pet: " + pet);
                }
                case 400 -> System.out.println("Invalid ID supplied.");
                case 404 -> System.out.println("Pet not found.");
                case 405 -> System.out.println("Validation exception.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void create() {
        try {
            Pet pet = new Pet();

            System.out.println("Enter parameters:");
            System.out.println("Print 'next' to skip the parameter.");
            for (int i = 0; i < petParameters.length; ) {
                String param = petParameters[i].split("\\W")[0];

                System.out.println("Current parameter: " + petParameters[i]);

                String line = reader.readLine();
                if (line.equals("exit")) {
                    break;
                } else if (line.equals("next")) {
                    i++;
                    continue;
                }


                try {
                    switch (param) {
                        case "ID" -> {
                            long newId = Long.parseLong(line);
                            if (pets.get(newId) == null) {
                                pets.remove(pet.getId());
                                pet.setId(newId);
                                pets.put(newId, pet);
                                i++;
                            } else {
                                System.out.printf("Pet with ID=%d already exist.%n", newId);
                            }
                        }
                        case "category" -> {
                            long id = Long.parseLong(line);
                            Category category = categories.get(id);
                            if (category == null) {
                                System.out.println("Enter category name.");
                                category = new Category();
                                category.setId(id);
                                category.setName(reader.readLine());
                            }
                            pet.setCategory(category);
                            i++;
                        }
                        case "name" -> {
                            pet.setName(line);
                            i++;
                        }
                        case "photoUrsl" -> {
                            List<String> urls = new ArrayList<>();
                            while (!line.equals("next")) {
                                urls.add(line);
                                line = reader.readLine();
                            }
                            i++;

                            pet.setPhotoUrls(urls.toArray(String[]::new));
                        }
                        case "tags" -> {
                            List<Tag> tagList = new ArrayList<>();

                            while (!line.equals("next")) {
                                long id = Long.parseLong(line);
                                Tag tag = tags.get(id);

                                if (tag == null) {
                                    tag = new Tag();
                                    System.out.println("Enter category name.");
                                    tag.setName(reader.readLine());
                                }
                                tagList.add(tag);

                                line = reader.readLine();
                            }
                            i++;

                            pet.setTags(tagList.toArray(Tag[]::new));
                        }

                        case "status" -> {
                            switch (line.toLowerCase()) {
                                case "available" -> pet.setStatus("available");
                                case "pending" -> pet.setStatus("pending");
                                case "sold" -> pet.setStatus("sold");
                                default -> {
                                    System.out.println("Unknown status.");
                                    continue;
                                }
                            }
                            i++;
                        }
                    }
                } catch (NumberFormatException e) {
                    System.out.println("Invalid number format. Try again.");
                }
            }

            System.out.printf("Created entity: %s%n", pet);

            System.out.println("Post to the server? 'yes' or 'no'");
            boolean push = reader.readLine().equals("yes");

            if (push) {
                post(pet.getId().toString());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void getByStatus(String params) {
        try {
            final HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(String.format("%spet/findByStatus?status=%s", URL, params)))
                    .header("Accept", "application/json")
                    .GET()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            switch (response.statusCode()) {
                case 200 -> {
                    List<Pet> petsResponse = GSON.fromJson(response.body(), new TypeToken<List<Pet>>() {
                    }.getType());


                    if (petsResponse.isEmpty()) {
                        System.out.println("Response is empty.");
                    } else {

                        petsResponse.forEach(p -> pets.put(p.getId(), p));

                        System.out.println("Print response? 'yes' or 'no'");
                        boolean print = reader.readLine().equals("yes");

                        if (print) petsResponse.forEach(System.out::println);
                    }

                    System.out.println("The operation is successful.");
                }
                case 400 -> System.out.printf("Pet with STATUS=%s not found.%n", params);
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void post(String params) {
        try {
            Pet pet = pets.get(Long.parseLong(params));

            if (pet == null) {
                System.out.printf("Pet with ID=%s not found.%n", params);
                return;
            }

            final HttpRequest request = HttpRequest
                    .newBuilder(URI.create(String.format("%spet", URL)))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(pet)))
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            switch (response.statusCode()) {
                case 200 -> {
                    Pet returnedPet = GSON.fromJson(response.body(), Pet.class);

                    pets.remove(pet.getId());
                    pets.put(returnedPet.getId(), returnedPet);
                    System.out.println("Returned pet:");
                    System.out.println(returnedPet);

                    System.out.println("The operation is successful.");
                }
                case 400 -> System.out.println("Invalid input.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid ID format.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void delete(String params) {
        try {
            final HttpRequest request = HttpRequest
                    .newBuilder(URI.create(String.format("%spet/%d", URL, Long.parseLong(params))))
                    .header("Accept", "application/json")
                    .DELETE()
                    .build();
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            switch (response.statusCode()) {
                case 200 -> System.out.println("Pet was deleted.");
                case 400 -> System.out.println("Invalid ID supplied.");
                case 404 -> System.out.printf("Pet with ID=%s not found.%n", params);
            }
        } catch (NumberFormatException e) {
            System.out.println("Invalid number format. Try again.");
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void postImage(String params) {
        try {
            String[] paramsArray = params.split(" ");
            Long id = Long.parseLong(paramsArray[0]);

            Pet pet = pets.get(id);
            Path imagePath = Paths.get(paramsArray[1]);

            if (pet == null) {
                Optional<Pet> byId = getById(paramsArray[0]);
                if (byId.isPresent()) {
                    pet = byId.get();
                } else {
                    System.out.printf("Pet with ID=%d not found.%n", id);
                    return;
                }
            }

            if (!Files.exists(imagePath)) {
                System.out.printf("File %s does not exist.%n", imagePath.toAbsolutePath());
                return;
            }

            String boundary = "-------------oiawn4tp89n4e9p5";
            Map<Object, Object> body = new HashMap<>();

            // some form fields
            body.put("additionalMetadata",
                    paramsArray[1].substring(paramsArray[1].lastIndexOf("\\")) //picture title
            );

            // file upload
            body.put("file", imagePath);

            final HttpRequest request = HttpRequest
                    .newBuilder(URI.create(String.format("%spet/%d/uploadImage", URL, pet.getId())))
                    .headers("Accept", "application/json",
                            "Content-Type", "multipart/form-data;boundary=" + boundary)
                    .POST(oMultipartData(body, boundary))
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

    private static HttpRequest.BodyPublisher oMultipartData(Map<Object, Object> data,
                                                            String boundary) throws IOException {
        var byteArrays = new ArrayList<byte[]>();
        byte[] separator = ("--" + boundary
                + "\r\nContent-Disposition: form-data; name=")
                .getBytes(StandardCharsets.UTF_8);
        for (Map.Entry<Object, Object> entry : data.entrySet()) {
            byteArrays.add(separator);

            if (entry.getValue() instanceof Path) {
                var path = (Path) entry.getValue();
                String mimeType = Files.probeContentType(path);
                byteArrays.add(("\"" + entry.getKey() + "\"; filename=\""
                        + path.getFileName() + "\"\r\nContent-Type: " + mimeType
                        + "\r\n\r\n").getBytes(StandardCharsets.UTF_8));
                byteArrays.add(Files.readAllBytes(path));
                byteArrays.add("\r\n".getBytes(StandardCharsets.UTF_8));
            } else {
                byteArrays.add(
                        ("\"" + entry.getKey() + "\"\r\n\r\n" + entry.getValue()
                                + "\r\n").getBytes(StandardCharsets.UTF_8));
            }
        }
        byteArrays
                .add(("--" + boundary + "--").getBytes(StandardCharsets.UTF_8));
        return HttpRequest.BodyPublishers.ofByteArrays(byteArrays);
    }

    @Override
    public void printActiveMenu() {
        System.out.println("------Pets menu------");
        System.out.println("Commands:");
        System.out.println("\t*create");
        System.out.println("\t*post {id} //Before posting, create pet");
        System.out.println("\t*postPetImage {id} {File path} //Upload an image. Before posting, create pet");
        System.out.println("\t*put {id}");
        System.out.println("\t*update {id} {name} {status} //Possible values: " + Arrays.toString(PetStatus.values()));
        System.out.println("\t*getByStatus {status} //Possible values: " + Arrays.toString(PetStatus.values()));
        System.out.println("\t*getById {id}");
        System.out.println("\t*delete {id}");
    }
}
