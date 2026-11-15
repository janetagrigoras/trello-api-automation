package com.trello.api;

import com.trello.models.Board;
import retrofit2.Call;
import retrofit2.http.*;

import java.util.Map;

public interface TrelloApiService {
    @POST("boards/")
    Call<Board> createBoard(@QueryMap Map<String, String> params);

    @GET("boards/{id}")
    Call<Board> getBoard(@Path("id") String id);

    @PUT("boards/{id}")
    Call<Board> updateBoard(@Path("id") String id, @QueryMap Map<String, String> params);

    @DELETE("boards/{id}")
    Call<Board> deleteBoard(@Path("id") String id);
}
