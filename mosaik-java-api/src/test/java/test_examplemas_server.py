import subprocess
import pytest
import os
import sys
import time

from simpy.io import select as backend
from simpy.io.packet import PacketUTF8 as Packet
from simpy.io.message import Message


def mosaik_mock(env, client_sock):
    """This functions pretends to be mosaik."""
    message = Message(env, Packet(client_sock))

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


def test_examplemas(network_client):
    # network is a fixture created by conftest.py and contains information
    # shared across test cases
    addr, env = network_client

    # start Java Simulator as Server in a separate process,
    # which listens on addr_server_test
    if sys.platform == 'win32':
        proc = subprocess.Popen(['examplemas.bat', addr, 'server'],
                                cwd=os.path.dirname(os.path.realpath(__file__))
                                .replace('\\tests', ''))
    else:
        proc = subprocess.Popen(['./examplemas.sh', addr, 'server'])

    addr_server_test_tuple = addr.split(':')
    addr_server_test_tuple = (addr_server_test_tuple[0],
                              int(addr_server_test_tuple[1]))

    # wait for Java process
    attempts = 0
    client_sock = None
    while attempts < 10:
        try:
            client_sock = backend.TCPSocket.connection(env,
                                                       addr_server_test_tuple)
            attempts = 10
        except ConnectionRefusedError:
            time.sleep(1)
            attempts += 1

    if client_sock is None:
        raise ConnectionRefusedError

    # start mosaik_mock. which listens on addr
    mosaik = env.process(mosaik_mock(env, client_sock))

    # start the simulation
    env.run(until=mosaik)

    proc.wait()
    client_sock.close()


if __name__ == '__main__':
    pytest.main(__file__)
