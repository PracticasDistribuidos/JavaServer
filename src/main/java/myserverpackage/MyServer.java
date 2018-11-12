package myserverpackage;
import com.google.gson.Gson;
import myserverpackage.requests.AddUser;
import myserverpackage.requests.RequestType;
import myserverpackage.utils.SocketDetails;

import java.io.IOException;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

public class MyServer {

    private static Map<String, SocketDetails> users;
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        DatagramSocket socket;

        try {
            socket = new DatagramSocket(6788);
            byte [] buffer = new byte[1024];
            users = new HashMap<>();
            while(true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);
                System.out.println("Recibí request");


                String json = new String(request.getData()).substring(0, request.getLength());
                System.out.println("Json = " + json);
                RequestType identify = gson.fromJson(json, RequestType.class);

                if(!identify.type.equals("CONNECT")) {
                    String validation = validateUser(request.getAddress(), request.getPort());
                    if (validation == null) {
                        byte[] replyMsg = ("Usuario no reconocido. Primero escoge un nick").getBytes();
                        DatagramPacket reply = new DatagramPacket(replyMsg, replyMsg.length, request.getAddress(), request.getPort());
                        socket.send(reply);
                        continue;
                    }
                }

                switch (identify.type) {
                    case "CONNECT":
                        addUser(json, request.getAddress(), request.getPort(), socket);
                        break;
                    default:
                        break;
                }

            }
        } catch (Exception exc) {

        }
    }

    private static String validateUser(InetAddress IPAddress, int port) {
        SocketDetails search = new SocketDetails(IPAddress, port);
        for(Map.Entry<String, SocketDetails> user: users.entrySet()) {
            if (user.getValue().equals(search)){
                return user.getKey();
            }
        }
        return null;
    }

    private static void addUser(String request, InetAddress ip, int port, DatagramSocket socket) throws IOException {
        AddUser user = gson.fromJson(request, AddUser.class);
        if (!users.containsKey(user.nick)) {
            users.put(user.nick, new SocketDetails(ip, port));
            byte [] sendMsg = ("Éxito, bienvenido").getBytes();
            DatagramPacket reply = new DatagramPacket(sendMsg,sendMsg.length,ip,port);
            socket.send(reply);
        } else {
            byte [] sendMsg = ("Error, ese nombre ya fue utilizado").getBytes();
            DatagramPacket reply = new DatagramPacket(sendMsg,sendMsg.length,ip,port);
            socket.send(reply);
        }
    }


}
