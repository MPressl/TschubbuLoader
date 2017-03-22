package com.mston.developments.tschubbuloader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    public static String VIDEO_URL_ATTRIBUTE = "videourl";
    public static final int SET_TAGS_AND_DOWNLOAD_REQUEST = 1;

    private boolean isSearching = false;

    private Button searchBtn;
    private EditText searchTxt;
    private ListView resultView;
    private ArrayList<YoutubeEntry> listItems;
    private ResultListAdapter listAdapter;



    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SET_TAGS_AND_DOWNLOAD_REQUEST) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Download started!", Toast.LENGTH_LONG ).show();
            }
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        init_components();
    }

    private void init_components(){

        searchBtn = (Button) findViewById(R.id.button);
        searchTxt = (EditText) findViewById(R.id.editText);
        resultView = (ListView) findViewById(R.id.list);
        listItems = new ArrayList<>();
        listAdapter = new ResultListAdapter(this,
                listItems, getResources());

        resultView.setAdapter(listAdapter);

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(searchTxt.getText() != null &&
                        !searchTxt.getText().toString().equals("")){
                    if(!isSearching) {
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                isSearching = true;
                                ArrayList<YoutubeEntry> resultList = wGetter
                                        .searchVids(searchTxt.getText().toString());
                                if (resultList != null) {
                                    displayResults(resultList);
                                }
                                isSearching = false;
                            }
                        }, "search thread").start();
                    }
                }
            }
        });

    }

    private void displayResults(ArrayList<YoutubeEntry> resultList){
        listItems.clear();

        for(YoutubeEntry result : resultList){
            listItems.add(result);
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listAdapter.notifyDataSetChanged();
            }
        });


    }

    public void runConversionActivity( String url){
        Intent conversionIntent = new Intent(MainActivity.this, SetTagActivity.class);

        conversionIntent.putExtra(this.VIDEO_URL_ATTRIBUTE, url);
        MainActivity.this.startActivityForResult(conversionIntent, SET_TAGS_AND_DOWNLOAD_REQUEST);

    }

    public void openLink(String url) {

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(browserIntent);

    }
}
