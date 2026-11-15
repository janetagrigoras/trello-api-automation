package com.trello.config;

public class TrelloConfig {
    public static final String BASE_URL = "https://api.trello.com/1/";
    public static final String KEY = System.getenv("TRELLO_KEY");
    public static final String TOKEN = System.getenv("TRELLO_TOKEN");
}
