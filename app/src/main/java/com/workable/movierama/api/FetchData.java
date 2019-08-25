package com.workable.movierama.api;

import com.workable.movierama.models.CreditApiResponse;
import com.workable.movierama.models.MovieApiResponse;
import com.workable.movierama.models.ReviewApiResponse;
import com.workable.movierama.models.TvShowApiResponse;

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
    Call<CreditApiResponse> getMovieCredits(@Path("movie_id") String movie_id, @Query("api_key") String apiKey);

    //Endpoint for fetching popular tv shows
    @GET("tv/popular")
    Call<TvShowApiResponse> getPopularTvShows(@Query("api_key") String apiKey, @Query("page") int pageIndex);

    //Endpoint for fetching search results
    @GET("search/tv")
    Call<TvShowApiResponse> getSearchResultTvShows(@Query("api_key") String apiKey, @Query("query") CharSequence charSequence, @Query("page") int pageIndex);

    //Endpoint for fetching similar tv shows
    @GET("tv/{tv_show_id}/similar")
    Call<TvShowApiResponse> getSimilarTvShows(@Path("tv_show_id") String tv_show_id, @Query("api_key") String apiKey);

    //Endpoint for fetching movie reviews
    @GET("tv/{tv_show_id}/reviews")
    Call<ReviewApiResponse> getTvShowReviews(@Path("tv_show_id") String tv_show_id, @Query("api_key") String apiKey);

    //Endpoint for fetching movie credits e.g cast, crew
    @GET("tv/{tv_show_id}/credits")
    Call<CreditApiResponse> getTvShowCredits(@Path("tv_show_id") String tv_show_id, @Query("api_key") String apiKey);

}
