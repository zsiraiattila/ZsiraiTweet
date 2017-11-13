package com.zsirai.zsiraitweet2;

import android.app.usage.ExternalStorageStats;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ThreadFactory;
import java.util.logging.FileHandler;

import retrofit2.Call;

import static android.R.attr.data;

public class HomePage extends AppCompatActivity {

    TextView titleTV;
    TwitterSession session;
    List<Tweet> myTweets;
    Button tweetPostButton;
    Button tweetTimeLineButton;
    Button getTweetsToCSVButton;
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
        session = TwitterCore.getInstance().getSessionManager().getActiveSession();
        myTweets = new ArrayList<Tweet>();
        final TweetGetTaskClass tweetGetTaskClass = new TweetGetTaskClass();


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
                tweetGetTaskClass.execute(new Integer[]{100, 70});
            }
        });

    }

    private void writeSomeThingToSDCard() {


        String filePath = new String(Environment.getExternalStorageDirectory().getAbsolutePath() + "/savingForder/state.txt");
        System.out.println("File path is: " + filePath);
        try {
            File file = new File(filePath);
            FileOutputStream outputStream = new FileOutputStream(file, true);
            String text1 = new String("My text to save is here.\n");
            String text2 = new String("Seccond sentence here.\n");
            outputStream.write(text1.getBytes());
            outputStream.write(text2.getBytes());

            outputStream.flush();
            outputStream.close();

        /*    BufferedWriter bufferedWriter = new BufferedWriter(fileWriter);
            bufferedWriter.append("This is my test text \n Second line \n Thirth line... ");
            Toast.makeText(getApplicationContext(),"The file successfully created and writed: "+filePath.toString(),Toast.LENGTH_LONG).show();
            bufferedWriter.flush();
            bufferedWriter.close(); */

        } catch (IOException e) {
            Log.w("ExternalStorage", "Error writing " + fileName, e);
        }
    }

    private void getAndWriteTweets() {

        TwitterApiClient client = TwitterCore.getInstance().getApiClient();
        StatusesService statusesService = client.getStatusesService();
        Call<List<Tweet>> call = statusesService.homeTimeline(15, null, null, null, null, null, null);
        call.enqueue(new Callback<List<Tweet>>() {

            @Override
            public void success(Result<List<Tweet>> result) {
                writeTweetsToCSV(result.data);
                Toast.makeText(getApplicationContext(), "Tweets number is " + result.data.size(), Toast.LENGTH_LONG).show();
            }

            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getApplicationContext(), "An error occured when tried to get tweets.", Toast.LENGTH_LONG).show();
            }
        });
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

    public void writeTweetsToCSV(List<Tweet> list) {
        baseDir = android.os.Environment.getExternalStorageDirectory().getPath();
        fileName = "tweets7.csv";
        filePath = baseDir + File.separator + fileName;
        File f = new File(filePath);
        int num = 0;

        try {
            FileOutputStream outputStream = new FileOutputStream(f, true);
            OutputStreamWriter osw = new OutputStreamWriter(outputStream);
            System.out.println("============ Tweets Start ================");
            for (int i = 0; i < list.size(); i++) {
                if (tweetIsOK(list.get(i)) && isInCSV(list.get(i), f) == false) {
                    if (list.get(i).lang.equals("en")) {
                        writeTweetOut(list.get(i), osw);
                        num++;
                    }
                }
            }
            osw.flush();
            osw.close();
            outputStream.flush();
            outputStream.close();
            System.out.println("============ Tweets End ================");
            Toast.makeText(getApplicationContext(), "Successfully went on the list. The writed out num is: " + num, Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void writeTweetOut(Tweet act, OutputStreamWriter osw) {
        StringBuilder sBuilder = new StringBuilder();
        StringBuilder tsBuilder = new StringBuilder();
        String[] text = act.text.split("http");
        if (text.length > 1) {
            tsBuilder.append(text[0] + ", http" + text[1]);
        } else {
            tsBuilder.append(text[0]);
        }

        sBuilder.append(act.user.name + " , " + act.user.followersCount + " , " + act.idStr + " , " + tsBuilder.toString() + " , " + act.favoriteCount + " , "
                + act.retweetCount + " , " + act.createdAt + "\n");
//        sBuilder.append(act.user.name + " , " + act.user.followersCount + " , " + act.idStr + " , " + act.source.toString() + " , " + text[0] + " , " + text[1] + " , "
//                + act.favorited).append(" , " + act.favoriteCount + " , " + displayUrls(act.entities.urls)
//                + " , " + displayHashTags(act.entities.hashtags) + " , " + displaySimbols(act.entities.symbols) + "\n");
        System.out.println(sBuilder.toString());
        try {
            osw.append(sBuilder.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean tweetIsOK(Tweet tweet) {
        if (tweet.user.name.length() > 0 && tweet.text.length() > 0
                && tweet.idStr.length() >= 18 && tweet.createdAt.length() > 10) {
            return true;
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
                    String[] sa = actLine.split(",");
                    if (sa.length >= 10) {
                        actId = actLine.split(",")[2];
                        if (actId.equals(tweet.idStr)) {
                            return true;
                        }
                    }
                    actLine = br.readLine();
                }

                return false;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return false;
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


    private class TweetGetTaskClass extends AsyncTask<Integer, Void, String> {

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
