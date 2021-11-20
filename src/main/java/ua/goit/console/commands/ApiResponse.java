package ua.goit.console.commands;

public class ApiResponse {
    private int code;
    private String type;
    private String message;

    private ApiResponse() {};

    public String getMessage() {
        return message;
    }
}
