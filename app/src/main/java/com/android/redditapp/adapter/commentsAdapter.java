package com.android.redditapp.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.redditapp.R;
import com.android.redditapp.interfaces.OnItemClickListener;
import com.android.redditapp.model.Comment;

import java.util.ArrayList;

public class CommentsAdapter extends RecyclerView.Adapter<CommentsAdapter.CommentsViewHolder> {

    private Context context;
    private ArrayList<Comment> comments = new ArrayList<>();
    private OnItemClickListener itemClickListener;

    public CommentsAdapter(Context context, ArrayList<Comment> comments, OnItemClickListener itemClickListener) {
        this.context = context;
        this.comments = comments;
        this.itemClickListener = itemClickListener;
    }

    @NonNull
    @Override
    public CommentsViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.comments_layout, viewGroup, false);
        return new CommentsViewHolder(view, itemClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentsViewHolder commentsViewHolder, int i) {
        String comment = comments.get(i).getComment();
        String author = comments.get(i).getAuthor();
        String updated = comments.get(i).getUpdated();

        commentsViewHolder.commentTextView.setText(comment);
        commentsViewHolder.commentAuthorTextView.setText(author);
        commentsViewHolder.commentUpdatedTextView.setText(updated);
        commentsViewHolder.commentProgressBar.setVisibility(View.GONE);
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class CommentsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView commentTextView, commentAuthorTextView, commentUpdatedTextView;
        ProgressBar commentProgressBar;

        OnItemClickListener clickListener;

        public CommentsViewHolder(@NonNull View itemView, OnItemClickListener clickListener) {
            super(itemView);

            commentTextView = itemView.findViewById(R.id.tv_comment);
            commentAuthorTextView = itemView.findViewById(R.id.tv_commentAuthor);
            commentUpdatedTextView = itemView.findViewById(R.id.tv_commentUpdated);
            commentProgressBar = itemView.findViewById(R.id.pb_comment);

            this.clickListener = clickListener;

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            clickListener.onItemClick(getAdapterPosition());
        }
    }
}
