package edu.nyu.scps.asa.jul11;

import android.graphics.PointF;

// class for individual rock objects

public class RockView {
    private static final float elasticity = -0.0020f; //higher number means more resistant to stretching
    private static final float gravity = 0.0002f;
    private static final float mass = 1.0f;          //must be non-negative

    private static String colorList[] = new String[] {
            "#ECEFF1",
            "#CFD8DC",
            "#B0BEC5",
            "#90A4AE",
            "#78909C",
            "#607D8B",
            "#546E7A",
            "#455A64",
            "#37474F",
            "#263238"
    };

    private PointF center = new PointF();
    private PointF velocity = new PointF(); //born with zero velocity
    private PointF target = new PointF();
    private float speed;
    private String color;

    private float screenWidth;
    private static int idNbr = 0;
    private int myId;

    private static boolean repulsorActive = false;

    //creator method
    public RockView(float width, float currentSpeed, int currentPower) {
        // generare a unique id to tell each rock apart
        ++idNbr;
        myId = idNbr;
        //destroyRock = false;
        screenWidth = width;
        speed = currentSpeed;

        // cycle through list of colors based on power of rock
        color = colorList[currentPower % colorList.length];
    }

    // This method calculates the new x,y location of the rock
    public void dragTowards(PointF touchPoint, float touchRadius) {

        boolean inArea;

        double distance = Math.abs(calcDistance(touchPoint));
        if ((distance < touchRadius)) {
            //Log.d("distance", distance + "");
            inArea = true;
        } else {
            inArea = false;
        }

        // controls how fast you can push away objects when beam is running
        float strength = 10;

        PointF force;

        if (repulsorActive && inArea) {

            force = new PointF(
                    ((strength * (touchPoint.x - center.x)) * elasticity) + ((target.x - center.x) * gravity * speed),
                    ((strength * (touchPoint.y - center.y)) * elasticity) + ((target.y - center.y) * gravity * speed)
            );
        } else {
            force = new PointF(
                    ((target.x - center.x) * gravity * speed),
                    ((target.y - center.y) * gravity * speed)
            );
        }

        // force y should be (p.y - center.y) * elasticity + gravity * mass

        //F = ma
        //Therefore a = F/m

        final PointF acceleration = new PointF(
                force.x / mass,
                force.y / mass
        );

        velocity.set(
                (velocity.x + acceleration.x),
                (velocity.y + acceleration.y)
        );

        float newX = center.x + velocity.x;

        // check if rocks have gone off the end of the screen and wrap them around
        if (newX < 0) {
            newX = screenWidth - newX;
        } else if (newX > screenWidth) {
            newX = newX % screenWidth;
        }

        float newY = center.y + velocity.y;

        center.set(
                newX,
                newY
        );
    }

    // these are getters and setters for checking rock location, target location, and if user is touching screen

    public void setCenter(PointF center) {
        // make a new object to prevent user from referencing private center variable
        PointF tmpCenter = new PointF(center.x, center.y);
        this.center.set(tmpCenter);
    }

    public PointF getCenter() {
        // make a new object to prevent user from referencing private center variable
        PointF newCenter = new PointF(center.x, center.y);
        return newCenter;
    }

    public PointF getTarget() {
        // make a new object to prevent user from referencing private target variable
        PointF tmpTarget = new PointF(this.target.x, this.target.y);
        return tmpTarget;
    }

    public void setTarget(PointF target) {
        // make a new object to prevent user from referencing private target variable
        PointF tmpTarget = new PointF(target.x, target.y);
        this.target.set(tmpTarget);
    }

    public static boolean isRepulsorActive() {
        return repulsorActive;
    }

    public static void setRepulsorActive(boolean repulsorActive) {
        RockView.repulsorActive = repulsorActive;
    }

    // calculate distance from the center of this rock to another object
    public double calcDistance(PointF objectCenter) {
        return Math.hypot(center.x - objectCenter.x, center.y - objectCenter.y);
    }

    public String getColor() {
        return color;
    }

    // equals method is used by GameView when checking to see if two rocks have hit each other
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RockView pearl = (RockView) o;

        return myId == pearl.myId;
    }

    @Override
    public int hashCode() {
        return myId;
    }

}

