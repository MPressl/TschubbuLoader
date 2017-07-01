//Copywrite (C) Martin Pressl 2017
package com.mston.developments.tschubbuloader;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Adapter realises the result list in form of an preview image and the video name
 */

public class ResultListAdapter extends BaseAdapter implements View.OnClickListener {

    private final MainActivity context;
    private final ArrayList<YoutubeEntry> data;
    public Resources resource;
    private YoutubeEntry tempValues=null;

    public ResultListAdapter(MainActivity context,
                             ArrayList<YoutubeEntry> data, Resources resources) {

        this.context = context;
        this.data = data;
        this.resource = resources;

    }

    @Override
    public int getCount() {
        if(data.size() <= 0 ){
            return 1;
        }
        return data.size();
    }

    @Override
    public Object getItem(int i) {
        if(data.size() == 0){
            return 1;
        }
        return data.get(i);
    }

    @Override
    public long getItemId(int i) {
        return data.indexOf(getItem(i));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View vi = convertView;
        ResultListItemView itmView;

        if(convertView==null){

            LayoutInflater mInflater = (LayoutInflater) context
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            vi = mInflater.inflate(R.layout.result_list, null);

            itmView = new ResultListItemView();
            itmView.videoName = (TextView) vi.findViewById(R.id.textView);
            itmView.image=(ImageView)vi.findViewById(R.id.imageView);
            itmView.videoName.setOnClickListener(this);
            itmView.image.setOnClickListener(this);
            vi.setTag( itmView );
        }
        else
            itmView=(ResultListItemView) vi.getTag();

        if(!(data.size()<=0))
        {
            tempValues=null;
            tempValues = data.get( position );

            itmView.videoName.setText( tempValues.getName() );

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inSampleSize = 2;
            Bitmap bm = BitmapFactory.decodeFile(tempValues.getImgUrl(), options);
            itmView.image.setImageBitmap(bm);
        }
            //vi.setOnClickListener(new OnItemClickListener( position ));
        return vi;
    }

    @Override
    public void onClick(View view) {
    String url = null;
        //get item youtube url
        TableRow parent = (TableRow) view.getParent();
        TextView text =  (TextView) parent.getChildAt(1);
        for(YoutubeEntry entry : data){
            if(entry.getName().equals( text.getText().toString())){
               url = entry.getUrl();
            }
        }
        if(view instanceof TextView){
            //start conversion
            this.context.runConversionActivity(url);

        }else if(view instanceof ImageView){
            //show video
            this.context.openLink(url);
        }
    }

    public static class ResultListItemView{

        public TextView videoName;
        public ImageView image;

    }
}
