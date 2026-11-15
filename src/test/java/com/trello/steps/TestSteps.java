package com.trello.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trello.config.TrelloConfig;
import com.trello.models.Board;
import com.trello.tests.BaseTest;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.assertEquals;

public class TestSteps extends BaseTest {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static HttpUriRequest buildRequest(String method, String endpoint, Map<String, String> queryParams) throws URISyntaxException {
        URIBuilder uri = new URIBuilder(TrelloConfig.BASE_URL + endpoint)
                .addParameter("key", TrelloConfig.KEY)
                .addParameter("token", TrelloConfig.TOKEN);

        if (queryParams != null) {
            queryParams.forEach((key, value) -> {
                uri.addParameter(key, value);
            });
        }
        URI builtUri = uri.build();
        switch (method.toUpperCase()) {
            case "GET":
                return new HttpGet(builtUri);
            case "POST":
                return new HttpPost(builtUri);
            case "PUT":
                return new HttpPut(builtUri);
            case "DELETE":
                return new HttpDelete(builtUri);
            default:
                throw new IllegalArgumentException("Method " + method + " not supported :(");
        }
    }

    public static HttpResponse sendRequest(HttpUriRequest request) throws IOException {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            return httpClient.execute(request);
        }
    }

    public static void checkResponseIsValid(HttpResponse response, int expectedCode) {
        assertEquals(response.getStatusLine().getStatusCode(), expectedCode);
    }

    public static Board prepareActualResponse(HttpResponse response) throws IOException {
        String body = EntityUtils.toString(response.getEntity());
        return OBJECT_MAPPER.readValue(body, Board.class);
    }

    public static Board prepareExpectedResponse(Board.BoardBuilder builder) {
        return builder.build();
    }

    public static void checkActualVsExpectedResponses(Board actual, Board expected) {
        assertThat(actual).isNotNull();
        assertThat(actual.getName()).isEqualTo(expected.getName());
        assertThat(actual.getDesc()).isEqualTo(expected.getDesc());
        assertThat(actual.getId()).isEqualTo(expected.getId());
        assertThat(actual.getShortUrl()).isEqualTo(expected.getShortUrl());
        assertThat(actual.getClosed()).isEqualTo(expected.getClosed());
    }
}
