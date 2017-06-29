package com.example.android.myguardian;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Adapter for binding News date to Recycler view item views
 */

class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.ViewHolder> {
    private List<News> newsList;
    private Context context;

    NewsAdapter(Context context, List<News> newsList) {
        this.context = context;
        this.newsList = newsList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View newsListItemView =
                LayoutInflater.from(context).inflate(R.layout.news_list_item, parent, false);

        final NewsAdapter.ViewHolder newsDataViewHolder =
                new NewsAdapter.ViewHolder(newsListItemView);

        // setting onClickListener on the list items that opens the corresponding article online
        newsListItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // getting the URL of the news at adapters current position
                String webUrl = newsList.get(newsDataViewHolder.getAdapterPosition()).getWebUrl();
                Uri webUri = Uri.parse(webUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, webUri);
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                }
            }
        });

        return newsDataViewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if (newsList == null) {
            return;
        }
        // retrieve the current News object
        News news = newsList.get(position);

        viewHolder.sectionNameTextView.setText(news.getSectionName());
        viewHolder.webTitleTextView.setText(news.getWebTitle());

        // creating a simple date format that matches the pattern that Guardian API returns
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault());
        Date date = null;
        try {
            // parse returned date string from Guardian API to a Date object
            date = simpleDateFormat.parse(news.getWebPublicationDate());
        } catch (ParseException e) {
            Log.e("NewsAdapter", e.getMessage());
        }

        // create a pattern that matches the way Hungarian users read date and time
        simpleDateFormat.applyLocalizedPattern("yyyy.MM.dd. HH:mm");
        // populate the date into the corresponding TextView in the above formatted way
        viewHolder.webPublicationDateTextView.setText(simpleDateFormat.format(date));
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    void setNewsList(List<News> newsList) {
        this.newsList = newsList;
    }

    void clear() {
        if (!newsList.isEmpty()) {
            int size = newsList.size();
            newsList.clear();
            this.notifyItemRangeRemoved(0, size);
        }
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView sectionNameTextView;
        TextView webPublicationDateTextView;
        TextView webTitleTextView;

        ViewHolder(View itemView) {
            super(itemView);
            this.sectionNameTextView = (TextView) itemView.findViewById(R.id.section_name_text_view);
            this.webPublicationDateTextView = (TextView) itemView.findViewById(R.id.publication_date_text_view);
            this.webTitleTextView = (TextView) itemView.findViewById(R.id.title_text_view);
        }
    }
}
