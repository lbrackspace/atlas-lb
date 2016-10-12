
import hashlib
import traceback
import calendar
import operator
import datetime
import string
import errno
import json
import sys
import os
import re


zip_re = re.compile(".*/(access_log_([0-9]+)_([0-9]{10})\.zip)")
lp = None

def printf(format,*args): 
    sys.stdout.write(format%args)


def excuse():
    except_message = traceback.format_exc()
    stack_message = traceback.format_stack()
    return except_message + " " + str(stack_message)


def mkdirs_p(file_path):
    try:
        os.makedirs(file_path)
    except OSError as e:
        if e.errno == errno.EEXIST and os.path.isdir(file_path):
            pass
        else:
            raise


def set_local_file(aid, lid, dt):
    hl = datetime_to_hourlong(dt)
    tail = "access_log_%i_%i.zip" % (lid, hl)
    return os.path.join(cfg.incoming, tail)


def datetime_to_hourlong(dt):
    hl =  dt.year * 1000000 + dt.month * 10000 + dt.day * 100 + dt.hour * 1
    return hl


def test_re(pattern, text):
    pattern_re = re.compile(pattern)
    m = pattern_re.match(text)
    if m:
        return m.groups()
    else:
        return False

def get_formatted_file_date(hl):
    hs = str(hl)
    fd = []
    for i in xrange(0, len(hs)):
        fd.append(hs[i])
        if i == 3 or i == 5 or i == 7:
            fd.append('-')
    fd.append(":00")
    return string.join(fd, "")


def get_month_year(hl):
    month = (hl / 10000) % 100
    year = hl / 1000000
    return "%s_%i" % (calendar.month_abbr[month], year)


def get_container_zip(db, obj):
    (aid, lid, hl, zip_file) = obj
    (aid, foo, name) = db.get_lb_name(lid)
    cnt = get_container_name(lid, name, hl)
    zfn = get_remote_file_name(lid, name, hl)
    return cnt, zfn


def sort_container_zips(czs):
    czs.sort(key=operator.itemgetter('hl', 'aid', 'lid'))


def get_remote_file_name(lid, name, hl):
    dt_str = get_formatted_file_date(hl)
    rfn = "lb_%i_%s_%s.zip" % (lid, name, dt_str)
    return rfn.replace(" ", "_").replace("/", "_")


def get_container_name(lid, name, hl):
    my = get_month_year(hl)
    cnt_name = "lb_%i_%s_%s" % (lid, name, my)
    return cnt_name.replace(" ", "_").replace("/", "-")


def hourlong_to_datetime(hl):
    hour = hl % 100
    hl /= 100
    day = hl % 100
    hl /= 100
    month = hl % 100
    hl /= 100
    year = hl
    dt = datetime.datetime(year, month, day, hour)
    return dt


def datetime_to_formatted_time(dt=None):
    if dt is None:
        dt = datetime.datetime.now()
    year = str(dt.year).zfill(4)
    month = str(dt.month).zfill(2)
    day = str(dt.day).zfill(2)
    hour = str(dt.hour).zfill(2)
    mnt = str(dt.minute).zfill(2)
    sec = str(dt.second).zfill(2)
    ms = str(dt.microsecond/1000).zfill(3)
    return "%s-%s-%s %s:%s:%s.%s" % (year, month, day, hour, mnt, sec, ms)


def md5sum_and_size(file_name, block_size=64*1024):
    md5 = hashlib.md5()
    full_path = os.path.expanduser(file_name)
    with open(full_path, "rb", block_size) as fp:
        total_bytes = 0
        while True:
            data = fp.read(block_size)
            nbytes = len(data)
            total_bytes += nbytes
            if nbytes == 0:
                break
            md5.update(data)
    return md5.hexdigest(), total_bytes


def log(fmt, *args):
    global lp
    lp.write("[%s] " % datetime_to_formatted_time())
    lp.write(fmt % args)
    lp.flush()


class Config(object):
    def __init__(self, conf_file="/etc/cfuploader/cfuploader.json"):
        global lp
        conf = self.load_json(conf_file)
        self.auth_url = conf['auth_url']
        self.auth_user = conf['auth_user']
        self.log_file = conf['log_file']
        self.incoming = conf['incoming']
        self.archive = conf['archive']
        if self.auth_url[-1] == "/":
            self.auth_url = self.auth_url[:-1]
        self.auth_passwd = conf['auth_passwd']
        self.db = conf['db']
        self.n_workers = conf['n_workers']
        if lp is None:
            lp = open(self.log_file, "a")
            log("Log file %s opened", self.log_file)

    def load_json(self, pathIn):
        full_path = os.path.expanduser(pathIn)
        with open(full_path, "r") as fp:
            data = json.loads(fp.read())
        return data

    def save_json(self, pathOut, obj):
        full_path = os.path.expanduser(pathOut)
        with open(full_path,"w") as fp:
            fp.write(json.dumps(obj, indent=2))


def parse_zip_name(zip_path):
    m = zip_re.match(zip_path)
    if m:
        zip_file = m.group(1)
        lid = int(m.group(2))
        hl = int(m.group(3))
        pzn = {"zip_file": zip_file, "lid": lid, "hl": hl,
               "zip_path": zip_path}
        return pzn
    return None


#Can't put dicts in a set so we have to canonicalize a dict to a tuple
#This makes the dictionary hashable at that point
def dict2tup(some_dict):
    return tuple(sorted([(k, v) for (k, v) in some_dict.iteritems()]))


def tup2dict(some_tup):
    return {k: v for (k,v) in some_tup}


def settups2listdicts(set_of_tups):
    list_of_dicts = []
    for tup in set_of_tups:
        d = {k: v for (k,v) in tup}
        list_of_dicts.append(d)
    return list_of_dicts

def excuse():
    except_message = traceback.format_exc()
    stack_message  = string.join(traceback.format_exc(),sep="")
    return "Exception:\n" + except_message + "Stack Frame:\n" + stack_message


cfg = Config()
