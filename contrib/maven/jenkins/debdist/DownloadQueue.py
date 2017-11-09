# This file is part of DebDist.
#
# DebDist is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
#
# DebDist is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU General Public License for more details.
#
# You should have received a copy of the GNU General Public License
# along with DebDist.  If not, see <http://www.gnu.org/licenses/>.

import hashlib
import os
import signal
import subprocess
import Queue
import sys

import requests


class DownloadQueue():
    def __init__(self, run_flag, queue, deb_path, script_path):
        self.RUN = run_flag
        self.queue = queue
        self.deb_path = deb_path
        self.script_path = script_path
        self.process = False

    def run(self):
        s = signal.signal(signal.SIGINT, signal.SIG_IGN)

        while self.RUN.value:
            try:
                deb = self.queue.get(block=True, timeout=1)
                if deb:
                    self.download_deb(deb)
                    self.process = True
            except Queue.Empty:
                if self.process:
                    self.process_debs()
        signal.signal(signal.SIGINT, s)

    def download_deb(self, deb):
        local_filename = os.path.join(self.deb_path,
                                      deb['url'].split('/')[-1])
        r = requests.get(deb['url'], stream=True)
        with open(local_filename, 'wb') as f:
            hash = hashlib.md5()
            for chunk in r.iter_content(chunk_size=4096):
                if chunk:
                    f.write(chunk)
                    f.flush()
                    hash.update(chunk)
            if hash.hexdigest() == deb['md5']:
                print("md5sum %s matches for %s" %
                      (hash.hexdigest(), local_filename))
            else:
                print("md5sum %s failed for %s" %
                      (hash.hexdigest(), local_filename))

        return local_filename

    def process_debs(self):
        #print("Run process.sh here.")
        #subprocess.call([self.script_path, "prod"])
        self.process = False
