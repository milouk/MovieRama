package com.workable.movierama;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;

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
import com.workable.movierama.models.TvShow;

import java.util.ArrayList;
import java.util.List;

public class SimilarTvShowAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private static final String BASE_URL_IMG = "https://image.tmdb.org/t/p/original";
    private List<TvShow> similarTvShows;
    private Context context;


    public SimilarTvShowAdapter(Context context) {
        this.context = context;
        similarTvShows = new ArrayList<>();
    }


    @NonNull
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        RecyclerView.ViewHolder viewHolder;
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        viewHolder = getViewHolder(parent, inflater);
        return viewHolder;
    }


    private RecyclerView.ViewHolder getViewHolder(ViewGroup parent, LayoutInflater inflater) {
        RecyclerView.ViewHolder viewHolder;
        View v1 = inflater.inflate(R.layout.similar_movie_item, parent, false);
        viewHolder = new SimilarTvShowAdapter.SimilarTvShowVH(v1);
        return viewHolder;
    }


    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        final TvShow result = similarTvShows.get(position); // Movie

        final SimilarTvShowAdapter.SimilarTvShowVH tvShowVH =
                (SimilarTvShowAdapter.SimilarTvShowVH) holder;
        //Get Backdrop Image for the Similar Movie thumbnails
        Glide
                .with(context)
                .load(BASE_URL_IMG + result.getBackdropPath())
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        tvShowVH.mProgress.setVisibility(View.GONE);
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource, boolean isFirstResource) {
                        tvShowVH.mProgress.setVisibility(View.GONE);

                        return false;
                    }
                }).apply(new RequestOptions()
                // cache both original & resized image
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .centerCrop())
                .into(tvShowVH.mPosterImg);


    }


    @Override
    public int getItemCount() {
        return similarTvShows == null ? 0 : similarTvShows.size();
    }

    //Add A movie to the adapter
    private void add(TvShow r) {
        similarTvShows.add(r);
        notifyItemInserted(similarTvShows.size() - 1);
    }

    //Add a set of movies to the adapter
    public void addAll(List<TvShow> tvShowResults) {
        for (TvShow result : tvShowResults) {
            add(result);
        }
    }

    class SimilarTvShowVH extends RecyclerView.ViewHolder {
        private ImageView mPosterImg;
        private ProgressBar mProgress;

        SimilarTvShowVH(View itemView) {
            super(itemView);
            mPosterImg = itemView.findViewById(R.id.similar_movie_poster);
            mProgress = itemView.findViewById(R.id.similar_movie_progress);

            //when a similar movie is clicked pass that movie's data to the MovieDetailActivity
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos = getAdapterPosition();
                    if (pos != RecyclerView.NO_POSITION) {
                        TvShow clickedDataItem = similarTvShows.get(pos);
                        Intent intent = new Intent(context, TvShowDetailActivity.class);
                        intent.putExtra("tv", clickedDataItem);
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    }
                }
            });
        }
    }


}
