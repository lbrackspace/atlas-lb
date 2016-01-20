#!/usr/bin/env python

from multiprocessing import Process
from cfuploader import utils
from cfuploader import clients
import threading
import thread
import Queue
import time
import os

l = thread.allocate_lock()
q = Queue.Queue()

all_files = set()
upload_files = set()

cfg = utils.Config()


def worker_process(zip_container, exp_md5):
    (aid, lid, hl, local_file, cnt, remote_file) = zip_container
    auth = clients.Auth()
    auth.get_admin_token()
    t = auth.get_token_and_endpoint(aid)
    cf = clients.CloudFiles(t)
    utils.log("using token '%s' for aid '%d' for cloud files client\n", t, aid)
    utils.log("creating container '%s'\n", cnt)
    cf.create_container(cnt)
    act_md5 = cf.upload_file(local_file, cnt, remote_file)
    if act_md5 == exp_md5:
        (archive_dir, archive_file) = utils.get_archive_file_path(
            zip_container)
        utils.mkdirs_p(archive_dir)
        archive_path = os.path.join(archive_dir, archive_file)
        os.rename(local_file, archive_path)


def worker_thread(zip_container):
    (aid, lid, hl, src_file, cnt, dst_file) = zip_container
    utils.log("worker thread %s\n", zip_container)
    try:
        (md5, fsize) = utils.md5sum_and_size(src_file, block_size=1024*1024)
        utils.log("%s: %s %i uploading now\n", src_file, md5, fsize)
        p = Process(target=worker_process, args=(zip_container, md5))
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
    n_workers = cfg.conf['n_workers']

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
                        assert len(zc) == 6
                        if zc not in upload_files:
                            all_files.add(zc)
                    l.release()
                except Exception:
                    utils.log("Error scanning zip directory: %s\n",
                              utils.excuse())
                    try:
                        l.release()
                    except:
                        utils.log("Warning lock was already released. ")
                        pass  # If the locks not even being held ignore it
                self.timestamp = time.time()
            #Drain Queue
            l.acquire()
            while not q.empty():
                zc = q.get()
                all_files.discard(zc)
                upload_files.discard(zc)
                utils.log("removing %s from upload status\n", zc[3])
            nready = self.n_workers - len(upload_files)
            sendable_files = list(all_files - upload_files)
            nfiles = len(sendable_files)
            if nfiles > 0:
                utils.log(
                    "%i files are currently sendable. Scheduling for send\n",
                    len(sendable_files))
            utils.sort_container_zips(sendable_files)
            while len(sendable_files) > 0 and nready > 0:
                zc = sendable_files.pop(0) # Grab the file at the front
                utils.log("spawning thread to send %s nread = %i\n",
                          zc, nready)
                nready -= 1
                upload_files.add(zc)
                self.start_worker(zc)
            l.release()