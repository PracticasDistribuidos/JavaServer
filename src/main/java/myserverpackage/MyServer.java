package myserverpackage;
import com.google.gson.Gson;
import myserverpackage.requests.RequestType;
import myserverpackage.utils.SocketDetails;

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

                String from = getUser(request.getAddress(), request.getPort());
                if (from == null) {
                    byte [] replyMsg = ("Usuario no reconocido. Primero escoge un nick").getBytes();
                    DatagramPacket reply = new DatagramPacket(replyMsg,replyMsg.length,request.getAddress(),request.getPort());
                    socket.send(reply);
                    continue;
                }

                System.out.println("Usuario reconocido");


                String json = new String(request.getData()).substring(0, request.getLength());
                RequestType r = gson.fromJson(json, RequestType.class);
                System.out.println(request);

                switch (r.tipo) {
                    case "conectar":
                        System.out.println("Éxito intentaste conectarte");
                        break;
                    default:
                        break;
                }


                String [] msgArray = (new String(request.getData())).split(" ");
                byte [] sendMsg = (msgArray[0] + " server processed").getBytes();
                DatagramPacket reply = new DatagramPacket(sendMsg,sendMsg.length,request.getAddress(),request.getPort());
                socket.send(reply);
            }
        } catch (Exception exc) {

        }
    }

    private static String getUser(InetAddress IPAddress, int port) {
        SocketDetails search = new SocketDetails(IPAddress, port);
        for(Map.Entry<String, SocketDetails> user: users.entrySet()) {
            if (user.getValue().equals(search)){
                return user.getKey();
            }
        }
        return null;
    }
}
