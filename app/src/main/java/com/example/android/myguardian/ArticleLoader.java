package com.example.android.myguardian;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * With the help of this custom ArticleLoader we can make queries from Guardian API and fetch the data.
 */

class ArticleLoader extends AsyncTaskLoader<List<Article>> {
    private String query;

    ArticleLoader(Context context, String query) {
        super(context);
        this.query = query;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<Article> loadInBackground() {
        String response = QueryUtils.fetchArticleData(query);
        List<Article> articleList = QueryUtils.parseArticle(response);

        return articleList;
    }
}
