#!/usr/bin/env python

import sys, os, time
import requests
import base64
import time
import cfupload
import datetime
import ConfigParser
import sys

config = ConfigParser.ConfigParser()
config.read(os.path.expanduser('~/cfupconfig'))

deleted_containers = set()

import pyrax
import pyrax.exceptions as exc
import traceback

pyrax.set_setting("identity_type", "rackspace")
pyrax.set_http_debug(True)

def getUser(acctId):
    username = config.get('general', 'b_user')
    password = config.get('general', 'b_pass')
    endpoint = config.get('general', 'mossoauth')
    base64string = base64.encodestring('%s:%s' % (username, password)).replace('\n', '')

    headers = {'content-type': 'application/json', 'Authorization': 'Basic ' + base64string}
    try:
       r = requests.get(format('%s%s' % (endpoint, acctId)), headers=headers)
    except Exception, e:
      print 'Exception retrieving user information', e
      return

    userjson = r.json()
    if r.status_code == 200 and  'user' in userjson:
       return userjson['user']['id'], userjson['user']['key']
    else:
       print 'Error gather 1.1 auth user: STATUS CODE: %s REASON %s ::  ' % (r.status_code, userjson)
       return


def processUsersSplat(**kw):
    print "Args: ", kw

    splats = cfupload.getCfFiles(**kw)[1]

    splat_count = 0
    for k in splats.keys():
        for splat in splats[k]:
            print "%i: %s" % (k, splat)
            splat_count += 1
    print 'Spalts count:', splat_count
    if verify(splats):
        for aid in splats:
            userenabled = True
            user = getUser(aid)
            if user is not None:
               username = user[0]
               userkey = user[1]
               print "Success User data retrieval, begin processing: ", username, userkey

               try:
                  pyrax.set_credentials(username, userkey)
               except exc.AuthenticationFailed, e:
                  print 'Authentication failed for user: %s: %s. Exception: %s' % (username, userkey, e)
                  if 'Forbidden - User \'%s\' is disabled..' % username in e:
                      userenabled = False
                      for fn in [d['file_path'] for d in splats[aid]]:
                          splat_count -= 1
                          print "DELETING DISABLEDUSER FILE:",fn
                          removeLocalFile(fn)
                      continue
                  return

               for d in splats[aid]:
                   fp = d['file_path']
                   lid = d['lid']
                   lname = d['lname']
                   date = d['date']
                   splat_count -= 1
                   print "FILES left to upload = ", splat_count
		   #uploadFile(username, aid, userkey, True, lid, lname, fp, date,**kw)
                   deleteFile(username, aid, userkey, True, lid, lname, fp, date,**kw)
    else:
        print 'Not attempting to process files, option declined. '


def uploadFile(username, userid, userkey, userenabled, lid, lname, fp, date,**kw):
    upenabled = kw.pop('upenabled','true')
    cleardirs = kw.pop('cleardirs','true')
    if upenabled == 'false':
        print "Not uploading files, option disabled!"
        print format('Files will not be uploaded: %s for userId: %s' % (fp, userid))
        return
    if userenabled:
        print format('Access CloudFiles for user id %s : user name %s' %
                     (pyrax.identity.user['id'], pyrax.identity.user['name']))
        cf = pyrax.connect_to_cloudfiles(region=getRegion())
        try:
            cf.create_container(genContName(lid, lname, date))
            chksum = pyrax.utils.get_checksum(fp)
            filename = genRemoteFileName(lid, lname, date)
            gencname = genContName(lid, lname, date)
            print format('Uploading... \n Remote File Name: %s size(%i)  as %s, Container Name: %s' % (fp,os.stat(fp).st_size,filename, gencname))
            ucont = cf.upload_file(gencname, fp, obj_name=filename, etag=chksum)
            print "Chksum valid: ", chksum == ucont.etag
            print format("Successfully uploaded file for: LBID: %s" % lid)
            print "DELETING UPLOADED FILE:",fp
            removeLocalFile(fp)
            
        except exc.UploadFailed, e:
            print 'Upload failed for %s %s. Exception: %s' % (userid, username, e)
            return
        except KeyboardInterrupt:
            print "Skipping this entry"
            time.sleep(1.0)
        except:
            print "Unknown Exception caught:", traceback.format_exc()
            time.sleep(1.0)

