package com.kubista.vkapitest;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewSwitcher;

import com.squareup.picasso.Picasso;
import com.stormpath.sdk.Stormpath;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.util.VKUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class MainActivity extends AppCompatActivity {

    private static final String ACTION_GET_PHOTOS = "get_photos";
    public static final String ACTION_ADD_PHOTO_TO_LIKE_PHOTOS = "like_photos_add";
    private static String ACTION_LIKE_PHOTO = "like_photo";
    private static String ACTION_LIKE_GET_PHOTOS = "like_photo";
    public GalleryFragment galleryFragment;
    public LikerFragment likerFragment;
    public SettingsFragment settingsFragment;
    private String usr_id;
    private VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                // VkAccessToken is invalid
            }
        }
    };

    private BroadcastReceiver response_received = new BroadcastReceiver() {


        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().contentEquals(ACTION_LIKE_GET_PHOTOS)){
            //      responseTV.setText(intent.getExtras().getString("notes"));
                likerFragment.getNextPhoto();
            }
            else if( intent.getAction().contentEquals(ACTION_GET_PHOTOS)){
                galleryFragment.onGetPhotos(intent.getStringExtra(ACTION_GET_PHOTOS));
            }
            else if( intent.getAction().contentEquals(ACTION_ADD_PHOTO_TO_LIKE_PHOTOS)){
                onAddPhotosToLikePhotos(intent.getBooleanExtra(ACTION_ADD_PHOTO_TO_LIKE_PHOTOS, false));
            }
        }
    };

    private void onAddPhotosToLikePhotos(Boolean succesful) {
        if(!succesful)
            Toast.makeText(this,"MOTheR fucker gee",Toast.LENGTH_SHORT);
    }

    private OkHttpClient okHttpClient;

    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(),this);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });


        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        vkAccessTokenTracker.startTracking();
        VKSdk.login(this, "wall", "photos");

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Stormpath.logger().d(message);
            }
        });

        this.okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(httpLoggingInterceptor)
                .build();

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class GalleryFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private  MainActivity parent;
        private  TextView textView;
        public  GridView gv;
        public GalleryFragment(){};
        public GridViewAdapter adapter;
        public  GalleryFragment(MainActivity parent) {
            this.parent = parent;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_gallery, container, false);
            textView = (TextView) rootView.findViewById(R.id.section_label);
            gv  = (GridView) rootView.findViewById(R.id.gridView);

     /*      gv.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                    final Picasso picasso = Picasso.with(context);
                    if (scrollState == SCROLL_STATE_IDLE || scrollState == SCROLL_STATE_TOUCH_SCROLL) {
                        picasso.resumeTag(context);
                    } else {
                        picasso.pauseTag(context);
                    }
                }

                @Override
                public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,
                                     int totalItemCount) {
                    // Do nothing.
                }
            }); */
            parent.getPhotos();
            return rootView;
        }

        public void onGetPhotos(String reponse) {
         //  textView.setText(reponse);
            JSONObject jsn_rspns = null;
            try {
                jsn_rspns = new JSONObject(reponse);
                JSONArray data =  jsn_rspns.getJSONObject("response").getJSONArray("items");
                List<String> urls = new ArrayList<>();
                List<String> bigPhotosUrls = new ArrayList<>();
                List<Integer> ids = new ArrayList<>();
                for(int i = 0 ; i < data.length(); i++){
                    urls.add(data.getJSONObject(i).getString("photo_75"));
                    bigPhotosUrls.add(data.getJSONObject(i).getString("photo_75"));
                    ids.add(data.getJSONObject(i).getInt("id"));
                }

                adapter = new GridViewAdapter(parent, urls,bigPhotosUrls, ids);
                gv.setAdapter(adapter);
                gv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                        parent.onPhotoToLikeSelected( new Integer((int)adapter.getItemId(i)),adapter.getItem(i));

                    }
                });

                Intent intent = new Intent(ACTION_GET_PHOTOS);
                intent.putExtra(ACTION_GET_PHOTOS, jsn_rspns.toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
       
            /*Picasso.with(parent) //
                    .load("https://www.google.pl/url?sa=i&rct=j&q=&esrc=s&source=images&cd=&cad=rja&uact=8&ved=0ahUKEwi0o56fn9TSAhUIdCwKHegFBnAQjRwIBw&url=http%3A%2F%2Fwww.cda.pl%2FeQplement%2Fgaleria%2Fzdjecie%2F394206&psig=AFQjCNH5YWSdqOS1z5sArC1yk_NdFxq1rQ&ust=1489520724603365") //
                    .resizeDimen(400,
                            400) //
                    .into(image); */
        }
    }



    public static class SettingsFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        MainActivity parent;

        public SettingsFragment() {
        }

        public SettingsFragment(MainActivity parent) {
            this.parent = parent;
        }



        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_menu, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
       //     textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    public static class LikerFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private MainActivity parent;
        public ImageSwitcher switcher;
        private LinkedList<String> urls;
        private LinkedList<String> ids;
        private String current_id;
        public LikerFragment() {
        }

        public LikerFragment(MainActivity parent) {
            this.parent = parent;
        }


        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_main_liker, container, false);
            Button like = (Button) rootView.findViewById(R.id.button_liker_like);
            switcher = (ImageSwitcher) rootView.findViewById(R.id.imageSwitcher);
            like.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    parent.onPhotoLike(current_id);
                    getNextPhoto();
                }
            });
            switcher.setFactory(new ViewSwitcher.ViewFactory() {
                public View makeView() {
                    ImageView myView = new ImageView(getActivity());
                    return myView;
                }
            });
            switcher.setImageResource(R.drawable.placeholder);

            Animation in = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_in_left);
            Animation out = AnimationUtils.loadAnimation(getActivity(), android.R.anim.slide_out_right);
            switcher.setInAnimation(in);
            switcher.setOutAnimation(out);
            Picasso.with(parent) //
                    .load("http://i.imgur.com/i482A1L.jpg") //
                    .into((ImageView) switcher.getCurrentView());
            getUrls();

            return rootView;
        }

        public void getUrls() {
            urls = new LinkedList<>();
            ids = new LinkedList<>();
            Request request = new Request.Builder()
                    .url("http://192.168.0.103:3000" + "/api/photos")
                    .get()
                    .build();
            parent.okHttpClient.newCall(request).enqueue(new Callback() {
                @Override
                public void onFailure(Call call, IOException e) {
                }

                @Override
                public void onResponse(Call call, Response response)
                        throws IOException {
                    JSONObject jsn_rspns = null;
                    try {
                        jsn_rspns = new JSONObject(response.body().string());
                        JSONArray data = jsn_rspns.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            urls.add(data.getJSONObject(i).getString("url"));
                            ids.add(data.getJSONObject(i).getString("id"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    Intent intent = new Intent(ACTION_LIKE_GET_PHOTOS);
                    intent.putExtra(ACTION_LIKE_GET_PHOTOS,response.isSuccessful());
                    LocalBroadcastManager.getInstance(getContext()).sendBroadcast(intent);
                   // getNextPhoto();
                }
            });

        }

        public void getNextPhoto(){
            if(urls.size() > 0) {
                current_id =  ids.poll();
                Picasso.with(parent) //
                        .load(urls.poll()) //
                        .into((ImageView) switcher.getCurrentView());
            }
        }
    }


    /*
     *  communication interface for fragments
     */


    private void onPhotoLike(String photo_id) {
        Request request = new Request.Builder()
                .url("http://192.168.0.103:3000" + "/api/photo:" + photo_id )
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override public
            void onFailure(Call call, IOException e) {
            }

            @Override public void onResponse(Call call, Response response)
                    throws IOException {
                    Intent intent = new Intent(ACTION_LIKE_PHOTO);
                    intent.putExtra(ACTION_LIKE_PHOTO,response.isSuccessful());
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        });
    }


    private void onPhotoToLikeSelected(Integer photo_id,String url) {

        DialogFragment newFragment = PhotoLikeDialog.newInstance(photo_id,url,usr_id);
        newFragment.show(getFragmentManager(), "dialog");


      /*  Request request = new Request.Builder()
                .url("http://192.168.0.103:3000" + "/api/photo:" + photo_id )
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override public
            void onFailure(Call call, IOException e) {
            }

            @Override public void onResponse(Call call, Response response)
                    throws IOException {
                Intent intent = new Intent(ACTION_ADD_PHOTO_TO_LIKE_PHOTOS);
                intent.putExtra(ACTION_ADD_PHOTO_TO_LIKE_PHOTOS,response.isSuccessful());
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }); */

  /*  Request request = new Request.Builder()
                .url("http://192.168.0.103:3000" + "/api/photos" )
                .get()
                .build();

        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override public
            void onFailure(Call call, IOException e) {
            }

            @Override public void onResponse(Call call, Response response)
                    throws IOException {
                JSONObject jsn_rspns;

                    Intent intent = new Intent(ACTION_GET_PHOTOS);
                    intent.putExtra(ACTION_GET_PHOTOS,response.body().string());
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }
        }); */
    }

    private void getPhotos() {

        VKRequest reg = new VKRequest("photos.getAll");
        VKAccessToken.currentToken();
        reg.addExtraParameter("extended", 1);
        reg.addExtraParameter("count", 2);
        reg.addExtraParameter("photo_sizes", 0);
        reg.addExtraParameter("no_service_albums", 0);
        reg.addExtraParameter("need_hidden", 0);
        reg.addExtraParameter("skip_hidden", 1);
        reg.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                //Do complete stuffg
            //    log.setText("complete " + response.responseString);
                Intent intent = new Intent(ACTION_GET_PHOTOS);
                intent.putExtra(ACTION_GET_PHOTOS,response.responseString);
                LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
            }

            @Override
            public void onError(VKError error) {
                //Do error stuff
           //     log.setText("error " + error.toString());
            }

            @Override
            public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                //I don't really believe in progress
            }
        });
    }





    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {
    MainActivity parent;

        public SectionsPagerAdapter(FragmentManager fm,MainActivity parent) {
            super(fm);
            this.parent = parent;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a GalleryFragment (defined as a static inner class below).
          switch(position){
              case 0:
                  parent.likerFragment = new LikerFragment(parent);
                  return  parent.likerFragment;
              case 1:
                  parent.galleryFragment = new GalleryFragment(parent);
                  return   parent.galleryFragment;
              case 2:
                  parent.settingsFragment = new SettingsFragment(parent);
                  return   parent.settingsFragment;
              default:
                  return null;
          }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "Photos";
                case 1:
                    return "Get Likes";
                case 2:
                    return "Menu";
            }
            return null;
        }


    }

    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(response_received, new IntentFilter(ACTION_GET_PHOTOS));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // User passed Authorization
                 usr_id = res.userId;


                RequestBody body = new FormBody.Builder()
                        .add("id", "" +  5)
                        .build();
                Request request = new Request.Builder()
                        .url("http://192.168.0.103:3000" + "/api/user")
                        .post(body)
                        .build();
                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override
                    public void onFailure(Call call, IOException e) {
                    }

                    @Override
                    public void onResponse(Call call, Response response)
                            throws IOException {
                    }
                });

            }
            @Override
            public void onError(VKError error) {
                // User didn't pass Authorization
                Log.e("error", error.toString());
                Toast.makeText(getApplicationContext(), "NEED TO LOGIN",Toast.LENGTH_LONG);
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
