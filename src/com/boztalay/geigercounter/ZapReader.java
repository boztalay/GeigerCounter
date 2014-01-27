package com.boztalay.geigercounter;

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortException;

public class ZapReader implements SerialPortEventListener {
    private static final String SERIAL_PORT_NAME = "/dev/ttyUSB0";

    private static final byte GEIGER_COUNTER_SPEAK_FOR_0 = 48;
    private static final byte GEIGER_COUNTER_SPEAK_FOR_1 = 49;

    private SerialPort serialPort;

    private ZapListener zapListener;

    public ZapReader(ZapListener zapListener) {
        this.zapListener = zapListener;
    }

    public void startReadingZaps() {
        serialPort = new SerialPort(SERIAL_PORT_NAME);

        try {
            serialPort.openPort();
            serialPort.setParams(9600, 8, 1, 0);
            serialPort.setEventsMask(SerialPort.MASK_RXCHAR);
            serialPort.addEventListener(this);
        } catch (SerialPortException e) {
            System.out.println(e);
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
                    zapListener.zapOccurred();
                }
            }
        } catch (SerialPortException e) {
            System.out.println(e);
        }
    }

    private boolean isValueValidGeigerCounterData(byte value) {
        return (value == GEIGER_COUNTER_SPEAK_FOR_0 || value == GEIGER_COUNTER_SPEAK_FOR_1);
    }

    public interface ZapListener {
        public abstract void zapOccurred();
    }
}
