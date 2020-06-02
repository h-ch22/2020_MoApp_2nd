package kr.ac.jbnu.se.MoApp2020_2nd;

import android.os.CountDownTimer;

import java.util.concurrent.TimeUnit;

class CounterClass extends CountDownTimer {
    public String hms;
    public String ymd;

    activity_timeCapsule time = new activity_timeCapsule();

    /**
     * @param millisInFuture    The number of millis in the future from the call
     *                          to {@link #start()} until the countdown is done and {@link #onFinish()}
     *                          is called.
     * @param countDownInterval The interval along the way to receive
     *                          {@link #onTick(long)} callbacks.
     */

    public CounterClass(long millisInFuture, long countDownInterval) {
        super(millisInFuture, countDownInterval);
    }

    @Override
    public void onTick(long millisUntilFinished) {
        long millis = millisUntilFinished;

        hms = String.format("%02d : %02d : %02d",
                TimeUnit.MILLISECONDS.toHours(millis),
                TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis)),
                TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millis)));

        time.TimeRemain.setText(hms);
    }

    @Override
    public void onFinish() {

    }
}
