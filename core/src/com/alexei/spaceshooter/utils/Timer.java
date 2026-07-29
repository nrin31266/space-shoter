package com.alexei.spaceshooter.utils;

/**
 * Created by Alex on 17/06/2015.
 *
 * A base class which helps time game events.
 * The timer is updated every render call with elapsedTime += Gdx.graphics.getDeltaTime()
 */
public class Timer {
    private long elapsedTime = 0;
    private long durationMillis;
    private int playbackCount = 0;
    private int playbackMax = 0; // 0 indicates infinite repeat count

    private boolean timerElapsed = false; // a flag which indicates the state of the timer at any point
    // this flag is raised whenever the duration period expired. However, it is lowered only by a call to
    // Timer.handleEvent() from outside the class, usually by the object which is

    public Timer(int durationMillis, int repeatMax) {
        if (repeatMax < 0) this.playbackMax = 0; else this.playbackMax = repeatMax;
        if (durationMillis < 0) this.durationMillis = 0; else this.durationMillis = durationMillis;
    }

    public void update(float deltaTime){
        if (playbackCount == playbackMax && playbackMax != 0) return; // do not update the timer when the timer has repeatedly elapsed a specified number of times.
        timerElapsed = false;
        elapsedTime += deltaTime;
        if (elapsedTime >= durationMillis) {
            timerElapsed = true; // raise the flag that indicates that a timer has elapsed.
            playbackCount++;
            if (playbackCount < playbackMax || playbackMax == 0) {
                //if (playbackMax != 0) playbackCount++; // update the counter only when it is not set to repeat infinitely
                //playbackCount++; // update the counter only when it is not set to repeat infinitely
                elapsedTime = elapsedTime - durationMillis; // instead of elapsedTime = 0,
                // we write this sneaky formula for accuracy. It ensures that milliseconds of time periods
                // which are smaller than the update delta are not lost when we reset the timer.
            }
        }
    }

    /***
     * Handles the timer elapsed event. This is simply a polling method that other objects can call
     * to check whether the timer has elapsed.
     * @return It returns true when the timer has elapsed and the 'timerElapsed' flag is raised,
     * otherwise returns false. Before returning true, the flag is lowered.
     */
    public boolean isTimerElapsed() {
        if (timerElapsed) {
            //timerElapsed = false;
            return true;
        }
        return false;
    }

    public void reset() { elapsedTime = 0; timerElapsed = false; playbackCount = 0; }
    public long getElapsedTime() { return elapsedTime >= durationMillis ? durationMillis : elapsedTime; }
    public long getDuration() { return durationMillis; }
    public void setDuration(long duration) {this.durationMillis = duration; }
    public void setElapsedTime(long time) { this.elapsedTime = time; }
    public float getProgress() { return this.elapsedTime/(float)this.durationMillis; }
}
