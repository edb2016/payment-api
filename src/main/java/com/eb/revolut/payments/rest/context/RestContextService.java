package com.eb.revolut.payments.rest.context;

import com.eb.revolut.payments.rest.endpoint.Endpoint;

public interface RestContextService {

    void addEndpoint(Endpoint endpoint);
}
