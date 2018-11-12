package myserverpackage.utils;

import java.net.InetAddress;

public class SocketDetails {
    public InetAddress ip;
    public int port;

    public SocketDetails(InetAddress ipA, int port) {
        this.ip = ipA;
        this.port = port;
    }

    public boolean equals(SocketDetails details) {
        return this.ip.equals(details.ip) && this.port == details.port;
    }
}
