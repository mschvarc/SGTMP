import subprocess
import pytest
import os
import sys

from simpy.io.packet import PacketUTF8 as Packet
from simpy.io.message import Message


def mosaik_mock(env, server_sock):
    """This functions pretends to be mosaik."""
    sock = yield server_sock.accept()
    message = Message(env, Packet(sock))

    reply = yield message.send(['init', ['ExampleSim-0'], {'step_size': 2}])
    assert reply == {
        'api_version': '2.2',
        'models': {
            'ExampleModel': {
                'public': True,
                'params': ['init_val'],
                'attrs': ['val', 'delta'],
            },
        },
    }

    reply = yield message.send(['create', [2, 'ExampleModel'], {
        'init_val': 3}])
    assert reply == [
        {'eid': 'ExampleSim-0_ExampleModel_0',
         'type': 'ExampleModel', 'rel': []},
        {'eid': 'ExampleSim-0_ExampleModel_1',
         'type': 'ExampleModel', 'rel': []},
    ]

    reply = yield message.send(["setup_done", [], {}])
    assert reply is None

    reply = yield message.send(['step', [
        0,
        {'ExampleSim-0_ExampleModel_0': {}, 'ExampleSim-0_ExampleModel_1': {}},
    ], {}])
    assert reply == 2

    reply = yield message.send(['get_data',
                                [{'ExampleSim-0_ExampleModel_0': ['val'],
                                    'ExampleSim-0_ExampleModel_1': ['val']}],
                                {}])
    assert reply == {
        'ExampleSim-0_ExampleModel_0': {'val': 4},
        'ExampleSim-0_ExampleModel_1': {'val': 4},
    }

    reply = yield message.send(['step', [
        2,
        {'ExampleSim-0_ExampleModel_0': {'val_in': {'foo': 2}},
         'ExampleSim-0_ExampleModel_1': {}},
    ], {}])
    assert reply == 4

    reply = yield message.send(['get_data',
                                [{'ExampleSim-0_ExampleModel_0': ['val'],
                                    'ExampleSim-0_ExampleModel_1': ['val']}],
                                {}])
    assert reply == {
        'ExampleSim-0_ExampleModel_0': {'val': 5},
        'ExampleSim-0_ExampleModel_1': {'val': 5},
    }

    try:
        message.send(['stop', [], {}])
    except ConnectionError:
        pass
    print('done')


def test_examplesim(network):
    # network is a fixture created by conftest.py and contains information
    # shared across test cases
    addr, env, server_sock = network

    # start mosaik_mock. which listens on addr
    mosaik = env.process(mosaik_mock(env, server_sock))

    # start Java simulator, which connects to addr
    if sys.platform == 'win32':
        proc = subprocess.Popen(['exampleutilsim.bat', addr],
                                cwd=os.path.dirname(os.path.realpath(__file__))
                                .replace('\\tests', ''))
    else:
        proc = subprocess.Popen(['./exampleutilsim.sh', addr])

    # start the simulation
    env.run(until=mosaik)

    proc.wait()


if __name__ == '__main__':
    pytest.main(__file__)
