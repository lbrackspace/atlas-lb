
import hashlib
import traceback
import calendar
import operator
import datetime
import string
import errno
import json
import os
import re

zip_re = re.compile(".*/(access_log_([0-9]+)_([0-9]+)_([0-9]{10})\.log.zip)")

lp = None

incoming = "/var/log/zxtm/processed"
archive = "/var/log/zxtm/archive"


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


def set_local_file(aid,lid,dt):
    hl = datetime_to_hourlong(dt)
    tail = "access_log_%i_%i_%i.log.zip" % (aid, lid, hl)
    return os.path.join(incoming, tail)


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


def get_archive_file_path(zip_container):
    (aid, lid, hl, local_file, cnt, remote_file) = zip_container
    file_name = os.path.split(local_file)[1]
    dir_name = os.path.join(archive, str(hl), str(aid))
    return dir_name, file_name


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
    czs.sort(key=operator.itemgetter(2, 0, 1))


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


def md5sum_and_size(file_name,block_size = 64*1024):
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
        if self.auth_url[-1] == "/":
            self.auth_url = self.auth_url[:-1]
        self.auth_passwd = conf['auth_passwd']
        self.db = conf['db']
        self.conf = conf
        if lp is None:
            lp = open(self.log_file, "a")
            log("Log file %s opened", self.log_file)

    def load_json(self, pathIn):
        full_path = os.path.expanduser(pathIn)
        return json.loads(open(full_path, "r").read())

    def save_json(self, pathOut, obj):
        full_path = os.path.expanduser(pathOut)
        open(full_path, "w").write(json.dumps(obj, indent=2))