import json
import os
import simplejson
import shutil
import sys

# TODO(ptoohill): make this better at some point...

def put(data, filename):
    try:
        jsondata = simplejson.dumps(
            data, indent=4,
            skipkeys=True,
            sort_keys=True)
        fd = open(filename, 'w')
        fd.write(jsondata)
        fd.close()
    except Exception as e:
        print 'ERROR writing', filename
        print e
        pass


def get(filename):
    returndata = {}
    try:
        fd = open(filename, 'r')
        text = fd.read()
        fd.close()
        returndata = json.loads(text)
    except Exception as e:
        print 'COULD NOT LOAD:', filename
        print e
    return returndata


def process_type_data(rootdir, dir, files, data_map, dirs, cleanup=None):
    for file in files:
        f = os.path.join(dir, file)
        if 'update' in file or 'util' in dir \
            or 'list' in dir or 'exception' in dir \
            or 'counters-virtual-servers' in dir:
            schema_file = get(f)
            ds = dir.split('/')
            d = ds[len(ds) - 1]

            try:
                schema_file['javaType'] = data_map[
                    # If util directory, compare by filename
                    file.split('.')[0] if 'util' in dir else d
                ]
            except KeyError:
                print 'Directory {0} did not match the ' \
                      'format: <schemas/config-pool> ' \
                      'or did not contain a mapping, ' \
                      'ignoring...'.format(dir)
                if cleanup:
                   try:
                       shutil.rmtree(dir)
                   except:
                       print 'Directory already removed'
                return
            except TypeError:
                print 'Directory {0} is a sub folder ' \
                      'not related to expected configs, ' \
                      'ignoring...'.format(dir)
                return
            put(schema_file, f)
            return
        else:
            print 'Schema does not meet criteria: ' \
                  'Must contain an update.schema.json or ' \
                  'must be of list, util, exception, ' \
                  'or counters directory'
            if cleanup:
                if 'update' not in f:
                    try:
                        print 'Cleaning up file %s.' % f
                        os.remove(f)
                    except:
                        print 'File already gone...'
                if 'counters' in dir \
                    and dir is not 'counters-virtual-servers':
                    # Right now we only care about vs counters,
                    # need to rethink this when/if we care for others.
                    print 'removing dir %s' % dir
                    try:
                        shutil.rmtree(dir)
                    except:
                        print 'Directory already removed...'

def update_schemas(rootdir, data_map, cleanup=None):
    for subdir, dirs, files in os.walk(rootdir):
        process_type_data(rootdir, subdir, files, data_map, dirs, cleanup)


if __name__ == '__main__':
    # Directory containing the schemas directories
    config_dir = sys.argv[1]
    # Data type mapping
    data_map = get(sys.argv[2])
    try:
        cleanup = sys.argv[3]
    except:
        cleanup = False

    if config_dir and data_map:
        update_schemas(config_dir, data_map, cleanup)
    else:
        print "It is required to supply the " \
              "directory containing schema directories" \
              "and a data map file. EX: jsupdate.py " \
              "/workspace/schemas datamap "
