#!/usr/bin/env python

import os
from setuptools import setup


def read(fname):
    file_path = os.path.join(os.path.dirname(__file__), fname)
    return open(file_path, "r").read()

setup(name='cfuploader',
      version='0.0.1',
      author='Carlos D. Garza',
      author_email='carlos.garza@rackspace.com',
      description='Uploads customer logs to Cloud Files for Rackspace Hosting',
      packages=['cfuploader'],
      long_description=read('readme.txt'),
      install_requires=read('requirements.txt').split("\n"),
      scripts=['scripts/cfuploader_app', 'scripts/fake_zips']
      )
