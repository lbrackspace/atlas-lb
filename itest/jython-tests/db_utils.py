from utils import *
from db import *
from db_classes import *

def newHdfsLzo(hour, finished=True):
    hl = HdfsLzo(hour, finished, nowCal())
    return hl

def newCloudFilesLzo(hour, frag=0, md5=""):
    cl = CloudFilesLzo(hour, frag, md5, nowCal())
    return cl;
