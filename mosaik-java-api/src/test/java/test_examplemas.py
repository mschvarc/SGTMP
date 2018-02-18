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

    reply = yield message.send(['init', ['ExampleMAS-0'], {'step_size': 2}])
    assert reply == {
        'api_version': '2.2',
        'models': {
            'ExampleAgent': {
                'public': True,
                'params': [],
                'attrs': [],
            },
        },
    }

    reply = yield message.send(['create', [1, 'ExampleAgent'], {}])
    assert reply == [
        {'eid': 'EA_0', 'type': 'ExampleAgent', 'rel': []},
    ]

    reply = yield message.send(["setup_done", [], {}])
    assert reply is None

    # Make step request
    step_req = message.send(['step', [0, {}], {}])

    # Handle async. requests
    request = yield message.recv()
    assert request.content == ['get_progress', [], {}]
    request.succeed(3.14)

    request = yield message.recv()
    assert request.content == ['get_related_entities', [], {}]
    request.succeed({
        'nodes': {
            'sid_0/eid_0': {'type': 'A'},
            'sid_0/eid_1': {'type': 'B'},
        },
        'edges': [
            ['sid_0/eid_0', 'sid_1/eid0', {}],
        ],
    })

    request = yield message.recv()
    assert request.content == ['get_related_entities', ['eid0'], {}]
    request.succeed({'sid_0/eid_0': {'type': 'A'}})

    request = yield message.recv()
    assert request.content == ['get_related_entities', [['eid0', 'eid1']], {}]
    request.succeed({'sid_0/eid_0': {'sid_0/eid_1': {'type': 'B'}}})

    request = yield message.recv()
    assert request.content == ['get_data', [{'eid0': ['a']}], {}]
    request.succeed({'eid0': {'a': 42}})

    request = yield message.recv()
    assert request.content == ['set_data', [{'EA_0': {'eid0': {'a': 42}}}], {}]
    request.succeed()

    # Wait for step to finish
    reply = yield step_req
    assert reply == 2

    try:
        message.send(['stop', [], {}])
    except ConnectionError:
        pass
    print('done')


def test_examplemas(network):
    # network is a fixture created by conftest.py and contains information
    # shared across test cases
    addr, env, server_sock = network

    # start mosaik_mock. which listens on addr
    mosaik = env.process(mosaik_mock(env, server_sock))

    # start Java simulator, which connects to addr
    if sys.platform == 'win32':
        proc = subprocess.Popen(['examplemas.bat', addr],
                                cwd=os.path.dirname(os.path.realpath(__file__))
                                .replace('\\tests', ''))
    else:
        proc = subprocess.Popen(['./examplemas.sh', addr])

    # start the simulation
    env.run(until=mosaik)

    proc.wait()


if __name__ == '__main__':
    pytest.main(__file__)
