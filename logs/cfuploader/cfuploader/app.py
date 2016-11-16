#!/usr/bin/env python

from multiprocessing.dummy import Pool #This is a ThreadPool not Process Pool
from cfuploader import utils
from cfuploader import clients
import threading
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

all_files = set()
upload_files = set()

cfg = utils.cfg

printf = utils.printf

def thread_worker(zip_container, expected_md5, fsize, auth):
    aid = zip_container['aid']
    lid = zip_container['lid']
    hl = zip_container['hl']
    zip_file = zip_container['zip_file']

    local_file = zip_container['zip_path']
    cnt = zip_container['cnt']
    remote_file = zip_container['remote_zf']
    auth.get_admin_token()
    utils.log("fetching token for anf client for %d:%d\n", aid, lid)
    try:
        t = auth.get_token_and_endpoint(aid)
    except:
        f = "ERROR getting token and endpoint for aid %i for zip %s\n"
        utils.log(f, aid, zip_container)
        raise
    cf = clients.CloudFiles(t)
    utils.log("using token '%s' for aid '%d' for cloud files client\n", t, aid)
    utils.log("try sending file %s -> %s: %s\n", local_file, cnt, remote_file)
    utils.log("creating container '%s'\n", cnt)
    try:
         cf.create_container(cnt)
    except:
         f = "ERROR creating container %s for %s with endpoint & token %s\n"
         utils.log(f, cnt, zip_container, t)
         raise
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


# This is just to wrap the child thread in an exception handler
# This way if a child throws an exception it won't bubble up and
# kill the parent
def upload_worker(q):
    while True:
        (zip_container, expected_md5, fsize, auth) = q.get()
        try:
            thread_worker(zip_container, expected_md5, fsize, auth)
        except:
            utils.log("Exception caught sending zip file %s excuse is %s\n",
                       zip_container, utils.excuse())
        finally:
            q.task_done()


class Uploader(object):
    def __init__(self):
        self.q = Queue.Queue()
        self.auth = clients.Auth()
        self.n_workers = cfg.n_workers
        # logging.basicConfig(level=logging.DEBUG)

    def main_loop(self):
        # spawn off self.n_worker threads
        for i in xrange(self.n_workers):
            t = threading.Thread(target=upload_worker,args=(self.q,))
            t.setDaemon(True)
            t.start()

        while True:
            self.auth.clear_cache() #Lets go ahead and clear cache every batch
            # Grab all the files that are in /processed
            zip_container_list = clients.get_container_zips()
            utils.log("FOUND %i zips to upload\n", len(zip_container_list))
            # Build the arguments for the ThreadPool mapper
            for zcl in zip_container_list:
                try:
                    (md5, fsize) = utils.md5sum_and_size(zcl['zip_path'])
                    # Schedule file for upload in Queue
                    self.q.put([zcl, md5, fsize, self.auth])
                except:
                    utils.log("Error grabbing md5sum and size for %s\n", zcl)
                    utils.log("Excuse is %s\n", utils.excuse())
                    continue
            self.q.join()
            utils.log("==================================================\n")
            utils.log("FINISHED batch cache performance was is %s\n",
                      self.auth.get_counts())
            time.sleep(60.0)

