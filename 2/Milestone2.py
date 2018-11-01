# Write your code here :-)
from microbit import *
import radio

radio.on()
radio.config(channel=42)
radio.config(power=7)
count = 0

while True:
    print(42)
'''
    if button_a.was_pressed():
        radio.send(str(count))
        display.show('S')
    
    incoming = radio.receive()
        
    while incoming is not None:
        display.show(incoming)
        if button_b.is_pressed():
            incoming = str(int(incoming)+1)
            radio.send(incoming)
            display.show(incoming)
            sleep(1000)
            break
        


radio.off()
'''