#!/usr/bin/env jython

import org.openstack.atlas.util.crypto.HashUtil as HashUtil
import org.openstack.atlas.service.domain.services.helpers.RootCAHelper
import utils
from zxtm import *
from utils import *
from db import *

RootCAHelper = org.openstack.atlas.service.domain.services.helpers.RootCAHelper

RootCAHelper.reloadCAs()

