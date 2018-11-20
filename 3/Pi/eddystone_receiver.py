import aioblescan as aiobs
from aioblescan.plugins import EddyStone
import asyncio

def _process_packet(data):
    ev = aiobs.HCI_Event()
    xx = ev.decode(data)
    xx = EddyStone().decode(ev)
    if xx and 'https://ab?' in xx['url']:
        #print("Google Beacon: {}".format(xx))
        print(xx['url'][11:])

if __name__ == '__main__':
    mydev = 0
    event_loop = asyncio.get_event_loop()
    mysocket = aiobs.create_bt_socket(mydev)
    fac = event_loop._create_connection_transport(mysocket, aiobs.BLEScanRequester, None, None)
    conn, btctrl = event_loop.run_until_complete(fac)
    btctrl.process = _process_packet
    btctrl.send_scan_request()
    try:
        event_loop.run_forever()
    except KeyboardInterrupt:
        print('Keyboard Interrupt')
    finally:
        print('closing event loop')
        btctrl.stop_scan_request()
        conn.close()
        event_loop.close()
