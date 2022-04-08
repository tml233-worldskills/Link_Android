package com.example.link;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

public class LinkPhotoAdapter extends ArrayAdapter<Bitmap> {
    public LinkPhotoAdapter(Context context, List<Bitmap> objects) {
        super(context, R.layout.list_item_photo, objects);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.list_item_photo,parent,false);
        final Bitmap bitmap = getItem(position);
        try{
            ((TextView)view.findViewById(R.id.id)).setText(Integer.toString(position));
            ((ImageView)view.findViewById(R.id.photo)).setImageBitmap(bitmap);
        }catch (Exception e){
            e.printStackTrace();
        }
        return view;
    }
}
