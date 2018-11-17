package myserverpackage.responses;

public class MessageResponse {
    public String type;
    public String sender;
    public String message;

    public MessageResponse(String sender, String message) {
        this.type = "MESSAGE";
        this.sender = sender;
        this.message = message;
    }
}
