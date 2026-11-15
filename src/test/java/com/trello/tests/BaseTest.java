package com.trello.tests;

import com.trello.config.TrelloConfig;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.testng.annotations.BeforeSuite;

public class BaseTest {
    @BeforeSuite
    public void configure() {
        RestAssured.baseURI = TrelloConfig.BASE_URL;
        RequestSpecification requestSpec = new RequestSpecBuilder()
                .setContentType(ContentType.JSON)
                .addQueryParam("key", TrelloConfig.KEY)
                .addQueryParam("token", TrelloConfig.TOKEN)
                .build();
        RestAssured.requestSpecification = requestSpec;
    }
}
