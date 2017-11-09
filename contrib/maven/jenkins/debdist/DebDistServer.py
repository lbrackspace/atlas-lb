#!/usr/bin/env python
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

import ConfigParser
import multiprocessing
import optparse

import flask
import OpenSSL

import DownloadQueue


app = flask.Flask(__name__)


class DebDistServer():
    def __init__(self, config_file):
        config = ConfigParser.SafeConfigParser()
        config.read(config_file)
        self.deb_path = config.get('server', 'deb_path')
        self.script_path = config.get('server', 'script_path')
        auth_token = config.get('auth', 'token')
        self.accept_tokens = [auth_token]
        self.host = config.get('server', 'host')
        self.port = config.getint('server', 'port')
        self.ssl_context = OpenSSL.SSL.Context(OpenSSL.SSL.SSLv23_METHOD)
        self.ssl_context.use_privatekey_file('server.key')
        self.ssl_context.use_certificate_file('server.crt')
        self.debug = config.get('server', 'debug')
        self.download_queue = multiprocessing.Queue(100)
        self.run_flag = multiprocessing.Value('b', True)
        downloader = DownloadQueue.DownloadQueue(self.run_flag,
                                                 self.download_queue,
                                                 self.deb_path,
                                                 self.script_path)
        self.downloader_process = multiprocessing.Process(target=downloader.run)
        app.config.from_object('config')
        app.secret_key = auth_token

    def validate_auth(self, headers):
        if ('X_AUTH_TOKEN' in headers and
                    headers['X_AUTH_TOKEN'] in self.accept_tokens):
            return True
        return False

    def download(self, deb):
        self.download_queue.put(deb)

    def run(self):
        self.downloader_process.start()
        try:
            app.run(debug=self.debug, host=self.host, port=self.port,
                    ssl_context=self.ssl_context)
            self.run_flag.value = False
            self.downloader_process.join()
        except KeyboardInterrupt:
            try:
                self.run_flag.value = False
                self.downloader_process.join()
            except KeyboardInterrupt:
                while True:
                    self.downloader_process.terminate()
                    break
        print("Exiting...")


@app.route('/fetch', methods=['POST'])
def fetch_debs():
    if not app.serverObject.validate_auth(flask.request.headers):
        flask.abort(401)
    if not flask.request.json or not 'debs' in flask.request.json:
        flask.abort(400)
    debList = flask.request.json['debs']
    for deb in debList:
        app.serverObject.download(deb)
    return flask.make_response("OK", 200)


if __name__ == '__main__':
    parser = optparse.OptionParser()
    parser.add_option("-c", "--config", dest="config",
                      help="Path to config file")
    options, args = parser.parse_args()
    config = options.config if options.config else "dev.cfg"
    server = DebDistServer(config)
    app.serverObject = server
    server.run()
