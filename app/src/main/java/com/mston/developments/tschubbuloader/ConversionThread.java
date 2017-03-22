package com.mston.developments.tschubbuloader;

import android.util.Log;

/**
 * Created by root on 1/6/17.
 */

public class ConversionThread implements Runnable {
    private SetTagActivity context;
    private final String conversionLink;


    public ConversionThread(String link, SetTagActivity context){
        this.conversionLink = link;
        this.context = context;

    }
    @Override
    public void run() {
        if(conversionLink == null){
            Log.d("Conversion Thread", " conversion link is null!");
            return;
        }

        TagProposals proposals = wGetter.convertVideo(conversionLink, context.getSelectedFormat());
        context.setTagProposals(proposals);
        context.setConversion_started(false);



    }
}
