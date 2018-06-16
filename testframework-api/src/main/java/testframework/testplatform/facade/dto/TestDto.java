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

package testframework.testplatform.facade.dto;

import testframework.testplatform.dal.entities.Test;

import java.util.UUID;

public class TestDto {

    private long id;
    private String uuid;

    private String testName;

    private String testDescription;

    private boolean oneStepPermanentFailure;

    private TopologyDto topology;

    public void initializeUUID() {
        if (uuid == null) {
            uuid = UUID.randomUUID().toString();
        }
    }

    public String getUuid() {
        if (uuid == null) {
            initializeUUID();
        }
        return uuid;
    }


    public void setUuid(String id) {
        this.uuid = id;
    }


    public String getTestName() {
        return testName;
    }

    public void setTestName(String testName) {
        this.testName = testName;
    }

    public TopologyDto getTopology() {
        return topology;
    }

    public void setTopology(TopologyDto topology) {
        this.topology = topology;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Test)) return false;
        if (!super.equals(o)) return false;

        Test test = (Test) o;

        if (getUuid() != null ? !getUuid().equals(test.getUuid()) : test.getUuid() != null) return false;
        return getTestName() != null ? getTestName().equals(test.getTestName()) : test.getTestName() == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (getUuid() != null ? getUuid().hashCode() : 0);
        result = 31 * result + (getTestName() != null ? getTestName().hashCode() : 0);
        return result;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTestDescription() {
        return testDescription;
    }

    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }

    public boolean isOneStepPermanentFailure() {
        return oneStepPermanentFailure;
    }

    public void setOneStepPermanentFailure(boolean oneStepPermanentFailure) {
        this.oneStepPermanentFailure = oneStepPermanentFailure;
    }
}
