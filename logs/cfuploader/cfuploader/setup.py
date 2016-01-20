#!/usr/bin/env python

from distutils.core import setup

setup(name='',
      version='1.0',
      description='Python Distribution Utilities',
      author='Carlos Diablo Garza',
      author_email='gward@python.net',
      url='https://www.python.org/sigs/distutils-sig/',
      packages=['distutils', 'distutils.command'],
      scripts=['scripts/fake_zips', 'scripts/cfuploader_app']
     )