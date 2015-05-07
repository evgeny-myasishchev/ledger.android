package com.infora.ledger.api;

import java.util.ArrayList;
import java.util.Date;

import retrofit.http.DELETE;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.PUT;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by jenya on 11.03.15.
 */
public interface LedgerApi {
    @FormUrlEncoded
    @POST("/api/sessions.json")
    AuthenticityToken authenticateByIdToken(@Field("google_id_token") String googleIdToken);

    @GET("/pending-transactions.json")
    ArrayList<PendingTransactionDto> getPendingTransactions();

    @FormUrlEncoded
    @POST("/pending-transactions")
    Void reportPendingTransaction(
            @Field("id") String transactionId,
            @Field("amount") String amount,
            @Field("comment") String comment,
            @Field("date") Date date);

    @FormUrlEncoded
    @PUT("/pending-transactions/{id}")
    Void adjustPendingTransaction(
            @Path("id") String transactionId,
            @Field("amount") String amount,
            @Field("comment") String comment);

    @DELETE("/pending-transactions/{id}")
    Void rejectPendingTransaction(@Path("id") String transactionId);
}