def removeLocalFile(fp):
    try:
        print 'File path: ', fp
        os.remove(fp.replace(' ', ''))
        print 'Successfully delete local file: ', fp
    except OSError, e:
        print format('Error occurred removing local file: %s Error: %s' % (fp, e))
        return

def deleteFile(username, userid, userkey, userenabled, lid, lname, fp, date,**kw):
                    
    if userenabled:
        print format('Access CloudFiles for user id %s : user name %s' %
			(pyrax.identity.user['id'], pyrax.identity.user['name']))
        filename = genRemoteFileName(lid, lname, date)
        gencname = genContName(lid, lname, date)

        if gencname in deleted_containers:
            print "container %s already deleted"%gencname
            return
        cf = pyrax.connect_to_cloudfiles(region=getRegion())
        try:
            print format('Deleteing... \n Remote File Name: %s size(%i)  as %s, Container Name: %s' % (fp,os.stat(fp).st_size,filename, gencname))
            print format('WARNING DELETING Container and files from %s' % gencname)
	    delcont = cf.get_container(gencname)
            print format('Retrieved container to delete is %s' % delcont.name)
	    cf.delete_container(gencname,del_objects=True)
            deleted_containers.add(gencname)
            print format("Successfully deleted file/container for: LBID: %s" % lid)
            
        except Exception, e:
            print 'DELETING failed for %s %s. Exception: %s' % (userid, username, e)
            return
        except KeyboardInterrupt:
            print "Skipping this entry"
            time.sleep(1.0)
        except:
            print "Unknown Exception caught:", traceback.format_exc()
            time.sleep(1.0)

def clearDirectories():
    print 'Clearing Directories: '
    cdir = config.get('general', 'cache_dir')

    for root, dirs, files in os.walk(cdir): # Walk directory tree
        print 'In Root: ', root
        for d in dirs:
            print format('Directory: %s/%s' % (root, d))
            try:
                os.rmdir(format('%s/%s' % (root, d)))
                print 'Successfully delete directory: ', format('%s/%s' % (root, d))
            except OSError, e:
                print format('Error occurred removing directory: %s/%s Error: %s' % (root, d, e))

    if not root.endswith('cache'):
        try:
            print 'Removing root: ', root
            os.rmdir(root)
            print 'Successfully delete directory: ', root
        except OSError, e:
            print format('Error occurred removing directory: %s Error: %s' % (root, e))


def genContName(lid, lname, date):
    return format('lb_%d_%s_%s' % (lid, lname.replace('/', '_'), parseMonthYear(date)))


def genRemoteFileName(lid, lname, date):
    os.environ["TZ"] = "UTC"
    time.tzset()
    return format('lb_%d_%s_%s.zip' % (lid, lname.replace('/', '_').replace(" ","_"),
                                       datetime.datetime(int(str(date)[:4]),
                                           int(str(date)[4:][:2]),
                                           int(str(date)[6:8]),
                                           int(str(date)[8:])).strftime('%Y-%m-%d-%H:%M')))


def getRegion():
    return config.get('general', 'region')


def parseMonthYear(date):
    year = int(str(date)[:4])
    fmonth = datetime.datetime(year, int(str(date)[4:][:2]), 1).strftime('%b')
    return format('%s_%s' % (fmonth, year))


def verify(splats):
    #raw_input returns the empty string for "enter"
    yes = set(['yes', 'y', 'ye', ''])
    no = set(['no', 'n'])

    print format('You are attempting to upload and process files for %s accounts, continue?' % len(splats))
    choice = raw_input().lower()
    if choice in yes:
        return True
    elif choice in no:
        return False
    else:
        sys.stdout.write("Please respond with 'yes' or 'no'")


def main(args):
    kw = {}

    for arg in args:
        (k, v) = arg.split("=")
        try:
            kw[k.strip()] = int(v.strip())
        except ValueError:
            #if its not an int treat it like a string    return getCfFiles(**kw)
            kw[k.strip()] = v.strip()

    if 'cleardirs' in kw and kw['cleardirs'] == 'standalone':
        clearDirectories()
    else:
        processUsersSplat(**kw)


if __name__ == '__main__':
    main(sys.argv[1:])
