import brainstem
# for easy access to error constants
from brainstem.result import Result
import time
import sys


def connect():
    return brainstem.stem.USBHub2x4()


def disconnect(stem):
    stem.disconnect()
    print 'disconnected'


def exitProgram(stem, errCode):
    disconnect(stem)
    exit(errCode)


# too few arguments
if len(sys.argv) < 2:
    print 'current arg count is ' + str(len(sys.argv))
    print 'Usage:'
    print '    python usbhub.py <option> [args] ...\n'
    print 'Options:'
    print '    open - open specified port(s)'
    print '    close - close specified port(s)'
    print '\n'
    exit(-1)

# Create USBHub2x4 object and connecting to the first module found
print 'Creating USBHub2x4 stem and connecting to first module found'
stem = connect()

# Locate and connect to the first object you find on USB
# Easy way: 1=USB, 2=TCPIP
while True:
    result = stem.discoverAndConnect(brainstem.link.Spec.USB)
    if result != Result.NO_ERROR:
        print 'Unable to find USB hub, retrying...'
        time.sleep(1)
    else:
        break
# Locate and connect to a specific module (replace you with Your Serial Number (hex))
# result = stem.discoverAndConnect(brainstem.link.Spec.USB, 0x66F4859B)

# Check error
result = stem.system.getSerialNumber()
print "Connected to USBStem with serial number: 0x%08X" % result.value

if sys.argv[1] == "open" or sys.argv[1] == "close":
    if len(sys.argv) == 2:
        print 'Error: no port number specified'
        exitProgram(stem, 0)

    for port in sys.argv[2:len(sys.argv)]:
        port = int(port)
        if str(port).isdigit() and 0 <= port <= 7:
            if sys.argv[1] == "open":
                stem.usb.setPortEnable(port)
                print 'Opened port ' + str(port)
            elif sys.argv[1] == "close":
                stem.usb.setPortDisable(port)
                print 'Closed port ' + str(port)
        else:
            print 'Invalid port of \"' + str(port) + '\" - skipped'

# Turn user LED on and off
time.sleep(2)  # in seconds

# Disconnect from device.
disconnect(stem)
