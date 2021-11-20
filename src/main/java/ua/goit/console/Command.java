package ua.goit.console;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.http.HttpClient;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public interface Command {

    Pattern pattern = Pattern.compile("^\\w+");

    String URL = "https://petstore.swagger.io/v2/";
    HttpClient client = HttpClient.newHttpClient();
    Gson GSON = new Gson().newBuilder().setPrettyPrinting().create();
    BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


    void handle(String params, Consumer<Command> setActive);

    void printActiveMenu();

    default Optional<String> getCommandString(String params) {
        Matcher matcher = pattern.matcher(params);

        if (matcher.find()) {
            return Optional.of(matcher.group());
        }

        return Optional.empty();
    }
}
