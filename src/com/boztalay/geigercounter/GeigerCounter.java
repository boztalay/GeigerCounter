package com.boztalay.geigercounter;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class GeigerCounter implements SerialPortEventListener {
    private static final float CPM_INTERVAL_IN_MINUTES = 0.5f;

    private static final byte GEIGER_COUNTER_SPEAK_FOR_0 = 48;
    private static final byte GEIGER_COUNTER_SPEAK_FOR_1 = 49;

    private SerialPort serialPort;

    private int currentCounts;
    private long lastCountCollectionTime;

    public static void main(String[] args) {
        GeigerCounter geigerCounter = new GeigerCounter();
        geigerCounter.startListeningAndCounting();
    }

    public void startListeningAndCounting() {
        serialPort = new SerialPort("/dev/tty.usbserial-AM01VHAL");

        lastCountCollectionTime = System.currentTimeMillis();

        try {
            serialPort.openPort();
            serialPort.setParams(9600, 8, 1, 0);
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(this);
        } catch (SerialPortException exception) {
            System.out.println(exception);
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
                }
            }
        } catch (SerialPortException ex) {
            System.out.println(ex);
        }
    }

    private boolean isValueValidGeigerCounterData(byte value) {
        return (value == GEIGER_COUNTER_SPEAK_FOR_0 || value == GEIGER_COUNTER_SPEAK_FOR_1);
    }
}
