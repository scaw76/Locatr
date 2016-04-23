package com.saracawley.locatr;

import android.location.Location;
import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by sara on 4/8/2016.
 */
public class FlickrFetchr {
    private static String TAG = "FlickrFetchr";
    private static String API_KEY ="143d81a86a615c508b11a2dbfc08aaad";


    public  byte[] getUrlBytes(String urlSpec) throws IOException{
        URL url = new URL(urlSpec);
        HttpURLConnection connection = (HttpURLConnection)url.openConnection();
        try{
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();
            if(connection.getResponseCode() != HttpURLConnection.HTTP_OK){
                throw new IOException(connection.getResponseMessage()+ ": with " +urlSpec);
            }
            int bytesRead = 0 ;
            byte[] buffer = new byte[1024];
            while ((bytesRead = in.read(buffer)) >0){
                out.write(buffer, 0, bytesRead);
            }
            out.close();
            return  out.toByteArray();
        }finally {
            connection.disconnect();
        }
    }
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }
    public List<GalleryItem> fetchItems(Location location){
        List<GalleryItem> items = new ArrayList<>();

        try {
            String url = Uri.parse("https://api.flickr.com/services/rest/")
                    .buildUpon()
                    .appendQueryParameter("method", "flickr.photos.search")
                    .appendQueryParameter("api_key", API_KEY)
                    .appendQueryParameter("format", "json")
                    .appendQueryParameter("nojsoncallback", "1")
                    .appendQueryParameter("lat", "" + location.getLatitude())
                    .appendQueryParameter("lon", "" + location.getLongitude())
                    .appendQueryParameter("extras", "url_s, geo")
                    .build().toString();
            String jsonString = getUrlString(url);
            //Log.i(TAG, "Recieved JSON: " + jsonString);
            JSONObject jsonBody = new JSONObject(jsonString);
            parseItems(items,jsonBody);
        }catch (JSONException je){
            Log.e(TAG, "Failed to parse JSON", je);
        }catch (IOException ioe){
            Log.e(TAG, "Failed to fetch items" +ioe);
        }
        Log.d(TAG, items.toString());
        return items;
    }
    public List<GalleryItem> searchPhotos(Location location){
        return fetchItems(location);
    }

    private  void parseItems(List<GalleryItem> items, JSONObject jsonBody)throws IOException, JSONException {
        JSONObject photosJsonObject = jsonBody.getJSONObject("photos");
        JSONArray photoJsonArray = photosJsonObject.getJSONArray("photo");

        for(int i = 0; i<photoJsonArray.length(); i++){
            JSONObject photoJsonObject = photoJsonArray.getJSONObject(i);

            GalleryItem item = new GalleryItem();
            item.setId(photoJsonObject.getString("id"));
            item.setCaption(photoJsonObject.getString("title"));

            if(!photoJsonObject.has("url_s")){
                continue;
            }
            item.setUrl(photoJsonObject.getString("url_s"));
            item.setLat(photoJsonObject.getDouble("latitude"));
            item.setLon(photoJsonObject.getDouble("longitude"));
            items.add(item);
        }
    }
}
