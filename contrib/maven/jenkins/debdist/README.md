# DebDist Installation and Usage

### Required Additional Files
server.crt = Certificate File

server.key = Private Key for Certificate

dev.cfg = Config file (TODO: need to rename this)

### Installation
```
virtualenv .venv
. .venv/bin/activate
pip install -r requires.txt
```

Copy config.cfg.example to dev.cfg, and edit it to contain the appropriate paths and authentication data.

Generate server.key and server.crt and put them in the project directory.

### Usage
On the server (destination):
```
python DebDistServer.py
```

On the client (main repository and web-host):
```
python DebDistClient.py
```


Publish icon source: http://openclipart.org/detail/13847/login-mini-icon-by-artmaster
