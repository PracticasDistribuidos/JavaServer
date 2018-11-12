package myserverpackage.requests;

import com.google.gson.annotations.Expose;

public class RequestType {
    @Expose
    public String type;

    public RequestType(String type) {
        this.type = type;
    }
}
