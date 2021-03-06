package myserverpackage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import myserverpackage.requests.*;
import myserverpackage.responses.*;
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
                    case "BLOCK_USER":
                        blockUser(json,request.getAddress(),request.getPort(),socket);
                        break;
                    case "UNBLOCK_USER":
                        unblockUser(json,request.getAddress(),request.getPort(),socket);
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
        ArrayList<String> response = new ArrayList<>();
        for ( String key : users.keySet() ) {
            if (users.get(key).loggedIn) {
                response.add(key);
            }
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
                    if (!users.get(key).blockedUsers.contains(sender)) {
                        sendResponse(msg,users.get(key).ip,users.get(key).port,socket);
                    }
                }
            }
        } else {
            for (String key : users.keySet()) {
                if (json.destinatary.equals(key)) {
                    if(users.get(key).loggedIn) {
                        if (!users.get(key).blockedUsers.contains(sender)) {
                            sendResponse(msg, users.get(key).ip, users.get(key).port, socket);
                            AcknowledgeResponse a = new AcknowledgeResponse("MESSAGE_SENT_SUCCESSFULLY");
                            msg = (gson.toJson(a));
                            sendResponse(msg,ip,port,socket);
                        } else {
                            ErrorResponse e = new ErrorResponse("BLOCKED_BY_USER");
                            msg = (gson.toJson(e));
                            sendResponse(msg,ip,port,socket);
                        }
                    } else {
                        AcknowledgeResponse a = new AcknowledgeResponse("SAVED_TO_INBOX");
                        msg = (gson.toJson(a));
                        users.get(key).inbox.add(new InboxDetail(sender,json.message));
                        sendResponse(msg,ip,port,socket);
                    }
                    return;
                }
            }
            ErrorResponse e = new ErrorResponse("USER_DOES_NOT_EXIST");
            msg = (gson.toJson(e));
            sendResponse(msg,ip,port,socket);
        }
    }


    private static void addUser(String request, InetAddress ip, int port, DatagramSocket socket) throws IOException {
        AddUser user = gson.fromJson(request, AddUser.class);
        if (!users.containsKey(user.nick)) {
            users.put(user.nick, new SocketDetails(ip, port,user.password));
            AcknowledgeResponse a = new AcknowledgeResponse("CONNECT_OK");
            String msg = (gson.toJson(a));
            sendResponse(msg,ip,port,socket);
        } else {
            SocketDetails usuario = users.get(user.nick);
            if (usuario.password.equals(user.password) && usuario.loggedIn == false) {
                //Actualizar información del usuario.
                usuario.loggedIn = true;
                usuario.port = port;
                usuario.ip = ip;

                if(usuario.inbox.size() > 0) {
                    Inbox i = new Inbox(usuario.inbox);
                    String msg = (gson.toJson(i));
                    usuario.inbox = new ArrayList<>();
                    sendResponse(msg,ip,port,socket);
                    return;
                }

                AcknowledgeResponse a = new AcknowledgeResponse("CONNECT_OK");
                String msg = (gson.toJson(a));
                sendResponse(msg,ip,port,socket);
            } else {
                ErrorResponse e = new ErrorResponse("USERNAME_TAKEN");
                String msg = (gson.toJson(e));
                sendResponse(msg, ip, port, socket);
            }
        }
    }

    private static void blockUser(String request, InetAddress ip, int port, DatagramSocket socket) throws IOException {
        BlockUser json = gson.fromJson(request, BlockUser.class);
        String sender = getUser(ip,port);
        if(users.containsKey(json.user)) {
            users.get(sender).blockedUsers.add(json.user);
            AcknowledgeResponse a = new AcknowledgeResponse("USER_BLOCKED");
            String msg = (gson.toJson(a));
            sendResponse(msg,ip,port,socket);
        } else {
            ErrorResponse e = new ErrorResponse("USER_DOES_NOT_EXIST");
            String msg = (gson.toJson(e));
            sendResponse(msg,ip,port,socket);
        }
    }

    private static void unblockUser(String request, InetAddress ip, int port, DatagramSocket socket) throws IOException {
        UnblockUser json = gson.fromJson(request, UnblockUser.class);
        String sender = getUser(ip,port);
        if(users.get(sender).blockedUsers.contains(json.user)) {
            users.get(sender).blockedUsers.remove(json.user);
            AcknowledgeResponse a = new AcknowledgeResponse("USER_UNBLOCKED");
            String msg = (gson.toJson(a));
            sendResponse(msg,ip,port,socket);
        } else {
            ErrorResponse e = new ErrorResponse("USER_WAS_NOT_BLOCKED");
            String msg = (gson.toJson(e));
            sendResponse(msg,ip,port,socket);
        }
    }

    private static void removeUser(InetAddress ip, int port, DatagramSocket socket) throws IOException {
        SocketDetails usuario = users.get(getUser(ip, port));
        usuario.loggedIn = false;
        AcknowledgeResponse a = new AcknowledgeResponse("EXIT_OK");
        String msg = (gson.toJson(a));
        sendResponse(msg,ip,port,socket);
    }

    private static void sendResponse(String msg, InetAddress ip, int port, DatagramSocket socket) {
        try {
            byte[] b = msg.getBytes();
            DatagramPacket request = new DatagramPacket(b, b.length, ip, port);
            socket.send(request);
        }
        catch (Exception e) {
        }
    }
}
