package com.eb.revolut.payments.rest.context.impl;

import com.eb.revolut.payments.rest.context.RestContextService;
import com.eb.revolut.payments.rest.endpoint.Endpoint;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;


@NoArgsConstructor
@Slf4j
public class RestContextServiceImpl implements RestContextService {

    @Override
    public void addEndpoint(Endpoint endpoint) {
        endpoint.configure();
        log.info("REST endpoint registered for {}.", endpoint.getClass().getSimpleName());
    }

}
