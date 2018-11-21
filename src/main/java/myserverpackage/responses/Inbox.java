package myserverpackage.responses;

import java.util.ArrayList;

public class Inbox {
    public String type;
    public ArrayList<String> inbox;

    public Inbox(ArrayList<String> inbox) {
        this.type = "INBOX";
        this.inbox = inbox;
    }
}
