package ua.goit.console;

import ua.goit.console.commands.MainMenuCommand;

import java.util.regex.Matcher;

import static ua.goit.console.Command.pattern;

public class CommandHandler {
    private final Command mainMenu = new MainMenuCommand();
    private Command activeMenu = mainMenu;

    public CommandHandler() {
        this.activeMenu.printActiveMenu();
    }

    public void handleCommand(String params) {
        Matcher matcher = pattern.matcher(params);

        if (matcher.find()) {
            String command = matcher.group();

            if ("exit".equalsIgnoreCase(command)) {
                System.exit(0);
            } else if ("active".equalsIgnoreCase(command)) {
                this.activeMenu.printActiveMenu();
            } else if ("main".equalsIgnoreCase(command)) {
                this.activeMenu = mainMenu;
                this.activeMenu.printActiveMenu();
            } else {
                this.activeMenu.handle(params, c -> {
                    this.activeMenu = c;
                    this.activeMenu.printActiveMenu();
                });
            }
        } else {
            System.out.println("Empty command");
        }
    }
}
