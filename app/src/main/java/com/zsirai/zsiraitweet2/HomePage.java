package com.zsirai.zsiraitweet2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.twitter.sdk.android.core.TwitterCore;
import com.twitter.sdk.android.core.TwitterSession;

public class HomePage extends AppCompatActivity {

    TextView titleTV;
    TwitterSession session;
    Button tweetPostButton;
    Button tweetTimeLineButton;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_page);
        tweetPostButton = (Button)findViewById(R.id.tweetUpdateButton);
        tweetTimeLineButton = (Button)findViewById(R.id.tweetTimeLineButton);
        session = TwitterCore.getInstance().getSessionManager().getActiveSession();

        if (session == null) {
            startActivity(new Intent(HomePage.this,LoginActivity.class));
            finish();
        }

        // go to the AddTweet activity If We pressed the button.
        tweetPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomePage.this,AddTweet.class));
            }
        });
        tweetTimeLineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent timeLineIntent = new Intent(HomePage.this,TimeLineActivity.class);
                startActivity(timeLineIntent);

            }
        });

    }

    // Create options menu.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.homepage_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }



    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // When select the LogOut Button you clear or delete the session, It means you logged out
        // and go to the login activity.
        if (item.getItemId() == R.id.menu_logout && session != null) {
            TwitterCore.getInstance().getSessionManager().clearActiveSession();
            Toast.makeText(getApplicationContext(),"Logout from Twitter",Toast.LENGTH_LONG).show();
            startActivity(new Intent(HomePage.this,LoginActivity.class));
            finish();

        }
        return super.onOptionsItemSelected(item);
    }


}
