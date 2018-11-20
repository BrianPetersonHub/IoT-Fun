import paho.mqtt.client as mqtt
import time

broker_address = "192.168.4.1" #enter your broker address here
subscribetopic = "weather"
publishtopic = "steps"

def on_message(client, userdata, message):
  print("message received ", str(message.payload.decode("utf-8")))
  print("message topic=", message.topic)
  print("message qos=", message.qos)
  print("message retain flag=", message.retain)
  time.sleep(5)
  print("sending publication")
  print("reading stepcount from file")
  with open ('stepcount.txt', 'r') as f:
      s = f.read()
  client.publish(publishtopic, s)

client = mqtt.Client("P1")
client.on_message = on_message
client.connect(broker_address)
client.loop_start()
client.subscribe(subscribetopic)
print("inside loop")
time.sleep(30)
client.loop_stop()
