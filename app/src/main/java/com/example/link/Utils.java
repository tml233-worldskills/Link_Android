package com.example.link;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Utils {
    public static String baseURL = "http://10.0.2.2:6001";
    public static ExecutorService threadPool= Executors.newFixedThreadPool(3);

    public static String sendRequest(String method,String url,String body,boolean strictOk){
        try {
            HttpURLConnection conn=(HttpURLConnection)new URL(url).openConnection();
            conn.setRequestMethod(method);
            if(body!=null) {
                conn.setDoOutput(true);
                PrintWriter writer=new PrintWriter(conn.getOutputStream());
                writer.write(body);
                writer.close();
            }

            int code=conn.getResponseCode();
            if(strictOk&&code != 200){
                return null;
            }

            StringBuilder builder=new StringBuilder();
            BufferedReader reader=new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) builder.append(line);
            return builder.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public static String sendPost(String url,String body){
        return sendRequest("POST",url,body,false);
    }
    public static String sendGet(String url){
        return sendRequest("GET",url,null,true);
    }
    public static String sendPut(String url,String body){
        return sendRequest("PUT",url,body,false);
    }

    public static Bitmap strToBitmap(String assetPhoto){
        byte[] bytes = Base64.decode(assetPhoto,Base64.DEFAULT);
        return BitmapFactory.decodeByteArray(bytes,Base64.DEFAULT,bytes.length);
    }
    public static String bitmapToStr(Bitmap bitmap){
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] bytes = baos.toByteArray();
        return Base64.encodeToString(bytes,Base64.DEFAULT);
    }
}