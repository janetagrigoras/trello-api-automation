package com.trello.tests;

import com.trello.models.Board;
import com.trello.steps.TestSteps;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

public class RestAssuredTest extends BaseTest {
    private String boardId;
    private String shortUrl;
    private String closed = "false";

    // POST
    @Test(priority = 1)
    public void testCreateBoard() throws Exception {
        Board actual = given()
                .queryParam("name", "RestAssured Test Board")
                .queryParam("desc", "Created via REST Assured")
            .when()
                .post("/boards/")
            .then()
                .statusCode(200)
                .extract().as(Board.class);

        assertThat(actual.getId()).isNotNull();
        assertThat(actual.getShortUrl()).isNotNull();

        this.boardId = actual.getId();
        this.shortUrl = actual.getShortUrl();
    }

    // GET
    @Test(priority = 2, dependsOnMethods = "testCreateBoard")
    public void testGetBoard() throws Exception {
        Board actual = given()
                .pathParam("id", this.boardId)
            .when()
                .get("/boards/{id}")
            .then()
                .statusCode(200)
                .extract().as(Board.class);

        Board.BoardBuilder expectedBuilder = Board.builder()
                .id(this.boardId)
                .name("RestAssured Test Board")
                .desc("Created via REST Assured")
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

        Board actual = given()
                .pathParam("id", this.boardId)
                .queryParams(updateParams)
            .when()
                .put("/boards/{id}")
            .then()
                .statusCode(200)
                .extract().as(Board.class);

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
        given()
                .pathParam("id", this.boardId)
            .when()
                .delete("/boards/{id}")
            .then()
                .statusCode(200);

        given()
                .pathParam("id", this.boardId)
            .when()
                .get("/boards/{id}")
            .then()
                .statusCode(404);
    }
}
