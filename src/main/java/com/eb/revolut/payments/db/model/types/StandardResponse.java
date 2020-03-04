package com.eb.revolut.payments.db.model.types;

import com.eb.revolut.payments.db.model.types.StatusResponse;
import com.google.gson.JsonElement;
import lombok.Getter;

@Getter
public class StandardResponse {

    private StatusResponse status;
    private String message;
    private JsonElement data;

    public StandardResponse(StatusResponse status, String message) {
        this.status=status;
        this.message=message;
    }

    public StandardResponse(StatusResponse status, JsonElement data) {
        this.status=status;
        this.data=data;
    }
}
