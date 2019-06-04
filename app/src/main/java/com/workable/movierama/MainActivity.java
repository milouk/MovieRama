package com.workable.movierama;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.workable.movierama.adapters.MovieAdapter;
import com.workable.movierama.models.Movie;
import com.workable.movierama.models.MovieApiResponse;
import com.workable.movierama.api.FetchData;
import com.workable.movierama.api.Client;
import com.workable.movierama.utilities.MovieScrollListener;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private MovieAdapter movieAdapter;
    private ProgressBar movieProgress;
    private SwipeRefreshLayout swipeRefreshLayout;

    private static final int PAGE_START = 1;
    private boolean isLoading = false;
    private boolean isLastPage = false;
    //Show 10 pages of results
    private int TOTAL_PAGES = 10;
    private int currentPage = PAGE_START;

    private FetchData movieService;
    private CharSequence searchTerm;
    private String origin = "popular";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Set Action Bar Title and Bg Color
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Home");
            this.getSupportActionBar().setBackgroundDrawable(new ColorDrawable(ContextCompat
                    .getColor(this, R.color.colorPrimaryDark)));
        }

        swipeRefreshLayout = findViewById(R.id.swipe_layout);
        EditText searchBar = findViewById(R.id.search_bar);

        RecyclerView movieRecycler = findViewById(R.id.movie_recycler);
        movieProgress = findViewById(R.id.movie_progress);

        movieAdapter = new MovieAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                RecyclerView.VERTICAL,
                false);
        movieRecycler.setLayoutManager(linearLayoutManager);
        movieRecycler.setItemAnimator(new DefaultItemAnimator());

        movieRecycler.setAdapter(movieAdapter);


        //Dynamically show search results
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                searchTerm = charSequence;
                if (charSequence.toString().equals("")) {
                    origin = "popular";
                } else {
                    origin = "search";
                }
                movieAdapter.clear();
                loadFirstPage();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        //Infinite Scroll
        movieRecycler.addOnScrollListener(new MovieScrollListener(linearLayoutManager) {
            @Override
            protected void loadMoreItems() {
                isLoading = true;
                currentPage += 1;

                // mocking network delay for API call
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        loadNextPage();
                    }
                }, 1000);
            }

            @Override
            public int getTotalPageCount() {
                return TOTAL_PAGES;
            }

            @Override
            public boolean isLastPage() {
                return isLastPage;
            }

            @Override
            public boolean isLoading() {
                return isLoading;
            }
        });

        //Swipe down to refresh
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                movieAdapter.clear();
                //Reload from Page 1
                currentPage = PAGE_START;
                //Reload data
                loadFirstPage();
                //Hide loading bar
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        //init service and load data
        movieService = Objects.requireNonNull(Client.INSTANCE.getClient()).create(FetchData.class);
        loadFirstPage();
    }

    //Inflate Options Menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //Settings option Clicked
        if (id == R.id.settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        movieAdapter.clear();
        loadFirstPage();
        super.onResume();
    }

    private void loadFirstPage() {

        //If the origin of the method call is NOT a search result
        if (origin.equals("popular") || searchTerm.toString().equals("")) {
            callPopularMovies().enqueue(new Callback<MovieApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<MovieApiResponse> call,
                                       @NonNull Response<MovieApiResponse> response) {
                    // Got data. Send it to adapter
                    List<Movie> results = fetchResults(response);
                    movieProgress.setVisibility(View.GONE);
                    movieAdapter.addAll(results);

                    if (currentPage <= TOTAL_PAGES) movieAdapter.addLoadingFooter();
                    else isLastPage = true;
                }

                @Override
                public void onFailure(@NonNull Call<MovieApiResponse> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });
            //if the origin of the method call is a search result
        } else if (origin.equals("search")) {
            callSearchResultMovies().enqueue(new Callback<MovieApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<MovieApiResponse> call,
                                       @NonNull Response<MovieApiResponse> response) {
                    // Got data. Send it to adapter
                    List<Movie> results = fetchResults(response);
                    movieProgress.setVisibility(View.GONE);
                    movieAdapter.addAll(results);
                    if (currentPage <= TOTAL_PAGES) movieAdapter.addLoadingFooter();
                    else isLastPage = true;
                }

                @Override
                public void onFailure(@NonNull Call<MovieApiResponse> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    //Fetch Popular Movies
    private List<Movie> fetchResults(Response<MovieApiResponse> response) {
        MovieApiResponse popular = response.body();
        if (popular != null) {
            return popular.getMovies();
        }
        return null;
    }

    private void loadNextPage() {

        if (origin.equals("popular") || searchTerm.toString().equals("")) {
            callPopularMovies().enqueue(new Callback<MovieApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<MovieApiResponse> call,
                                       @NonNull Response<MovieApiResponse> response) {
                    movieAdapter.removeLoadingFooter();
                    isLoading = false;
                    List<Movie> results = fetchResults(response);
                    movieAdapter.addAll(results);

                    if (currentPage != TOTAL_PAGES) movieAdapter.addLoadingFooter();
                    else isLastPage = true;
                }

                @Override
                public void onFailure(@NonNull Call<MovieApiResponse> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });
        } else if (origin.equals("search")) {
            callSearchResultMovies().enqueue(new Callback<MovieApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<MovieApiResponse> call,
                                       @NonNull Response<MovieApiResponse> response) {
                    movieAdapter.removeLoadingFooter();
                    isLoading = false;
                    List<Movie> results = fetchResults(response);
                    movieAdapter.addAll(results);

                    if (currentPage != TOTAL_PAGES) movieAdapter.addLoadingFooter();
                    else isLastPage = true;
                }

                @Override
                public void onFailure(@NonNull Call<MovieApiResponse> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    //Ping the Popular movies endpoint
    private Call<MovieApiResponse> callPopularMovies() {
        return movieService.getPopularMovies(
                BuildConfig.THE_MOVIE_DB_API_TOKEN,
                currentPage
        );
    }

    //Ping the search movies endpoint
    private Call<MovieApiResponse> callSearchResultMovies() {
        return movieService.getSearchResultMovies(
                BuildConfig.THE_MOVIE_DB_API_TOKEN,
                searchTerm, currentPage
        );
    }
}
