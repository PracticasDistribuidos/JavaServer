package myserverpackage.requests;

import com.google.gson.annotations.Expose;

public class RequestType {
    @Expose
    public String tipo;

    public RequestType(String type) {
        this.tipo = type;
    }
}
