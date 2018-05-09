package com.zsirai.zsiraitweet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
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

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class FeedActivity extends AppCompatActivity {

    TextView titleFeedTV;
    TwitterSession twitterSession;
    RecyclerView tweetFeedRV;
    List<Tweet> tweets;
    Long sinceId;
    int numTweets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zsirai.zsiraitweet.R.layout.activity_feed);
        numTweets = 50;

        tweets = new ArrayList<Tweet>();
        tweetFeedRV = (RecyclerView) findViewById(com.zsirai.zsiraitweet.R.id.tweetFeedRV);
        twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        titleFeedTV = (TextView) findViewById(com.zsirai.zsiraitweet.R.id.titleFeedTV);
        titleFeedTV.setText(twitterSession.getUserName() + " 's " + titleFeedTV.getText());
        getandloadTweets(twitterSession,numTweets);
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

    private void getandloadTweets(TwitterSession session,int numTweets) {
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();

        tweetFeedRV.setLayoutManager(new LinearLayoutManager(this));

        statusesService.userTimeline(null, null, null, null, null, null, null, null, null);
        Call<List<Tweet>> call = statusesService.homeTimeline(numTweets, null, null, null, null, null, null);

        call.enqueue(new Callback<List<Tweet>>() {

            @Override
            public void success(Result<List<Tweet>> result) {
                ArrayList<Tweet> tweets = new ArrayList<Tweet>();
                for (int i = 0; i < result.data.size(); i++) {
                    if (result.data.get(i).lang.compareTo("en") == 0) {
                        tweets.add(result.data.get(i));
                    }
                }
                sinceId = tweets.get(tweets.size() - 1).getId();
                addTweetsToRecycleView(tweets);

                Toast.makeText(getApplicationContext(), "Tweets number is " + result.data.size(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getApplicationContext(), "An error happened when tried to get tweets.", Toast.LENGTH_LONG).show();
            }
        });

    }

    private void addTweetsToRecycleView(List<Tweet> tweets) {

        FixedTweetTimeline fixedTimeLine = new FixedTweetTimeline.Builder().setTweets(tweets).build();
        final TweetTimelineRecyclerViewAdapter adapter = new TweetTimelineRecyclerViewAdapter.Builder(this)
                .setTimeline(fixedTimeLine)
                .setViewStyle(com.zsirai.zsiraitweet.R.style.tw__TweetLightWithActionsStyle)
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


}
