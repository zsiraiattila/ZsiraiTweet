package com.zsirai.zsiraitweet2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;
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
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TweetTimelineRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;

public class TimeLineActivity extends AppCompatActivity {

    TextView titleTimeLineTV;
    TwitterSession twitterSession;
    FrameLayout myFrameLayout;
    RecyclerView myRecyclerView;
    List<Tweet> tweets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);

        tweets = new ArrayList<Tweet>();
        myFrameLayout = (FrameLayout) findViewById(R.id.myFrameLayout);
        myRecyclerView = (RecyclerView) findViewById(R.id.myRecyclerView);
        twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        titleTimeLineTV = (TextView) findViewById(R.id.titleTimeLineTV);
        titleTimeLineTV.setText(twitterSession.getUserName() + " 's " + titleTimeLineTV.getText());
        getandloadTweets(twitterSession);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_referesh) {
            getandloadTweets(twitterSession);
        }
        return super.onOptionsItemSelected(item);
    }

    private void getandloadTweets(TwitterSession session) {
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();

        myRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        statusesService.userTimeline(null, null, null, null, null, null, null, null, null);
        Call<List<Tweet>> call = statusesService.homeTimeline(null, null, null, null, null, null, null);

        call.enqueue(new Callback<List<Tweet>>() {

            @Override
            public void success(Result<List<Tweet>> result) {
                addTweetsToRecycleView(result.data);
                Toast.makeText(getApplicationContext(), "Tweets number is " + result.data.size(), Toast.LENGTH_LONG).show();
                tweets = result.data;
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getApplicationContext(), "An error occured when tried to get tweets.", Toast.LENGTH_LONG).show();

            }
        });


/*        TwitterListTimeline twitterListTimeline = new TwitterListTimeline.Builder().includeRetweets(true).maxItemsPerRequest(20).build();
        twitterListTimeline.next(tweets.get(19).getId(), new Callback<TimelineResult<Tweet>>() {
            @Override
            public void success(Result<TimelineResult<Tweet>> result) {

            }

            @Override
            public void failure(TwitterException exception) {

            }
        });

        TwitterListTimeline twitterListTimeLine = new TwitterListTimeline.Builder().
                id(10L).includeRetweets(true)
                .maxItemsPerRequest(20)
                .slugWithOwnerId("myslug",session.getUserId())
                .build();

*/
        final SearchTimeline searchTimeline = new SearchTimeline.Builder().query("").maxItemsPerRequest(20).build();
    }

    private void addTweetsToRecycleView(List<Tweet> tweets) {



        FixedTweetTimeline fixedTimeLine = new FixedTweetTimeline.Builder().setTweets(tweets).build();
        final TweetTimelineRecyclerViewAdapter adapter = new TweetTimelineRecyclerViewAdapter.Builder(this)
                .setTimeline(fixedTimeLine)
                .setViewStyle(R.style.tw__TweetLightWithActionsStyle)
                .build();
        myRecyclerView.setAdapter(adapter);
    }




  /*  private void addTweet(final Tweet tweet) {
        TweetUtils.loadTweet(tweet.getId(), new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                CompactTweetView compactTweetView = new CompactTweetView(TimeLineActivity.this, result.data);
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
    */

}
