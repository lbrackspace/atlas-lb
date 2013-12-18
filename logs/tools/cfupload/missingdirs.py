#!/usr/bin/env python

import ConfigParser
import datetime
import sys
import os

oneHour = datetime.timedelta(hours=1)

config = ConfigParser.ConfigParser()
config.read(os.path.expanduser('~/cfupconfig'))


def printf(format,*args): sys.stdout.write(format%args)

def lpad(n,*args,**kw):
    pad_char = kw.pop("pad_char","0")
    if(len(args)>=1):
        digits = kw.pop("digits",2)
    else:
        digits = 2
    str_val = str(n)
    return pad_char*(digits - len(str_val)) + str_val

def dt2ordHour(dt):
    out = 0
    out += dt.year*1000000
    out += dt.month*10000
    out += dt.day * 100
    out += dt.hour * 1
    return out

def ordHour2Dt(ordHour):
    hour = ordHour % 100
    ordHour /= 100
    day = ordHour % 100
    ordHour /= 100
    month = ordHour %100
    ordHour /= 100
    year = ordHour
    return datetime.datetime(year,month,day,hour)

def buildOrdRange(ordStart,ordStop):
    nHours = 0
    dtStart = ordHour2Dt(ordStart)
    dtStop = ordHour2Dt(ordStop)
    dt = dtStart
    while dt<dtStop:
        dt = dtStart + nHours*oneHour
        nHours += 1
        yield dt2ordHour(dt)
        
def listHourKeys(dir_path):
    hourKeys = []
    for fn in os.listdir(dir_path):
        if not os.path.isdir(os.path.join(dir_path,fn)):
            continue
        try:
            ordHour = int(fn)
            hourKeys.append(ordHour)
        except:
            continue
    hourKeys.sort()
    return hourKeys

def main(*args,**kw):
    cache_dir = config.get("general","cache_dir")
    printf("Scanning dir %s for files\n",cache_dir)
    hoursKeyList = listHourKeys(cache_dir)
    firstHour = hoursKeyList[0]
    lastHour = hoursKeyList[-1]
    if len(args)>=2:
        firstHour = int(args[1])
    if len(args)>=3:
        lastHour = int(args[2])
    
    printf("searching for missing hours between %s and %s\n",firstHour,lastHour)
    missingHours = set(buildOrdRange(firstHour,lastHour)) - set(hoursKeyList)
    nMissing = 0
    for missingHour in sorted(list(missingHours)):
        printf("Missing %s\n",missingHour)
        nMissing += 1
    printf("Total missing dates = %i\n",nMissing)

if __name__ == "__main__":
    main(*sys.argv)    
