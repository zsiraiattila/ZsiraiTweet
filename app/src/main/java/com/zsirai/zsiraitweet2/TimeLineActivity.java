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
import com.twitter.sdk.android.core.models.HashtagEntity;
import com.twitter.sdk.android.core.models.SymbolEntity;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.models.UrlEntity;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.BasicTimelineFilter;
import com.twitter.sdk.android.tweetui.FilterValues;
import com.twitter.sdk.android.tweetui.FixedTweetTimeline;
import com.twitter.sdk.android.tweetui.SearchTimeline;
import com.twitter.sdk.android.tweetui.TimelineFilter;
import com.twitter.sdk.android.tweetui.TweetTimelineRecyclerViewAdapter;
import com.monkeylearn.MonkeyLearn;

import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Iterator;
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
        getMenuInflater().inflate(R.menu.timeline_menu, menu);
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
        Call<List<Tweet>> call = statusesService.homeTimeline(50, null, null, null, null, null, null);

        call.enqueue(new Callback<List<Tweet>>() {

            @Override
            public void success(Result<List<Tweet>> result) {
                addTweetsToRecycleView(result.data);
                StringBuilder sBuilder = new StringBuilder();
                StringBuilder tsBuilder = new StringBuilder();

                int num = 0;

                try {
                    for (int i = 0; i < result.data.size(); i++) {
                        Tweet act = result.data.get(i);
                        sBuilder = new StringBuilder();
                        tsBuilder = new StringBuilder();
                        String[] text = act.text.split("https");
                        if (text.length > 1) {
                            tsBuilder = new StringBuilder(text[0] + ", https" + text[1]);
                        } else {
                            tsBuilder = new StringBuilder(text[0]);
                        }

                        sBuilder.append(act.user.name + " , " + act.user.followersCount + " , "
                                + tsBuilder.toString() + act.idStr + " , " + act.favoriteCount
                                + " , " + act.createdAt + "\n");
                        System.out.println(sBuilder.toString());
                        num++;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    System.out.println(e.getMessage());
                }
                Toast.makeText(getApplicationContext(), "Tweets number is " + result.data.size(), Toast.LENGTH_LONG).show();
                Toast.makeText(getApplicationContext(), "Writted out tweet num is " + num, Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getApplicationContext(), "An error occured when tried to get tweets.", Toast.LENGTH_LONG).show();

            }
        });

        //      final SearchTimeline searchTimeline = new SearchTimeline.Builder().query("").maxItemsPerRequest(20).build();
    }

    private void addTweetsToRecycleView(List<Tweet> tweets) {

            /*
            sBuilder.append(act.user.name + "\t" + act.user.followersCount + "\t" + act.idStr + "\t" + act.source.toString() + "\t" + text[0] + "\t" + text[1] + "\t"
                    + act.favorited + "\t" + act.favoriteCount + "\t" + displayUrls(act.entities.urls)
                    + "\t" + displayHashTags(act.entities.hashtags) + "\t" + displaySimbols(act.entities.symbols)+"\n");
            System.out.println(sBuilder.toString());
            sBuilder = new StringBuilder("");
        } */
        System.out.println("Connect the recyclerview to the tweet list!");

        FixedTweetTimeline fixedTimeLine = new FixedTweetTimeline.Builder().setTweets(tweets).build();
        final TweetTimelineRecyclerViewAdapter adapter = new TweetTimelineRecyclerViewAdapter.Builder(this)
                .setTimeline(fixedTimeLine)
                .setViewStyle(R.style.tw__TweetLightWithActionsStyle)
                .build();

        myRecyclerView.setAdapter(adapter);
    }

    public void writeTweetOut(Tweet act) {
        StringBuilder sBuilder = new StringBuilder();
        String[] text = act.text.split("https");
        text[1] = "https" + text[1];
        sBuilder.append(act.user.name + " , " + act.user.followersCount + " , "
                + text[0] + " , " + text[1] + " , " + act.idStr + " , " + act.favoriteCount
                + " , " + act.createdAt + "\n");
//        sBuilder.append(act.user.name + " , " + act.user.followersCount + " , " + act.idStr + " , " + act.source.toString() + " , " + text[0] + " , " + text[1] + " , "
//                + act.favorited).append(" , " + act.favoriteCount + " , " + displayUrls(act.entities.urls)
//                + " , " + displayHashTags(act.entities.hashtags) + " , " + displaySimbols(act.entities.symbols) + "\n");
        System.out.println(sBuilder.toString());
    }

    private String displaySimbols(List<SymbolEntity> symbols) {
        StringBuilder result = new StringBuilder();
        Iterator<SymbolEntity> iterator = symbols.iterator();
        while (iterator.hasNext()) {
            result.append(iterator.next().text + ", ");
        }
        return result.toString();
    }

    private String displayHashTags(List<HashtagEntity> hashtags) {
        StringBuilder result = new StringBuilder();
        Iterator<HashtagEntity> iterator = hashtags.iterator();
        while (iterator.hasNext()) {
            result.append(iterator.next().text + ", ");
        }
        return result.toString();
    }

    private String displayUrls(List<UrlEntity> urls) {
        StringBuilder result = new StringBuilder();
        Iterator<UrlEntity> iterator = urls.iterator();
        while (iterator.hasNext()) {
            result.append(iterator.next().expandedUrl + ", ");
        }
        return result.toString();
    }

    public Tweet copyTweet(Tweet act) {
        return new Tweet(act.coordinates, act.createdAt, act.currentUserRetweet, act.entities,
                act.extendedEntities, act.favoriteCount, act.favorited,
                act.filterLevel, act.id, act.idStr, act.inReplyToScreenName,
                act.inReplyToStatusId, act.inReplyToStatusIdStr, act.inReplyToUserId,
                act.inReplyToUserIdStr, act.lang, act.place, act.possiblySensitive,
                act.scopes, act.quotedStatusId, act.quotedStatusIdStr, act.quotedStatus,
                act.retweetCount, act.retweeted, act.retweetedStatus, act.source,
                act.text, act.displayTextRange, act.truncated, act.user,
                act.withheldCopyright, act.withheldInCountries,
                act.withheldScope, act.card);
    }

}
