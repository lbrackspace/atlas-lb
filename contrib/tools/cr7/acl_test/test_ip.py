#!/usr/bin/env python

import traceback
import threading
import operator
import datetime
import socket
import thread
import Queue
import time
import json
import sys
import os

q = Queue.Queue()

def printf(format,*args): sys.stdout.write(format%args)

def fprintf(fp,format,*args): fp.write(format%args)

def load_json(pathIn):
    return json.loads(open(os.path.expanduser(pathIn),"r").read())

def save_json(pathOut,obj):
    open(os.path.expanduser(pathOut),"w").write(json.dumps(obj,indent=2))

def load_csv_lids(file_path):
    lids = []
    data = open(os.path.expanduser(file_path),"r").read()
    for line in data.split("\r")[1:]:
        cols = line.split(",")
        if len(cols) >= 2:
            lid = cols[1].strip()
            lids.append(int(lid))
    lids.sort()
    return lids

def eclose(obj): #not sure if sockets trigger exception if already closed
    try:
        obj.close()
    except KeyboardInterrupt:
        sys.exit()
    except:
        pass

def test_connect(args):
    (node, i) = (args[0], args[1])
    update(node)
    ip = node['ip_address']
    port = node['port']
    aid = node['aid']
    lid = node['lid']
    printf("starting %i\n", i)
    sys.stdout.flush()
    if node['protocol'] not in tcp_prots:
        q.put((aid,lid,ip,"UNKNOWN NOT TCP"))
        printf("%i done\n",i)
        return
    try:
        s = socket.create_connection((ip,port),timeout=1.0)
    except KeyboardInterrupt:
        sys.exit()
    except:
        q.put((aid,lid,ip,"DOWN"))
        printf("done %i\n",i)
        return
    q.put((aid,lid,ip,"UP"))
    printf("done %i\n", i)
    return

class ThreadPool(object):
    def __init__(self, numThreads):
        self.__threads = []
        self.__resizeLock = threading.Condition(threading.Lock())
        self.__taskLock = threading.Condition(threading.Lock())
        self.__tasks = []
        self.__isJoining = False
        self.setThreadCount(numThreads)

    def setThreadCount(self, newNumThreads):
        # Can't change the thread count if we're shutting down the pool!
        if self.__isJoining:
            return False
        
        self.__resizeLock.acquire()
        try:
            self.__setThreadCountNolock(newNumThreads)
        finally:
            self.__resizeLock.release()
        return True

    def __setThreadCountNolock(self, newNumThreads):
        # If we need to grow the pool, do so
        while newNumThreads > len(self.__threads):
            newThread = ThreadPoolThread(self)
            self.__threads.append(newThread)
            newThread.start()
        # If we need to shrink the pool, do so
        while newNumThreads < len(self.__threads):
            self.__threads[0].goAway()
            del self.__threads[0]

    def getThreadCount(self):
        self.__resizeLock.acquire()
        try:
            return len(self.__threads)
        finally:
            self.__resizeLock.release()

    def queueTask(self, task, args=None, taskCallback=None):
        if self.__isJoining == True:
            return False
        if not callable(task):
            return False
        
        self.__taskLock.acquire()
        try:
            self.__tasks.append((task, args, taskCallback))
            return True
        finally:
            self.__taskLock.release()

    def getNextTask(self):
        self.__taskLock.acquire()
        try:
            if self.__tasks == []:
                return (None, None, None)
            else:
                return self.__tasks.pop(0)
        finally:
            self.__taskLock.release()
    
    def joinAll(self, waitForTasks = True, waitForThreads = True):
        # Mark the pool as joining to prevent any more task queueing
        self.__isJoining = True
        # Wait for tasks to finish
        if waitForTasks:
            while self.__tasks != []:
                time.sleep(.25)
        # Tell all the threads to quit
        self.__resizeLock.acquire()
        try:
            self.__setThreadCountNolock(0)
            self.__isJoining = True

            # Wait until all threads have exited
            if waitForThreads:
                for t in self.__threads:
                    t.join()
                    del t
            # Reset the pool for potential reuse
            self.__isJoining = False
        finally:
            self.__resizeLock.release()

        
class ThreadPoolThread(threading.Thread):
    threadSleepTime = 1.0
    def __init__(self, pool):
        threading.Thread.__init__(self)
        self.__pool = pool
        self.__isDying = False
        
    def run(self):
        while self.__isDying == False:
            cmd, args, callback = self.__pool.getNextTask()
            # If there's nothing to do, just sleep a bit
            if cmd is None:
                time.sleep(ThreadPoolThread.threadSleepTime)
            elif callback is None:
                cmd(args)
            else:
                callback(cmd(args))
    
    def goAway(self):
        self.__isDying = True

result_file_name = sys.argv[1]


printf("reading json conf\n")

ips = load_json("~/cr7/ips.json")
printf("computing nodes\n")
tcp_prots = ['IMAPS','FTP','IMAPv3','HTTP','IMAPv4','DNS_TCP',
'LDAP','SMTP','TCP','LDAPS','POP3','TCP_CLIENT_FIRST','HTTPS','MYSQL'
'SFTP','TCP_STREAM','POP3S']

progress_fp = open("./cr7/progress.txt","w")

nchecked = 0
lock = thread.allocate_lock()

def update(data):
    global progress_fp
    global lock
    global nchecked
    nchecked += 1
    lock.acquire()
    try:
        progress_fp.write("%s\n"%(data,))
        progress_fp.flush()
    except:
        pass
    try:
        lock.release()
    except:
        pass
    
        

nodes = {}
node_list = []
for ip in ips:
    cid = ip['cid']
    lid = ip['lid']
    if ip['loadbalancer_status'] == "DELETED":
        continue
    if cid not in [1,2,3,4,8,9,10,11]:
        continue
    if lid not in nodes:
        nodes[lid] = []
    nodes[lid].append(ip)
    node_list.append(ip)

p = ThreadPool(50)
printf("spawning connect threads\n")
i = 0
n = len(node_list)
for node in node_list:
        printf("%i of %i nodes issuing\n", i, n)
        p.queueTask(test_connect,(node,i),None)
        time.sleep(0.1)
        i += 1

printf("joinng all Threads\n")
p.joinAll()
printf("joined\n")
time.sleep(5.0)
printf("qSize = %i\n", q.qsize())
time.sleep(5.00)
results = []
while not q.empty():
    results.append(q.get())

results.sort()


fp = open(result_file_name,"w")
for e in results:
    fprintf(fp,"%i,%i,%s,%s\n", e[0],e[1],e[2],e[3])
fp.close()
