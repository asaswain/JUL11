package edu.nyu.scps.asa.jul11;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.os.Build;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import java.util.Iterator;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

// class for the game screen

/**
 * Features I didn't have time to implement:
 *
 * A timer to prevent how long you can use the magnetron continuously
 * Explosions when the rocks are destroyed or hit the ground
 * Explosions when a city is destroyed
 */

/**
 * Bugs I discovered during my development:
 *
 * - All the rocks were going to the same target city because I wasn't making a copy of the center and target variables in my Rock getter and setter methods
 * - I was getting ConcurrentModificationException errors when I tried to remove items from the rockList ArrayList using an iterator object,
 * so I had to change it to a CopyOnWriteArrayList object and use the RemoveAll method instead
 */

public class GameView extends View {
    private CopyOnWriteArrayList<RockView> rockList = new CopyOnWriteArrayList<RockView>();
    private PointF touchPoint = new PointF(); //location of touch in dp
    private final float density;              //pixels per dp
    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint touchPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    private float horizon;
    private final int cityCount = 3;
    private final int citySize = 20;
    private PointF[] cityLocations = new PointF[cityCount];
    private String cityNames[] = new String[]{
        getResources().getString(R.string.boston),
        getResources().getString(R.string.newyork),
        getResources().getString(R.string.dc)
    };

    private int score;
    private int scoreTimer;
    private final int scoreInterval = 10; // update score every second

    private int rockTimer;
    private final int rockInterval = 20; // add new rock every 20 seconds - gameLevel (which increases every 10 seconds)

    private int gameLevel;
    private boolean gameOver;
    private boolean gameWon;

    private int currentSpeed;
    private int speedTimer;
    private final float speedFactor = 1.5f;
    private final int speedInterval = 100; // increase rock speed by 150% every 10 seconds

    private boolean touchActive;
    private final float touchWidth = 64.0f;

    private final float rockRadius = 16.0f;



    public GameView(Context context) {
        super(context);

        // Java doesn't like it if I define the final dentist varibale in initializeView
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        density = displayMetrics.density;

        initializeView();
    }

