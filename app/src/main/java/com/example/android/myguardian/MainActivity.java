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

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Article>>, SwipeRefreshLayout.OnRefreshListener {
    // this is the search URL of Guardian API
    public static final String URL = "http://content.guardianapis.com/search";
    private static final int ARTICLE_LOADER_ID = 0;

    private static String query = "";
    SearchView searchView;
    private List<Article> articleList = new ArrayList<>();
    private RecyclerView articleRecyclerView;
    private ArticleAdapter articleAdapter;
    private TextView emptyStateTextView;
    private View progressBar;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Defining the RecyclerView and its related Adapter and LayoutManager
        articleRecyclerView = (RecyclerView) findViewById(R.id.article_recycler_view);
        LinearLayoutManager articleLayoutManager = new LinearLayoutManager(this);
        articleRecyclerView.setLayoutManager(articleLayoutManager);
        articleAdapter = new ArticleAdapter(this, articleList);
        articleRecyclerView.setAdapter(articleAdapter);
        articleRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));

        emptyStateTextView = (TextView) findViewById(R.id.empty_text_view);
        progressBar = findViewById(R.id.progress_bar);
        //with the help of swipeRefreshLayout we can update the data conveniently
        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh);
        swipeRefreshLayout.setOnRefreshListener(this);

        startGuardianSearch();
    }

    private void startGuardianSearch() {
        if (hasConnection()) {
            LoaderManager loaderManager = getLoaderManager();
            loaderManager.initLoader(ARTICLE_LOADER_ID, null, this);
        }
    }

    // checking the internet connection
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

        // setting up the search function
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setIconifiedByDefault(false);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        // start the SettingsActivity with the help of a new intent
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<List<Article>> onCreateLoader(int id, Bundle args) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        // extracting user preferences and store them in local variables
        String orderByValue = sharedPreferences.getString(
                getString(R.string.settings_order_by_key),
                getString(R.string.settings_order_by_default));
        String pageSize = sharedPreferences.getString(
                getString(R.string.settings_page_size_key),
                getString(R.string.settings_page_size_default));

        // defining the base Guardian search API Uri
        Uri baseUri = Uri.parse(URL);
        Uri.Builder uriBuilder = baseUri.buildUpon();

        // attaching order-by preference to the API query
        uriBuilder.appendQueryParameter(getString(R.string.settings_order_by_key), orderByValue);

        // attaching the page-size preference to the API query
        uriBuilder.appendQueryParameter(getString(R.string.settings_page_size_key), pageSize);


        //encoding Uri in order to avoid problems that special characters can cause in the search
        query = Uri.encode(query);
        uriBuilder.appendQueryParameter("q", query);
        // appending api-key to the query
        uriBuilder.appendQueryParameter("api-key", "b6298fa2-2521-4ae7-8d93-64e34d23cef7");

        return new ArticleLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Article>> loader, List<Article> updatedArticleList) {
        progressBar.setVisibility(View.GONE);
        swipeRefreshLayout.setRefreshing(false);


        if (updatedArticleList != null && !updatedArticleList.isEmpty()) {
            if (!TextUtils.isEmpty(emptyStateTextView.getText())) {
                emptyStateTextView.setText("");
            }

            articleAdapter.setArticleList(null);
            articleAdapter.setArticleList(updatedArticleList);
            articleAdapter.notifyDataSetChanged();
            articleRecyclerView.setVisibility(View.VISIBLE);
        } else {
            articleRecyclerView.setVisibility(View.GONE);
            emptyStateTextView.setText(getString(R.string.no_news_error, query));
        }
    }

    @Override
    public void onLoaderReset(Loader<List<Article>> loader) {
        articleAdapter.setArticleList(null);
    }


    @Override
    public void onRefresh() {
        restartLoader();
    }


    private void restartLoader() {
        if (hasConnection()) {
            getLoaderManager().restartLoader(ARTICLE_LOADER_ID, null, this);
        } else {
            articleAdapter.clear();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {

        if (Intent.ACTION_SEARCH.equals(intent.getAction())) {
            progressBar.setVisibility(View.VISIBLE);
            query = intent.getStringExtra(SearchManager.QUERY);
            restartLoader();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (searchView != null) {
            searchView.clearFocus();
        }

        restartLoader();
    }
}
