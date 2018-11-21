package myserverpackage.responses;

import java.util.ArrayList;

public class Inbox {
    public String type;
    public ArrayList<InboxDetail> inbox;

    public Inbox(ArrayList<InboxDetail> inbox) {
        this.type = "INBOX";
        this.inbox = inbox;
    }
}
