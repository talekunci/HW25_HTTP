package ua.goit;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import ua.goit.console.CommandHandler;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Client {

    public static final Logger LOGGER = LogManager.getLogger(Client.class);

    public static void main(String[] args) {
        LOGGER.log(Level.ALL, Client.class);
        runApp();
    }

    private static void runApp() {
        CommandHandler handler = new CommandHandler();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            String line;
            while ((line = reader.readLine()) != null) {
                handler.handleCommand(line);
            }
        } catch (IOException e) {
            LOGGER.error("Console reader throws IOException.", e);
        }
    }
}
