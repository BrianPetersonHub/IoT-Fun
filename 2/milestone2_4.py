'''C8:AB:A0:86:75:6F - Micro bit
B8:27:EB:D8:50:73 - Controller
E9:7F:A2:0F:3C:2C BBC micro:bit [gatig]'''

from bluezero import microbit
ubit = microbit.Microbit(adapter_addr='B8:27:EB:D8:50:73',
                         device_addr = 'E9:7F:A2:0F:3C:2C')

my_text = 'Hello World'
ubit.connect()
'''
while my_text is not '':
    ubit.text = my_text
    my_text = input('Enter a message:')
'''
ubit.disconnect()

'''import time
from bluezero import microbit
ubit = microbit.Microbit(adapter_addr='B8:27:EB:D8:50:73',
                         device_addr = 'E9:7F:A2:0F:3C:2C')

ubit.connect()
while(ubit.button_a < 1):
    ubit.pixels = [0b00000,
                   0b01000,
                   0b11111,
                   0b01000,
                   0b00000]
    time.sleep(0.5)
    ubit.clear_display()

ubit.disconnect()'''
