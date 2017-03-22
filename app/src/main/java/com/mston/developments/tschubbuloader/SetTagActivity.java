package com.mston.developments.tschubbuloader;
import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Created by root on 1/6/17.
 */

public class SetTagActivity extends Activity implements View.OnClickListener{

    private final String[] formats = {"mp3", "mp4"};
    private final int REQUEST_WRITE_PERMISSIONS = 0;

    private ImageView convertSymbol;
    private ImageView downloadSymbol;

    private Button button_convert;
    private Button download_select;
    private Button goBack;

    private InstantAutoCompleteTextView tv_artist;
    private InstantAutoCompleteTextView tv_title;

    private TextView text_set_tags;
    private TextView text_set_title;
    private TextView text_set_artist;

    private Spinner spinner_format;

    private String video_url;

    private boolean download_started;
    private boolean conversion_started;
    private TagProposals tagProposals;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_tag);
        init_components();
    }

    private void init_components() {

        this.video_url = (String) getIntent().getSerializableExtra(MainActivity.VIDEO_URL_ATTRIBUTE);

        this.text_set_artist = (TextView) findViewById(R.id.text_view_sartist);
        this.text_set_artist.setVisibility(View.INVISIBLE);

        this.text_set_tags = (TextView) findViewById(R.id.text_view_select_tags);
        this.text_set_tags.setVisibility(View.INVISIBLE);

        this.text_set_title = (TextView) findViewById(R.id.text_view_stitle);
        this.text_set_title.setVisibility(View.INVISIBLE);

        this.button_convert = (Button) findViewById(R.id.button_convert);
        this.button_convert.setOnClickListener(this);

        this.spinner_format = (Spinner) findViewById(R.id.spinner_format);
        this.spinner_format.setAdapter(new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item,
                this.formats));


        this.download_select = (Button) findViewById(R.id.buton_download_selected);
        this.download_select.setOnClickListener(this);
        this.download_select.setVisibility(View.INVISIBLE);

        this.goBack = (Button) findViewById(R.id.button_back);
        this.goBack.setOnClickListener(this);

        this.tv_title = (InstantAutoCompleteTextView) findViewById(R.id.tv_title);
        this.tv_title.setVisibility(View.INVISIBLE);


        this.tv_artist = (InstantAutoCompleteTextView) findViewById(R.id.tv_artist);
        this.tv_artist.setVisibility(View.INVISIBLE);

        convertSymbol = (ImageView) findViewById(R.id.converting_image);
        convertSymbol.setVisibility(View.INVISIBLE);

        this.downloadSymbol = (ImageView) findViewById(R.id.downloading_image);
        this.downloadSymbol.setVisibility(View.INVISIBLE);

    }


    @Override
    public void onClick(View view) {

        if(view instanceof Button){

            Button b = (Button) view;
            if(b == button_convert){
                String format = spinner_format.getSelectedItem().toString();
                this.spinner_format.setEnabled(false);
                b.setEnabled(false);
                this.setConversion_started(true);

            }
            if(b == download_select){
                if(this.tv_artist.getText().toString().equals("")
                        || this.tv_title.getText().toString().equals("")){
                    Toast.makeText(this, "Please set an Artist and Title.",
                            Toast.LENGTH_LONG).show();
                    return;
                }
               setDownloadStarted(true, false, this.tagProposals.getSetTagLink(),
                        this.tv_artist.getText().toString(),
                        this.tv_title.getText().toString());

            }else if(b == goBack){
                setResult(Activity.RESULT_CANCELED);
                finish();
            }
        }

    }

    public void askWritePermission(){
        new Thread(new Runnable(){
            public void run(){
                ActivityCompat.requestPermissions(SetTagActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_WRITE_PERMISSIONS);
            }
        }, "Request Thread").start();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case REQUEST_WRITE_PERMISSIONS: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //TODO: null checks!
                    setDownloadStarted(true, false, this.tagProposals.getSetTagLink(),
                            this.tv_artist.getText().toString(),
                            this.tv_title.getText().toString());

                } else {
                    Toast.makeText(this, "Cannot download without permissions.",
                            Toast.LENGTH_LONG).show();

                }
                return;
            }
        }
    }

    public void setTagProposals(TagProposals proposals) {
       this.tagProposals = proposals;

        if(tagProposals != null ) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ArrayAdapter<String> artistAdapter = new ArrayAdapter<String>(SetTagActivity.this,
                            R.layout.drop_down_item, tagProposals.getArtist());
                    tv_artist.setAdapter(artistAdapter);

                    ArrayAdapter<String> titleAdapter = new ArrayAdapter<String>(SetTagActivity.this,
                            R.layout.drop_down_item, tagProposals.getTitle());
                    tv_title.setAdapter(titleAdapter);
                    enableTagArea();
                }
            });
        }
    }

    private void enableTagArea(){
        this.text_set_tags.setVisibility(View.VISIBLE);
        this.text_set_artist.setVisibility(View.VISIBLE);
        this.text_set_title.setVisibility(View.VISIBLE);
        this.tv_artist.setVisibility(View.VISIBLE);
        this.tv_title.setVisibility(View.VISIBLE);
        this.download_select.setVisibility(View.VISIBLE);
    }

    public void setConversion_started(boolean started){

        if(!this.conversion_started && started){
            //display load symbol while converting
            this.conversion_started = started;
            new Thread(new ConversionThread(this.video_url, this), "Conversion Thread").start();
            convertSymbol.setVisibility(ImageView.VISIBLE);

        }
        if(this.conversion_started && !started ){
            //start the activity for setting tag
            this.conversion_started = started;
            runOnUiThread(new Runnable() {
                              @Override
                              public void run() {
                                  convertSymbol.setVisibility(ImageView.INVISIBLE);
                              }
                          });
        }


    }

    public int getSelectedFormat() {
       if(this.spinner_format.getSelectedItem().toString().equals("mp4")){
            return wGetter.FORMAT_MP4;
        }else{
           return wGetter.FORMAT_MP3;
       }
    }

    public void setDownloadStarted(boolean started, boolean finishActivity, String link, String artist, String title){

        if(!this.download_started && started){
            new Thread(new DownloadThread(link, artist, title, this, getSelectedFormat()),
                    "DownloadThread").start();
            this.download_started = started;
            downloadSymbol.setVisibility(ImageView.VISIBLE);

        }
        if(this.download_started && !started) {
            download_started = started;
            if (finishActivity){

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setResult(Activity.RESULT_OK);
                    finish();
                }
            });
            }else {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        downloadSymbol.setVisibility(ImageView.INVISIBLE);
                    }
                });
            }

        }

    }
}
