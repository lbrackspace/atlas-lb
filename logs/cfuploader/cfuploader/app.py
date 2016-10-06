#!/usr/bin/env python

from multiprocessing import Process
from cfuploader import utils
from cfuploader import clients
import random
import logging
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


def worker_process(zip_container, expected_md5, fsize):
    aid = zip_container['aid']
    lid = zip_container['lid']
    hl = zip_container['hl']
    zip_file = zip_container['zip_file']

    local_file = zip_container['zip_path']
    cnt = zip_container['cnt']
    remote_file = zip_container['remote_zf']
    auth = clients.Auth()
    auth.get_admin_token()  #Todo: Need to try to avoid regrabbing token repeatedly
    utils.log("fetching token for anf client for %d:%d\n", aid, lid)
    t = auth.get_token_and_endpoint(aid)
    cf = clients.CloudFiles(t)
    utils.log("using token '%s' for aid '%d' for cloud files client\n", t, aid)
    utils.log("try sending file %s -> %s: %s\n", local_file, cnt, remote_file)
    utils.log("creating container '%s'\n", cnt)
    cf.create_container(cnt)
    act_md5 = cf.upload_file(local_file, cnt, remote_file)
    utils.log("SUCCESS sending %s-> %s: %s\n", local_file, cnt, remote_file)
    if act_md5 == expected_md5:
        dir_path = os.path.join(cfg.archive, str(hl)[:8], str(aid))
        utils.mkdirs_p(dir_path)
        archive_path = os.path.join(dir_path, zip_file)
        utils.log("moved %s to %s\n", local_file, archive_path)
        os.rename(local_file, archive_path)
        utils.log("moved %s to %s\n", local_file, archive_path)
    else:
        utils.log("checksome failed for file %s\n", local_file, cnt)


def worker_thread(zip_container):
    #(aid, lid, hl, src_file, cnt, dst_file) = zip_container
    aid = zip_container['aid']
    lid = zip_container['lid']
    hl = zip_container['hl']
    src_file = zip_container['zip_path']
    utils.log("worker thread %s\n", zip_container)
    try:
        (md5, fsize) = utils.md5sum_and_size(src_file, block_size=1024*1024)
        utils.log("%s: %s %i uploading now\n", src_file, md5, fsize)
        p = Process(target=worker_process, args=(zip_container, md5, fsize))
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
        self.timestamp = time.time() - 60.0
        logging.basicConfig(level=logging.DEBUG)

    def start_worker(self, zip_container):
        th = threading.Thread(target=worker_thread, args=(zip_container,))
        th.setDaemon(True)
        utils.log("starting thread %s\n", zip_container)
        th.start()

    def main_loop(self):
        while True:
            time.sleep(1.0)
            now = time.time() #Run the queue drainer every second
            if self.timestamp + 60.0 < now: # but only run the scanner each minute
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
                zc_tup = utils.dict2tup(zc)
                all_files.discard(zc_tup)
                upload_files.discard(zc_tup)
                utils.log("removing %s from upload status\n", zc['zip_path'])
            nready = self.n_workers - len(upload_files)
            sendable_files = utils.settups2listdicts(all_files - upload_files)
            nfiles = len(sendable_files)
            if nfiles > 0:
                utils.log(
                    "%i files are currently sendable. Scheduling for send\n",
                    len(sendable_files))
            #utils.sort_container_zips(sendable_files)
            random.shuffle(sendable_files)
            while len(sendable_files) > 0 and nready > 0:
                zcd = sendable_files.pop()
                utils.log("spawning thread to send %s nread = %i\n",
                          zcd, nready)
                nready -= 1
                upload_files.add(utils.dict2tup(zcd))
                self.start_worker(zcd)
            l.release()
