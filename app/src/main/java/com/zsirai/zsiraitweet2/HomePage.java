package com.zsirai.zsiraitweet2;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import retrofit2.Call;

public class HomePage extends AppCompatActivity {

    TextView titleTV;
    TwitterSession session;
    List<Tweet> myTweets;
    Button tweetPostButton;
    Button tweetTimeLineButton;
    Button getTweetsToCSVButton;
    TimeLineActivity timeLineActivity;
    TweetGetTaskClass tweetGetTaskClass;
    Long sinceId;
    int numTweets;
    String baseDir;
    String fileName;
    String filePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        tweetPostButton = (Button) findViewById(R.id.tweetUpdateButton);
        tweetTimeLineButton = (Button) findViewById(R.id.tweetTimeLineButton);
        getTweetsToCSVButton = (Button) findViewById(R.id.getTCSVButton);
        timeLineActivity = new TimeLineActivity();
        session = TwitterCore.getInstance().getSessionManager().getActiveSession();
        myTweets = new ArrayList<Tweet>();
        tweetGetTaskClass = new TweetGetTaskClass();



        if (session == null) {
            startActivity(new Intent(HomePage.this, LoginActivity.class));
            finish();
        }

        // go to the AddTweet activity If We pressed the button.
        tweetPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomePage.this, AddTweet.class));
            }
        });
        tweetTimeLineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent timeLineIntent = new Intent(HomePage.this, TimeLineActivity.class);
                startActivity(timeLineIntent);

            }
        });
        getTweetsToCSVButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getTweetsToCSV();

            }
        });

    }

   public void getTweetsToCSV() {
        tweetGetTaskClass.execute(new Integer[]{100, 70});
    }


    private void getAndWriteTweets() {

        TwitterApiClient client = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = client.getStatusesService();
        Call<List<Tweet>> call = statusesService.homeTimeline(15, null, null, null, null, null, null);
        call.enqueue(new Callback<List<Tweet>>() {

            @Override
            public void success(Result<List<Tweet>> result) {
                int num = writeTweetsToCSV(result.data);
                Toast.makeText(getApplicationContext(), "Tweets number is " + num, Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getApplicationContext(), "An error occured when tried to get tweets.", Toast.LENGTH_LONG).show();
            }
        });
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
                    writeTweetOut(list.get(i),osw);
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

    // Create options menu.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.homepage_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // When select the LogOut Button you clear or delete the session, It means you logged out
        // and go to the login activity.
        if (item.getItemId() == R.id.menu_logout && session != null) {
            TwitterCore.getInstance().getSessionManager().clearActiveSession();
            Toast.makeText(getApplicationContext(), "Logout from Twitter", Toast.LENGTH_LONG).show();
            startActivity(new Intent(HomePage.this, LoginActivity.class));
            finish();

        }
        return super.onOptionsItemSelected(item);
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
            result.append(iterator.next().text + "-");
        }
        return result.toString();
    }


    private String displayUrls(List<UrlEntity> urls) {
        StringBuilder result = new StringBuilder();
        Iterator<UrlEntity> iterator = urls.iterator();
        while (iterator.hasNext()) {
            result.append(iterator.next().expandedUrl + "-");
        }
        return result.toString();
    }

    public class TweetGetTaskClass extends AsyncTask<Integer, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Integer... params) {


            final String returnString = new String("default message");
            TwitterApiClient client = TwitterCore.getInstance().getApiClient();
            StatusesService statusesService = client.getStatusesService();
            if (params[0] > 200) {
                params[0] = 200;
            }
            Call<List<Tweet>> call = statusesService.homeTimeline(params[0], null, null, null, null, null, null);
            call.enqueue(new Callback<List<Tweet>>() {

                @Override
                public void success(Result<List<Tweet>> result) {
                   Toast.makeText(getApplicationContext(),writeTweetsToCSV(result.data)+" tweets are writed out to csv file!",Toast.LENGTH_LONG).show();
                }

                @Override
                public void failure(TwitterException exception) {
                    Toast.makeText(getApplicationContext(), "An error occured when tried to get tweets.", Toast.LENGTH_LONG).show();
                }
            });

            return true;
        }

        @Override
        protected void onPostExecute(Boolean returnValue) {
            super.onPostExecute(returnValue);
            Toast.makeText(getApplicationContext(), returnValue.toString(), Toast.LENGTH_LONG).show();
        }
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

}
