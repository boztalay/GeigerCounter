package com.boztalay.geigercounter;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

import java.applet.Applet;
import java.applet.AudioClip;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Timer;
import java.util.TimerTask;

public class GeigerCounter implements SerialPortEventListener {
    private static final String SERIAL_PORT_NAME = "/dev/tty.usbserial-AM01VHAL";
    private static final String CLICK_SOUND_PATH = "file:res/switch-5.wav";

    private static final byte GEIGER_COUNTER_SPEAK_FOR_0 = 48;
    private static final byte GEIGER_COUNTER_SPEAK_FOR_1 = 49;

    private static final float COUNT_PERIOD_IN_MINUTES = 0.5f;

    private SerialPort serialPort;
    private AudioClip clickSound;

    private int currentCounts;

    private BackendHandler backendHandler;

    public static void main(String[] args) {
        GeigerCounter geigerCounter = new GeigerCounter();
        geigerCounter.startListeningAndCounting();
    }

    public GeigerCounter() {
        backendHandler = new BackendHandler();

        try {
            clickSound = Applet.newAudioClip(new URL(CLICK_SOUND_PATH));
        } catch (MalformedURLException e) {
            throw new RuntimeException("Bad click sound path!");
        }
    }

    public void startListeningAndCounting() {
        serialPort = new SerialPort(SERIAL_PORT_NAME);

        try {
            serialPort.openPort();
            serialPort.setParams(9600, 8, 1, 0);
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(this);
        } catch (SerialPortException e) {
            System.out.println(e);
        }

        Timer timer = new Timer();
        long timerPeriodInMilliseconds = (long)(60000 * COUNT_PERIOD_IN_MINUTES);
        timer.scheduleAtFixedRate(new CalculateCPMTask(), timerPeriodInMilliseconds, timerPeriodInMilliseconds);
    }

    class CalculateCPMTask extends TimerTask {
        public void run() {
            float CPM = currentCounts / COUNT_PERIOD_IN_MINUTES;
            System.out.println("CPM: " + CPM);
            currentCounts = 0;
        }
    }

    //Called whenever something comes over the serial line
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
                    clickSound.play();
                    currentCounts++;
                    backendHandler.recordReading();
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
