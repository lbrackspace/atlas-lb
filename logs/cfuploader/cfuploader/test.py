import unittest
from cfuploader import utils
from cfuploader import clients
from cfuploader import app
import time

def nop():
    pass

class TestZipFinder(unittest.TestCase):
    def test_something(self):
        test_set = set()
        zc = clients.get_container_zips()

