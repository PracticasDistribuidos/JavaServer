package myserverpackage.utils;

import java.net.InetAddress;
import java.util.ArrayList;

public class SocketDetails {
    public InetAddress ip;
    public int port;
    public ArrayList<String> inbox;
    public boolean loggedIn;
    public String password;

    public SocketDetails(InetAddress ipA, int port, String password) {
        this.ip = ipA;
        this.port = port;
        inbox = new ArrayList<>();
        loggedIn = true;
        this.password = password;
    }

    public SocketDetails(InetAddress ipA, int port) {
        this.ip = ipA;
        this.port = port;
        inbox = new ArrayList<>();
    }


    public boolean equals(SocketDetails details) {
        return this.ip.equals(details.ip) && this.port == details.port;
    }
}
