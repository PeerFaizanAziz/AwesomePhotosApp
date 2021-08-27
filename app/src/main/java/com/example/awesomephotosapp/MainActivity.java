package com.example.awesomephotosapp;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {


    private RecyclerView recyclerView;
    private ImageListAdapter mAdapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView.LayoutManager mLayoutManager;
    private String clientId = "Client-ID N_sLL516R-J1Lb91_rkJshfo2wJjznxO5SjtdCwV9Q8";
    SharedPreferences sharedPreferences;
    List<ModelClass> arrayItems;
    HashMap<String, String> hashMap = new HashMap();
    List<ModelClass> listOfData;
    int counter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sharedPreferences = getSharedPreferences("list", MODE_PRIVATE);
        recyclerView = findViewById(R.id.recycler_view);
        swipeRefreshLayout = findViewById(R.id.swiper_refresh_layout);
        mLayoutManager = new LinearLayoutManager(this);
        checkPermissions();

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (isNetworkAvailable()) {
                    callData();
                    swipeRefreshLayout.setRefreshing(false);
                }
            }
        });
        if (isNetworkAvailable()) {
            callData();
        } else {
            getList();
        }
    }


    private void callData() {
        ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setTitle("Loading data ");
        progressDialog.setMessage("Please wait..");
        progressDialog.show();
        Toast.makeText(MainActivity.this, "network", Toast.LENGTH_SHORT).show();
        Call<List<ModelClass>> call = APIClient.getUserServices().getListOfPhotos(clientId,
                "30");
        call.enqueue(new Callback<List<ModelClass>>() {
            @Override
            public void onResponse(Call<List<ModelClass>> call, Response<List<ModelClass>> response) {
                if (response.code() == 200) {
                    listOfData = response.body();
                    mAdapter = new ImageListAdapter(listOfData, MainActivity.this, hashMap);
                    recyclerView.setLayoutManager(mLayoutManager);
                    recyclerView.setAdapter(mAdapter);

//                    downloadImage(listOfData);
                    for (int i = 0; i < listOfData.size(); i++) {
                        MyImageTask task = new MyImageTask();
                        task.execute(listOfData.get(i));
                    }
                }
                progressDialog.dismiss();
                Log.d("TAG", "onResponse: " + response.code());
            }

            @Override
            public void onFailure(Call<List<ModelClass>> call, Throwable t) {
                Toast.makeText(MainActivity.this, "Failed to load data due to :" + t.getCause(), Toast.LENGTH_SHORT).show();
                Log.d("TAG", "onFailure: " + t.getCause());
                Log.d("TAG", "onFailure: " + t.getMessage());
                progressDialog.dismiss();

            }
        });

    }


    private void checkPermissions() {
        if (!(ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)) {
            requestStoragePermission();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 2) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                new AlertDialog.Builder(this)
                        .setTitle("Permission Denied")
                        .setMessage("Please enable the permissions in settings")
                        .setCancelable(false)
                        .setPositiveButton("ok", null)
                        .create().show();
            }
        }
    }

    public void requestStoragePermission() {

        new AlertDialog.Builder(this)
                .setTitle("Permission needed")
                .setCancelable(false)
                .setMessage("This Application needs location and other permissions")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(MainActivity.this,
                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                                2);
                    }
                })
                .create().show();
    }

    private void savtopref(List<ModelClass> listOfData, HashMap<String, String> hashMap) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        Gson gson = new Gson();

        String json = gson.toJson(listOfData);
        editor.putString("list", json);

        String hmp = gson.toJson(hashMap);
        editor.putString("bitmap", hmp);

        editor.commit();
        Log.d("TAG", "savtopref: saved for later use");
    }


    public void getList() {
        HashMap<String, String> newHashmap = new HashMap<>();
        Toast.makeText(MainActivity.this, "phone", Toast.LENGTH_SHORT).show();
        String serializedObject = sharedPreferences.getString("list", null);
        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<ModelClass>>() {
            }.getType();
            arrayItems = gson.fromJson(serializedObject, type);
        }

        String sObj = sharedPreferences.getString("bitmap", null);
        if (sObj != null) {
            Gson gson = new Gson();
            Type type2 = new TypeToken<HashMap<String, String>>() {
            }.getType();
            newHashmap = gson.fromJson(sObj, type2);
        }

        mAdapter = new ImageListAdapter(arrayItems, MainActivity.this, newHashmap);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setAdapter(mAdapter);
        Log.d("TAG", "getList: " + arrayItems.size());
    }


    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    class MyImageTask extends AsyncTask<ModelClass, Void, Bitmap> {

        private static final String PHOTO_BASE_URL =
                "https://images.unsplash.com";
        private ModelClass model;
        private ImageListAdapter.MyViewHolder mViewHolder;

        public void setViewHolder(ImageListAdapter.MyViewHolder myViewHolder) {
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


                savtopref(listOfData, hashMap);
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

            return bitmap;
        }

        @SuppressLint("WrongThread")
        @Override
        protected void onPostExecute(Bitmap bitmap) {
            super.onPostExecute(bitmap);
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 50, byteArrayOutputStream);
            byte[] byteArray = byteArrayOutputStream.toByteArray();
//                to encode base64 from byte array use following method
            String encoded = Base64.encodeToString(byteArray, Base64.DEFAULT);

            Log.d("TAG", "onPostExecute: activity " + encoded);

            if (counter < 29) {
                hashMap.put(listOfData.get(counter).getId(), encoded);
                Log.d("TAG", "onPostExecute: before" + counter);
                counter = counter + 1;
                Log.d("TAG", "onPostExecute: after" + counter);
                savtopref(listOfData, hashMap);
            }
//            mViewHolder.imageView.setImageBitmap(bitmap);
        }
    }

}



