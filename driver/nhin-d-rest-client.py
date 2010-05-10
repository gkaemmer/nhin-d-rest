# nhin-d-rest-client.py
#
# This is a client for the NHIN Direct REST Interface. It's gross and non-idiomatic, but gets the job done (mostly). 
# TODO: Add client certificate support.
# 
# Chris Moyer


# usage: %prog [options] ADDRESS [GETALL|GETMSG|POSTMSG|GETSTATUS|PUTSTATUS] <MESSAGE ID> <STATUS (PUTSTATUS ONLY)> <FILENAME (POSTMSG ONLY)>


from optparse import OptionParser
import sys
import httplib

usage = "usage: %prog [options] ADDRESS [GETALL|GETMSG|POSTMSG|GETSTATUS|PUTSTATUS] <MESSAGE ID> <STATUS (PUTSTATUS ONLY)> <FILENAME (POSTMSG ONLY)>"
parser = OptionParser(usage=usage)
parser.add_option("--host", dest="host", help="hostname of nhin-d-rest service")
parser.add_option("--port", dest="port", help="port of nhin-d-rest service")
parser.add_option("--user", dest="username", help="username")
parser.add_option("--pass", dest="password", help="password")
parser.add_option("--ssl", action="store_true", dest="ssl", default=False, help="enable ssl")
parser.add_option("-v", "--verbose", action="store_true", dest="verbose", default=False, help="verbose mode")
(options, args) = parser.parse_args()

commands = ['GETALL', 'GETMSG', 'POSTMSG', 'GETSTATUS', 'PUTSTATUS']

# parse arguments

address = ''
command = ''
message_id = ''
status = ''
filename = ''

host = 'localhost'
port = '8080'
username = None
password = None
ssl = options.ssl
verbose = options.verbose

if options.host != None:
	host = options.host

if options.port != None:
	port = options.port

if options.username != None:
	username = options.username
	
if options.password != None:
	password = options.password
	

if len(args) < 2:
	print "ADDRESS and COMMAND required"
	sys.exit(2)
else: 
	address = args[0]
	command = args[1]
	
	if command not in commands:
		print "Unrecognized command: " + command
		sys.exit(2)
	
	
if command == 'GETMSG' or command == 'GETSTATUS' or command == 'PUTSTATUS':
	# message id required
	if len(args) < 3:
		print "MESSAGE ID required"
		sys.exit(2)
	else:
		message_id = args[2]
	
if command == 'PUTSTATUS':
	if len(args) < 4:
		print "STATUS required"
		sys.exit(2)
	else:
		status = args[3]
	
if command == 'POSTMSG':
	if (len(args)) < 3:
		print 'FILENAME required'
	else:
		filename = args[2]
	

def  get_uri(command, address, message_id):
	parts = address.split('@')
	uri = '/nhin/v1/' + parts[1] + '/' + parts[0] + '/messages'
	
	if command == 'GETMSG':
		uri = uri + '/' + message_id
		
	if command == 'GETSTATUS' or command == 'PUTSTATUS':
		uri = uri + '/' + message_id + '/status'
		
	return uri
		
	
def get_auth_string(username, password) :
	s = username + ':' + password
	return 'Basic ' + s.encode('base64').rstrip()

def process_response(response):
	if verbose:
		print response.status, response.reason
		print response.getheaders()
		print '---------'
		
	print response.read()

def handle_auth(headers):
	if username != None and password != None:
		headers['Authorization'] = get_auth_string(username, password)


if ssl:	
	h1 = httplib.HTTPSConnection(host, port)
else:
	h1 = httplib.HTTPConnection(host, port)

uri = get_uri(command, address, message_id)
if verbose:
	print 'URI: ' + uri
	print "STATUS: " + status

headers = {}
handle_auth(headers)
		
# determine which command we are running
if command == 'GETALL'  or command == 'GETMSG' or command == 'GETSTATUS':
	h1.request('GET', uri, None, headers)
	
if command == 'PUTSTATUS':
	headers['Content-Type'] = 'text/plain'
	h1.request('PUT', uri, status, headers)

if command == 'POSTMSG':
	headers['Content-Type'] = 'text/plain'
	
	f = open(filename)
	content = f.read()
	
	h1.request('POST', uri, content, headers)

r1 = h1.getresponse()
process_response(r1)
