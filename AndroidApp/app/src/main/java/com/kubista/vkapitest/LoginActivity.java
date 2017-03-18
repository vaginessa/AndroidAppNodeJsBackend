package com.kubista.vkapitest;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Debug;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build.VERSION;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.stormpath.sdk.Stormpath;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKAccessTokenTracker;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.util.VKUtil;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.FormBody;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

import static android.Manifest.permission.READ_CONTACTS;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {

    TextView log;
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private View mProgressView;
    private View mLoginFormView;
    public TextView responseTV;
    String id = "";

    VKAccessTokenTracker vkAccessTokenTracker = new VKAccessTokenTracker() {
        @Override
        public void onVKAccessTokenChanged(VKAccessToken oldToken, VKAccessToken newToken) {
            if (newToken == null) {
                // VkAccessToken is invalid
            }
        }
    };

    private BroadcastReceiver onNoteReceived = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
        /*    if(intent.getAction().contentEquals(ACTION_GET_NOTES))
                responseTV.setText(intent.getExtras().getString("notes"));
            else if(intent.getAction().contentEquals(ACTION_POST_NOTES))
                Snackbar.make(mNote, getString(R.string.saved), Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show(); */
            responseTV.setText(intent.getExtras().getString("response"));
        }
    };

    private OkHttpClient okHttpClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        // Set up the login form.
        log = (TextView)findViewById(R.id.textViewnrewlog);
        vkAccessTokenTracker.startTracking();

        // Initialize OkHttp library.
        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Stormpath.logger().d(message);
            }
        });

        this.okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(httpLoggingInterceptor)
                .build();

        VKSdk.login(this, "wall","photos");

        responseTV = (TextView)findViewById(R.id.textViewnrewlog);

        Button showImageButton = (Button) findViewById(R.id.buttonShowPhotos);
        showImageButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Request request = new Request.Builder()
                        .url("http://192.168.0.12:3000" + "/api/photos" )
                                .get()
                                .build();

                okHttpClient.newCall(request).enqueue(new Callback() {
                    @Override public
                    void onFailure(Call call, IOException e) {
                    }

                    @Override public void onResponse(Call call, Response response)
                            throws IOException {
                        JSONObject mNotes;

                        try {
                            mNotes = new JSONObject(response.body().string());
                            String noteCloud = mNotes.toString();

                            Intent intent = new Intent("my_reponse");
                            intent.putExtra("response",noteCloud);
                            LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
                        } catch (JSONException e) {
                        }
                    }
                });
            }
        });
        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                VKRequest reg = new VKRequest("likes.add");
                VKAccessToken.currentToken();
                reg.addExtraParameter("type", "photo");
                reg.addExtraParameter("item_id", id);
              //  reg.addExtraParameter("access_key",VKAccessToken.currentToken().accessToken);
                reg.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        //Do complete stuffg
                        log.setText("complete " + response.responseString);
                    }

                    @Override
                    public void onError(VKError error) {
                        //Do error stuff
                        log.setText("error " + error.toString() + " " + VKAccessToken.currentToken().accessToken);
                    }

                    @Override
                    public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                        //I don't really believe in progress
                    }
                });

            }
        });

        final Button btn2 = (Button) findViewById(R.id.btn_test);
        btn2.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                VKRequest reg = VKApi.users().get();
                reg.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        //Do complete stuffg
                        log.setText("complete " + response.responseString);
                    }

                    @Override
                    public void onError(VKError error) {
                        //Do error stuff
                        log.setText("error " + error.toString());
                    }

                    @Override
                    public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                        //I don't really believe in progress
                    }
                });

            }
        });


        final Button btn3 = (Button) findViewById(R.id.buttonPhotos);
        btn3.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                VKRequest reg = new VKRequest("photos.getAll");
                VKAccessToken.currentToken();
                reg.addExtraParameter("extended", 1);
                reg.addExtraParameter("count", 100);
                reg.addExtraParameter("photo_sizes", 1);
                reg.addExtraParameter("no_service_albums", 0);
                reg.addExtraParameter("need_hidden", 0);
                reg.addExtraParameter("skip_hidden", 1);
                reg.executeWithListener(new VKRequest.VKRequestListener() {
                    @Override
                    public void onComplete(VKResponse response) {
                        //Do complete stuffg
                        log.setText("complete " + response.responseString);
                        try {
                            id = response.json.getString("id");
                        } catch (JSONException e) {
                            e.printStackTrace();
                            id = "456239017";
                        }
                    }

                    @Override
                    public void onError(VKError error) {
                        //Do error stuff
                        log.setText("error " + error.toString());
                    }

                    @Override
                    public void attemptFailed(VKRequest request, int attemptNumber, int totalAttempts) {
                        //I don't really believe in progress
                    }
                });

            }
        });



        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken res) {
                // User passed Authorization

            }
            @Override
            public void onError(VKError error) {
                // User didn't pass Authorization
                Log.e("error",error.toString());
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    /**
     * Callback received when a permissions request has been completed.
     */


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */


    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

    }

    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Use an AsyncTask to fetch the user's email addresses on a background thread, and update
     * the email text field with results on the main UI thread.
     */
    class SetupEmailAutoCompleteTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected List<String> doInBackground(Void... voids) {
            ArrayList<String> emailAddressCollection = new ArrayList<>();

            // Get all emails from the user's contacts and copy them to a list.
            ContentResolver cr = getContentResolver();
            Cursor emailCur = cr.query(ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                    null, null, null);
            while (emailCur.moveToNext()) {
                String email = emailCur.getString(emailCur.getColumnIndex(ContactsContract
                        .CommonDataKinds.Email.DATA));
                emailAddressCollection.add(email);
            }
            emailCur.close();

            return emailAddressCollection;
        }

        @Override
        protected void onPostExecute(List<String> emailAddressCollection) {
            addEmailsToAutoComplete(emailAddressCollection);
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        IntentFilter notePostFilter = new IntentFilter("my_reponse");
        LocalBroadcastManager.getInstance(this).registerReceiver(onNoteReceived, notePostFilter);
    }
}

