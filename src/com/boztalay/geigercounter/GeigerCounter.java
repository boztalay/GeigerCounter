package com.boztalay.geigercounter;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.net.URL;

public class GeigerCounter implements SerialPortEventListener {
    private static final String SERIAL_PORT_NAME = "/dev/tty.usbserial-AM01VHAL";
    private static final String CLICK_SOUND_PATH = "file:res/switch-5.wav";

    private static final float CPM_INTERVAL_IN_MINUTES = 0.5f;

    private static final byte GEIGER_COUNTER_SPEAK_FOR_0 = 48;
    private static final byte GEIGER_COUNTER_SPEAK_FOR_1 = 49;

    private SerialPort serialPort;
    private AudioClip clickSound;

    private int currentCounts;
    private long lastCountCollectionTime;

    public static void main(String[] args) {
        GeigerCounter geigerCounter = new GeigerCounter();
        geigerCounter.startListeningAndCounting();
    }

    public GeigerCounter() {
        try {
            clickSound = Applet.newAudioClip(new URL(CLICK_SOUND_PATH));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Bad click sound path!");
        }
    }

    public void startListeningAndCounting() {
        serialPort = new SerialPort(SERIAL_PORT_NAME);

        lastCountCollectionTime = System.currentTimeMillis();

        try {
            serialPort.openPort();
            serialPort.setParams(9600, 8, 1, 0);
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(this);
        } catch (SerialPortException e) {
            System.out.println(e);
        }
    }

    public void serialEvent(SerialPortEvent event) {
        if (doesEventHaveDataReady(event)) {
            processDataFromEvent(event);
        }
    }

    private boolean doesEventHaveDataReady(SerialPortEvent event) {
        return (event.isRXCHAR() && event.getEventValue() > 0);
    }

    private void processDataFromEvent(SerialPortEvent event) {
        try {
            byte[] geigerCounterData = serialPort.readBytes(event.getEventValue());
            for(byte dataValue : geigerCounterData) {
                if(isValueValidGeigerCounterData(dataValue)) {
                    System.out.println("ZAP!");
                    clickSound.play();
                }
            }
        } catch (SerialPortException e) {
            System.out.println(e);
        }
    }

    private boolean isValueValidGeigerCounterData(byte value) {
        return (value == GEIGER_COUNTER_SPEAK_FOR_0 || value == GEIGER_COUNTER_SPEAK_FOR_1);
    }
}
