package com.example.android.myguardian;

import android.content.AsyncTaskLoader;
import android.content.Context;

import java.util.List;

/**
 * A custom loader that queries the given URL from Guardian API in background and returns a list
 * of NewsDate as a result.
 */

class NewsLoader extends AsyncTaskLoader<List<News>> {
    private String query;

    NewsLoader(Context context, String query) {
        super(context);
        this.query = query;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<News> loadInBackground() {
        String response = QueryUtils.fetchNewsData(query);
        List<News> newsList = QueryUtils.parseNews(response);

        return newsList;
    }
}
