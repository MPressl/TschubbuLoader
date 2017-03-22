package com.mston.developments.tschubbuloader;

import android.app.DownloadManager;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import static android.content.Context.DOWNLOAD_SERVICE;

public class wGetter {

	public static final int FORMAT_MP3 = 0;
	public static final int FORMAT_MP4 = 1;

	private static final String LINE_FEED = "\r\n";
	private static final String POST_METHOD = "POST";
	private static final String GET_METHOD = "GET";

	private static PrintWriter writer;
	private static HttpURLConnection connection;
	private static String boundary;
	private static String uId;

	


	private static BufferedReader createRequestResultReader(String method,
			URL url, Hashtable<String, String> formFields){
		
		connection = createHeader(method, url);
		OutputStream outputStream;
		try {
			outputStream = connection.getOutputStream();

			writer = new PrintWriter(new OutputStreamWriter(outputStream,
					"utf-8"), true);
			
			//add form fields
			if(formFields != null){
				Set<String> keys = formFields.keySet();
		        for(String key: keys){
		        	addFormField(key, formFields.get(key), boundary);
		        }
			}
	        
	        writer.flush();
	
			writer.append(LINE_FEED).flush();
			if(method == POST_METHOD){
				writer.append("--" + boundary + "--").append(LINE_FEED);
			}
			writer.close();
			

			return new BufferedReader(new InputStreamReader(
					connection.getInputStream()));
			
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Log.d("MSG:" ,e.toString());;
			}
		return null;
		
		
	}

	private static void addFormField(String name, String value, String boundary) {
		writer.append("--" + boundary).append(LINE_FEED);
		writer.append("Content-Disposition: form-data; name=\"" + name + "\"")
				.append(LINE_FEED);

		writer.append(LINE_FEED);
		writer.append(value).append(
				LINE_FEED);
	}

	private static HttpURLConnection createHeader(String method, URL url) {

		try {
			connection = (HttpURLConnection) url.openConnection();

			connection.setRequestMethod(method);
			boundary = "------------------------" + System.currentTimeMillis();
			connection.setRequestProperty("User-Agent", "curl/7.50.1");
			connection.setRequestProperty("Accept", "*/*");
			connection.setRequestProperty("Expect", "100-continue");
			if(method == POST_METHOD){
				connection.setRequestProperty("Content-Type",
						"multipart/form-data; boundary=" + boundary);
			}
			connection.setDoOutput(true);
			if(method == POST_METHOD){
				connection.setDoInput(true);
			}
			return connection;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("MSG:" ,e.toString());;
			return null;
		}

	}

	public static ArrayList<YoutubeEntry> searchVids(String searchString) {
		try {
			Hashtable<String,String> formFields = new Hashtable<String, String>();
			formFields.put("q", searchString);
			BufferedReader reader = createRequestResultReader(POST_METHOD, new URL(
					"http://convert2mp3.net?p=search"), formFields);
			
			//process the result
			String line = null;
			ArrayList<String> lines = new ArrayList<String>();
			while ((line = reader.readLine()) != null) {
				//System.out.println(line);
				if (line.contains("youtube.com")) {
					lines.add(line);
				}
			}
			//create YoutubeEnries
			ArrayList<YoutubeEntry> entries = new ArrayList<YoutubeEntry>();
			for (int i = 0; i < lines.size(); i = i + 4) {
				YoutubeEntry entry = new YoutubeEntry();
				line = lines.get(i);
				int i1 = line.indexOf("http://www.youtube");
				int i2 = line.indexOf("target");
				entry.setUrl(line.substring(line.indexOf("http://www.youtube"),
						line.indexOf("\" target")));
				entry.setName(line.substring(line.indexOf("<b>") + 3,
						line.indexOf("</b>")));

				line = lines.get(i + 1);
				String imgUrl = line.substring(
						line.indexOf("<img src=\"") + 10,
						line.indexOf(".jpg\"") + 4);
				entry.setImgUrl(imgUrl);
				entries.add(entry);
			}
			endConnection();
			for (int i = 0; i < entries.size(); i++) {
				//download images set path
				YoutubeEntry entry = entries.get(i);
				entry.setImgUrl(downloadImageToPath(entry.getImgUrl(), "./" + entry.getName()
						+ ".jpg"));
			}

            return entries;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("MSG:" , e.toString());;
			endConnection();
            return null;
		}
		
	}

	

