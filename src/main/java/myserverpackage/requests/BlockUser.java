package myserverpackage.requests;

public class BlockUser {
    public String type;
    public String user;

    public BlockUser(String nick) {
        this.type = "BLOCK_USER";
        this.user = nick;
    }
}
