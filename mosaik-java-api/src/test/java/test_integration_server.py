import sys
import subprocess
import os
import mosaik
import time
import mosaik.scheduler
import mosaik.util


def test_integration_server(sim_config_data_client):
    # sim_config_data_client is a fixture created by conftest.py and contains
    # information shared across test cases

    sim_config, end, addr = sim_config_data_client

    # start Java Simulator as Server in a separate process,
    # which listens on addr_server_test
    if sys.platform == 'win32':
        proc = subprocess.Popen(['examplesim.bat', addr, 'server'],
                                cwd=os.path.dirname(os.path.realpath(__file__))
                                .replace('\\tests', ''))
    else:
        proc = subprocess.Popen(['./examplesim.sh', addr, 'server'])

    # wait for the Java Server
    time.sleep(2)

    # Create World
    print("Create World")
    world = mosaik.World(sim_config)

    # Start simulatorsfs
    examplesim = world.start('ExampleSim', eid_prefix='Model_')
    examplectrl = world.start('ExampleCtrl')
    collector = world.start('Collector', step_size=60)

    # Instantiate models
    models = [examplesim.ExampleModel(init_val=i) for i in range(-2, 3, 2)]
    agents = examplectrl.Agent.create(len(models))
    monitor = collector.Monitor()

    # Connect entities
    for model, agent in zip(models, agents):
        world.connect(model, agent, ('val', 'val_in'), async_requests=True)

    mosaik.util.connect_many_to_one(world, models, monitor, 'val', 'delta')

    # Run simulation. The collector will test for error free execution.
    world.run(until=end)
