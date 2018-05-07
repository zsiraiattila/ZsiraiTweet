package com.zsirai.zsiraitweet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.twitter.sdk.android.core.Callback;
import com.twitter.sdk.android.core.Result;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterException;
import com.twitter.sdk.android.core.TwitterSession;
import com.twitter.sdk.android.core.identity.TwitterLoginButton;
import com.twitter.sdk.android.tweetcomposer.TweetComposer;
import com.twitter.sdk.android.tweetui.TweetUi;

public class LoginActivity extends AppCompatActivity {

    TwitterLoginButton twitterLoginButton;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        twitterLoginButton.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Twitter.initialize(this);
        setContentView(com.zsirai.zsiraitweet.R.layout.activity_login);

        twitterLoginButton = (TwitterLoginButton) findViewById(com.zsirai.zsiraitweet.R.id.login_button);
        twitterLoginButton.setCallback(new Callback<TwitterSession>() {
            @Override
            public void success(Result<TwitterSession> result) {
                Toast.makeText(getApplicationContext(), "Successfully LogIn", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(intent);

            }

            @Override
            public void failure(TwitterException exception) {

            }
        });


        TwitterCore.getInstance();
        TweetUi.getInstance();
        TweetComposer.getInstance();

    }

}
