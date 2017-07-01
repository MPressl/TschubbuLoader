//Copywrite (C) Martin Pressl 2017
package com.mston.developments.tschubbuloader;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * serializable tag proposals containing artist and title.
 */

public class TagProposals implements Serializable {

    private ArrayList<String> artist;
    private ArrayList<String> title;
    private String setTagLink;

    public TagProposals(ArrayList<String> artist, ArrayList<String> title,
                        String setTagLink){
        if(artist != null) {
            this.artist = artist;
        }else{
            this.artist = new ArrayList<String>();
        }
        if(title != null) {
            this.title = title;
        }else{
            this.title = new ArrayList<String>();
        }
        this.setTagLink = setTagLink;
    }

    public ArrayList<String> getArtist() {
        return artist;
    }

    public ArrayList<String> getTitle() {
        return title;
    }

    public String getSetTagLink() {
        return setTagLink;
    }
}
