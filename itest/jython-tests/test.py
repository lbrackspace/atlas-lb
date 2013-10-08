#!/usr/bin/env jython

from zxtm import *
from utils import *
from db import *

#getDb()

zt = getZeusTest(hid=1)

obj = """
{"root":
   {"array":[1,2,3,"4"],
    "dict":{"k1":"v1","k2":"v2"}
   }
}
"""

jp = JsonParserUtils.getJsonParser(obj)

n = jp.readValueAsTree()


JsonParserUtils.getChildrenNodeKeys(root)
