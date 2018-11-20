import aioblescan as aiobs
from aioblescan.plugins import EddyStone
import asyncio
import paho.mqtt.client as mqtt
import time

broker_address = "192.168.4.1" #enter your broker address here
subscribetopic = "weather"
publishtopic = "steps"
steps = 0


def _process_packet(data):
    ev = aiobs.HCI_Event()
    xx = ev.decode(data)
    xx = EddyStone().decode(ev)
    if xx and 'https://ab?' in xx['url']:
        print(xx['url'][11:])
        steps = xx['url'][11:]
        print("Writing stepcount to file")
        with open('stepcount.txt', 'w') as f:
            f.write(steps)
        print("Finished writing stepcount {} to file".format(str(steps)))
            
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
