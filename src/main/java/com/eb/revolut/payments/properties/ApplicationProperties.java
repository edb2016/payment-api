package com.eb.revolut.payments.properties;

public interface ApplicationProperties {

    int getIntProperty(String property);

    long getLongProperty(String property);

    String getStringProperty(String property);

}
