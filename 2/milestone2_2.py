import serial, time
port = "/dev/ttyACM0"
baud = 115200
while True:
    s = serial.Serial(port)
    s.baudrate = baud
    data = s.readline().strip()
    #print(len(data), data)
    if len(data) > 0:
        data = int(data[0:4])
        print(data)
    time.sleep(1)