import paho.mqtt.client as mqtt
import time

broker_address = "192.168.4.1" #enter your broker address here
subscribetopic = "testTopic1"
publishtopic = "testTopic2"

def on_message(client, userdata, message):
  print("message received ", str(message.payload.decode("utf-8")))
  print("message topic=", message.topic)
  print("message qos=", message.qos)
  print("message retain flag=", message.retain)
  time.sleep(5)
  print("sending pulication")
  client.publish(publishtopic, "1000")

client = mqtt.Client("P1")
client.on_message = on_message
#client.publish(publishtopic, "1000")
#print("creating ")
#client.on_connect = client.publish(publishtopic, "1000")
client.connect(broker_address)
client.loop_start()
client.subscribe(subscribetopic)
print("inside loop")
time.sleep(10)

#client.publish(publishtopic, "2000")
client.loop_stop()
