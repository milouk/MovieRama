package com.workable.movierama;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.CollapsingToolbarLayout;
import com.workable.movierama.adapters.SimilarMovieAdapter;
import com.workable.movierama.models.Cast;
import com.workable.movierama.models.CreditApiResponse;
import com.workable.movierama.models.Crew;
import com.workable.movierama.models.Movie;
import com.workable.movierama.models.MovieApiResponse;
import com.workable.movierama.models.Review;
import com.workable.movierama.models.ReviewApiResponse;
import com.workable.movierama.api.FetchData;
import com.workable.movierama.api.Client;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class DetailActivity extends AppCompatActivity {

    private TextView nameOfMovie;
    private TextView releaseDate;
    private TextView genre;
    private TextView author1;
    private TextView author2;
    private TextView review1;
    private TextView review2;
    private TextView director;
    private TextView actors;
    private ImageView divider;
    private MaterialFavoriteButton mFavorite;

    private SharedPreferences favorites;
    private FetchData movieService;
    private SparseArray<String> genres = new SparseArray<>();

    private SimilarMovieAdapter similarMovieAdapter;

    private String movieName;
    int movie_id;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //Set toolbar with Back Arrow
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        initCollapsingToolbar();

        //Setup Similar Movies Recycler View
        RecyclerView similarMovies = findViewById(R.id.similar_movies);
        similarMovieAdapter = new SimilarMovieAdapter(this);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this,
                RecyclerView.HORIZONTAL, false);
        similarMovies.setLayoutManager(linearLayoutManager);
        similarMovies.setItemAnimator(new DefaultItemAnimator());

        similarMovies.setAdapter(similarMovieAdapter);

        author1 = findViewById(R.id.author1);
        author2 = findViewById(R.id.author2);
        review1 = findViewById(R.id.review1);
        review2 = findViewById(R.id.review2);
        actors = findViewById(R.id.cast);
        director = findViewById(R.id.director);
        genre = findViewById(R.id.genre);
        ImageView posterImage = findViewById(R.id.thumbnail_image_header);
        divider = findViewById(R.id.divider);
        nameOfMovie = findViewById(R.id.title);
        TextView plot = findViewById(R.id.description);
        TextView userRating = findViewById(R.id.rating_main);
        releaseDate = findViewById(R.id.release);
        mFavorite = findViewById(R.id.favorite_button_details);


        //Fetch Clicked Movie Data
        Intent intentThatStartedThisActivity = getIntent();
        if (intentThatStartedThisActivity.hasExtra("movies")) {

            Movie movie = getIntent().getParcelableExtra("movies");

            String thumbnail = movie.getPosterPath();
            movieName = movie.getOriginalTitle();
            String synopsis = movie.getOverview();
            String rating = Double.toString(movie.getVoteAverage());
            String dateOfRelease = movie.getReleaseDate();
            movie_id = movie.getId();
            List<Integer> genreIds = movie.getGenreIds();

            //Restore Favorite Status
            favorites = this.getSharedPreferences("favorites", MODE_PRIVATE);
            if (favorites.contains(String.valueOf(movie_id))) {
                mFavorite.setFavorite(favorites.getBoolean(String.valueOf(movie_id), false));
            } else {
                mFavorite.setFavorite(false);
            }

            fillGenreArray();

            //Fetch Poster as a similar movie thumbnail
            String poster = "https://image.tmdb.org/t/p/original" + thumbnail;
            Glide.with(this)
                    .load(poster)
                    .into(posterImage);

            //Build Genre String
            StringBuilder movieGenres = new StringBuilder(100);
            for (int i = 0; i < genreIds.size(); i++) {
                movieGenres.append(genres.get(genreIds.get(i))).append(", ");
            }

            nameOfMovie.setText(movieName + "\n\n");
            plot.setText("\n" + synopsis);
            userRating.setText("\n" + rating + "/10");
            releaseDate.setText(dateOfRelease);
            //Omit the last ", "
            genre.setText(movieGenres.substring(0, movieGenres.length() - 2));
        } else {
            Toast.makeText(this, "No API Data", Toast.LENGTH_SHORT).show();
        }

        //Change favorite status and save it to SharedPreferences
        mFavorite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor edit = favorites.edit();
                if (favorites.getBoolean(String.valueOf(movie_id), false)) {
                    //Save Movie ID instead of Title because of title collisions e.g Alladin
                    edit.putBoolean(String.valueOf(movie_id), false);
                    mFavorite.setFavorite(false);
                } else {
                    edit.putBoolean(String.valueOf(movie_id), true);
                    mFavorite.setFavorite(true);
                }
                edit.apply();
            }
        });

        movieService = Objects.requireNonNull(Client.INSTANCE.getClient()).create(FetchData.class);
        loadJSON();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu
        getMenuInflater().inflate(R.menu.settings_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.settings) {
            Intent intent = new Intent(DetailActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    //Collapsing Toolbar Operations. Sets Title accordingly etc.
    private void initCollapsingToolbar() {
        final CollapsingToolbarLayout collapsingToolbarLayout =
                findViewById(R.id.collapsing_toolbar);
        collapsingToolbarLayout.setTitle(" ");
        AppBarLayout appBarLayout = findViewById(R.id.appbar);
        appBarLayout.setExpanded(true);

        appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isShow = false;
            int scrollRange = -1;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    collapsingToolbarLayout.setTitle(movieName);
                    nameOfMovie.setVisibility(View.GONE);
                    genre.setVisibility(View.GONE);
                    releaseDate.setVisibility(View.GONE);
                    isShow = true;
                } else if (isShow) {
                    collapsingToolbarLayout.setTitle(" ");
                    nameOfMovie.setVisibility(View.VISIBLE);
                    genre.setVisibility(View.VISIBLE);
                    releaseDate.setVisibility(View.VISIBLE);
                    isShow = false;
                }
            }
        });
    }

    private void loadJSON() {

        //Get Movie Reviews.
        callMovieReviews().enqueue(new Callback<ReviewApiResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<ReviewApiResponse> call,
                                   @NonNull Response<ReviewApiResponse> response) {
                // Got reviews data.
                List<Review> reviews = fethReviews(response);
                assert reviews != null;
                int numberOfReviews = reviews.size();
                String sauthor1;
                String sreview1;
                //Set reviews according to the fetched number (up to 2)
                switch (numberOfReviews) {
                    case 0:
                        review1.setText("\n\n" + R.string.no_reviews);
                        review2.setVisibility(View.GONE);
                        divider.setVisibility(View.GONE);
                        break;
                    case 1:
                        sauthor1 = reviews.get(0).getAuthor();
                        sreview1 = reviews.get(0).getContent();
                        author1.setText("\n\n" + sauthor1);
                        review1.setText("\n\n" + sreview1);
                        review2.setVisibility(View.GONE);
                        divider.setVisibility(View.GONE);
                        break;
                    default:
                        sauthor1 = reviews.get(0).getAuthor();
                        sreview1 = reviews.get(0).getContent();
                        String sauthor2 = reviews.get(1).getAuthor();
                        String sreview2 = reviews.get(1).getContent();
                        author1.setText("\n\n" + sauthor1);
                        review1.setText("\n\n" + sreview1);
                        author2.setText(sauthor2);
                        review2.setText("\n" + sreview2);
                        break;
                }
            }

            @Override
            public void onFailure(@NonNull Call<ReviewApiResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });

        //Get Credits (Cast, Director)
        callCredits().enqueue(new Callback<CreditApiResponse>() {
            @SuppressLint("SetTextI18n")
            @Override
            public void onResponse(@NonNull Call<CreditApiResponse> call,
                                   @NonNull Response<CreditApiResponse> response) {
                // Got Actor data.
                List<Cast> cast = fetchCast(response);
                List<Crew> crew = fetchCrew(response);
                StringBuilder movieCast = new StringBuilder(1000);
                assert cast != null;
                for (int i = 0; i < cast.size(); i++) {
                    movieCast.append(cast.get(i).getCastName()).append(", ");
                }
                actors.setText("\n" + movieCast.substring(0, movieCast.length() - 2));

                //Get Directors Name.
                assert crew != null;
                for (int i = 0; i < crew.size(); i++) {
                    if (crew.get(i).getCrewJob().equals("Director")) {
                        director.setText("\n" + crew.get(i).getCrewName());
                        break;
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<CreditApiResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });

        //Get Similar Movies
        callSimilarMovies().enqueue(new Callback<MovieApiResponse>() {
            @Override
            public void onResponse(@NonNull Call<MovieApiResponse> call,
                                   @NonNull Response<MovieApiResponse> response) {
                // Got movies data. Send it to adapter
                List<Movie> similarMovies = fetchMovies(response);
                similarMovieAdapter.addAll(similarMovies);
            }

            @Override
            public void onFailure(@NonNull Call<MovieApiResponse> call, @NonNull Throwable t) {
                t.printStackTrace();
            }
        });
    }

    //Values were taken from
    // http://api.themoviedb.org/3/genre/movie/list?api_key=&append_to_response=credits
    private void fillGenreArray() {
        genres.put(28, "Action");
        genres.put(12, "Adventure");
        genres.put(16, "Animation");
        genres.put(35, "Comedy");
        genres.put(80, "Crime");
        genres.put(99, "Documentary");
        genres.put(18, "Drama");
        genres.put(10751, "Family");
        genres.put(14, "Fantasy");
        genres.put(36, "History");
        genres.put(27, "Horror");
        genres.put(10402, "Music");
        genres.put(9648, "Mystery");
        genres.put(10749, "Romance");
        genres.put(878, "Sci-Fi");
        genres.put(10770, "TV Movie");
        genres.put(53, "Thriller");
        genres.put(10752, "War");
        genres.put(37, "Western");
    }

    //Fetch Movies
    private List<Movie> fetchMovies(Response<MovieApiResponse> response) {
        MovieApiResponse similar = response.body();
        if (similar != null) {
            return similar.getMovies();
        }
        return null;
    }

    //Fetch Reviews
    private List<Review> fethReviews(Response<ReviewApiResponse> response) {
        ReviewApiResponse reviews = response.body();
        if (reviews != null) {
            return reviews.getReviews();
        }
        return null;
    }

    //Fetch Cast
    private List<Cast> fetchCast(Response<CreditApiResponse> response) {
        CreditApiResponse cast = response.body();
        if (cast != null) {
            return cast.getCast();
        }
        return null;
    }

    //Fetch Crew
    private List<Crew> fetchCrew(Response<CreditApiResponse> response) {
        CreditApiResponse crew = response.body();
        if (crew != null) {
            return crew.getCrew();
        }
        return null;
    }

    //Get Similar Movies
    private Call<MovieApiResponse> callSimilarMovies() {
        return movieService.getSimilarMovies(String.valueOf(movie_id),
                BuildConfig.THE_MOVIE_DB_API_TOKEN);
    }

    //Get Reviews
    private Call<ReviewApiResponse> callMovieReviews() {
        return movieService.getMovieReviews(String.valueOf(movie_id),
                BuildConfig.THE_MOVIE_DB_API_TOKEN);
    }

    //Get Credits
    private Call<CreditApiResponse> callCredits() {
        return movieService.getCredits(String.valueOf(movie_id),
                BuildConfig.THE_MOVIE_DB_API_TOKEN);
    }

    //When Toolbar Back Arrow get clicked go back.
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}
