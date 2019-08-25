package com.workable.movierama.fragments;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.github.clans.fab.FloatingActionMenu;
import com.workable.movierama.BuildConfig;
import com.workable.movierama.R;
import com.workable.movierama.SettingsActivity;
import com.workable.movierama.adapters.TvShowAdapter;
import com.workable.movierama.api.Client;
import com.workable.movierama.api.FetchData;
import com.workable.movierama.models.TvShow;
import com.workable.movierama.models.TvShowApiResponse;
import com.workable.movierama.utilities.MovieScrollListener;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TvShowFragment extends Fragment {

    private TvShowAdapter tvShowAdapter;
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
    private FloatingActionMenu fltnMenu;



    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        @SuppressLint("InflateParams") View view = inflater.inflate(R.layout.movierama_main_fragment, null);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        //Set Action Bar Title and Bg Color
        ActionBar actionBar = ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("Home");
            actionBar.setBackgroundDrawable(new ColorDrawable(ContextCompat
                    .getColor(Objects.requireNonNull(getContext()), R.color.colorPrimaryDark)));
        }

        swipeRefreshLayout = view.findViewById(R.id.swipe_layout);
        EditText searchBar = view.findViewById(R.id.search_bar);
        searchBar.setHint(R.string.search_tv_shows);

        fltnMenu = view.findViewById(R.id.fltn_menu);

        RecyclerView movieRecycler = view.findViewById(R.id.movie_recycler);
        movieProgress = view.findViewById(R.id.movie_progress);

        tvShowAdapter = new TvShowAdapter(getContext());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(),
                RecyclerView.VERTICAL,
                false);
        movieRecycler.setLayoutManager(linearLayoutManager);
        movieRecycler.setItemAnimator(new DefaultItemAnimator());

        movieRecycler.setAdapter(tvShowAdapter);


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
                tvShowAdapter.clear();
                loadFirstPage();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        //Hide Floating Menu on Scroll
        movieRecycler.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0 || dy < 0 && fltnMenu.isShown())
                    fltnMenu.hideMenu(true);
            }

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE)
                    fltnMenu.showMenu(true);
                super.onScrollStateChanged(recyclerView, newState);
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
                tvShowAdapter.clear();
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
    public void onCreateOptionsMenu(@NotNull Menu menu, @NotNull MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.settings_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        //Settings option Clicked
        if (id == R.id.settings) {
            Intent intent = new Intent(getActivity(), SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();
        tvShowAdapter.clear();
        loadFirstPage();
    }

    private void loadFirstPage() {

        //If the origin of the method call is NOT a search result
        if (origin.equals("popular") || searchTerm.toString().equals("")) {
            callPopularTvShows().enqueue(new Callback<TvShowApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<TvShowApiResponse> call,
                                       @NonNull Response<TvShowApiResponse> response) {
                    // Got data. Send it to adapter
                    List<TvShow> results = fetchResults(response);
                    movieProgress.setVisibility(View.GONE);
                    if (results != null) {
                        tvShowAdapter.addAll(results);
                    }

                    if (currentPage <= TOTAL_PAGES) tvShowAdapter.addLoadingFooter();
                    else isLastPage = true;
                }

                @Override
                public void onFailure(@NonNull Call<TvShowApiResponse> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });
            //if the origin of the method call is a search result
        } else if (origin.equals("search")) {
            callSearchResultTvShows().enqueue(new Callback<TvShowApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<TvShowApiResponse> call,
                                       @NonNull Response<TvShowApiResponse> response) {
                    // Got data. Send it to adapter
                    List<TvShow> results = fetchResults(response);
                    movieProgress.setVisibility(View.GONE);
                    if (results != null) {
                        tvShowAdapter.addAll(results);
                    }
                    if (currentPage <= TOTAL_PAGES) tvShowAdapter.addLoadingFooter();
                    else isLastPage = true;
                }

                @Override
                public void onFailure(@NonNull Call<TvShowApiResponse> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    //Fetch Popular Movies
    private List<TvShow> fetchResults(Response<TvShowApiResponse> response) {
        TvShowApiResponse popular = response.body();
        if (popular != null) {
            return popular.getTvShows();
        }
        return null;
    }

    private void loadNextPage() {

        if (origin.equals("popular") || searchTerm.toString().equals("")) {
            callPopularTvShows().enqueue(new Callback<TvShowApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<TvShowApiResponse> call,
                                       @NonNull Response<TvShowApiResponse> response) {
                    tvShowAdapter.removeLoadingFooter();
                    isLoading = false;
                    List<TvShow> results = fetchResults(response);
                    if (results != null) {
                        tvShowAdapter.addAll(results);
                    }

                    if (currentPage != TOTAL_PAGES) tvShowAdapter.addLoadingFooter();
                    else isLastPage = true;
                }

                @Override
                public void onFailure(@NonNull Call<TvShowApiResponse> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });
        } else if (origin.equals("search")) {
            callSearchResultTvShows().enqueue(new Callback<TvShowApiResponse>() {
                @Override
                public void onResponse(@NonNull Call<TvShowApiResponse> call,
                                       @NonNull Response<TvShowApiResponse> response) {
                    tvShowAdapter.removeLoadingFooter();
                    isLoading = false;
                    List<TvShow> results = fetchResults(response);
                    if (results != null) {
                        tvShowAdapter.addAll(results);
                    }

                    if (currentPage != TOTAL_PAGES) tvShowAdapter.addLoadingFooter();
                    else isLastPage = true;
                }

                @Override
                public void onFailure(@NonNull Call<TvShowApiResponse> call, @NonNull Throwable t) {
                    t.printStackTrace();
                }
            });
        }
    }

    //Ping the Popular movies endpoint
    private Call<TvShowApiResponse> callPopularTvShows() {
        return movieService.getPopularTvShows(
                BuildConfig.THE_MOVIE_DB_API_TOKEN,
                currentPage
        );
    }

    //Ping the search movies endpoint
    private Call<TvShowApiResponse> callSearchResultTvShows() {
        return movieService.getSearchResultTvShows(
                BuildConfig.THE_MOVIE_DB_API_TOKEN,
                searchTerm, currentPage
        );
    }


}
