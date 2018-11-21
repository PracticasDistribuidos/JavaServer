package myserverpackage.responses;

public class InboxDetail {
    public String sender;
    public String message;

    public InboxDetail(String sender, String message) {
        this.sender = sender;
        this.message = message;
    }
}
