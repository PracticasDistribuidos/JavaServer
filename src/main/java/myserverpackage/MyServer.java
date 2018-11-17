package myserverpackage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import myserverpackage.requests.AddUser;
import myserverpackage.requests.RequestType;
import myserverpackage.responses.AcknowledgeResponse;
import myserverpackage.responses.ErrorResponse;
import myserverpackage.responses.ListResponse;
import myserverpackage.utils.SocketDetails;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MyServer {

    private static Map<String, SocketDetails> users;
    private static Gson gson = new Gson();

    private static GsonBuilder builder = new GsonBuilder();
    private static Gson gsonB = builder.create();

    public static void main(String[] args) {
        DatagramSocket socket;

        try {
            socket = new DatagramSocket(6788);
            byte [] buffer = new byte[1024];
            users = new HashMap<>();
            while(true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);


                String json = new String(request.getData()).substring(0, request.getLength());

                RequestType identify = gson.fromJson(json, RequestType.class);

                System.out.println("Json = " + json);

                if(!(identify.type.equals("CONNECT"))) {
                    System.out.println("Entre a intentar validar");
                    String validation = validateUser(request.getAddress(), request.getPort());
                    if (validation == null) {
                        ErrorResponse e = new ErrorResponse("USER_NOT_RECOGNIZED");
                        String msg = (gson.toJson(e));
                        sendMessage(msg,request.getAddress(),request.getPort(),socket);
                        continue;
                    }
                }

                switch (identify.type) {
                    case "CONNECT":
                        addUser(json, request.getAddress(), request.getPort(), socket);
                        break;
                    case "LIST_USERS":
                        listUsers(request.getAddress(),request.getPort(),socket);
                    case "EXIT":
                        removeUser(request.getAddress(),request.getPort(),socket);
                    default:
                        break;
                }
            }
        } catch (Exception exc) {

        }
    }

    private static String validateUser(InetAddress IPAddress, int port) {
        SocketDetails details = new SocketDetails(IPAddress, port);
        for(Map.Entry<String, SocketDetails> user: users.entrySet()) {
            if (user.getValue().equals(details)){
                return user.getKey();
            }
        }
        return null;
    }

    private static void listUsers(InetAddress ip, int port, DatagramSocket socket) throws IOException {
        ArrayList<String> response = new ArrayList<String>();
        for ( String key : users.keySet() ) {
            response.add(key);
        }
        ListResponse l = new ListResponse(response);
        String msg = (gson.toJson(l));
        sendMessage(msg,ip,port,socket);
    }

    private static void addUser(String request, InetAddress ip, int port, DatagramSocket socket) throws IOException {
        AddUser user = gson.fromJson(request, AddUser.class);
        if (!users.containsKey(user.nick)) {
            users.put(user.nick, new SocketDetails(ip, port));
            AcknowledgeResponse a = new AcknowledgeResponse("CONNECT_OK");
            String msg = (gson.toJson(a));
            sendMessage(msg,ip,port,socket);
        } else {
            ErrorResponse e = new ErrorResponse("USERNAME_TAKEN");
            String msg = (gson.toJson(e));
            sendMessage(msg,ip,port,socket);
        }
    }

    private static void removeUser(InetAddress ip, int port, DatagramSocket socket) throws IOException {
        users.remove(validateUser(ip, port));
        AcknowledgeResponse a = new AcknowledgeResponse("END_OK");
        String msg = (gson.toJson(a));
        sendMessage(msg,ip,port,socket);
    }

    public static void sendMessage(String msg, InetAddress ip, int port, DatagramSocket socket) {
        try {
            byte[] b = msg.getBytes();
            DatagramPacket request = new DatagramPacket(b, b.length, ip, port);
            socket.send(request);
        }
        catch (Exception e) {
        }
    }
}
