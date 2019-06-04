package com.workable.movierama.api;

import com.workable.movierama.models.CreditApiResponse;
import com.workable.movierama.models.MovieApiResponse;
import com.workable.movierama.models.ReviewApiResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface FetchData {

    //Endpoint for fetching popular movies
    @GET("movie/popular")
    Call<MovieApiResponse> getPopularMovies(@Query("api_key") String apiKey, @Query("page") int pageIndex);

    //Endpoint for fetching search results
    @GET("search/movie")
    Call<MovieApiResponse> getSearchResultMovies(@Query("api_key") String apiKey, @Query("query") CharSequence charSequence, @Query("page") int pageIndex);

    //Endpoint for fetching similar movies
    @GET("movie/{movie_id}/similar")
    Call<MovieApiResponse> getSimilarMovies(@Path("movie_id") String movie_id, @Query("api_key") String apiKey);

    //Endpoint for fetching movie reviews
    @GET("movie/{movie_id}/reviews")
    Call<ReviewApiResponse> getMovieReviews(@Path("movie_id") String movie_id, @Query("api_key") String apiKey);

    //Endpoint for fetching movie credits e.g cast, crew
    @GET("movie/{movie_id}/credits")
    Call<CreditApiResponse> getCredits(@Path("movie_id") String movie_id, @Query("api_key") String apiKey);

}
