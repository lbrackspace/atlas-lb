
from utils import *
import sys

cfg = CloudFilesConfig.readJsonConfig("~/cloudFiles.json")
cfu = CloudFilesUtils(cfg)
cfu.getAuthToken()



getContainerSizes(cfu)


cfu.listContainer("lb_224687_test_Dec_2014")


deleteAllContainers(cfu)

cfu.createContainer("test").getEntity()

sc = segmentFile("~/test.dat")

l = writeSegmentContainer(cfu,"test",sc)

