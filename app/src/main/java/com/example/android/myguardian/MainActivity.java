package com.example.android.myguardian;

import android.app.LoaderManager;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<News>>, SwipeRefreshLayout.OnRefreshListener {
    // base search URL of Guardian API
    public static final String URL = "http://content.guardianapis.com/search";
    private static final int NEWS_LOADER_ID = 0;
    // variable for holding default and user's query String
    private static String query = "";
    SearchView searchView;
    private List<News> newsList = new ArrayList<>();
    private RecyclerView newsRecyclerView;
    private NewsAdapter newsAdapter;
    private TextView emptyStateTextView;
    private View progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setting up RecyclerView and its LayoutManager and Adapter
        newsRecyclerView = (RecyclerView) findViewById(R.id.news_recycler_view);
        LinearLayoutManager newsLayoutManager = new LinearLayoutManager(this);
        newsRecyclerView.setLayoutManager(newsLayoutManager);
        newsAdapter = new NewsAdapter(this, newsList);
        newsRecyclerView.setAdapter(newsAdapter);
        newsRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        emptyStateTextView = (TextView) findViewById(R.id.empty_text_view);
        progressBar = findViewById(R.id.progress_bar);
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        startGuardianSearch();
    }

    private void startGuardianSearch() {
        if (hasConnection()) {
            LoaderManager loaderManager = getLoaderManager();
            // Starting Loader thread for network and parsing
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        }
    }

    // Checks whether device has available internet connection
    private boolean hasConnection() {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            return true;
        } else {
            progressBar.setVisibility(View.GONE);
            swipeRefreshLayout.setRefreshing(false);
            emptyStateTextView.setText(R.string.internet_connection_error);
            return false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);

        // Get the SearchView and set the searchable configuration
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        // Assumes current activity is the searchable activity
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // if Settings action was clicked then start SettingsActivity
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<News>> onCreateLoader(int id, Bundle args) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // extracting user preferences and store them in local variables
        String orderByValue = sharedPreferences.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
        String pageSize = sharedPreferences.getString(
                getString(R.string.settings_page_size_key),
                getString(R.string.settings_page_size_default));

        // Build the base Guardian search API Uri
        Uri baseUri = Uri.parse(URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // appending order-by preference to the API query
        uriBuilder.appendQueryParameter(getString(R.string.settings_order_by_key), orderByValue);

        // appending the page-size preference to the API query
        uriBuilder.appendQueryParameter(getString(R.string.settings_page_size_key), pageSize);

        // encode the search topic to ensure spaces and special chares don't cause problems
        // in the query
        query = Uri.encode(query);
        uriBuilder.appendQueryParameter("q", query);
        // appending api-key to the query
        uriBuilder.appendQueryParameter("api-key", "a6ba290e-d70e-44da-93bb-3a04f7dcb7e9");

        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> updatedNewsList) {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);

        // If there is new data then update the list and notify adapter about data change
        if (updatedNewsList != null && !updatedNewsList.isEmpty()) {
            if (!TextUtils.isEmpty(emptyStateTextView.getText())) {
                emptyStateTextView.setText("");
            }
            // releasing references to old data list
            newsAdapter.setNewsList(null);
            newsAdapter.setNewsList(updatedNewsList);
            newsAdapter.notifyDataSetChanged();
            newsRecyclerView.setVisibility(View.VISIBLE);
        } else {
            newsRecyclerView.setVisibility(View.GONE);
            emptyStateTextView.setText(getString(R.string.no_news_error, query));
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        newsAdapter.setNewsList(null);
    }

    // This method is called back when user wants to refresh list via swiping the RecyclerView down
    // when reached the top most list item
    @Override
    public void onRefresh() {
        restartLoader();
    }

    // logic for restarting the loader
    private void restartLoader() {
        if (hasConnection()) {
            getLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);
        } else {
            newsAdapter.clear();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        // Handle search intent
        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            progressBar.setVisibility(View.VISIBLE);
            // retrieve user's query string
            query = intent.getStringExtra(SearchManager.QUERY);
            restartLoader();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // preventing soft keyboard showing up and search view getting focus when navigating
        // back from other screens
        if (searchView != null) {
            searchView.clearFocus();
        }

        restartLoader();
    }
}
