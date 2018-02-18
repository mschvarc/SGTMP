import mosaik
import mosaik.scheduler
import mosaik.util


def test_integration(sim_config_data):
    # sim_config_data is a fixture created by conftest.py and contains
    # information shared across test cases

    sim_config, end = sim_config_data

    # Create World
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

    # Run simulation The collector will test for error free execution.
    world.run(until=end)
