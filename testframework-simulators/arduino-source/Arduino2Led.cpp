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

//board specific
const int INPUT_PORT = A1;
const int OUTPUT_PORT_1 = A8;
const int OUTPUT_PORT_2 = A10;

//serial port commands input
const String LED_STATE_1_ON = "BBB";
const String LED_STATE_1_OFF = "AAA";
const String LED_STATE_2_ON = "DDD";
const String LED_STATE_2_OFF = "CCC";
const String GET_STATE = "ZZZ";

const String STATUS_ACK = "STS_AXK";

bool LED_STATE_1 = false;
bool LED_STATE_2 = false;

String inputBuffer = "";

int counter = 0;

void setup() {
    Serial.begin(115200 L); //115200L || 230400L || 9600
    Serial.println("[[[[[Ready]]]]]]");
    pinMode(INPUT_PORT, INPUT);
    pinMode(OUTPUT_PORT_1, OUTPUT);
    pinMode(OUTPUT_PORT_2, OUTPUT);
}

//responds to LED_ON / LED_OFF signals
void processInput() {
    if (Serial.available() <= 0) {
        return;
    }

    char inChar = 0;
    while (Serial.available() > 0) {
        inChar = Serial.read();
        if (inChar < 127) {
            inputBuffer += inChar;
        }
        if (inChar == '\n') {
            parseInput();
        }
    }
}

void acknowledgeLed() {
    inputBuffer = "";
    Serial.print("ACK ");
    Serial.print(LED_STATE_1);
    Serial.print("_");
    Serial.println(LED_STATE_2);
}

void acknowledgeGetState() {
    inputBuffer = "";
    Serial.println(STATUS_ACK);
}

void parseInput() {
    bool new_led_ack = false;
    bool recognized_command = false;

    if (find_text(LED_STATE_1_ON, inputBuffer) != -1) {
        new_led_ack = true;
        recognized_command = true;
        LED_STATE_1 = true;
    }
    if (find_text(LED_STATE_1_OFF, inputBuffer) != -1) {
        new_led_ack = true;
        recognized_command = true;
        LED_STATE_1 = false;
    }

    if (find_text(LED_STATE_2_ON, inputBuffer) != -1) {
        new_led_ack = true;
        recognized_command = true;
        LED_STATE_2 = true;
    }
    if (find_text(LED_STATE_2_OFF, inputBuffer) != -1) {
        new_led_ack = true;
        recognized_command = true;
        LED_STATE_2 = false;
    }

    if (find_text(GET_STATE, inputBuffer) != -1) {
        recognized_command = true;
        acknowledgeGetState();
        sendOutput();
    }

    if (new_led_ack) {
        acknowledgeLed();
    }
    if (!recognized_command) {
        Serial.println("NAK: " + inputBuffer); //echo back unrecognized command
        inputBuffer = "";
    }
}

//transforms state of 2 LEDs back to single state
int transformSingleLedState() {
    if (LED_STATE_1) {
        if (LED_STATE_2) {
            return 0;
        } else {
            return 1;
        }
    } else {
        if (LED_STATE_2) {
            return 2;
        } else {
            return 3;
        }
    }
}

int find_text(String needle, String haystack) {
    int foundpos = -1;
    for (int i = 0;
        (i < haystack.length() - needle.length()); i++) {
        if (strncmp((haystack.substring(i, needle.length() + i)).c_str(), needle.c_str(), needle.length()) == 0) {
            foundpos = 1;
        }
    }
    return foundpos;
}

void sendOutput() {
    // read the input on analog pin 0:
    delay(50); //pause for 50ms, this gives LEDs time to warm up (~5ms per spec sheet), will impact next line analogRead()
    int sensorValue = analogRead(A1);

    // Convert the analog reading (which goes from 0 - 1023) to a voltage (0 - 5V):
    //float voltage = sensorValue * (5.0 / 1023.0);
    Serial.print("V:");
    Serial.println(sensorValue);

    Serial.print("L:");
    Serial.println(transformSingleLedState());
}

void switchLeds() {
    if (LED_STATE_1) {
        digitalWrite(OUTPUT_PORT_1, HIGH);
    } else {
        digitalWrite(OUTPUT_PORT_1, LOW);
    }

    if (LED_STATE_2) {
        digitalWrite(OUTPUT_PORT_2, HIGH);
    } else {
        digitalWrite(OUTPUT_PORT_2, LOW);
    }
}

void loop() {
    processInput();
    switchLeds();
    counter = (++counter) % 64;
}