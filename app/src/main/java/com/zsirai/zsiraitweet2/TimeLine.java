package com.zsirai.zsiraitweet2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.widget.FrameLayout;
import android.widget.ScrollView;
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
import com.twitter.sdk.android.tweetui.CompactTweetView;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.Timeline;
import com.twitter.sdk.android.tweetui.TimelineResult;
import com.twitter.sdk.android.tweetui.TweetTimelineRecyclerViewAdapter;
import com.twitter.sdk.android.tweetui.TweetUtils;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class TimeLine extends AppCompatActivity {

    TextView titleTimeLineTV;
    TwitterSession twitterSession;
    FrameLayout myFrameLayout;
    RecyclerView myRecyclerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);

        myFrameLayout = (FrameLayout) findViewById(R.id.myFrameLayout);
        myRecyclerView = (RecyclerView) findViewById(R.id.myRecyclerView);
        twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        titleTimeLineTV = (TextView) findViewById(R.id.titleTimeLineTV);
        titleTimeLineTV.setText(twitterSession.getUserName() + " 's " + titleTimeLineTV.getText());
        getandloadTweets(twitterSession);

    }

    private void getandloadTweets(TwitterSession session) {
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();

        myRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        TimeLine timeLine;
        timeLine = new TimeLine();
        timeLine.addTweet();

        final SearchTimeline searchTimeline = new SearchTimeline.Builder().query("").maxItemsPerRequest(20).build();
        final TweetTimelineRecyclerViewAdapter adapter = new TweetTimelineRecyclerViewAdapter.Builder(this)
                .setTimeline(searchTimeline)
                .setViewStyle(R.style.tw__TweetLightWithActionsStyle)
                .build();
        myRecyclerView.setAdapter(adapter);

        getTweets(statusesService);

    }

    private List<Tweet> getTweets(StatusesService statusesService) {
        statusesService.userTimeline(null, null, null, null, null, null, null, null, null);
        Call<List<Tweet>> call = statusesService.homeTimeline(null, null, null, null, null, null, null);
        call.enqueue(new Callback<List<Tweet>>() {

            @Override
            public void success(Result<List<Tweet>> result) {
                List<Tweet> tweets = new ArrayList<Tweet>();
                for (Tweet actTweet : result.data) {

                    tweets.add(actTweet);
                }
                Toast.makeText(getApplicationContext(), "Tweets number is " + result.data.size(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getApplicationContext(), "An error occured when tried to get tweets.", Toast.LENGTH_LONG).show();

            }
        });
    }


    private void addTweet(final Tweet tweet) {
        TweetUtils.loadTweet(tweet.getId(), new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                CompactTweetView compactTweetView = new CompactTweetView(TimeLine.this, result.data);
                compactTweetView.setTweetActionsEnabled(true);
                compactTweetView.setOverScrollMode(1);
                compactTweetView.setVerticalScrollBarEnabled(true);
                compactTweetView.setHorizontalScrollBarEnabled(true);
                myFrameLayout.addView(compactTweetView);
            }

            @Override
            public void failure(TwitterException exception) {

            }
        });
    }
}
