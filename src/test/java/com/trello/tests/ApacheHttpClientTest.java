package com.trello.tests;

import com.trello.models.Board;
import com.trello.steps.TestSteps;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpUriRequest;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class ApacheHttpClientTest extends BaseTest {
    private String boardId;
    private String shortUrl;
    private String closed = "false";

    // POST
    @Test(priority = 1)
    public void testCreateBoard() throws Exception {
        HttpUriRequest req = TestSteps.buildRequest("POST", "/boards/", Map.of("name", "Apache Test Board", "desc", "My Apache Board"));
        HttpResponse res = TestSteps.sendRequest(req);

        TestSteps.checkResponseIsValid(res, 200);

        Board actual = TestSteps.prepareActualResponse(res);

        assertThat(actual.getId()).isNotNull();

        assertThat(actual.getShortUrl()).isNotNull();

        this.boardId = actual.getId();
        this.shortUrl = actual.getShortUrl();
    }

    // GET
    @Test(priority = 2, dependsOnMethods = "testCreateBoard")
    public void testGetBoard() throws Exception {
        HttpUriRequest req = TestSteps.buildRequest("GET", "/boards/" + this.boardId, null);
        HttpResponse res = TestSteps.sendRequest(req);

        TestSteps.checkResponseIsValid(res, 200);
        Board actual = TestSteps.prepareActualResponse(res);
        Board.BoardBuilder expectedBuilder = Board.builder()
                        .id(this.boardId)
                        .name("Apache Test Board")
                        .desc("My Apache Board")
                        .shortUrl(this.shortUrl);
        if (closed != null) {
            expectedBuilder.closed(this.closed);
        }
        TestSteps.checkActualVsExpectedResponses(actual, TestSteps.prepareExpectedResponse(expectedBuilder));
    }

    @DataProvider(name = "boardUpdateData")
    public Object[][] boardUpdateData() {
        return new Object[][] {
            { "Updated Name Only", "", null },
            { "Updated Name & Desc", "New board description", null },
            { "Special Chars Board @#!", "Testing spaces & symbols", null },
            { "Close board", "This board will be closed", "true" }
        };
    }

    // PUT
    @Test(dataProvider = "boardUpdateData", priority = 3, dependsOnMethods = "testCreateBoard")
    public void testUpdateBoard(String newName, String newDesc, String closed) throws Exception {
        Map<String, String> updateParams = new HashMap<>();
        updateParams.put("name", newName);
        updateParams.put("desc", newDesc);
        if (closed != null) {
            updateParams.put("closed", closed);
            this.closed = closed;
        }

        HttpUriRequest req = TestSteps.buildRequest("PUT", "/boards/" + boardId, updateParams);
        HttpResponse res = TestSteps.sendRequest(req);

        TestSteps.checkResponseIsValid(res, 200);

        Board actual = TestSteps.prepareActualResponse(res);
        Board expected = TestSteps.prepareExpectedResponse(
                Board.builder()
                        .id(this.boardId)
                        .name(newName)
                        .desc(newDesc)
                        .shortUrl(this.shortUrl)
                        .closed(this.closed)
        );

        TestSteps.checkActualVsExpectedResponses(actual, expected);
    }

    // DELETE
    @Test(priority = 4, dependsOnMethods = "testCreateBoard")
    public void testDeleteBoard() throws Exception{
        HttpUriRequest req = TestSteps.buildRequest("DELETE", "/boards/" + boardId, null);
        HttpResponse res = TestSteps.sendRequest(req);

        TestSteps.checkResponseIsValid(res, 200);

        HttpUriRequest getReq = TestSteps.buildRequest("GET", "/boards/" + boardId, null);
        HttpResponse getRes = TestSteps.sendRequest(getReq);
        TestSteps.checkResponseIsValid(getRes, 404);
    }
}
