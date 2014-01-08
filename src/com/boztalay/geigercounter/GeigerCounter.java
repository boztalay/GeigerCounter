package com.boztalay.geigercounter;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.net.URL;

public class GeigerCounter implements ZapReader.ZapListener {
    private static final String CLICK_SOUND_PATH = "file:res/switch-5.wav";

    private AudioClip clickSound;

    private ZapReader zapReader;
    private ZapRecorder zapRecorder;

    public static void main(String[] args) {
        GeigerCounter geigerCounter = new GeigerCounter();
        geigerCounter.startListeningAndCounting();
    }

    public GeigerCounter() {
        zapReader = new ZapReader(this);
        zapRecorder = new ZapRecorder();

        try {
            clickSound = Applet.newAudioClip(new URL(CLICK_SOUND_PATH));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Bad click sound path!");
        }
    }

    public void startListeningAndCounting() {
        zapReader.startReadingZaps();
    }

    @Override
    public void zapOccurred() {
        System.out.println("Zap!");
        clickSound.play();

        zapRecorder.recordZap();
    }
}
