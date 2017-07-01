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
 * With the help of this Adapter we can populate the Recycler view with Article date.
 */

class ArticleAdapter extends RecyclerView.Adapter<ArticleAdapter.ViewHolder> {
    private List<Article> articleList;
    private Context context;

    ArticleAdapter(Context context, List<Article> articleList) {
        this.context = context;
        this.articleList = articleList;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View newsListItemView =
                LayoutInflater.from(context).inflate(R.layout.article_list_item, parent, false);

        final ArticleAdapter.ViewHolder articleDataViewHolder =
                new ArticleAdapter.ViewHolder(newsListItemView);

        // setting onClickListener on the list items in order to open the corresponding web sites.
        newsListItemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // getting the URL of the current Article
                String webUrl = articleList.get(articleDataViewHolder.getAdapterPosition()).getWebUrl();
                Uri webUri = Uri.parse(webUrl);
                Intent intent = new Intent(Intent.ACTION_VIEW, webUri);
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
                }
            }
        });

        return articleDataViewHolder;

    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, int position) {
        if (articleList == null) {
            return;
        }
        // retrieve the current Article object
        Article article = articleList.get(position);

        viewHolder.sectionNameTextView.setText(article.getSectionName());
        viewHolder.webTitleTextView.setText(article.getWebTitle());

        // creating a simple date format based on the Guardian API returns
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                Locale.getDefault());
        Date date = null;
        try {
            // parsing the received date string
            date = simpleDateFormat.parse(article.getWebPublicationDate());
        } catch (ParseException e) {
            Log.e("ArticleAdapter", e.getMessage());
        }

        // localizing the date format to Hungarian standards
        simpleDateFormat.applyLocalizedPattern("yyyy.MM.dd. HH:mm");
        // filling the date into the appropriate TextView
        viewHolder.webPublicationDateTextView.setText(simpleDateFormat.format(date));
    }

    @Override
    public int getItemCount() {
        return articleList.size();
    }

    void setArticleList(List<Article> articleList) {
        this.articleList = articleList;
    }

    void clear() {
        if (!articleList.isEmpty()) {
            int size = articleList.size();
            articleList.clear();
            this.notifyItemRangeRemoved(0, size);
        }
    }

    //using the ViewHolder pattern in order to make the code more efficient
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
