from simpy.io import select as backend
import pytest
import sys
import os


ADDR = 'localhost:5555'
ADDR_SERVER_TEST = 'localhost:5556'
ADDR_SERVER_TEST_INTEGRATION = 'localhost:5557'


@pytest.yield_fixture
def network():
    env = backend.Environment()
    addr = ADDR.split(':')
    addr = (addr[0], int(addr[1]))
    server_sock = backend.TCPSocket.server(env, addr)
    yield ADDR, env, server_sock
    server_sock.close()


@pytest.yield_fixture
def network_client():
    env = backend.Environment()
    yield ADDR_SERVER_TEST, env


@pytest.yield_fixture
def sim_config_data():
    if sys.platform == 'win32':
        java_sim_path = {'cmd': 'examplesim.bat %(addr)s',
                         'cwd': os.path.dirname(os.path.realpath(__file__))
                                .replace('\\tests', '')}
    else:
        java_sim_path = {'cmd': './examplesim.sh %(addr)s'}

    sim_config = {
        'ExampleSim': java_sim_path,
        'ExampleCtrl': {
            'python': 'controller:Controller',
        },
        'Collector': {
            'python': 'collector:Collector',
        },
    }
    end = 10 * 60  # 10 minutes
    yield sim_config, end


@pytest.yield_fixture
def sim_config_data_client():

    sim_config = {
        'ExampleSim': {
            'connect': ADDR_SERVER_TEST_INTEGRATION
        },
        'ExampleCtrl': {
            'python': 'controller:Controller',
        },
        'Collector': {
            'python': 'collector:Collector',
        },
    }
    end = 10 * 60  # 10 minutes
    yield sim_config, end, ADDR_SERVER_TEST_INTEGRATION