    // used when constructing object from layout activity_main.xml file
    public GameView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);

        // Java doesn't like it if I define the final dentist varibale in initializeView
        Resources resources = getResources();
        DisplayMetrics displayMetrics = resources.getDisplayMetrics();
        density = displayMetrics.density;

        initializeView();
    }


    // initialize game screen
    private void initializeView() {
        setBackgroundColor(Color.CYAN);
        paint.setStrokeWidth(0f);   //hairline

        touchPaint.setColor(Color.RED);
        touchPaint.setStyle(Paint.Style.STROKE);
        //touchPaint.setAlpha(50);
        touchPaint.setStrokeWidth(10f);   //hairline

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            //Turn off hardware acceleration,
            //because it would not call onDraw as often as it should.
            setLayerType(LAYER_TYPE_SOFTWARE, paint);
        }

        setOnTouchListener(new View.OnTouchListener() {

            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {

                switch (motionEvent.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        // create new point object

                        touchActive = true;
                        RockView.setRepulsorActive(touchActive);
                        //Convert pixels to dp by dividing by density.
                        touchPoint.set(motionEvent.getX() / density, motionEvent.getY() / density);

                        return true;

                    case MotionEvent.ACTION_UP:
                        touchActive = false;
                        RockView.setRepulsorActive(touchActive);
                        invalidate();

                        return false;    //do nothing else

                    case MotionEvent.ACTION_MOVE:
                        touchActive = true;
                        RockView.setRepulsorActive(touchActive);
                        //Convert pixels to dp by dividing by density.
                        touchPoint.set(motionEvent.getX() / density, motionEvent.getY() / density);

                        return true;    //do nothing else

                    default:
                        return true;
                }
            }
        });

        initializeGame();

        Runnable runnable = new Runnable() {

            //This method is run by rockList thread that is not the UI thread.
            @Override
            public void run() {
                for (;;) {  //infinite loop

                    // increment score, speed and create rock counters
                    incrementCounters();

                    if (gameLevel > 24) {
                        gameWon = true;
                    } else {

                        // create rocks
                        // don't try to create rocks until we're drawn the screen
                        if (getWidth() > 0) {
                            if (rockTimer >= rockInterval - gameLevel) {
                                createRock();
                                rockTimer = 0;
                            }
                        }

                        // move rocks
                        for (RockView rock : rockList) {
                            rock.dragTowards(touchPoint, touchWidth);
                        }

                        // check for collision with city -> end of game

                        for (RockView rock : rockList) {
                            if (isRockTouchingCity(rock)) {
                                gameOver = true;
                            }
                        }

                        // check for collision with ground -> delete rock

                        CopyOnWriteArrayList<RockView> tmp1 = new CopyOnWriteArrayList<RockView>();

                        Iterator<RockView> rockIterator = rockList.iterator();
                        while (rockIterator.hasNext()) {
                            RockView rock = rockIterator.next();
                            if (isRockTouchingGround(rock)) {
                                tmp1.add(rock);
                            }
                        }

                        rockList.removeAll(tmp1);

                        // check for collision with other rocks -> delete both rocks

                        CopyOnWriteArrayList<RockView> tmp2 = new CopyOnWriteArrayList<RockView>();

                        Iterator<RockView> rockIterator1 = rockList.iterator();
                        while (rockIterator1.hasNext()) {
                            RockView rock1 = rockIterator1.next();

                            Iterator<RockView> rockIterator2 = rockList.iterator();
                            while (rockIterator2.hasNext()) {
                                RockView rock2 = rockIterator2.next();
                                if (rock1 != rock2) {
                                    if (isRockTouchingRock(rock1, rock2)) {
                                        tmp2.add(rock1);
                                        // get 100 points for each rock you destroy
                                        score += 100;
                                    }
                                }
                            }
                        }

                        // remove rocks marked for destruction
                        rockList.removeAll(tmp2);

                    }

                    //Call onDraw.
                    postInvalidate();

                    if (gameOver || gameWon) {
                        // pause for 7 seconds to display toast
                        try {
                            Thread.sleep(7000L);   //milliseconds
                        } catch (InterruptedException interruptedException) {
                        }

                        gameOver = false;
                        gameWon = false;
                    } else {

                        //Sleep for 1/10 of a second.
                        try {
                            Thread.sleep(100L);   //milliseconds
                        } catch (InterruptedException interruptedException) {
                        }
                    }
                }
            }
        };

        Thread thread = new Thread(runnable);   //rockList thread that is not the UI thread
        thread.start();   //The thread will execute the run method of the Runnable object.
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        // set to center
        //touchPoint = new PointF(getWidth() / (density * 2), getHeight() / (density * 2));

        horizon = (getHeight() - 100) / density;

        // set city coordinates
        float loc[] = new float[3];
        loc[0] = getWidth() / (density * 4);
        loc[1] = getWidth() / (density * 2);
        loc[2] = (getWidth() * 3) / (density * 4);

        for (int i = 0; i < cityCount; ++i){
            cityLocations[i] = new PointF(loc[i], horizon);
        }
    }

    // create a new rock
    private void createRock() {

        // generate x,y coordinates

        RockView newRock = new RockView(getWidth(),currentSpeed, gameLevel);
        newRock.setCenter(getRandCoordinate());

        // assign target

        float maxWidth = getWidth() / density;
        float boundry1 = maxWidth / 3;
        float boundry2 = (maxWidth * 2) / 3;

        int target;
        if (newRock.getCenter().x > 0 && newRock.getCenter().x < boundry1) {
            target = 0;
        } else if (newRock.getCenter().x > boundry1 && newRock.getCenter().x < boundry2) {
            target = 1;
        } else {
            target = 2;
        }

        newRock.setTarget(cityLocations[target]);

        // add to array of rocks
        rockList.add(newRock);

    }

    // get a random X coordinate for the new rock position
    private PointF getRandCoordinate() {
        Random r = new Random();
        float xRand = (r.nextInt(getWidth()) / density) + 1;
        float y = 10 / density;

        return new PointF(xRand, y);
    }

    public int getGameLevel() {
        return gameLevel;
    }

    public int getScore() {
        return score;
    }

    // draw game screen
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (gameOver) {
            // end game - loss
            gameOverDialog();

            // reset game
            resetGame();
        }

        if (gameWon) {
            // end game - win
            gameWonDialog();

            // reset game
            resetGame();
        }

        //Convert dp to pixels by multiplying times density.
        canvas.scale(density, density);

        paint.setColor(Color.GREEN);
        paint.setStyle(Paint.Style.FILL);
        canvas.drawRect(0, horizon, getWidth() / density, getHeight() / density, paint);

        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(10);

        for (int i = 0; i < cityCount; ++i) {
            canvas.drawRect(cityLocations[i].x - citySize, cityLocations[i].y - citySize, cityLocations[i].x + citySize, cityLocations[i].y + citySize, paint);
            canvas.drawText(cityNames[i], cityLocations[i].x - (citySize / 2) - 5, cityLocations[i].y - (citySize / 2) + 10, textPaint);
        }

        for (RockView rock : rockList) {
            PointF center = rock.getCenter();
            String color = rock.getColor();

            paint.setColor(Color.parseColor(color));

            paint.setStyle(Paint.Style.FILL);
            //Log.d("center.x", center.x + "");
            //Log.d("center.y", center.y + "");
            canvas.drawCircle(center.x, center.y, rockRadius, paint);
        }

        if (touchActive) {
            canvas.drawCircle(touchPoint.x, touchPoint.y, touchWidth, touchPaint);
        }
    }


    // check if a rock has collided with a city
    private boolean isRockTouchingCity(RockView targetRock) {

        boolean isTouching = false;

        for (int i = 0; i < cityCount; ++i) {
            double distance = targetRock.calcDistance(cityLocations[i]);

            if (distance <= citySize*1.5) {
                isTouching = true;
            }
        }

        return isTouching;
    }

    // check if a rock has collided with ground
    private boolean isRockTouchingGround(RockView targetRock) {

        boolean isTouching = false;

        if ((targetRock.getCenter().y) > horizon) {
            isTouching = true;
        }

        return isTouching;
    }

    // check if a rock has collided with another rock
    private boolean isRockTouchingRock(RockView rock1, RockView rock2) {

        boolean isTouching = false;

        double distance = rock1.calcDistance(rock2.getCenter());

        if (distance <= rockRadius*1.5) {
            isTouching = true;
        }

        return isTouching;
    }

    private void gameOverDialog() {
        Toast makeText;
        makeText = Toast.makeText(this.getContext(), getResources().getString(R.string.game_over), Toast.LENGTH_LONG);
        makeText.show();
        makeText = Toast.makeText(this.getContext(), getResources().getString(R.string.final_score) + score, Toast.LENGTH_LONG);
        makeText.show();
    }

    private void gameWonDialog() {
        Toast makeText;
        makeText = Toast.makeText(this.getContext(), getResources().getString(R.string.game_won), Toast.LENGTH_LONG);
        makeText.show();
        makeText = Toast.makeText(this.getContext(), getResources().getString(R.string.final_score) + score, Toast.LENGTH_LONG);
        makeText.show();
    }

    private void resetGame() {
        rockList.clear();

        initializeGame();
    }

    private void initializeGame() {
        score = 0;
        scoreTimer = 0;
        rockTimer = 0;
        touchActive = false;
        gameOver = false;
        currentSpeed = 1;
        gameLevel = 0;
        speedTimer = 0;
    }

    private void incrementCounters() {
        // update score by 5 every second
        ++scoreTimer;
        if (scoreTimer > scoreInterval) {
            score += 5;
            scoreTimer = 0;
        }

        // update speed by 100% every second
        ++speedTimer;
        if (speedTimer > speedInterval) {
            currentSpeed *= speedFactor;
            ++gameLevel;
            speedTimer = 0;
        }

        // increment rock timer (we create a new rock every 2 seconds)
        ++rockTimer;
    }

}

