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
        {'eid': 'EM_0', 'type': 'ExampleModel', 'rel': []},
        {'eid': 'EM_1', 'type': 'ExampleModel', 'rel': []},
    ]

    reply = yield message.send(["setup_done", [], {}])
    assert reply is None

    reply = yield message.send(['step', [
        0,
        {'EM_0': {}, 'EM_1': {}},
    ], {}])
    assert reply == 2

    reply = yield message.send(['get_data', [{'EM_0': ['val'],
                                              'EM_1': ['val']}], {}])
    assert reply == {
        'EM_0': {'val': 4},
        'EM_1': {'val': 4},
    }

    reply = yield message.send(['step', [
        2,
        {'EM_0': {'val_in': {'foo': 2}}, 'EM_1': {}},
    ], {}])
    assert reply == 4

    reply = yield message.send(['get_data', [{'EM_0': ['val'],
                                              'EM_1': ['val']}], {}])
    assert reply == {
        'EM_0': {'val': 5},
        'EM_1': {'val': 5},
    }

    try:
        message.send(['stop', [], {}])
    except ConnectionError:
        pass
    print('done')


def test_examplesim(network_client):
    # network is a fixture created by conftest.py and contains information
    # shared across test cases
    addr, env = network_client

    # start Java Simulator as Server in a separate process,
    # which listens on addr_server_test
    if sys.platform == 'win32':
        proc = subprocess.Popen(['examplesim.bat', addr, 'server'],
                                cwd=os.path.dirname(os.path.realpath(__file__))
                                .replace('\\tests', ''))
    else:
        proc = subprocess.Popen(['./examplesim.sh', addr, 'server'])

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

    # start mosaik_mock and connect it to addr_server_test
    mosaik = env.process(mosaik_mock(env, client_sock))

    # start the simulation
    env.run(until=mosaik)

    proc.wait()
    client_sock.close()


if __name__ == '__main__':
    pytest.main(__file__)
