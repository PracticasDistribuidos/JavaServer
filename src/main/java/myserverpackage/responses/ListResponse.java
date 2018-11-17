package myserverpackage.responses;

import java.util.ArrayList;

public class ListResponse {
    private String type;
    private ArrayList<String> users;

    public ListResponse(ArrayList<String> users) {
        this.type = "USER_LIST";
        this.users = users;
    }
}
