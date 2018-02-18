/*
 * Copyright 2017 Martin Schvarcbacher
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package testframework.mosaik.example.models;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fazecast.jSerialComm.SerialPort;
import org.apache.commons.lang3.tuple.Pair;
import testframework.mosaik.dto.measure.Measure;
import testframework.mosaik.dto.measure.MeasureAttribute;
import testframework.mosaik.dto.measure.MeasureType;
import testframework.mosaik.models.AbstractModel;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

import static com.fazecast.jSerialComm.SerialPort.TIMEOUT_READ_SEMI_BLOCKING;
import static java.lang.Thread.sleep;
import static testframework.mosaik.util.Helpers.getInt;


public class ModelArduino extends AbstractModel {

    public static final long NANOS_TO_MS = 1_000_000L;
    private static final int BAUD_RATE = 115200;
    private static final String NL = "\r\n"; //serial port windows encoding
    private static final String LED_STATE_1_ON = "BBBBBB";
    private static final String LED_STATE_1_OFF = "AAAAAA";
    private static final String LED_STATE_2_ON = "DDDDDD";
    private static final String LED_STATE_2_OFF = "CCCCCC";
    private static final String GET_STATE_CMD = "ZZZZZZZ\r\n"; //7bit long ASCII, receiver reacts to ZZZ
    private static final String STATUS_ACK_CMD = "STS_AXK"; //Arduino response to GET_STATE_CMD
    private static final long MAXIMUM_STEP_TIME_NS =
            500L * (1_000_000L); //maximum allowed time for round trip
    private static final String[] LED_STATE_COMMANDS = new String[]{
            LED_STATE_1_ON + LED_STATE_2_ON + NL,   //0
            LED_STATE_1_ON + LED_STATE_2_OFF + NL,  //1
            LED_STATE_1_OFF + LED_STATE_2_ON + NL,  //2
            LED_STATE_1_OFF + LED_STATE_2_OFF + NL  //3
    };
    @SuppressWarnings("unchecked")
    private static final Pair<Integer, Integer>[] LED_STATE_LOOKUP = new Pair[]{
            Pair.of(1, 1),
            Pair.of(1, 0),
            Pair.of(0, 1),
            Pair.of(0, 0)
    };
    private static final String LED_PAIR_SEPARATOR = "_";
    private static final boolean DEBUG_PRINT = false;
    private static final int READ_TIMEOUT = 100_000;
    private final ObjectMapper mapper = new ObjectMapper();
    private final String portAdr;
    private volatile int voltageOut = -1;
    private volatile int ledStateOut = -1;
    private volatile int ledStateIn = -1;
    private SerialPort comPort;
    private boolean readyReceived = false;
    private int nonce = 0;
    private boolean isRealSimulator = false;
    private boolean isComConnected = false;
    private MeasureAttribute voltageOutMeasure;


    public ModelArduino(int ledStateIn, String port) {
        this.portAdr = port;
        this.ledStateIn = ledStateIn;
        isRealSimulator = !port.contains("FAKEPORT");
        setupMeasures();

        if (isRealSimulator) {
            try {
                connect(this.portAdr);
                firstStepSync();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            addShutDownHook();
        }
        synchronizeFieldsToMap();
    }

    public String getPortAddress() {
        return portAdr;
    }

    /**
     * Synchronizes the Arduino and Mosaik controllers
     * Waits for both devices to send/recieve READY signals on serial ports
     */
    public void firstStepSync() throws InterruptedException {
        int maxAttempts = 100;
        int attempts = 0;
        while (!readyReceived) {
            getMessage();
            if (readyReceived) {
                break;
            }
            Thread.sleep(100);
            attempts++;
            if (attempts >= maxAttempts) {
                throw new RuntimeException("READY signal not received in time, check if correct port: " + portAdr);
            }
        }

        writeMessage(GET_STATE_CMD);
        readLine(); //STS_ACK
        readLine(); //V:
        readLine(); //L:
        if (DEBUG_PRINT) {
            System.out.println("READY RECEIVED");
        }
        getMessage(); //discard all
        isComConnected = true;
    }

    @Override
    public void step(long time) {
        synchronizeMapToFields();
        if (isRealSimulator) {
            stepReal();
        } else {
            stepFake();
        }

        try {
            setMeasures();
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        synchronizeFieldsToMap();
    }

    private void setupMeasures() {
        voltageOutMeasure = new MeasureAttribute();
        voltageOutMeasure.setSimulatorId("SimulatorArduino");
        voltageOutMeasure.setModelId("Model_Arduino");
        voltageOutMeasure.setModelId("Model_Arduino");
        voltageOutMeasure.setName("SimulatorArduino:Model_Arduino:voltageMeasure");
        voltageOutMeasure.setMeasureInputName("voltageMeasure");


    }

    private void setMeasures() throws JsonProcessingException {
        Measure voltageOutMeasureActual = new Measure(voltageOut);
        voltageOutMeasureActual.setUnit("Volts");
        voltageOutMeasureActual.setMeasureAttribute(voltageOutMeasure);
        voltageOutMeasureActual.setMeasureType(MeasureType.ACTUAL);

        String measureResult = mapper.writeValueAsString(voltageOutMeasureActual);
        setValue("voltageMeasure", measureResult);
    }

    //simulate communication delay, do nothing
    private void stepFake() {
        try {
            Thread.sleep(400);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    private synchronized void stepReal() {
        long startTime = System.nanoTime();
        try {
            //discard any previous data, clear buffers, non-blocking
            getMessage();

            //query current state
            writeMessage(GET_STATE_CMD);
            String statusAck = readLine(); //STS_AXK
            String voltageRet = readLine(); //V:###
            String ledRet = readLine(); //L:###

            assert statusAck.contains(STATUS_ACK_CMD);
            assert voltageRet.contains("V:");
            assert ledRet.contains("L:");

            writeLedState();

            //read ACK 0/1 for target LED state
            String ledAckSwitch = readLine(); //ACK [0/1]_[0/1]
            Pair<Integer, Integer> expectedAckState = LED_STATE_LOOKUP[ledStateIn];
            assert ledAckSwitch.contains("ACK " + expectedAckState.getLeft() + LED_PAIR_SEPARATOR + expectedAckState.getRight());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        nonce = (++nonce) % 32;
        long stopTime = System.nanoTime();
        long stepTime = stopTime - startTime;

        if (DEBUG_PRINT) {
            System.out.println(String.format(Locale.US, "Execution on %s took %d ms, maximum: %d", portAdr, stepTime / NANOS_TO_MS, MAXIMUM_STEP_TIME_NS / NANOS_TO_MS));
        }
        if (stepTime > MAXIMUM_STEP_TIME_NS) {
            throw new IllegalStateException(
                    String.format(Locale.US, "Execution on %s took %d ms, RT requirement failed, maximum allowed: %d", portAdr, stepTime / NANOS_TO_MS, MAXIMUM_STEP_TIME_NS / NANOS_TO_MS));
        }
    }

    private void writeLedState() {

        if (ledStateIn < 0 || ledStateIn >= LED_STATE_COMMANDS.length) {
            throw new IllegalArgumentException("ledStateIn out of bounds: " + ledStateIn);
        }
        writeMessage(LED_STATE_COMMANDS[ledStateIn]);
    }


    /**
     * Connects serial connector to given portName
     *
     * @param portName eg: COM3, /dev/stt1
     */
    private void connect(String portName) {
        comPort = SerialPort.getCommPort(portName);

        if (comPort.isOpen()) {
            throw new RuntimeException("port already open");
        }
        comPort.openPort();
        //All serial port parameters or timeouts can be changed at any time after the port has been opened.
        comPort.setComPortParameters(BAUD_RATE, 8, SerialPort.ONE_STOP_BIT, SerialPort.NO_PARITY);
        comPort.setComPortTimeouts(TIMEOUT_READ_SEMI_BLOCKING, 0, 0);
        assert comPort.isOpen();
    }

    private void writeMessage(String msg) {
        if (DEBUG_PRINT) {
            System.out.println("TX: " + msg);
        }
        byte[] output = msg.getBytes(StandardCharsets.US_ASCII);
        comPort.writeBytes(output, output.length);
    }

    /**
     * Blocking serial readLine. Blocks until a newline is received
     * runs processLine() internally
     *
     * @return returns processed newline without ending \n
     */
    private String readLine() {
        int timeLeft = READ_TIMEOUT;

        StringBuilder output = new StringBuilder(1024);
        byte[] buffer = new byte[1024];
        while (true) {
            timeLeft--;
            if (timeLeft <= 0) {
                throw new RuntimeException("failed to read new line, timeout exceeded");
            }

            int bit;
            if (comPort.bytesAvailable() <= 0) {
                Thread.yield();
                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }
            comPort.readBytes(buffer, 1); //NEVER read more than 1 byte, there could be a new line after \n
            bit = buffer[0];
            if (bit == '\n') {
                processLine(output.toString());
                return output.toString(); //return parsed line
            } else {
                output.append((char) bit);
            }
            // wait 10ms when stream is broken and check again
            try {
                sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Non-blocking.
     * Retrieves message from the serial port buffer and processes any newline delimited messages
     */
    private void getMessage() {
        byte[] buffer = new byte[1024 * 64];
        int length = 0;
        StringBuilder output = new StringBuilder(1024 * 64);
        while (comPort.bytesAvailable() > 0) {
            length = comPort.bytesAvailable();
            comPort.readBytes(buffer, comPort.bytesAvailable());
        }
        for (int i = 0; i < length; i++) {
            byte b = buffer[i];
            if (b == '\n') {
                processLine(output.toString());
                output.delete(0, output.length());
            } else {
                output.append((char) b);
            }
        }
    }

    /**
     * Processes individual line for Voltage and LED state
     *
     * @param message
     */
    private void processLine(String message) {
        if (DEBUG_PRINT) {
            System.out.println("RX: " + message);
        }
        message = trimNewlines(message);
        if (message.contains("Ready")) {
            readyReceived = true;
        } else if (message.contains("V:")) {
            this.voltageOut = Integer.parseInt(message.substring("V:".length(), message.length()));
        } else if (message.contains("L:")) {
            this.ledStateOut = Integer.parseInt(message.substring("L:".length(), message.length()));
        }

    }

    /**
     * Trims a string and removes newline (\r,\n) from the end
     *
     * @param message
     * @return trimmed string
     */
    private String trimNewlines(String message) {
        message = message.trim();
        if (message.endsWith("\n")) {
            message = message.substring(0, message.length() - 1);
        }
        if (message.endsWith("\r")) {
            message = message.substring(0, message.length() - 1);
        }
        return message;
    }

    /**
     * Closes the COM connection if it exists
     */
    public void disconnect() {
        if (isRealSimulator) {
            //SWITCH OFF LEDS WHEN DONE!!!
            ledStateIn = 3; //all off
            try {
                writeLedState();
            } catch (Exception e) {
                //closing, do nothing, errors expected
                e.printStackTrace();
            }
            if (comPort.isOpen()) {
                comPort.closePort();
                isComConnected = false;
            }
            assert !comPort.isOpen();
        }
    }

    private void addShutDownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (isComConnected) {
                disconnect();
            }
        }));
    }


    @Override
    public void synchronizeFieldsToMap() {
        setValue("ledStateIn", ledStateIn);
        setValue("ledStateOut", ledStateOut);
        setValue("voltageOut", voltageOut);
    }

    @Override
    public void synchronizeMapToFields() {
        ledStateIn = getInt(getValue("ledStateIn"));
        ledStateOut = getInt(getValue("ledStateOut"));
        voltageOut = getInt(getValue("voltageOut"));
    }
}
