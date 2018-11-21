package myserverpackage.requests;

public class UnblockUser {
    public String type;
    public String user;

    public UnblockUser(String nick) {
        this.type = "UNBLOCK_USER";
        this.user = nick;
    }
}
