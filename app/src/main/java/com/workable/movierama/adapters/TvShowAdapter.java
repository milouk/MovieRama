package com.workable.movierama.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.github.ivbaranov.mfb.MaterialFavoriteButton;
import com.workable.movierama.R;
import com.workable.movierama.TvShowDetailActivity;
import com.workable.movierama.models.TvShow;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class TvShowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int ITEM = 0;
    private static final int LOADING = 1;
    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/original";
    private List<TvShow> tvShowResults;
    private Context context;
    private boolean isLoadingAdded = false;
    private SharedPreferences favorites;

    public TvShowAdapter(Context context) {
        this.context = context;
        tvShowResults = new ArrayList<>();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        RecyclerView.ViewHolder viewHolder = null;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        favorites = context.getSharedPreferences("favorites", MODE_PRIVATE);

        switch (viewType) {
            case ITEM:
                viewHolder = getViewHolder(parent, inflater);
                break;
            case LOADING:
                View v2 = inflater.inflate(R.layout.load_more, parent, false);
                viewHolder = new TvShowAdapter.LoadingVH(v2);
                break;
        }
        return viewHolder;
    }


    @NonNull
    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        RecyclerView.ViewHolder viewHolder;
        View v1 = inflater.inflate(R.layout.movie_item, parent, false);
        viewHolder = new TvShowAdapter.TvShowVH(v1);
        return viewHolder;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final TvShow result = tvShowResults.get(position); // TvShow

        switch (getItemViewType(position)) {
            case ITEM:
                final TvShowAdapter.TvShowVH tvShowVH = (TvShowAdapter.TvShowVH) holder;

                if (result.getId() != null) {
                    //Restore favorite state
                    if (favorites.contains(result.getId().toString())) {
                        tvShowVH.materialFavoriteButton.setFavorite(favorites.getBoolean(result.getId()
                                .toString(), false));
                    } else {
                        tvShowVH.materialFavoriteButton.setFavorite(false);
                    }
                }

                //Change favorite status
                tvShowVH.materialFavoriteButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        SharedPreferences.Editor edit = favorites.edit();
                        if (favorites.getBoolean(result.getId().toString(), false)) {
                            //Save Movie ID instead of Title because of title collisions e.g Alladin
                            edit.putBoolean(result.getId().toString(), false);
                            tvShowVH.materialFavoriteButton.setFavorite(false);
                        } else {
                            edit.putBoolean(result.getId().toString(), true);
                            tvShowVH.materialFavoriteButton.setFavorite(true);

                        }
                        edit.apply();
                    }
                });

                tvShowVH.mRating.setText(result.getVoteAverage() + "/10");
                tvShowVH.mMovieTitle.setText(result.getOriginalName());
                tvShowVH.mDate.setText(result.getFirstAirDate());
                //Fetch Backdrop Image
                //Backdrop was used instead of Poster due to Poster dimensions not being
                //suitable for the the cardView in movie_item.xml
                if (result.getBackdropPath() != null) {
                    Glide
                            .with(context)
                            .load(BASE_URL_IMG + result.getBackdropPath())
                            //Set ImageView progress while fetching Image
                            .listener(new RequestListener<Drawable>() {
                                @Override
                                public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                            Target<Drawable> target,
                                                            boolean isFirstResource) {
                                    tvShowVH.mProgress.setVisibility(View.GONE);
                                    return false;
                                }

                                @Override
                                public boolean onResourceReady(Drawable resource, Object model,
                                                               Target<Drawable> target,
                                                               DataSource dataSource,
                                                               boolean isFirstResource) {
                                    tvShowVH.mProgress.setVisibility(View.GONE);
                                    return false;
                                }
                            }).apply(new RequestOptions()
                            // cache both original & resized image
                            .diskCacheStrategy(DiskCacheStrategy.ALL)
                            .centerCrop())
                            .into(tvShowVH.mPosterImg);
                }
                break;
            case LOADING:
                //Do nothing
                break;
        }
    }

    @Override
    public int getItemCount() {
        return tvShowResults == null ? 0 : tvShowResults.size();
    }

    @Override
    public int getItemViewType(int position) {
        return (position == tvShowResults.size() - 1 && isLoadingAdded) ? LOADING : ITEM;
    }

    //Add a movie to the adapter
    private void add(TvShow r) {
        tvShowResults.add(r);
        notifyItemInserted(tvShowResults.size() - 1);
    }

    //Add a set of movies to the adapter
    public void addAll(List<TvShow> tvShowResults) {
        for (TvShow result : tvShowResults) {
            add(result);
        }
    }

    //Remove a movie from the adapter
    private void remove(TvShow r) {
        int position = tvShowResults.indexOf(r);
        if (position > -1) {
            tvShowResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    //Clear the adapter
    public void clear() {
        isLoadingAdded = false;
        while (getItemCount() > 0) {
            remove(getItem(0));
        }
    }

    public void addLoadingFooter() {
        isLoadingAdded = true;
        add(new TvShow());
    }

    public void removeLoadingFooter() {
        isLoadingAdded = false;

        int position = tvShowResults.size() - 1;
        TvShow result = getItem(position);

        if (result != null) {
            tvShowResults.remove(position);
            notifyItemRemoved(position);
        }
    }

    private TvShow getItem(int position) {
        return tvShowResults.get(position);
    }

    //Tv Show ViewHolder
    class TvShowVH extends RecyclerView.ViewHolder {
        private TextView mMovieTitle, mDate, mRating;
        private ImageView mPosterImg;
        private ProgressBar mProgress;
        private MaterialFavoriteButton materialFavoriteButton;

        TvShowVH(View itemView) {
            super(itemView);

            mMovieTitle = itemView.findViewById(R.id.movie_title);
            mDate = itemView.findViewById(R.id.movie_date);
            mPosterImg = itemView.findViewById(R.id.movie_poster);
            mProgress = itemView.findViewById(R.id.image_progress_bar);
            materialFavoriteButton = itemView.findViewById(R.id.favorite);
            mRating = itemView.findViewById(R.id.rating_main);

            //Pass TvShow object to the TvShowDetailActivity
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        TvShow clickedDataItem = tvShowResults.get(pos);
                        Intent intent = new Intent(context, TvShowDetailActivity.class);
                        intent.putExtra("tv", clickedDataItem);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
            });
        }
    }


    protected class LoadingVH extends RecyclerView.ViewHolder {

        LoadingVH(View itemView) {
            super(itemView);
        }
    }

}
