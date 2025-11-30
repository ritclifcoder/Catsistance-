package com.example.myapplication.WithingAPI;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

public interface WithingsApiService {
    
    @GET("measure")
    Call<WithingsResponse> getHealthData(
        @Header("Authorization") String authorization,
        @Query("action") String action,
        @Query("meastype") String meastype,
        @Query("category") String category,
        @Query("startdate") long startdate,
        @Query("enddate") long enddate
    );
    
    @GET("v2/measure")
    Call<WithingsResponse> getBloodPressure(
        @Header("Authorization") String authorization,
        @Query("action") String action,
        @Query("meastype") String meastype
    );
    
    @GET("v2/measure") 
    Call<WithingsResponse> getSteps(
        @Header("Authorization") String authorization,
        @Query("action") String action,
        @Query("meastype") String meastype,
        @Query("startdateymd") String startdate,
        @Query("enddateymd") String enddate
    );
    

    @GET("v2/measure")
    Call<WithingsResponse> getWeight(
        @Header("Authorization") String authorization,
        @Query("action") String action,
        @Query("meastype") String meastype
    );
}