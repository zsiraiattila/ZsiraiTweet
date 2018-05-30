package com.zsirai.zsiraitweet;

import android.content.Intent;
import android.icu.util.TimeUnit;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mopub.common.util.Json;
import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterApiClient;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.FixedTweetTimeline;
import com.twitter.sdk.android.tweetui.TweetTimelineRecyclerViewAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;

public class FeedActivity extends AppCompatActivity {

    private static final String REST_ENDPOINT = "http://10.34.10.18:3000";
    private static final String REST_ENDPOINT_KABINET = "http://192.168.0.101:3000";
    TextView titleFeedTV;
    TwitterSession twitterSession;
    TwitterApiClient twitterApiClient;
    StatusesService statusesService;
    RecyclerView tweetFeedRV;
    List<Tweet> gtweets;
    int numTweets;
    OkHttpClient okHttpClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zsirai.zsiraitweet.R.layout.activity_feed);
        numTweets = 15;
        tweetFeedRV = (RecyclerView) findViewById(com.zsirai.zsiraitweet.R.id.tweetFeedRV);
        titleFeedTV = (TextView) findViewById(com.zsirai.zsiraitweet.R.id.titleFeedTV);
        twitterApiClient = TwitterCore.getInstance().getApiClient();
        statusesService = twitterApiClient.getStatusesService();
        twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        titleFeedTV.setText(twitterSession.getUserName() + " 's " + titleFeedTV.getText());
        okHttpClient = new OkHttpClient.Builder().connectTimeout(100, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(100,java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(100, java.util.concurrent.TimeUnit.SECONDS)
                .build();
        getandloadTweets(twitterSession, numTweets);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.zsirai.zsiraitweet.R.menu.timeline_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == com.zsirai.zsiraitweet.R.id.menu_referesh) {
            getandloadTweets(twitterSession, numTweets);
        }
        if (item.getItemId() == com.zsirai.zsiraitweet.R.id.menu_logout && twitterSession != null) {
            TwitterCore.getInstance().getSessionManager().clearActiveSession();
            Toast.makeText(getApplicationContext(), "Logout from Twitter", Toast.LENGTH_LONG).show();
            startActivity(new Intent(FeedActivity.this, LoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private String getTweetsInJson(List<Tweet> tweets) throws JSONException {
        JSONArray arrJson = new JSONArray();
        for (Tweet actTweet : tweets) {
            JSONObject objJson = getTweetInJSON(actTweet);
            if (objJson != null) {
                arrJson.put(objJson);
            }
        }
        return arrJson.toString();
    }

    private void getandloadTweets(TwitterSession session, int numTweets) {
        tweetFeedRV.setLayoutManager(new LinearLayoutManager(this));
        Call<List<Tweet>> call = statusesService.homeTimeline(numTweets, null,
                null, null, null, null, null);
        call.enqueue(new Callback<List<Tweet>>() {

            @Override
            public void success(Result<List<Tweet>> result) {
                final ArrayList<Tweet> tweets = new ArrayList<Tweet>();
                for (int i = 0; i < result.data.size(); i++) {
                    if ((result.data.get(i).lang.compareTo("en") == 0) && tweetIsOK(result.data.get(i))) {
                        tweets.add(result.data.get(i));
                    }
                }
                RequestBody requestBody = null;
                try {
                    requestBody = RequestBody.create(MediaType.parse("application/json"), getTweetsInJson(tweets));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Request request = new Request.Builder().url(REST_ENDPOINT + "/addtweet").post(requestBody).build();
                //Request request = new Request.Builder().url(REST_ENDPOINT+"/").get().build();
                okhttp3.Call restCall = okHttpClient.newCall(request);
                restCall.enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        FeedActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(getApplicationContext(), "Error occured!", Toast.LENGTH_LONG).show();
                            }
                        });
                        Log.e("Log from ","onResponse()");
                    }

                    @Override
                    public void onResponse(okhttp3.Call call, Response response) throws IOException {

                        Log.e("Log from ","onResponse()");
                        JSONObject jsonObject = null;
                        StringBuilder jsonString = new StringBuilder(response.body().string().toString());

                        try {
                            jsonObject = new JSONObject(jsonString.toString());
                            JSONArray jsonArray = jsonObject.getJSONArray("tweets");
                            for (int i = 0; i < jsonArray.length(); i++) {
                                JSONObject act = (JSONObject) jsonArray.get(i);
                                String actId = act.getString("id");
                                String actP = act.getString("point");
                                for (final Tweet tweet : tweets) {
                                    if (tweet.idStr.equals(actId) && (actP.contentEquals("0"))) {
                                        FeedActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                tweets.remove(tweet);
                                            }
                                        });
                                    }
                                }
                            }

                            FeedActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    addTweetsToRecycleView(tweets);
                                    Toast.makeText(getApplicationContext(), "Tweets successfully loaded" + tweets.size(), Toast.LENGTH_LONG).show();
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getApplicationContext(), "An error happened!\n" + exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void sendRequest(List<Tweet> tweets) {
        OkHttpClient client = new OkHttpClient();
        MediaType MIMEType = MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create(MIMEType, getTweetsInJson(tweets));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Request request = new Request.Builder().url(REST_ENDPOINT + "/addtweet").post(requestBody).build();
        try {
            Response response = client.newCall(request).execute();
            System.out.println("Response is: " + response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTweetsToRecycleView(List<Tweet> tweets) {
        FixedTweetTimeline fixedTimeLine = new FixedTweetTimeline.Builder().setTweets(tweets).build();
        final TweetTimelineRecyclerViewAdapter adapter = new TweetTimelineRecyclerViewAdapter.Builder(this)
                .setTimeline(fixedTimeLine)
                .setViewStyle(R.style.tw__TweetLightWithActionsStyle)
                .build();
        tweetFeedRV.setAdapter(adapter);
    }

    public boolean tweetIsOK(Tweet tweet) {
        if (tweet.user.name != null && tweet.text != null &&
                tweet.idStr != null && tweet.createdAt != null) {
            String idStr = tweet.idStr.toString().trim();
            String name = tweet.user.name.toString().trim();
            String text = tweet.text.toString().trim();
            String createdAt = tweet.createdAt.toString().trim();
            if (name.length() > 2 && text.length() > 25
                    && idStr.length() >= 18 && createdAt.length() > 10) {
                return true;
            }
        }
        return false;
    }

    public JSONObject getTweetInJSON(Tweet act) {
        StringBuilder sBuilder = new StringBuilder();
        StringBuilder tsBuilder = new StringBuilder();
        String[] text = act.text.split("http");
        tsBuilder.append(text[0].replaceAll("\\r|\\n", " "));
    /*    if (text.length > 1) {
            text[1] = " , http" + text[1].replaceAll("\\r|\\n", " ");
            tsBuilder.append(text[1]);
        }
    */
        JSONObject actObj = new JSONObject();
        try {
            actObj.put("id", act.idStr.trim());
            actObj.put("source", act.user.name.trim());
            actObj.put("text", tsBuilder.toString());
            actObj.put("favcount", act.favoriteCount);
            actObj.put("retcount", act.retweetCount);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return actObj;
    }


    public class TweetGetTaskClass extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {


            MediaType MIMEType = MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = null;
            try {
                requestBody = RequestBody.create(MIMEType, getTweetsInJson(gtweets));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Request request = new Request.Builder().url(REST_ENDPOINT + "/addtweet").post(requestBody).build();
            try {
                Response response = okHttpClient.newCall(request).execute();
                System.out.println("Response is: " + response.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "Message";
        }

        @Override
        protected void onPostExecute(String returnString) {
            super.onPostExecute(returnString);
            Toast.makeText(getApplicationContext(), returnString, Toast.LENGTH_LONG).show();
        }
    }
}
