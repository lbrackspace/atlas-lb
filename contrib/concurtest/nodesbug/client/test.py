import sys
import httplib
import httplib2
import urllib2

#try:
#    #conn.putheader('Authorization', 'Basic YWRtaW46RnBoOVNjMlc=')
#    #conn = httplib.HTTPSConnection("10.12.99.47", 9070);
#    h = {"Authorization": "Basic YWRtaW46RnBoOVNjMlc="}
#    conn = httplib2.Http(disable_ssl_certificate_validation=True).request("https://10.12.99.47:9070/api/tm/2.0/config/active/pools/406271_831", 9070, headers=h)
#    conn.force_exception_to_status_code=True
#    #conn.request('GET', "/api/tm/2.0/config/active/pools/406271_831", headers=h)
#    content = conn.getresponse()
#    print content.reason, content.status
#    print content.read()
#    #conn.close()
#except:
#  print sys.exc_info()[:2]


r = urllib2.Request("https://10.12.99.47:9070/api/tm/2.0/config/active/pools/",
                     headers={'Content-Type': 'application/json',
                              'Authorization': 'Basic YWRtaW46RnBoOVNjMlc=',
                              'User-Agent': 'Mozilla/5.0 (Windows NT 5.1; rv:10.0.1) Gecko/20100101 Firefox/10.0.1'})
u = urllib2.urlopen(r)
response = u.read()
print response

#import urllib2
#
#opener = urllib2.build_opener()
#
#headers = {
#    'Content-Type': 'application/json',
#    'Accept': 'application/json',
#    'Accept-Encoding': 'gzip, deflate, compress',
#    'Authorization': 'Basic YWRtaW46RnBoOVNjMlc=',
#    'Connection:' 'keep-alive'
#    'User-Agent': 'Mozilla/5.0 (Macintosh; Intel Mac OS X 10_8_5) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/32.0.1700.77 Safari/537.36',
#}
#
#opener.addheaders = headers.items()
#response = opener.open("https://10.12.99.47:9070/")
#
#print response.headers
#print response.read()

#from socket import socket
#
#host = '10.12.99.47'
#port = 9070
#path = "api/tm/2.0/config/active/pools/406271_831"
#xmlmessage = "<port>0</port>"
#
#s = socket()
#s.connect((host, port))
#s.send("GET %s HTTP/1.1\r\n" % path)
#s.send("Host: %s\r\n" % host)
#s.send("Authorization: Basic YWRtaW46RnBoOVNjMlc=\r\n")
#s.send("Content-Type: application/xml\r\n")
##s.send("Content-Length: %d\r\n\r\n" % len(xmlmessage))
##s.send(xmlmessage)
#for line in s.makefile():
#    print line,
#s.close()
#print 'closing'