	private static String downloadImageToPath(String imgUrl, String path) {
        try {
	  	     URL url = new URL(imgUrl);
	  	     connection = (HttpURLConnection) url.openConnection();
	  	     connection.setRequestMethod("GET");
	  		
        	InputStream fileStream = connection.getInputStream();
			String filename_standard = path;
			String format = ".jpg";
			
			//File file = new File();
			File file = File.createTempFile(filename_standard, format);
		    OutputStream outstream = new FileOutputStream(file);
		    byte[] buffer = new byte[4096];
		    int actual;
		    while ((actual = fileStream.read(buffer)) > 0) {

					outstream.write(buffer, 0, actual);
		    }
		    outstream.close();
		    connection.disconnect();
		    connection = null;
		    return file.getAbsolutePath();
        } catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("MSG:" ,e.toString());;
		}
        	connection.disconnect();
		    connection = null;
		    return null;
		
	}

	private static void endConnection() {
        if(connection != null) {
            connection.disconnect();
        }
		connection = null;
		writer = null;
		boundary = null;
		
	}

	public static TagProposals convertVideo(String url, int format){

		String conversionLink = initiateVideoConversion(url, format);
		if(conversionLink == null){
			Log.d("Conversion", "could not initiate conversion");
			return null;
		}
		String resultLink = getConversionResultPage(conversionLink);
		if(resultLink == null){
			Log.d("Conversion" ,"could not determine a link to fetch tags");
			return null;
		}
		if(format == FORMAT_MP4){
			//create empty proposals with resultLink es SetTagLink
			TagProposals proposals = new TagProposals(new ArrayList<String>(),
					new ArrayList<String>(), resultLink);

			return proposals;

		}else if(format == FORMAT_MP3) {
			TagProposals proposals = getVideoTags(resultLink);
			if (proposals.getSetTagLink() == null ||
					proposals.getSetTagLink().equals("")) {
				Log.d("Conversion", "could not determine a link to set tags for download");
				return null;
			}
			return proposals;
		}
        return null;

	}

	private static String initiateVideoConversion(String url, int format) {
		uId = "";
		if(!getUidFromServer()){
			System.out.println("couldnt determine a valid user ID! :/");
			return null;
		}
		try {
			//create video conversion request
			Hashtable<String,String> formFields = new Hashtable<String, String>();
			formFields.put("url", url);
			if(format == FORMAT_MP3) {
				formFields.put("format", "mp3");
			}else if(format == FORMAT_MP4){
				formFields.put("format", "mp4");
			}
			formFields.put("quality", "1");
			formFields.put("9di2n3", uId);
			BufferedReader reader = createRequestResultReader(POST_METHOD, new URL(
					"http://convert2mp3.net?p=convert"), formFields);
			//Process Result
			String line = null;
			while ((line = reader.readLine()) != null) {
				//System.out.println(line);
				String linkHeader = "<iframe id=\"convertFrame\" src=\"";
				if(line.contains(linkHeader)){
					line = line.substring(line.indexOf(linkHeader) + linkHeader.length(), line.length());
					String conversionLink = line.substring(0, 
							line.indexOf("style") - 2 );
					endConnection();
					return conversionLink;	
				}
			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("MSG:" ,e.toString());;
		}
		endConnection();
		return null;

	}

	private static String getConversionResultPage(String link) {
		try {
			BufferedReader reader = createRequestResultReader("GET", new URL(link), null);
			String line = null;
			while ((line = reader.readLine()) != null) {
				String linkHeader = "window.parent.location.href = \"";
				if(line.contains(linkHeader)){
					String tagLink = line.substring(line.indexOf(linkHeader) + linkHeader.length(), line.length() - 2);
					endConnection();
					return tagLink;
				}
				
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("MSG:" ,e.toString());;
		}
		endConnection();
		return null;
		
	}

	private static TagProposals getVideoTags(String getTagLink) {
		try {
			String link = null;
			ArrayList<String> artistTags = new ArrayList<String>();
			ArrayList<String> titleTags = new ArrayList<String>();
			BufferedReader reader = createRequestResultReader(GET_METHOD, new URL(getTagLink), null);
			String line = null;
			while ((line = reader.readLine()) != null) {
				Log.d("", line);
				//System.out.println(line);
				String linkIndicator = "<form class=\"form-horizontal\" action=\"";
				String tagIndicator = "<option value=\"";
				// artist tag options
				if(line.contains("<select id=\"inputArtist\" name=\"artist\"")){
					while(line.indexOf(tagIndicator) != -1){
						//get options
						int startPos = line.indexOf(tagIndicator) + tagIndicator.length();
						int endPos = line.indexOf("\"",startPos);
						artistTags.add(line.substring( startPos, endPos));
						line = line.substring(endPos + 1, line.length());
					}
					Iterator<String> iterator = artistTags.iterator();
					while(iterator.hasNext()){
						if(iterator.next().equals("")) {
							iterator.remove();
						}
					}
				}else if(line.contains("<select id=\"inputTitle\" name=\"title\"")){
					// title tag options
					while(line.indexOf(tagIndicator) != -1){
						//get options
						int startPos = line.indexOf(tagIndicator) + tagIndicator.length();
						int endPos = line.indexOf("\"",startPos);
						titleTags.add(line.substring( startPos, endPos));
						line = line.substring(endPos + 1, line.length());
					}
					Iterator<String> iterator = titleTags.iterator();
					while(iterator.hasNext()){
						if(iterator.next().equals("")) {
							iterator.remove();
						}
					}
				}else if(line.contains(linkIndicator)){
					//link to follow for download generation
					link = line.substring(line.indexOf(linkIndicator) + linkIndicator.length(),
							line.indexOf("\" method=\"post\">"));
					//System.out.println(link);

				}

			}
			TagProposals proposals = new TagProposals(artistTags, titleTags, link);
			endConnection();
			return proposals;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("MSG:" ,e.toString());;
		}
		endConnection();
		return null;

	}

	public static void downloadVideo(SetTagActivity context, String link, int format,
									 String artist, String title){
		String downlink = null;
		downlink = getDownloadLinkWithTags(link, format,
					artist, title);
		if(downlink != null ){
			getFile(context, downlink, artist, title, format);
		}
	}

	private static String getDownloadLinkWithTags(String link, int format,
												  String artist, String title){

		String url = "http://convert2mp3.net/";
		if(format == FORMAT_MP3){ link = url + link;}
		String downloadLink = null;
		Hashtable<String, String> formFields = null;
		if(format == FORMAT_MP3) {
			formFields = new Hashtable<>();
			formFields.put("artist", artist);
			formFields.put("title", title);
		}

		try {
			BufferedReader reader = createRequestResultReader(POST_METHOD, new URL(link), formFields);
			String line = null;
			String linkHeader = "<a class=\"btn btn-success btn-large\" href=\"";
			while((line = reader.readLine()) != null ){
				if(line.contains(linkHeader)){
					downloadLink = line.substring(line.indexOf(linkHeader) + linkHeader.length(),
							line.indexOf("\">", line.indexOf(linkHeader)));
					//System.out.println(downloadLink);
					break;
				}

			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("MSG:" ,e.toString());;
		}
		endConnection();
		return downloadLink;
	}

	private static File getFile(SetTagActivity context, String download, String artist,
								   String title, int format) {
		if ( download == null || download.equals("")){
			return null;
		}
		Uri downURI = Uri.parse(download);
		DownloadManager.Request downRequest = new DownloadManager.Request(downURI);
		String ending = (format == FORMAT_MP4) ? ".mp4" : ".mp3";
		downRequest.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, artist +
		" - " + title + ending);
		downRequest.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); // to notify when download is complete
		downRequest.allowScanningByMediaScanner();// if you want to be available from media players
		DownloadManager manager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
		manager.enqueue(downRequest);

		return null;
	}

	private static boolean getUidFromServer() {
		try {
			BufferedReader reader = createRequestResultReader(POST_METHOD, new URL(
					"http://convert2mp3.net"), null);
			//Process Result
			String line = null;
			while ((line = reader.readLine()) != null) {
				//search script which generates UID
				if(line.contains("</form>")){
					String nextLine = reader.readLine();
					if(nextLine.contains("<script type=\"text/javascript\">")){
						String scriptLine = reader.readLine();
						if(scriptLine.contains("var _0x2879 =")){
							//found the script
							int value1  = Integer.valueOf(scriptLine.substring(scriptLine.indexOf("=") + 2,
									scriptLine.indexOf(";")));
							scriptLine = reader.readLine();
							if(scriptLine.contains("var _0xa3bd =")){
								int value2  = Integer.valueOf(scriptLine.substring(scriptLine.indexOf("=") + 2,
										scriptLine.indexOf("+") - 1 ));
								uId = String.valueOf(value1 + value2);
								endConnection();
								return true;
							}
							
						}
					}
				}
			}

			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Log.d("MSG:" ,e.toString());;
		}
		endConnection();
		return false;
		
	}

}
