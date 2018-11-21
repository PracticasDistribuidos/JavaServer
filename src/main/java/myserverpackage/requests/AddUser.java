package myserverpackage.requests;

public class AddUser {
    public String type;
    public String nick;
    public String password;

    public AddUser(String nick) {
        this.type = "CONNECT";
        this.nick = nick;
    }
}
