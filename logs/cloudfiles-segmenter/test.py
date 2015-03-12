
from utils import *

cfg = CloudFilesConfig.readJsonConfig("~/cloudFiles.json")
cfu = CloudFilesUtils(cfg)
resp = cfu.getAuthToken()
resp = cfu.listContainers()

resp = cfu.listContainer("lb_254769_test_Mar_2015")
