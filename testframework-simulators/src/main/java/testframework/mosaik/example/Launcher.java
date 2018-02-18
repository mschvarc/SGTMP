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

package testframework.mosaik.example;

import testframework.mosaik.simulators.GlobalTestEvaluatorSimulator;
import testframework.mosaik.example.simulators.Simulator1;
import testframework.mosaik.example.simulators.Simulator2;
import testframework.mosaik.example.simulators.SimulatorArduino;
import testframework.mosaik.example.simulators.SimulatorArduinoLed;

public class Launcher {

    public static void main(String[] args) throws Throwable {

        final String address = args.length >= 1 ? args[0] : "localhost";
        final int offset = args.length >= 2 ? Integer.parseInt(args[1]) : 5670;

        Runnable task1 = () -> {
            try {
                Simulator1.main(new String[]{address + ":" + (offset + 1), "server"});
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        };
        new Thread(task1).start();

        Runnable task2 = () -> {
            try {
                Simulator2.main(new String[]{address + ":" + (offset + 2), "server"});
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        };
        new Thread(task2).start();

        Runnable task3 = () -> {
            try {
                SimulatorArduinoLed.main(new String[]{address + ":" + (offset + 7), "server"});
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
        new Thread(task3).start();

        Runnable task4 = () -> {
            try {
                SimulatorArduino.main(new String[]{address + ":" + (offset + 8), "server"});
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
        new Thread(task4).start();

        Runnable task5 = () -> {
            try {
                GlobalTestEvaluatorSimulator.main(new String[]{address + ":" + (offset + 9), "server"});
            } catch (Throwable throwable) {
                throw new RuntimeException(throwable);
            }
        };
        new Thread(task5).start();

    }
}
