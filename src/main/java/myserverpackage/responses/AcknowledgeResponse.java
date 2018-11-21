package myserverpackage.responses;

public class AcknowledgeResponse {
    private String type;
    private String description;

    public AcknowledgeResponse(String description) {
        this.type = "ACKNOWLEDGE";
        this.description = description;
    }

    public static class Inbox {
    }
}
