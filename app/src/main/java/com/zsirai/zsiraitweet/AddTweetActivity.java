package com.zsirai.zsiraitweet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.models.Tweet;
import com.twitter.sdk.android.core.services.StatusesService;
import com.twitter.sdk.android.tweetui.CompactTweetView;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;

public class AddTweetActivity extends AppCompatActivity {

    TwitterSession twitterSession;
    EditText tweetET;
    TextView tvCharacters;
    Button tweetButton;
    FrameLayout tweetView_Container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zsirai.zsiraitweet.R.layout.activity_add_tweet);
        twitterSession = TwitterCore.getInstance().getSessionManager().getActiveSession();
        if (twitterSession == null) {
            Intent intent = new Intent(AddTweetActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
        setupUI();
    }

    private void setupUI() {
        tweetET = (EditText) findViewById(com.zsirai.zsiraitweet.R.id.tweetTextET);
        tvCharacters = (TextView) findViewById(com.zsirai.zsiraitweet.R.id.charNumTV);
        tweetButton = (Button) findViewById(com.zsirai.zsiraitweet.R.id.tweetButton);
        tweetView_Container = (FrameLayout) findViewById(com.zsirai.zsiraitweet.R.id.tweetView_Container);

        tweetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postTweet(tweetET.getText().toString());
            }
        });
        tweetET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (charactersCountOk(s.toString())) {
                    tweetButton.setEnabled(true);
                } else {
                    tweetButton.setEnabled(false);
                }
            }
            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private boolean charactersCountOk(String text) {
        // in this part, we count the caracters of the text ( without the hyperlinks) and add the
        // number of tweets * 23 ( this is something standard from twitter, that every URL "costs" 23 char.
        // Maximum character length is 140.

        int numberUrls = 0;
        int lengthAllUrls = 0;
        String regex = "\\(?\\b(https://|http://|www[.])[-A-Za-z0-9+&@#/%?=~_()|!:,.;]*[~A-Za-z0-9+&@#/%=~_()|]";
        Pattern urlPattern = Pattern.compile(regex);
        Matcher urlMatcher = urlPattern.matcher(text);
        while (urlMatcher.find()) {
            lengthAllUrls += urlMatcher.group().length();
            numberUrls++;
        }
        int tweetLength = text.length() - lengthAllUrls + numberUrls * 23;
        tvCharacters.setText(Integer.toString(140 - tweetLength));
        if (tweetLength > 0 && tweetLength <= 140) {
            return true;
        } else {
            return false;
        }
    }

    private void postTweet(String text) {
        StatusesService statusesService = TwitterCore.getInstance().getApiClient().getStatusesService();
        Call<Tweet> updateCall = statusesService.update(text, null, false, null,
                null, null, false, false, null);
        updateCall.enqueue(new Callback<Tweet>() {
            @Override
            public void success(Result<Tweet> result) {
                Toast.makeText(getApplicationContext(), "Tweet posted!", Toast.LENGTH_LONG).show();
                displayTweet(result.data);
            }
            @Override
            public void failure(TwitterException exception) {
                Toast.makeText(getApplicationContext(),
                        "Error happened when tried to post tweet!"+exception.getMessage()
                        , Toast.LENGTH_LONG).show();
            }
        });
    }

    private void displayTweet(Tweet tweet) {
        CompactTweetView compactTweetView = new CompactTweetView(AddTweetActivity.this, tweet);
        compactTweetView.setTweetActionsEnabled(true);
        tweetView_Container.addView(compactTweetView);
    }
}
