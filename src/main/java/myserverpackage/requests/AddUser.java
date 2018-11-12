package myserverpackage.requests;

import com.google.gson.annotations.Expose;

public class AddUser {
    public String type;
    public String nick;

    public AddUser(String nick) {
        this.type = "CONNECT";
        this.nick = nick;
    }
}
