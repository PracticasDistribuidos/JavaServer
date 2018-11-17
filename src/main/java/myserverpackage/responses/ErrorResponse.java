package myserverpackage.responses;

public class ErrorResponse {
    private String type;
    private String description;

    public ErrorResponse(String description) {
        this.type = "ERROR";
        this.description = description;
    }
}
