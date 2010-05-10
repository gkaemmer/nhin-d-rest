= NHIN Direct Rest Client =

== Introduction ==

During development of the REST prototype it quickly became painful to use a browser or other HTTP 
tool to exercise the API that we're building. This tool scratches an itch I had to be able to quickly invoke
the API with a variety of connectivity options. The code is hacked together quickly and is non-idiomatic, I am not a python expert.

== Usage == 

Usage: nhin-d-rest-client.py [options] ADDRESS [GETALL|GETMSG|POSTMSG|GETSTATUS|PUTSTATUS] <MESSAGE ID> <STATUS (PUTSTATUS ONLY)> <FILENAME (POSTMSG ONLY)>

Options:
  -h, --help       show this help message and exit
  --host=HOST      hostname of nhin-d-rest service
  --port=PORT      port of nhin-d-rest service
  --user=USERNAME  username
  --pass=PASSWORD  password
  --ssl            enable ssl
  -v, --verbose    verbose mode


== Examples ==

Get all messages for Dr. Smith:

python nhin-d-rest-client.py --host localhost --port 8080 --user drsmith --pass drsmith drsmith@nhin.sunnyfamilypractice.example.org GETALL

Get a particular message for Dr. Smith:

python nhin-d-rest-client.py --host localhost --port 8080 --user drsmith --pass drsmith drsmith@nhin.sunnyfamilypractice.example.org GETMSG b5135bd5-9dc3-4688-9ae1-4493abbcebc7
  
Get a particular message status for Dr. Smith:

python nhin-d-rest-client.py --host localhost --port 8080 --user drsmith --pass drsmith drsmith@nhin.sunnyfamilypractice.example.org GETSTATUS b5135bd5-9dc3-4688-9ae1-4493abbcebc7

Update a particular message status for Dr. Smith:
  
python nhin-d-rest-client.py --host localhost --port 8080 --user drsmith --pass drsmith drsmith@nhin.sunnyfamilypractice.example.org PUTSTATUS b5135bd5-9dc3-4688-9ae1-4493abbcebc7  ACK

Post a new message to Dr. Jones:

python nhin-d-rest-client.py --host localhost --port 8080 --user drsmith --pass drsmith drjones@nhin.sunnyfamilypractice.example.org POSTMSG testmsg.txt

== SSL ==

SSL support is enabled by setting the right port and using the --ssl command line option. Server certs are not validated.
  
 
== TODO ==

Working on enabling client side certs for testing of trust models on the server. 
  
  
  
Chris Moyer
