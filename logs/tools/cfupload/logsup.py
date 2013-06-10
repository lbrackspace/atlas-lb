#!/usr/bin/python

import sys, os, time
import requests
import base64
import calendar
import cfupload
import datetime
import ConfigParser

config = ConfigParser.ConfigParser()
config.read('/Users/philliptoohill/openWorkSpace/python/config')

import pyrax
import pyrax.exceptions as exc

pyrax.set_setting("identity_type", "rackspace")
pyrax.set_http_debug(False)

def getUser(acctId):
    username = config.get('general', 'b_user')
    password = config.get('general', 'b_pass')
    endpoint = config.get('general', 'mossoauth')
    base64string = base64.encodestring('%s:%s' % (username, password)).replace('\n', '')

    headers = {'content-type': 'application/json', 'Authorization': 'Basic ' + base64string}

    r = requests.get(format('%s%s' % (endpoint, acctId)), headers=headers)
    userjson = r.json()
    return userjson['user']['id'], userjson['user']['key']


def processUsersSplat(args):
    print "Args: ", args

    #    splats = cfupload.getCfFiles(args)[1]
    splats=cfupload.getCfFiles(date=args.get('date'), date_gte=args.get('date_gte'), date_lte=args.get('date_lte'))[1]
    print 'Spalt count: ', len(splats)
    splats = cfupload.testSplat()
    if verify(splats):
        for key in splats:
            user = getUser(key)
            username = user[0]
            userkey = user[1]
            print "Success User data retrieval, begin processing: ", username, userkey

            try:
                pyrax.set_credentials(username, userkey)
            except exc.AuthenticationFailed:
                print "Authentication failed for user: ", username, key

            for d in splats[key]:
                fp = d['file_path']
                lid = d['lid']
                lname = d['lname']
                date = d['date']
                uploadFile(args, username, key, userkey, lid, lname, fp, date)
    print 'Not attempting to process files, option declined. '

    if 'cleardirs' in args and args['cleardirs'] == 'false':
        print 'Not clearing directories, option disabled!'
        return
    clearDirectories()


def uploadFile(args, username, userid, userkey, lid, lname, fp, date):
    if 'upenabled' in args and args['upenabled'] == 'false':
        print "Not uploading files, option disabled!"
        print format('Files will not be uploaded: %s for userId: $s' % (fp, userid))
        return

    print format('Access CloudFiles for user id %s : user name %s' %
                 (pyrax.identity.user['id'], pyrax.identity.user['name']))

    cf = pyrax.connect_to_cloudfiles(region=getRegion())
    try:
        cf.create_container(genContName(lid, lname, date))
        chksum = pyrax.utils.get_checksum(fp)
        filename = genRemoteFileName(lid, lname, date)
        gencname = genContName(lid, lname, date)
        print format('Uploading... \n Remote File Name: %s, Container Name: %s' % (filename, gencname))
        ucont = cf.upload_file(gencname, fp, obj_name=filename, etag=chksum)
        print "Chksum valid: ", chksum == ucont.etag
    except exc.UploadFailed:
        print "Upload faile for: ", lid, lname
        return

    print format("Successfully uploaded file for: LBID: %s" % lid)
    removeLocalFile(fp)
    return


def removeLocalFile(fp):
    try:
        print 'File path: ', fp
        os.remove(fp.replace(' ', ''))
        print 'Successfully delete local file: ', fp
    except OSError, e:
        print format('Error occurred removing local file: %s Error: %s' % (fp, e))
        return


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
    return format('lb_%d_%s_%s.zip' % (lid, lname.replace('/', '_'),
                                       datetime.datetime(int(str(date)[:4]),
                                           int(str(date)[4:][:2]),
                                           int(str(date)[6:8]),
                                           int(str(date)[8:])).strftime('%Y-%m-%d-%H:%M')))


def getRegion():
    return config.get('general', 'region')


def parseMonthYear(date):
    month = calendar.month_name[int(str(date)[4:][:2])]
    year = int(str(date)[:4])
    return format('%s_%s' % (month, year))


def verify(splats):
    # raw_input returns the empty string for "enter"
    yes = set(['yes', 'y', 'ye', ''])
    no = set(['no', 'n'])

    print format('You are attempting to upload and process %s files, continue?' % len(splats))
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
        processUsersSplat(kw)


if __name__ == '__main__':
    main(sys.argv[1:])