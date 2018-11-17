package myserverpackage.requests;

public class SendMessage {
    public String type;
    public String destinatary;
    public String message;

    public SendMessage(String destinatary, String message) {
        this.type = "SEND_MESSAGE";
        this.destinatary = destinatary;
        this.message = message;
    }
}
