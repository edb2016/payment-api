package com.eb.revolut.payments.properties;

import com.eb.revolut.payments.properties.ApplicationProperties;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@Slf4j
public class ApplicationPropertiesImpl implements ApplicationProperties {

    private final Properties properties;

    public ApplicationPropertiesImpl(String file) {
        properties = new Properties();
        loadProperties(file);
    }

    @Override
    public int getIntProperty(String property) {
        String strPropertry = properties.getProperty(property);
        return Integer.parseInt(strPropertry);
    }

    @Override
    public long getLongProperty(String property) {
        String strPropertry = properties.getProperty(property);
        return Long.parseLong(strPropertry);
    }

    @Override
    public String getStringProperty(String property) {
        return properties.getProperty(property);
    }

    private void loadProperties(String file) {
        try (InputStream input = new FileInputStream(file)) {

            // load a properties file
            properties.load(input);

        } catch (IOException e) {
            log.error("Error loading properties file",e);
        }
    }

}
