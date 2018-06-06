package com.zsirai.zsiraitweet;

import android.content.Intent;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


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

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import retrofit2.Call;

public class FeedActivity extends AppCompatActivity {

    private static final String REST_ENDPOINT = "http://10.34.10.18:3000";
    private static final String REST_ENDPOINT_HOME = "http://192.168.1.4:3000";
    private static final int SEC_TIMEOUT = 90;
    TextView titleFeedTV;
    TwitterSession twitterSession;
    TwitterApiClient twitterApiClient;
    StatusesService statusesService;
    RecyclerView tweetFeedRV;
    ProgressBar progressBar;
    CopyOnWriteArrayList<Tweet> tweets;
    int numTweets;
    OkHttpClient okHttpClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zsirai.zsiraitweet.R.layout.activity_feed);
        numTweets = 40;
        tweetFeedRV = (RecyclerView) findViewById(com.zsirai.zsiraitweet.R.id.tweetFeedRV);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        titleFeedTV = (TextView) findViewById(com.zsirai.zsiraitweet.R.id.titleFeedTV);
        twitterApiClient = TwitterCore.getInstance().getApiClient();
        statusesService = twitterApiClient.getStatusesService();
        twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        titleFeedTV.setText(twitterSession.getUserName() + " 's " + titleFeedTV.getText());
        okHttpClient = new OkHttpClient.Builder().connectTimeout(SEC_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS)
                .readTimeout(SEC_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS)
                .writeTimeout(SEC_TIMEOUT, java.util.concurrent.TimeUnit.SECONDS)
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

    public boolean tweetIsOK(Tweet tweet) {
        if (tweet.user.name != null && tweet.text != null &&
                tweet.idStr != null && tweet.createdAt != null) {
            String idStr = tweet.idStr.toString().trim();
            String name = tweet.user.name.toString().trim();
            String text = tweet.text.toString().trim();
            if (name.length() > 2 && text.length() > 10
                    && idStr.length() >= 18) {
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
        tweetFeedRV.setVisibility(View.INVISIBLE);
        progressBar.setVisibility(View.VISIBLE);
        tweets = new CopyOnWriteArrayList<Tweet>();
        tweetFeedRV.setLayoutManager(new LinearLayoutManager(this));
        Call<List<Tweet>> call = statusesService.homeTimeline(numTweets, null,
                null, null, null, null, null);
        call.enqueue(new Callback<List<Tweet>>() {

            @Override
            public void success(Result<List<Tweet>> result) {
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
                okhttp3.Call restCall = okHttpClient.newCall(request);
                restCall.enqueue(new okhttp3.Callback() {
                    @Override
                    public void onFailure(okhttp3.Call call, IOException e) {
                        FeedActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                progressBar.setVisibility(View.INVISIBLE);
                                tweetFeedRV.setVisibility(View.VISIBLE);
                                Toast.makeText(getApplicationContext(), "Error happened while try to filter!", Toast.LENGTH_LONG).show();
                                addTweetsToRecycleView(tweets);
                                Toast.makeText(getApplicationContext(),tweets.size()+" tweets have displayed!",Toast.LENGTH_LONG).show();
                            }
                        });
                    }

                    @Override
                    public void onResponse(okhttp3.Call call, Response response) throws IOException {

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
                                    Toast.makeText(getApplicationContext(), tweets.size() + " tweets had displayed", Toast.LENGTH_LONG).show();
                                }
                            });

                            FeedActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    progressBar.setVisibility(View.INVISIBLE);
                                    tweetFeedRV.setVisibility(View.VISIBLE);

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
                addTweetsToRecycleView(tweets);
            }
        });

    }

    private void addTweetsToRecycleView(List<Tweet> tweets) {
        FixedTweetTimeline fixedTimeLine = new FixedTweetTimeline.Builder().setTweets(tweets).build();
        final TweetTimelineRecyclerViewAdapter adapter = new TweetTimelineRecyclerViewAdapter.Builder(this)
                .setTimeline(fixedTimeLine)
                .setViewStyle(R.style.tw__TweetLightWithActionsStyle)
                .build();
        tweetFeedRV.setAdapter(adapter);
    }


}