package edu.nyu.scps.asa.jul11;

import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Timer;
import java.util.TimerTask;

// layout class for game - with methods to update level and score views

public class GameActivity extends AppCompatActivity {

    final Handler myHandler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        // update score view
        Timer myTimer = new Timer();
        myTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                UpdateScore();
            }
        }, 0, 1000);
    }

    // these methods constantly update the score View
    final Runnable myRunnable = new Runnable() {
        public void run() {
            GameView gameView = (GameView) findViewById(R.id.gameWindow);
            int score = gameView.getScore();
            TextView scoreView = (TextView) findViewById(R.id.score);
            scoreView.setText(getResources().getString(R.string.score) + " " + score);

            int level = gameView.getGameLevel();
            TextView levelView = (TextView) findViewById(R.id.level);
            levelView.setText(getResources().getString(R.string.level) + " " + level);
        }
    };

    private void UpdateScore() {
        myHandler.post(myRunnable);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

