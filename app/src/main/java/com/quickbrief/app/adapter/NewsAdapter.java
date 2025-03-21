package com.quickbrief.app.adapter;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.quickbrief.app.R;
import com.quickbrief.app.model.News;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final String TAG = "NewsAdapter";
    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;
    
    private List<News> newsList;
    private Context context;
    private boolean isLoadingAdded = false;

    public NewsAdapter(Context context, List<News> newsList) {
        this.context = context;
        this.newsList = newsList;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Log.d(TAG, "onCreateViewHolder: viewType=" + viewType);
        if (viewType == VIEW_TYPE_ITEM) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
            return new NewsViewHolder(view);
        } else {
            View view = LayoutInflater.from(context).inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Log.d(TAG, "onBindViewHolder: position=" + position + ", holder type=" + 
              (holder instanceof NewsViewHolder ? "NewsViewHolder" : "LoadingViewHolder"));
        if (holder instanceof NewsViewHolder) {
            bindNewsViewHolder((NewsViewHolder) holder, position);
        }
        // No binding needed for loading view holder
    }
    
    private void bindNewsViewHolder(NewsViewHolder holder, int position) {
        News news = newsList.get(position);
        holder.newsTitle.setText(news.getTitle());
        holder.newsDescription.setText(news.getDescription());
        holder.newsSource.setText(news.getSource());

        if (news.getImageUrl() != null && !news.getImageUrl().isEmpty()) {
            Glide.with(context)
                .load(news.getImageUrl())
                .centerCrop()
                .placeholder(R.drawable.placeholder_image)
                .error(R.drawable.placeholder_image)
                .into(holder.newsImage);
        } else {
            holder.newsImage.setImageResource(R.drawable.placeholder_image);
        }

        holder.itemView.setOnClickListener(v -> {
            if (news.getUrl() != null && !news.getUrl().isEmpty()) {
                Intent intent = new Intent(context, com.quickbrief.app.WebViewActivity.class);
                intent.putExtra(com.quickbrief.app.WebViewActivity.EXTRA_URL, news.getUrl());
                intent.putExtra(com.quickbrief.app.WebViewActivity.EXTRA_TITLE, news.getTitle());
                context.startActivity(intent);
            }
        });
        
        holder.itemView.setOnLongClickListener(v -> {
            if (news.getUrl() != null && !news.getUrl().isEmpty()) {
                android.widget.PopupMenu popup = new android.widget.PopupMenu(context, v);
                popup.getMenuInflater().inflate(R.menu.news_context_menu, popup.getMenu());
                
                popup.setOnMenuItemClickListener(item -> {
                    int id = item.getItemId();
                    if (id == R.id.action_open_browser) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(news.getUrl()));
                        context.startActivity(intent);
                        return true;
                    } else if (id == R.id.action_share) {
                        shareArticle(news);
                        return true;
                    }
                    return false;
                });
                
                popup.show();
                return true;
            }
            return false;
        });
        
        holder.btnShare.setOnClickListener(v -> {
            if (news.getUrl() != null && !news.getUrl().isEmpty()) {
                shareArticle(news);
            }
        });
    }

    @Override
    public int getItemCount() {
        int count = newsList.size() + (isLoadingAdded ? 1 : 0);
        Log.d(TAG, "getItemCount: count=" + count + ", newsList.size()=" + newsList.size() + ", isLoadingAdded=" + isLoadingAdded);
        return count;
    }
    
    @Override
    public int getItemViewType(int position) {
        if (isLoadingAdded && position == newsList.size()) {
            Log.d(TAG, "getItemViewType: Showing loading footer at position " + position);
            return VIEW_TYPE_LOADING;
        }
        Log.d(TAG, "getItemViewType: position=" + position + ", newsList.size()=" + newsList.size() + 
              ", isLoadingAdded=" + isLoadingAdded + ", viewType=" + VIEW_TYPE_ITEM);
        return VIEW_TYPE_ITEM;
    }

    public void updateNews(List<News> newNews) {
        Log.d(TAG, "updateNews: newNews.size()=" + newNews.size());
        this.newsList.clear();
        this.newsList.addAll(newNews);
        notifyDataSetChanged();
    }
    
    public void addNews(List<News> moreNews) {
        Log.d(TAG, "addNews: moreNews.size()=" + moreNews.size());
        
        // First remove loading footer if it exists
        if (isLoadingAdded) {
            removeLoadingFooter();
        }
        
        int startPosition = newsList.size();
        newsList.addAll(moreNews);
        notifyItemRangeInserted(startPosition, moreNews.size());
    }
    
    public void addLoadingFooter() {
        Log.d(TAG, "addLoadingFooter: isLoadingAdded=" + isLoadingAdded);
        if (!isLoadingAdded) {
            isLoadingAdded = true;
            int position = newsList.size();
            Log.d(TAG, "addLoadingFooter: Added loading footer at position " + position);
            notifyItemInserted(position);
            Log.d(TAG, "addLoadingFooter: After adding, isLoadingAdded=" + isLoadingAdded + ", getItemCount=" + getItemCount());
        }
    }
    
    public void removeLoadingFooter() {
        Log.d(TAG, "removeLoadingFooter: isLoadingAdded=" + isLoadingAdded);
        if (isLoadingAdded) {
            int position = newsList.size();
            Log.d(TAG, "removeLoadingFooter: Removing loading footer at position " + position);
            
            isLoadingAdded = false;
            notifyItemRemoved(position);
            Log.d(TAG, "removeLoadingFooter: After removing, isLoadingAdded=" + isLoadingAdded + ", getItemCount=" + getItemCount());
        }
    }

    static class NewsViewHolder extends RecyclerView.ViewHolder {
        ImageView newsImage;
        TextView newsTitle;
        TextView newsDescription;
        TextView newsSource;
        com.google.android.material.button.MaterialButton btnShare;

        NewsViewHolder(@NonNull View itemView) {
            super(itemView);
            newsImage = itemView.findViewById(R.id.newsImage);
            newsTitle = itemView.findViewById(R.id.newsTitle);
            newsDescription = itemView.findViewById(R.id.newsDescription);
            newsSource = itemView.findViewById(R.id.newsSource);
            btnShare = itemView.findViewById(R.id.btnShare);
        }
    }
    
    static class LoadingViewHolder extends RecyclerView.ViewHolder {
        LoadingViewHolder(@NonNull View itemView) {
            super(itemView);
        }
    }

    /**
     * Helper method to share an article
     */
    private void shareArticle(News news) {
        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("text/plain");
        shareIntent.putExtra(Intent.EXTRA_SUBJECT, news.getTitle());
        shareIntent.putExtra(Intent.EXTRA_TEXT, news.getTitle() + "\n\n" + news.getDescription() + "\n\nRead more: " + news.getUrl());
        
        Intent chooser = Intent.createChooser(shareIntent, context.getString(R.string.share_news_via));
        
        context.startActivity(chooser);
    }
} 