package com.trello.tests;

import com.trello.api.TrelloApiService;
import com.trello.config.TrelloConfig;
import com.trello.models.Board;
import com.trello.steps.TestSteps;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;

import java.util.HashMap;
import java.util.Map;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class RetrofitTest extends BaseTest {

    private TrelloApiService service;
    private String boardId;
    private String shortUrl;
    private String closed = "false";

    @BeforeClass
    public void initRetrofit() {
        // building the okhttp client with auth interceptor
        // so we dont manually pass key and token into every req
        OkHttpClient client = new OkHttpClient.Builder()
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    HttpUrl urlWithAuth = original.url().newBuilder()
                            .addQueryParameter("key", TrelloConfig.KEY)
                            .addQueryParameter("token", TrelloConfig.TOKEN)
                            .build();
                    return chain.proceed(original.newBuilder().url(urlWithAuth).build());
                })
                .build();

        // retrofit baseUrl must end with '/'
        String baseUrl = TrelloConfig.BASE_URL.endsWith("/") ? TrelloConfig.BASE_URL : TrelloConfig.BASE_URL + "/";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(JacksonConverterFactory.create())
                .build();

        service = retrofit.create(TrelloApiService.class);
    }

    // POST
    @Test(priority = 1)
    public void testCreateBoard() throws Exception {
        Response<Board> res = service.createBoard(Map.of(
                "name", "Retrofit Test Board",
                "desc", "Created via Retrofit"
        )).execute();

        assertEquals(res.code(), 200, "Expected HTTP 200 on POST");
        assertNotNull(res.body(), "Response body should not be null");

        Board actual = res.body();

        assertNotNull(actual.getId(), "Board ID should not be null");
        assertNotNull(actual.getShortUrl(), "Short URL should not be null");

        this.boardId = actual.getId();
        this.shortUrl = actual.getShortUrl();
    }

    // GET
    @Test(priority = 2, dependsOnMethods = "testCreateBoard")
    public void testGetBoard() throws Exception {
        Response<Board> res = service.getBoard(this.boardId).execute();

        assertEquals(res.code(), 200, "Expected HTTP 200 on GET");

        Board.BoardBuilder expectedBuilder = Board.builder()
                .id(this.boardId)
                .name("Retrofit Test Board")
                .desc("Created via Retrofit")
                .shortUrl(this.shortUrl);
        if (closed != null) {
            expectedBuilder.closed(this.closed);
        }
        TestSteps.checkActualVsExpectedResponses(res.body(), TestSteps.prepareExpectedResponse(expectedBuilder));
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

        Response<Board> res = service.updateBoard(this.boardId, updateParams).execute();
        assertEquals(res.code(), 200, "Expected HTTP 200 on PUT");

        Board expected = TestSteps.prepareExpectedResponse(
                Board.builder()
                        .id(this.boardId)
                        .name(newName)
                        .desc(newDesc)
                        .shortUrl(this.shortUrl)
                        .closed(this.closed)
        );

        TestSteps.checkActualVsExpectedResponses(res.body(), expected);
    }

    // DELETE
    @Test(priority = 4, dependsOnMethods = "testCreateBoard")
    public void testDeleteBoard() throws Exception{
        Response<Board> res = service.deleteBoard(this.boardId).execute();
        assertEquals(res.code(), 200, "Expected HTTP 200 on DELETE");


        Response<Board> getRes = service.getBoard(this.boardId).execute();
        assertEquals(getRes.code(), 404, "Expected HTTP 404 on GET after DELETE");
    }
}
