package myserverpackage;
import com.google.gson.Gson;
import myserverpackage.requests.RequestType;
import myserverpackage.utils.SocketDetails;

import java.net.*;
import java.util.Map;

public class MyServer {

    private static Map<String, SocketDetails> users;
    private static Gson gson = new Gson();

    public static void main(String[] args) {
        DatagramSocket socket;

        try {
            socket = new DatagramSocket(6788);
            byte [] buffer = new byte[1024];

            while(true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                String json = new String(request.getData()).substring(0, request.getLength());
                RequestType r = gson.fromJson(json, RequestType.class);
                System.out.println(request);

                switch (r.type) {
                    case "conectar":
                        System.out.println("Éxito intentaste conectarte");
                        break;
                    default:
                        System.out.println("Error");
                }


                String [] msgArray = (new String(request.getData())).split(" ");
                byte [] sendMsg = (msgArray[0] + " server processed").getBytes();
                DatagramPacket reply = new DatagramPacket(sendMsg,sendMsg.length,request.getAddress(),request.getPort());
                socket.send(reply);
            }
        } catch (Exception exc) {

        }
    }
}
