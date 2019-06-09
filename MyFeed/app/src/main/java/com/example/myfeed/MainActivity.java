package com.example.myfeed;

import java.io.*;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager ;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static com.example.myfeed.XmlParser.Entry;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    // XML Rss Feed
    private static final String URL = "https://www.buzzfeed.com/world.xml";
    //"https://www.buzzfeed.com/index.xml"

    // Initialization
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private Adapter adapter;
    private DBInterface db;
    private List<Entry> listSearch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //RecyclerView - used to display a scrolling list
        recyclerView = (RecyclerView) findViewById(R.id.rView);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Adapter with data  LinearLayoutManager will print the data
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new Adapter(this);
        recyclerView.setAdapter(adapter);

        //Using animations on list
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        //DB
        db = new DBInterface(this);
        listSearch = new ArrayList<>();

        //We add reference and listener to search
        ImageButton imageSearchButton = (ImageButton) findViewById(R.id.imageSearchButton);
        imageSearchButton.setOnClickListener(this);

        //Upload the news to a separate thread using AsyncTask
        loadNews();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //Clicking the update button updates the status of the network and uploads the news
        if (id == R.id.action_update) {
            loadNews();
            return true;
            //Clicking search button filters the list
        } else if (id == R.id.action_search) {
            showSearchBar();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showSearchBar() {
        LinearLayout searchBar = (LinearLayout) findViewById(R.id.searchBar);
        if (searchBar.getVisibility() == View.GONE) {
            searchBar.setVisibility(View.VISIBLE);
        } else {
            searchBar.setVisibility(View.GONE);
        }
    }

    // Use AsyncTask to download the XML feed
    public void loadNews() {
        //If the network is in CONNECTED state
        if (Util.isConnected(this)) {
            new DownloadTask().execute(URL);
        } else {
            Toast.makeText(this, "No connection", Toast.LENGTH_LONG).show();
            List<Entry> list = loadDB();
            adapter.setList(list);
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            //update the local listing for searches
            listSearch = list;
        }
    }

    private void downloadImages(List<Entry> result) {
        if(result == null)
            return;
        for (Entry entry : result) {
            try {
                // Take url as argument
                URL image = new URL(entry.image);
                // Create input buffer
                InputStream inputstream = (InputStream) image.getContent();
                byte[] bufferImage = new byte[1024];
                // Create output buffer for image path
                String path = getCacheDir().toString() + File.separator
                        + entry.image.substring(entry.image.lastIndexOf('/') + 1, entry.image.length());
                OutputStream outputstream = new FileOutputStream(path);
                int count;
                // While it has not reached end
                while ((count = inputstream.read(bufferImage)) != -1) {
                    // Write to the disk
                    outputstream.write(bufferImage, 0, count);
                }
                // Close i/p & o/p streams
                inputstream.close();
                outputstream.close();
            } catch (IOException exception) {
                Log.d("ERR", "Something went wrong!");
            }
        }
    }

    // Download XML, analyze it and create HTML code that returns as String
    private List<Entry> loadXML(String urlString) throws XmlPullParserException, IOException {
        List<Entry> entries = null;
        InputStream stream = null;
        XmlParser analyzer = new XmlParser();
        try {
            // open connection
            stream = OpenHTTPConnection(urlString);
            // Get list of entries
            entries = analyzer.analyze(stream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Close stream
            if (stream != null) {
                stream.close();
            }
        }
        return entries;
    }

    // Open HTTP Connection and return InputStream
    private InputStream OpenHTTPConnection(String adrecaURL) throws IOException {
        InputStream in = null;
        int response = -1;


        URL url = new URL(adrecaURL);

        HttpURLConnection httpConn = (HttpURLConnection) url.openConnection();

        try {

            httpConn.setReadTimeout(10000);
            httpConn.setConnectTimeout(15000);
            httpConn.setRequestMethod("GET");
            httpConn.setDoInput(true);

            httpConn.connect();

            //Get HTTP Response Code
            response = httpConn.getResponseCode();

            // Check if server returns a response Ok
            // content downloaded correctly
            if (response == HttpURLConnection.HTTP_OK) {
                // get input stream from server
                in = httpConn.getInputStream();
            }
            else {
                String noConnString = "<?xml version='1.0' encoding='UTF-8'?>" +
                        "<rss version=\"2.0\"> <channel> <item> <title> Unable to fetch the feed!</title> </item> </channel> </rss>";
                in = new ByteArrayInputStream(noConnString.getBytes());
            }
        } catch (Exception ex) {
            // Error connecting
            String noConnString = "<?xml version='1.0' encoding='UTF-8'?>" +
                    "<rss version=\"2.0\"> <channel> <item> <title> Check your internet!</title> </item> </channel> </rss>";
            in = new ByteArrayInputStream(noConnString.getBytes());
            return in;
            //throw new IOException("Error connecting");
        }

        //return data stream
        return in;
    }

    public void dbInsertAll(List<Entry> result) {
        db.dropAndRecreateTable();
        db.open();
        if(result == null)
            return;
        for (Entry r : result) {
            String title = r.title;
            String link = r.link;
            String author = r.author;
            String description = r.description;
            String date = r.date;
            String image = r.image;
            db.insert(title, description, link, author, date, image);
        }
        db.close();
    }

    protected List<Entry> loadDB() {
        List<Entry> entries = new ArrayList<>();

        db.open();
        Cursor cursor = db.getAll();
        
        while (cursor.moveToNext()) {
            String title = cursor.getString(cursor.getColumnIndex("title"));
            String link = cursor.getString(cursor.getColumnIndex("link"));
            String author = cursor.getString(cursor.getColumnIndex("author"));
            String description = cursor.getString(cursor.getColumnIndex("description"));
            String date = cursor.getString(cursor.getColumnIndex("date"));
            String image = cursor.getString(cursor.getColumnIndex("image"));
            entries.add(new Entry(title, description, link, author, date, image));
        }
        return entries;
    }

    @Override
    public void onClick(View v) {
        // Generate a new list and pass it to adapter
        List<Entry> filteredList = new ArrayList<>();
        TextView editTextSearch = (TextView) findViewById(R.id.editTextSearch);
        String searchQuery = editTextSearch.getText().toString().toLowerCase();
        for (Entry e : listSearch) {
            if (e.title.toLowerCase().contains(searchQuery))
                filteredList.add(e);
        }
        adapter.setList(filteredList);
        adapter.notifyDataSetChanged();
    }

    //Implementation of AsyncTask to download the XML feed
    private class DownloadTask extends AsyncTask<String, Void, List<Entry>> {

        @Override
        protected void onPreExecute() {
            progressBar.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.INVISIBLE);
        }

        @Override
        //The one that is executed in the background
        protected List<Entry> doInBackground(String... urls) {
            List<Entry> list = null;
            try {
                //load XML
                list = loadXML(urls[0]);
                // Insert into DB
                dbInsertAll(list);
                // Download images
                downloadImages(list);
            } catch (IOException | XmlPullParserException e) {
                //Error
            }
            return list;
        }


        @Override
        // Link to webView
        protected void onPostExecute(List<Entry> list) {
            adapter.setList(list);
            adapter.notifyDataSetChanged();
            progressBar.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            // Update search list
            listSearch = list;
        }

    }

}
