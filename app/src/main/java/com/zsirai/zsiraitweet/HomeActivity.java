package com.zsirai.zsiraitweet;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

public class HomeActivity extends AppCompatActivity {

    TwitterSession session;
    Button tweetPostButton;
    Button tweetTimeLineButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.zsirai.zsiraitweet.R.layout.activity_home);
        tweetPostButton = (Button) findViewById(com.zsirai.zsiraitweet.R.id.tweetPostButton);
        tweetTimeLineButton = (Button) findViewById(com.zsirai.zsiraitweet.R.id.FeedButton);
        session = TwitterCore.getInstance().getSessionManager().getActiveSession();

        if (session == null) {
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }

        // go to the AddTweetActivity activity If We pressed the button.
        tweetPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent tweetIntent = new Intent(HomeActivity.this, AddTweetActivity.class);
                startActivity(tweetIntent);
            }
        });
        tweetTimeLineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent timeLineIntent = new Intent(HomeActivity.this, FeedActivity.class);
                startActivity(timeLineIntent);
            }
        });
    }

    // Create options menu.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(com.zsirai.zsiraitweet.R.menu.home_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // When select the LogOut Button you clear or delete the session, It means you logged out and go to the login activity.
        if (item.getItemId() == com.zsirai.zsiraitweet.R.id.menu_logout && session != null) {
            TwitterCore.getInstance().getSessionManager().clearActiveSession();
            Toast.makeText(getApplicationContext(), "Logout from Twitter", Toast.LENGTH_LONG).show();
            startActivity(new Intent(HomeActivity.this, LoginActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

}
