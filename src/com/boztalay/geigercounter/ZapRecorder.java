package com.boztalay.geigercounter;

import com.firebase.client.*;

import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

public class ZapRecorder {
    private static final int CPM_CALCULATION_PERIOD_IN_MINS = 5;

    private Firebase zapsFirebaseRef;
    private Firebase cpmCalculationsFirebaseRef;

    private Timer cpmCalculationTimer;

    public ZapRecorder() {
        zapsFirebaseRef = new Firebase("https://geigercounter.firebaseio.com/zaps");
        cpmCalculationsFirebaseRef = new Firebase("https://geigercounter.firebaseio.com/cmpCalculations");

        cpmCalculationTimer = new Timer();
        long cpmCalculationTimerPeriod = minutesToMillis(CPM_CALCULATION_PERIOD_IN_MINS);
        cpmCalculationTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                calculateCPM();
            }
        }, cpmCalculationTimerPeriod, cpmCalculationTimerPeriod);
    }

    public void recordZap() {
        zapsFirebaseRef.push().setValue(new Zap(System.currentTimeMillis()));
    }

    public void calculateCPM() {
        zapsFirebaseRef.runTransaction(new Transaction.Handler() {
            @Override
            public Transaction.Result doTransaction(MutableData currentData) {
                if (currentData.getChildrenCount() > 0) {
                    long dateMax = System.currentTimeMillis();
                    long dateMin = dateMax - minutesToMillis(CPM_CALCULATION_PERIOD_IN_MINS);

                    int numZapsOverTimeToCalculateCPM = 0;

                    for (MutableData zapChild : currentData.getChildren()) {
                        long zapTime = ((HashMap<String, Long>) zapChild.getValue()).get("date");
                        if (zapTime >= dateMin && zapTime <= dateMax) {
                            numZapsOverTimeToCalculateCPM++;
                        }
                    }

                    float cpm = ((float) numZapsOverTimeToCalculateCPM / (float)CPM_CALCULATION_PERIOD_IN_MINS);
                    cpmCalculationsFirebaseRef.push().setValue(new CPMCalculation(System.currentTimeMillis(), cpm));

                    System.out.println("Number of zaps over last " + CPM_CALCULATION_PERIOD_IN_MINS + " minutes: " + numZapsOverTimeToCalculateCPM);
                    System.out.println("CPM: " + cpm);

                    zapsFirebaseRef.removeValue();
                }

                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(FirebaseError error, boolean committed, DataSnapshot currentData) {

            }
        });
    }

    private long minutesToMillis(int minutes) {
        return (minutes * 60 * 1000);
    }

    private class Zap {
        private long date;

        public Zap(long date) {
            this.date = date;
        }

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }
    }

    private class CPMCalculation {
        private long date;
        private float cpm;

        public CPMCalculation(long date, float cpm) {
            this.date = date;
            this.cpm = cpm;
        }

        public long getDate() {
            return date;
        }

        public void setDate(long date) {
            this.date = date;
        }

        public float getCpm() {
            return cpm;
        }

        public void setCpm(float cpm) {
            this.cpm = cpm;
        }
    }
}
