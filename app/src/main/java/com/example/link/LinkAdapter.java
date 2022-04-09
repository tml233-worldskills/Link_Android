package com.example.link;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

public class LinkAdapter extends ArrayAdapter<LinkData> {
    public LinkAdapter(Context context, List<LinkData> objects) {
        super(context, R.layout.list_item_link, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view=LayoutInflater.from(getContext()).inflate(R.layout.list_item_link,parent,false);
        final LinkData obj=getItem(position);
        try {
            ((TextView)view.findViewById(R.id.lbl_name)).setText(obj.siteName);
            ((TextView)view.findViewById(R.id.lbl_url)).setText(obj.url);
            ((TextView)view.findViewById(R.id.lbl_grade)).setText(obj.gradeName);
            ((TextView)view.findViewById(R.id.lbl_date)).setText(obj.time);
            if(obj.photos.size()>0){
                ((ImageView)view.findViewById(R.id.img_photo)).setImageBitmap(obj.photos.get(0));
            }
            ((ImageButton)view.findViewById(R.id.btn_edit)).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent=new Intent(getContext(),AddActivity.class);
                    intent.putExtra("editingId",obj.id);
                    ((Activity)getContext()).startActivityForResult(intent,0);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
        return view;
    }
}
