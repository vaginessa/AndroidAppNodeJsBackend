package com.kubista.vkapitest;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;
import com.stormpath.sdk.Stormpath;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * Created by wilek on 2017-03-15.
 */
public   class PhotoLikeDialog extends DialogFragment {
    private OkHttpClient okHttpClient;

    public static PhotoLikeDialog newInstance( int photo_id,String url,String usr_id) {
        PhotoLikeDialog frag = new PhotoLikeDialog();
        Bundle args = new Bundle();
        args.putString("title", "Order Likes!");
        args.putString("url", url);
        args.putInt("id", photo_id);
        args.putString("usr_id", usr_id);
        frag.setArguments(args);
        return frag;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String title = getArguments().getString("title");

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Stormpath.logger().d(message);
            }
        });
        this.okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(httpLoggingInterceptor)
                .build();

        LayoutInflater inflater = getActivity().getLayoutInflater();
       final View dialogLayout = inflater.inflate(R.layout.like_photo_dialog, null);
        AlertDialog  builder = new  AlertDialog.Builder(getActivity())
                .setIcon(R.drawable.like)
                .setTitle(title)
                .setView(dialogLayout)
                .setPositiveButton("Order Likes!",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                EditText tv = (EditText) dialogLayout.findViewById(R.id.editTextDialogLikeNum);
                                int likes = 0;
                                try {
                                    likes = Integer.parseInt(tv.getText().toString());
                                } catch (Exception e) {


                                }
                                if (likes < 1)
                                    getDialog().cancel();
                                else {
                                    RequestBody body = new FormBody.Builder()
                                            .add("id", "" + getArguments().getInt("id"))
                                            .add("url", getArguments().getString("url"))
                                       //     .add("usr_id", getArguments().getString("usr_id"))
                                            .add("usr_id", "" + 1)
                                            .add("likes", "" + likes)
                                            .build();
                                    Request request = new Request.Builder()
                                            .url("http://192.168.0.103:3000" + "/api/photo")
                                            .post(body)
                                            .build();
                                    okHttpClient.newCall(request).enqueue(new Callback() {
                                        @Override
                                        public void onFailure(Call call, IOException e) {
                                        }

                                        @Override
                                        public void onResponse(Call call, Response response)
                                                throws IOException {

                                            Intent intent = new Intent(MainActivity.ACTION_ADD_PHOTO_TO_LIKE_PHOTOS);
                                            intent.putExtra(MainActivity.ACTION_ADD_PHOTO_TO_LIKE_PHOTOS, response.isSuccessful());
                                            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(intent);
                                            // getNextPhoto();
                                        }
                                    });
                                }
                            }
                        }
                )
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int whichButton) {
                                getDialog().cancel();
                            }
                        }
                )
                .create();
        // Inflate and set the layout for the dialog
        // Pass null as the parent view because its going in the dialog layout

        Picasso.with(getActivity()) //
                .load(getArguments().getString("url")) //
                .placeholder(R.drawable.placeholder) //
                .error(R.drawable.error) //
                .fit() //
                .tag(getActivity()) //
                .into((ImageView)   dialogLayout.findViewById(R.id.imageViewDialog));


        return builder;
    }
}