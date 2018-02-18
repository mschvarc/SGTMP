# Copyright 2017 Martin Schvarcbacher
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#   http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

#Smart Grid Testing Management Platform - Mosaik Connector

import mosaik
import mosaik.util
import sys
from lxml import etree
import time
import timeit

# parse config files
print(sys.argv)
# args[1] = sim config
# args[2] = simulators
# args[3] = models
# args[4] = generated wire connectors


def extract_first(treeRoot, expression):
    result = treeRoot.xpath(expression + "/text()")
    if len(result) < 1:
        raise Exception("node empty or not found")
    return result[0]


def attribute_exists(treeRoot, expression):
    return len(treeRoot.xpath(expression + "/text()")) > 0


simConfigTree = etree.parse(sys.argv[1])
simConfigRoot = simConfigTree.getroot()

simulatorsTree = etree.parse(sys.argv[2])
simulatorsRoot = simulatorsTree.getroot()

modelTree = etree.parse(sys.argv[3])
modelRoot = modelTree.getroot()

wireTree = etree.parse(sys.argv[4])
wireRoot = wireTree.getroot()


# Sim config. and other parameters
SIM_CONFIG = {}
for simulator in simulatorsRoot.xpath("/simulators/simulator"):
    sim_name = extract_first(simulator, "./id")
    if (extract_first(simulator, "./type")).lower() == "connect":
        SIM_CONFIG[sim_name] = {'connect': extract_first(simulator, "./address")}
    elif extract_first(simulator, "./type").lower() == "cmd":
        SIM_CONFIG[sim_name] = {'cmd': extract_first(simulator, "./command")}
    elif extract_first(simulator, "./type").lower() == "python":
        SIM_CONFIG[sim_name] = {'python': extract_first(simulator, "./pythonFile")}

print(SIM_CONFIG)

# Create World
world = mosaik.World(SIM_CONFIG)

# Start simulators
simulators_config = {}
simulator_objects = {}
for simulator in simulatorsRoot.xpath("/simulators/simulator"):
    params = {}
    simulatorId = str(extract_first(simulator, "./id"))

    for kv in simulator.xpath("./additionalWorldParams/param"):
        key = extract_first(kv, "./key")
        value = extract_first(kv, "./value")
        params[key] = value
    simulators_config[simulatorId] = params
    simulator_objects[simulatorId] = world.start(sim_name=simulatorId, **params)



#async connection handling
asyncInfo = {}
for connection in wireRoot.xpath("/wiring/asyncConnections/connection"):
    sourceSimulatorID = extract_first(connection, "./source/simulatorID")
    sourceModelID = extract_first(connection, "./source/modelID")
    sourceModelIndex = int(extract_first(connection, "./source/modelIndex"))
    outputName = extract_first(connection, "./source/outputName")

    destinationSimulatorID = extract_first(connection, "./destination/simulatorID")
    destinationModelID = extract_first(connection, "./destination/modelID")
    inputName = extract_first(connection, "./destination/inputName")
    syncTargetName = extract_first(connection, "./destination/syncTargetName")

    #configure dictionary
    if sourceSimulatorID not in asyncInfo:
        asyncInfo[sourceSimulatorID] = {} #map
    if sourceModelID not in asyncInfo[sourceSimulatorID]:
        asyncInfo[sourceSimulatorID][sourceModelID] = {} #map
    if sourceModelIndex not in asyncInfo[sourceSimulatorID][sourceModelID]:
        asyncInfo[sourceSimulatorID][sourceModelID][sourceModelIndex] = [] #array

    #construct data triplet
    (asyncInfo[sourceSimulatorID][sourceModelID][sourceModelIndex]).append((outputName, inputName, syncTargetName))

print(asyncInfo)
#end async connection handling


# Instantiate models
models = {}
for model in modelRoot.xpath("/models/model"):
    simulatorID = extract_first(model, "./simulatorID")
    modelID = extract_first(model, "./modelID")
    modelIndex = int(extract_first(model, "./modelIndex"))

    simObject = simulator_objects[simulatorID]
    params = {}
    for kv in model.xpath("./additionalModelParams/param"):
        key = extract_first(kv, "./key")
        value = extract_first(kv, "./value")
        params[key] = value

    #inject asyncInfo to params
    if simulatorID in asyncInfo and modelID in asyncInfo[simulatorID] and modelIndex in asyncInfo[simulatorID][modelID]:
        params['asyncInfo'] = asyncInfo[simulatorID][modelID][modelIndex]

    # inject modelIndex to params
    params['__model_index_user__'] = modelIndex

    print("Creating: ", simulatorID, " : ", "index: ", modelIndex, modelID, " >> ", params )
    #dynamically get and create a new model object instance (__call__ creates new instance of itself internally)

    callable_mosaik_model = getattr(simObject, modelID)

    modelObject = callable_mosaik_model.__call__(**params)

    if simulatorID not in models:
        models[simulatorID] = {}
    if modelID not in models[simulatorID]:
        models[simulatorID][modelID] = {}

    models[simulatorID][modelID][modelIndex] = modelObject

# Connect entities
for connection in wireRoot.xpath("/wiring/directConnections/connection"):
    sourceSimulatorID = extract_first(connection, "./source/simulatorID")
    sourceModelID = extract_first(connection, "./source/modelID")
    sourceModelIndex = int(extract_first(connection, "./source/modelIndex"))
    outputName = extract_first(connection, "./source/outputName")

    destinationSimulatorID = extract_first(connection, "./destination/simulatorID")
    destinationModelID = extract_first(connection, "./destination/modelID")
    destinationModelIndex = int(extract_first(connection, "./destination/modelIndex"))
    inputName = extract_first(connection, "./destination/inputName")

    sourceModel = models[sourceSimulatorID][sourceModelID][sourceModelIndex]

    destinationModel = models[destinationSimulatorID][destinationModelID][destinationModelIndex]
    asyncRequired = extract_first(connection, "./asyncRequired").lower() == "true"

    #wire TUPLE (output, input)
    print("connecting: ", sourceModel, " -> ", destinationModel, "; ", outputName, " => ", inputName)
    world.connect(sourceModel, destinationModel, (outputName, inputName), async_requests=asyncRequired)


# Run simulation
END = int(simConfigRoot.xpath("/simulation/stepsToPerform/text()")[0])
start_time=timeit.default_timer()

if simConfigRoot.xpath("/simulation/simulationSpeed/isRealtime/text()")[0].lower() == "true":
    rt_strict_cfg = False
    rt_factor_cfg = int(simConfigRoot.xpath("/simulation/simulationSpeed/realtimeFactor/text()")[0])
    if simConfigRoot.xpath("/simulation/simulationSpeed/strictRealtime/text()")[0].lower() == "true":
        rt_strict_cfg = True
    world.run(until=END, rt_factor=rt_factor_cfg, rt_strict=rt_strict_cfg)
else:
    world.run(until=END)

end_time = timeit.default_timer()

print(end_time - start_time, " seconds, expected steps: ", END)
