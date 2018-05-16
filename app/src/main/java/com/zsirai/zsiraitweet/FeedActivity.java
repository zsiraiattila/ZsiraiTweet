package com.zsirai.zsiraitweet;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

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

    private static final String REST_ENDPOINT ="http://10.34.10.18:3000";
    TextView titleFeedTV;
    TweetGetTaskClass taskClass;
    TwitterSession twitterSession;
    TwitterApiClient twitterApiClient;
    StatusesService statusesService;
    RecyclerView tweetFeedRV;
    List<Tweet> gtweets;
    int numTweets;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zsirai.zsiraitweet.R.layout.activity_feed);
        numTweets = 50;
        gtweets = new ArrayList<Tweet>();
        tweetFeedRV = (RecyclerView) findViewById(com.zsirai.zsiraitweet.R.id.tweetFeedRV);
        titleFeedTV = (TextView) findViewById(com.zsirai.zsiraitweet.R.id.titleFeedTV);
        twitterApiClient = TwitterCore.getInstance().getApiClient();
        statusesService = twitterApiClient.getStatusesService();
        twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        titleFeedTV.setText(twitterSession.getUserName() + " 's " + titleFeedTV.getText());
        getandloadTweets(twitterSession,numTweets);
        taskClass.doInBackground();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.zsirai.zsiraitweet.R.menu.timeline_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == com.zsirai.zsiraitweet.R.id.menu_referesh) {
            getandloadTweets(twitterSession,numTweets);
        }
        if (item.getItemId() == com.zsirai.zsiraitweet.R.id.menu_logout && twitterSession != null) {
            TwitterCore.getInstance().getSessionManager().clearActiveSession();
            Toast.makeText(getApplicationContext(), "Logout from Twitter", Toast.LENGTH_LONG).show();
            startActivity(new Intent(FeedActivity.this, LoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private String getTweetsInJSON(List<Tweet> tweets) throws JSONException {
        JSONArray arrJson = new JSONArray();
        for (Tweet actTweet: tweets) {
            JSONObject objJson = new JSONObject();
            objJson.put("id",actTweet.idStr);
            objJson.put("source",actTweet.source);
            objJson.put("text",actTweet.text);
            objJson.put("favcount",actTweet.favoriteCount.toString());
            objJson.put("retcount",Integer.toString(actTweet.retweetCount));
            arrJson.put(objJson);
        }
        return arrJson.toString();
    }

    private void getandloadTweets(TwitterSession session,int numTweets) {
        tweetFeedRV.setLayoutManager(new LinearLayoutManager(this));
        Call<List<Tweet>> call = statusesService.homeTimeline(numTweets, null,
                null, null, null, null, null);
        call.enqueue(new Callback<List<Tweet>>() {

            @Override
            public void success(Result<List<Tweet>> result) {
                ArrayList<Tweet> tweets = new ArrayList<Tweet>();
                for (int i = 0; i < result.data.size(); i++) {
                    if (result.data.get(i).lang.compareTo("en") == 0) {
                        tweets.add(result.data.get(i));
                        gtweets.add(result.data.get(i));
                    }
                }

                addTweetsToRecycleView(tweets);

                Toast.makeText(getApplicationContext(), "Tweets successfully loaded"+tweets.size(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getApplicationContext(), "An error happened!\n"+exception.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    private void sendRequest(List<Tweet> tweets) {
        OkHttpClient client = new OkHttpClient();
        MediaType MIMEType= MediaType.parse("application/json; charset=utf-8");
        RequestBody requestBody = null;
        try {
            requestBody = RequestBody.create (MIMEType,getTweetsInJSON(tweets));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Request request = new Request.Builder().url(REST_ENDPOINT+"/addtweet").post(requestBody).build();
        try {
            Response response = client.newCall(request).execute();
            System.out.println("Response is: "+response.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void addTweetsToRecycleView(List<Tweet> tweets) {

        sendRequest(tweets);
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


    public class TweetGetTaskClass extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {


            OkHttpClient client = new OkHttpClient();
            MediaType MIMEType= MediaType.parse("application/json; charset=utf-8");
            RequestBody requestBody = null;
            try {
                requestBody = RequestBody.create (MIMEType,getTweetsInJSON(gtweets));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Request request = new Request.Builder().url(REST_ENDPOINT+"/addtweet").post(requestBody).build();
            try {
                Response response = client.newCall(request).execute();
                System.out.println("Response is: "+response.toString());
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
