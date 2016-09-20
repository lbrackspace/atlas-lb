#!/usr/bin/env python

from multiprocessing import Process
from cfuploader import utils
from cfuploader import clients
import threading
import thread
import Queue
import time
import sys
import os

import pdb

l = thread.allocate_lock()
q = Queue.Queue()

all_files = set()
upload_files = set()

cfg = utils.cfg


def worker_process(zip_container):
    aid = zip_container['aid']
    lid = zip_container['lid']
    hl = zip_container['hl']
    zip_file = zip_container['zip_file']

    local_file = zip_container['zip_path']
    cnt = zip_container['cnt']
    remote_file = zip_container['remote_zf']
    auth = clients.Auth()
    auth.get_admin_token()
    t = auth.get_token_and_endpoint(aid)
    cf = clients.CloudFiles(t)
    utils.log("using token '%s' for aid '%d' for cloud files client\n", t, aid)
    utils.log("sending file %s -> %s: %s\n", local_file, cnt, remote_file)
    utils.log("creating container '%s'\n", cnt)
    cf.create_container(cnt)
    act_md5 = cf.upload_file(local_file, cnt, remote_file)
    if act_md5 == zip_container['md5']:
        dir_path = os.path.join(cfg.archive, str(hl)[:8], str(aid))
        utils.mkdirs_p(dir_path)
        archive_path = os.path.join(dir_path, zip_file)
        os.rename(local_file, archive_path)


def worker_thread(zip_container):
    #(aid, lid, hl, src_file, cnt, dst_file) = zip_container
    aid = zip_container['aid']
    lid = zip_container['lid']
    hl = zip_container['hl']
    src_file = zip_container['zip_path']
    utils.log("worker thread %s\n", zip_container)
    try:
        (md5, fsize) = utils.md5sum_and_size(src_file, block_size=1024*1024)
        zip_container['md5'] = md5
        zip_container['fsize'] = fsize
        utils.log("%s: %s %i uploading now\n", src_file, md5, fsize)
        p = Process(target=worker_process, args=(zip_container,))
        p.start()
        p.join()
        utils.log("finished sending %i %7i %7i log file.\n", hl, aid, lid)
    except Exception:
        utils.log("error sending log file %i %7i %7i: %s\n", hl, aid, lid,
                  utils.excuse())
    l.acquire()
    q.put(zip_container)
    l.release()


class Uploader(object):
    n_workers = cfg.n_workers

    def __init__(self):
        self.timestamp = time.time() - 120.00

    def start_worker(self, zip_container):
        th = threading.Thread(target=worker_thread, args=(zip_container,))
        th.setDaemon(True)
        utils.log("starting thread %s\n", zip_container)
        th.start()

    def main_loop(self):
        while True:
            time.sleep(0.25)
            now = time.time()
            if self.timestamp + 120.0 < now:
                utils.log("scanning zips\n")
                try:
                    zip_containers = clients.get_container_zips()
                    utils.log("zips scanner found %d zips\n",
                              len(zip_containers))
                    l.acquire()
                    for zc in zip_containers:
                        if utils.dict2tup(zc) not in upload_files:
                            all_files.add(utils.dict2tup(zc))
                    l.release()
                except Exception:
                    utils.log("Error scanning zip directory: %s\n",
                              utils.excuse())
                    try:
                        l.release()
                    except Exception:
                        utils.log("Warning lock was already released. ")
                        pass  # If the locks not even being held ignore it
                self.timestamp = time.time()
            #Drain Queue
            l.acquire()
            while not q.empty():
                zc = q.get()
                all_files.discard(utils.dict2tup(zc))
                upload_files.discard(utils.dict2tup(zc))
                utils.log("removing %s from upload status\n", zc['zip_path'])
                pdb.set_trace()
            nready = self.n_workers - len(upload_files)
            sendable_files = utils.settups2listdicts(all_files - upload_files)
            nfiles = len(sendable_files)
            if nfiles > 0:
                utils.log(
                    "%i files are currently sendable. Scheduling for send\n",
                    len(sendable_files))
            utils.sort_container_zips(sendable_files)
            while len(sendable_files) > 0 and nready > 0:
                zcd = sendable_files.pop(0)
                utils.log("spawning thread to send %s nread = %i\n",
                          zcd, nready)
                nready -= 1
                upload_files.add(utils.dict2tup(zcd))
                self.start_worker(zcd)
            l.release()