package com.boztalay.geigercounter;

import com.firebase.client.Firebase;

public class ZapRecorder {
    private Firebase zapsFirebaseRef;

    public ZapRecorder() {
        zapsFirebaseRef = new Firebase("https://geigercounter.firebaseio.com/zaps");
    }

    public void recordZap() {
        zapsFirebaseRef.push().setValue(new Zap(System.currentTimeMillis()));
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
}
