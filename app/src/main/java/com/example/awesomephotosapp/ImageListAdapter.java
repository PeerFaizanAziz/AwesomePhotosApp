package com.example.awesomephotosapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class ImageListAdapter extends RecyclerView.Adapter<ImageListAdapter.MyViewHolder> {
    List<ModelClass> myDataList;
    Context context;
    HashMap<String, String> hashMap;


    public ImageListAdapter(List<ModelClass> myDataList, Context context, HashMap<String, String> hashMap) {
        this.myDataList = myDataList;
        this.context = context;
        this.hashMap = hashMap;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.single_item_photo_layout, parent, false);
        MyViewHolder holder = new MyViewHolder(v);
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        ModelClass modelClass = myDataList.get(position);


        String encodedStr=hashMap.get(modelClass.getId());
//        Bitmap bitmap = hashMap.get(modelClass.getId());

        holder.nameTv.setText(modelClass.getUser().getUsername().toUpperCase());
        holder.descTv.setText(modelClass.getDescription());
        Log.d("TAG", "onBindViewHolder:1 "+position+" pre"+hashMap.get(modelClass.getId()));

        if (encodedStr == null) {
            MyImageTask task = new MyImageTask();
            task.setViewHolder(holder);
            task.execute(modelClass);
        } else {
//            convert
            byte[] decodedString = Base64.decode(encodedStr, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            holder.imageView.setImageBitmap(decodedByte);
            Log.d("TAG", "onBindViewHolder: from phone"+hashMap.get(modelClass.getId()));
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String url = modelClass.getUrls().getRegular();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                context.startActivity(i);
            }
        });
    }

    @Override
    public int getItemCount() {
        return myDataList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView nameTv, descTv;
        ImageView imageView;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_view);
            nameTv = itemView.findViewById(R.id.photo_name);
            descTv = itemView.findViewById(R.id.photo_desc);

        }
    }


    class MyImageTask extends AsyncTask<ModelClass, Void, Bitmap> {

        private static final String PHOTO_BASE_URL =
                "https://images.unsplash.com";
        private ModelClass model;
        private MyViewHolder mViewHolder;

        public void setViewHolder(MyViewHolder myViewHolder) {
            this.mViewHolder = myViewHolder;
        }

        @Override
        protected Bitmap doInBackground(ModelClass... modelClasses) {
            Bitmap bitmap = null;
            model = modelClasses[0];

            String imageurl = model.getUrls().getThumb();

            InputStream inputStream = null;

            try {
                URL imageUrl = new URL(imageurl);
                inputStream = (InputStream) imageUrl.getContent();
                bitmap = BitmapFactory.decodeStream(inputStream);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
            Log.d("TAG", "doInBackground: Image downloaded:adapter " + imageurl);

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            mViewHolder.imageView.setImageBitmap(bitmap);
        }
    }
}
