package com.zsirai.zsiraitweet2;

import android.os.AsyncTask;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
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
    HomePage homePage;
    Long sinceId;
    int numTweets;
    String baseDir;
    String fileName;
    String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_time_line);
        numTweets = 20;

        homePage = new HomePage();
        tweets = new ArrayList<Tweet>();
        myFrameLayout = (FrameLayout) findViewById(R.id.myFrameLayout);
        myRecyclerView = (RecyclerView) findViewById(R.id.myRecyclerView);
        twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        titleTimeLineTV = (TextView) findViewById(R.id.titleTimeLineTV);
        titleTimeLineTV.setText(twitterSession.getUserName() + " 's " + titleTimeLineTV.getText());
        getandloadTweets(twitterSession,numTweets);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.timeline_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_referesh) {
            getandloadTweets(twitterSession,numTweets);
        }
        return super.onOptionsItemSelected(item);
    }

    private void getandloadTweets(TwitterSession session,int numTweets) {
        TwitterApiClient twitterApiClient = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = twitterApiClient.getStatusesService();

        myRecyclerView.setLayoutManager(new LinearLayoutManager(this));

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
                int num = 0;

                num = writeTweetsToCSV(tweets);
                Toast.makeText(getApplicationContext(), "Tweets number is " + result.data.size(), Toast.LENGTH_LONG).show();
           //     Toast.makeText(getApplicationContext(), "Writted out tweet num to csv is " + num, Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getApplicationContext(), "An error occured when tried to get tweets.", Toast.LENGTH_LONG).show();

            }
        });

        //      final SearchTimeline searchTimeline = new SearchTimeline.Builder().query("").maxItemsPerRequest(20).build();
    }

    public int writeTweetsToCSV(List<Tweet> list) {
        baseDir = android.os.Environment.getExternalStorageDirectory().getPath();
        fileName = "tweets25.csv";
        filePath = baseDir + File.separator + fileName;
        File f = new File(filePath);
        int num = 0;

        try {
            FileOutputStream outputStream = new FileOutputStream(f, true);
            OutputStreamWriter osw = new OutputStreamWriter(outputStream);
            System.out.println("============ Tweets Start ================");

            for (int i = 0; i < list.size(); i++) {
                if (tweetIsOK(list.get(i)) == true) {
                    if (isInCSV(list.get(i), f) == false) {
                        writeTweetOut(list.get(i), osw);
                        num++;
                    }
                    writeTweetOut(list.get(i));
                }
            }
            System.out.println("Writed " + num + " tweet to csv!");
            osw.flush();
            osw.close();
            outputStream.flush();
            outputStream.close();
            System.out.println("============ Tweets End ================");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return num;
    }

    private void addTweetsToRecycleView(List<Tweet> tweets) {


        System.out.println("Connect the recyclerview to the tweet list!");
        FixedTweetTimeline fixedTimeLine = new FixedTweetTimeline.Builder().setTweets(tweets).build();
        final TweetTimelineRecyclerViewAdapter adapter = new TweetTimelineRecyclerViewAdapter.Builder(this)
                .setTimeline(fixedTimeLine)
                .setViewStyle(R.style.tw__TweetLightWithActionsStyle)
                .build();

        myRecyclerView.setAdapter(adapter);
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


    public int writeTweetsOut(List<Tweet> list ) {
        int num = 0;
        for(int i=0;i<list.size();i++) {
            Tweet actTweet = list.get(i);
            if (tweetIsOK(actTweet)) {
                writeTweetOut(actTweet);
                num++;
            }
        }
        return num;
    }

    public void writeTweetOut(Tweet act) {
        StringBuilder sBuilder = new StringBuilder();
        StringBuilder tsBuilder = new StringBuilder();
        String[] text = act.text.split("http");
        tsBuilder.append(text[0].replaceAll("\\r|\\n", " "));
        if (text.length > 1) {
            text[1] = " , http" + text[1].replaceAll("\\r|\\n", " ");
            tsBuilder.append(text[1]);
        }

        sBuilder.append(act.user.name.trim() + " , " + act.user.followersCount + " , " + act.idStr.trim() + " , " + tsBuilder.toString() + " , " + act.favoriteCount + " , "
                + act.retweetCount + " , " + act.createdAt.trim() + "\n");
        System.out.println(sBuilder.toString());

    }

    public void writeTweetOut(Tweet act, OutputStreamWriter osw) {
        StringBuilder sBuilder = new StringBuilder();
        StringBuilder tsBuilder = new StringBuilder();
        String[] text = act.text.split("http");
        tsBuilder.append(text[0].replaceAll("\\r|\\n", " "));
        if (text.length > 1) {
            text[1] = " , http" + text[1].replaceAll("\\r|\\n", " ");
            tsBuilder.append(text[1]);
        }


        sBuilder.append(act.user.name.trim() + " , " + act.user.followersCount + " , " + act.idStr.trim() + " , " + tsBuilder.toString() + " , " + act.favoriteCount + " , "
                + act.retweetCount + " , " + act.createdAt.trim() + "\n");
        System.out.println(sBuilder.toString());
        try {
            osw.append(sBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
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

    private boolean isInCSV(Tweet tweet, File f) {

        if (f.exists()) {
            try {
                FileReader fr = new FileReader(f);
                BufferedReader br = new BufferedReader(fr);
                String actLine = new String("");
                String actId = new String("");
                actLine = br.readLine();
                while (actLine != null) {
                    String[] sa = actLine.split(" , ");
                    if (sa.length >= 7) {
                        actId = actLine.split(" , ")[2];
                        actId = actId.trim();
                        if (actId.equals(tweet.idStr) == true) {
                            return true;
                        }
                    }
                    actLine = br.readLine();
                }

            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public class TweetGetTaskClass extends AsyncTask<Integer, Void, String> {

        @Override
        protected String doInBackground(Integer... params) {


            final String[] returnString = {new String("default message")};
            TwitterApiClient client = TwitterCore.getInstance().getApiClient();
            StatusesService statusesService = client.getStatusesService();
            if (params[0] > 200) {
                params[0] = 200;
            }
            Call<List<Tweet>> call = statusesService.homeTimeline(params[0], null, null, null, null, null, null);
            call.enqueue(new Callback<List<Tweet>>() {

                @Override
                public void success(Result<List<Tweet>> result) {
                    writeTweetsToCSV(result.data);
                }

                @Override
                public void failure(TwitterException exception) {
                    Toast.makeText(getApplicationContext(), "An error occured when tried to get tweets.", Toast.LENGTH_LONG).show();
                }
            });

            return returnString[0];
        }

        @Override
        protected void onPostExecute(String returnString) {
            super.onPostExecute(returnString);
            Toast.makeText(getApplicationContext(), returnString, Toast.LENGTH_LONG).show();
        }
    }
}
