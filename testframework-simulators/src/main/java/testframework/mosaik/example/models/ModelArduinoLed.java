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


import testframework.mosaik.models.AbstractModel;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;
import java.util.stream.Stream;

public class ModelArduinoLed extends AbstractModel {
    private int ledInput = 0;
    private int ledOutput = 0;
    private int voltageIn = 0;
    private String path;

    private int currentStep = 0;
    private ArrayList<Integer> ledStateTimeProfile = new ArrayList<>(1024);

    public ModelArduinoLed(Map<String, Object> modelParams) {
        this.ledInput = Integer.parseInt((String) modelParams.get("initLedState"));
        this.path = (String) modelParams.get("path");
        loadProfile();
        synchronizeFieldsToMap();
    }

    @Override
    public void synchronizeFieldsToMap() {
        setValue("voltageIn", voltageIn);
        setValue("ledStateIn", ledInput);
        setValue("ledStateOut", ledOutput);
    }

    @Override
    public void synchronizeMapToFields() {
        ledInput = ((Number) getValue("ledStateIn")).intValue();
        ledOutput = ((Number) getValue("ledStateOut")).intValue();
        voltageIn = ((Number) getValue("voltageIn")).intValue();
    }

    public void step(long time) {
        synchronizeMapToFields();
        this.ledOutput = ledStateTimeProfile.get(currentStep % ledStateTimeProfile.size());
        ledInput = ledOutput;
        currentStep++;
        synchronizeFieldsToMap();
    }

    private void loadProfile() {
        File file = new File(path);
        try (Stream<String> stream = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {
            stream.forEach((String line) -> {
                if (!line.trim().isEmpty())
                    ledStateTimeProfile.add(Integer.parseInt(line.trim()));
            });
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
