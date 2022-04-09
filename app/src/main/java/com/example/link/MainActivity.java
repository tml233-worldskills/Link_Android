package com.example.link;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public class MainActivity extends AppCompatActivity {
    List<LinkData> links=new ArrayList<>();
    ListView listLinks;
    LinkAdapter adapter;
    TextView lblTotal;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        lblTotal=findViewById(R.id.main_text_total);
        ImageButton btnAdd=findViewById(R.id.main_btn_add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent it=new Intent(MainActivity.this,AddActivity.class);
                startActivityForResult(it,0);
            }
        });
        ImageButton btnRefresh=findViewById(R.id.main_btn_refresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshData(null);
            }
        });
        final EditText editSearch=findViewById(R.id.main_edit_search);
        ImageButton btnSearch=findViewById(R.id.main_btn_search);
        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                refreshData(editSearch.getText().toString());
            }
        });
        listLinks=findViewById(R.id.main_list);
        adapter=new LinkAdapter(this,links);
        refreshData(null);
    }

    void refreshData(final String searchText){
        links.clear();
        Future<?> future=Utils.threadPool.submit(new Runnable() {
            @Override
            public void run() {
                try {
                    String linkIdsJson=Utils.sendGet(searchText==null?Utils.baseURL + "/links":Utils.baseURL + "/links?name="+searchText);
                    JSONObject resultObj=new JSONObject(linkIdsJson);
                    JSONArray idArr=resultObj.getJSONArray("data");
                    for(int i=0;i<idArr.length();i+=1) {
                        int id = idArr.getInt(i);
                        String linkJson = Utils.sendGet(Utils.baseURL+"/links/" + id+"?limited-photo=1");
                        JSONObject linkObj = new JSONObject(linkJson);
                        JSONObject linkDataObj = linkObj.getJSONObject("data");
                        LinkData linkData = new LinkData();
                        linkData.id = id;
                        linkData.siteName = linkDataObj.getString("siteName");
                        linkData.url = linkDataObj.getString("url");
                        linkData.gradeId = linkDataObj.getInt("gradeId");
                        linkData.gradeName = linkDataObj.getString("gradeName");
                        linkData.beizhu = linkDataObj.getString("beizhu");
                        linkData.time = linkDataObj.getString("time");
                        linkData.photos = new ArrayList<>();
                        JSONArray photoArr = linkDataObj.getJSONArray("photos");
                        for (int j = 0; j < photoArr.length(); j += 1) {
                            String photoStr = photoArr.getString(j);
                            linkData.photos.add(Utils.strToBitmap(photoStr));
                        }
                        links.add(linkData);
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        try{
            future.get();
            listLinks.setAdapter(adapter);
            lblTotal.setText("Total Links: "+links.size());
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        refreshData(null);
    }
}
