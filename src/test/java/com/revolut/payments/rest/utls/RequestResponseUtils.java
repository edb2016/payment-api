package com.revolut.payments.rest.utls;

import com.eb.revolut.payments.db.model.types.StandardResponse;
import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import spark.utils.IOUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.fail;

@Slf4j
public class RequestResponseUtils {

    public static StandardResponse requestJsonPost(String baseUrl, String jsonBody) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpPost request = new HttpPost(baseUrl);
            StringEntity params =new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            String body = IOUtils.toString(response.getEntity().getContent());
            return new Gson().fromJson(body, StandardResponse.class);
        } catch (Exception e) {
            log.error("Request failed ", e);
            fail("Request failed: " + e.getMessage());
        } finally {
            try {
                ((CloseableHttpClient) httpClient).close();
            } catch (IOException e) {
                log.error("Trying to close httpClient..");
            }
        }
        return null;
    }

    public static StandardResponse requestJsonPut(String baseUrl, String jsonBody) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpPut request = new HttpPut(baseUrl);
            StringEntity params =new StringEntity(jsonBody, ContentType.APPLICATION_JSON);
            request.addHeader("content-type", "application/json");
            request.setEntity(params);
            HttpResponse response = httpClient.execute(request);
            String body = IOUtils.toString(response.getEntity().getContent());
            return new Gson().fromJson(body, StandardResponse.class);
        } catch (Exception e) {
            log.error("Request failed ", e);
            fail("Request failed: " + e.getMessage());
        } finally {
            try {
                ((CloseableHttpClient) httpClient).close();
            } catch (IOException e) {
                log.error("Trying to close httpClient..");
            }
        }
        return null;
    }

    public static StandardResponse requestJsonGet(String baseUrl) {
        HttpClient httpClient = HttpClientBuilder.create().build();
        try {
            HttpGet httpget = new HttpGet(baseUrl);
            HttpResponse response = httpClient.execute(httpget);
            String body = IOUtils.toString(response.getEntity().getContent());
            return new Gson().fromJson(body, StandardResponse.class);
        } catch (Exception e) {
            log.error("Request failed ", e);
            fail("Request failed: " + e.getMessage());
        }
        return null;
    }

}
