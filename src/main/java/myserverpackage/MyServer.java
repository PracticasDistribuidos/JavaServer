package myserverpackage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import myserverpackage.requests.AddUser;
import myserverpackage.requests.RequestType;
import myserverpackage.requests.SendMessage;
import myserverpackage.responses.AcknowledgeResponse;
import myserverpackage.responses.ErrorResponse;
import myserverpackage.responses.ListResponse;
import myserverpackage.responses.MessageResponse;
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
                    String validation = getUser(request.getAddress(), request.getPort());
                    if (validation == null) {
                        ErrorResponse e = new ErrorResponse("USER_NOT_RECOGNIZED");
                        String msg = (gson.toJson(e));
                        sendResponse(msg,request.getAddress(),request.getPort(),socket);
                        continue;
                    }
                }

                switch (identify.type) {
                    case "CONNECT":
                        addUser(json, request.getAddress(), request.getPort(), socket);
                        break;
                    case "LIST_USERS":
                        listUsers(request.getAddress(),request.getPort(),socket);
                        break;
                    case "EXIT":
                        removeUser(request.getAddress(),request.getPort(),socket);
                        break;
                    case "SEND_MESSAGE":
                        sendMessage(json,request.getAddress(),request.getPort(),socket);
                        break;
                    default:
                        break;
                }
            }
        } catch (Exception exc) {

        }
    }

    private static String getUser(InetAddress IPAddress, int port) {
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
        sendResponse(msg,ip,port,socket);
    }

    private static void sendMessage(String request, InetAddress ip, int port, DatagramSocket socket) throws IOException {
        String sender = getUser(ip,port);
        SendMessage json = gson.fromJson(request, SendMessage.class);
        MessageResponse r = new MessageResponse(getUser(ip,port),json.message);
        String msg = (gson.toJson(r));
        if (json.destinatary.equals("ALL")) {
            for ( String key : users.keySet() ) {
                if(!key.equals(sender)) {
                    sendResponse(msg,users.get(key).ip,users.get(key).port,socket);
                }
            }
        } else {
            for (String key : users.keySet()) {
                if (json.destinatary.equals(key)) {
                    sendResponse(msg, users.get(key).ip, users.get(key).port, socket);
                    System.out.println("Mande Mensaje nomas a uno");
                    return;
                }
            }
            ErrorResponse e = new ErrorResponse("DESTINATARY_NOT_FOUND");
            msg = (gson.toJson(e));
            sendResponse(msg,ip,port,socket);
        }
    }


    private static void addUser(String request, InetAddress ip, int port, DatagramSocket socket) throws IOException {
        AddUser user = gson.fromJson(request, AddUser.class);
        if (!users.containsKey(user.nick)) {
            users.put(user.nick, new SocketDetails(ip, port));
            AcknowledgeResponse a = new AcknowledgeResponse("CONNECT_OK");
            String msg = (gson.toJson(a));
            sendResponse(msg,ip,port,socket);
        } else {
            ErrorResponse e = new ErrorResponse("USERNAME_TAKEN");
            String msg = (gson.toJson(e));
            sendResponse(msg,ip,port,socket);
        }
    }

    private static void removeUser(InetAddress ip, int port, DatagramSocket socket) throws IOException {
        users.remove(getUser(ip, port));
        AcknowledgeResponse a = new AcknowledgeResponse("EXIT_OK");
        String msg = (gson.toJson(a));
        sendResponse(msg,ip,port,socket);
    }

    public static void sendResponse(String msg, InetAddress ip, int port, DatagramSocket socket) {
        try {
            byte[] b = msg.getBytes();
            DatagramPacket request = new DatagramPacket(b, b.length, ip, port);
            socket.send(request);
        }
        catch (Exception e) {
        }
    }
}
