package com.boztalay.geigercounter;

import com.firebase.client.Firebase;
import com.firebase.client.Logger;

public class BackendHandler {
    private Firebase firebaseRef;

    public BackendHandler() {
        firebaseRef = new Firebase("https://geigercounter.firebaseio.com/zaps");
    }

    public void recordReading() {
        firebaseRef.push().setValue(new Reading(System.currentTimeMillis()));
    }

    private class Reading {
        private long date;

        public Reading(long date) {
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
