from multiprocessing import Pool

import requests
import json
import traceback


class Worker():
    def __init__(self, apicli):
        self.result_list = []
        self.apicli = apicli