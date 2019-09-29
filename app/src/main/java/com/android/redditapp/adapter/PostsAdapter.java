package com.android.redditapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.redditapp.Post;
import com.android.redditapp.R;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.PostsViewHolder> {

    private Context context;
    private ArrayList<Post> posts = new ArrayList<>();

    public PostsAdapter(Context context, ArrayList<Post> posts) {
        this.context = context;
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.card_layout_main, viewGroup, false);
        return new PostsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PostsViewHolder postsViewHolder, int i) {
        String imageURL = posts.get(i).getThumbnailURL();
        String title = posts.get(i).getTitle();
        String author = posts.get(i).getAuthor();
        String updated = posts.get(i).getDate_updated();

        postsViewHolder.progressBar.setVisibility(View.VISIBLE);

        Picasso.with(context).load(imageURL)
                .placeholder(R.drawable.ic_image_failed)
                .error(R.drawable.ic_image_failed)
                .into(postsViewHolder.thumbnailImageView, new Callback() {
                    @Override
                    public void onSuccess() {
                        postsViewHolder.progressBar.setVisibility(View.GONE);
                    }

                    @Override
                    public void onError() {
                        postsViewHolder.progressBar.setVisibility(View.GONE);
                    }
                });

        postsViewHolder.titleTextView.setText(title);
        postsViewHolder.authorTextView.setText(author);
        postsViewHolder.updatedTextView.setText(updated);
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    public class PostsViewHolder extends RecyclerView.ViewHolder {

        ImageView thumbnailImageView;
        TextView titleTextView, authorTextView, updatedTextView;
        ProgressBar progressBar;

        public PostsViewHolder(@NonNull View itemView) {
            super(itemView);

            thumbnailImageView = itemView.findViewById(R.id.cardImage);
            titleTextView = itemView.findViewById(R.id.cardTitle);
            authorTextView = itemView.findViewById(R.id.cardAuthor);
            updatedTextView = itemView.findViewById(R.id.cardUpdated);
            progressBar = itemView.findViewById(R.id.cardProgressDialog);
        }
    }
}
