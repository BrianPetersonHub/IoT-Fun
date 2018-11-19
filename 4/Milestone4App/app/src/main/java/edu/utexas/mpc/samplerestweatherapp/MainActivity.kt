package edu.utexas.mpc.samplerestweatherapp

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.TextView
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {


    // I'm using lateinit for these widgets because I read that repeated calls to findViewById
    // are energy intensive
    lateinit var textView: TextView
    lateinit var retrieveButton: Button
    lateinit var confirmButton: Button
    lateinit var successView: TextView
    lateinit var publishButton: Button
    lateinit var stepsView: TextView

    lateinit var weatherData: String

    lateinit var queue: RequestQueue
    lateinit var gson: Gson
    lateinit var mostRecentWeatherResult: WeatherResult

    lateinit var mqttAndroidClient: MqttAndroidClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = this.findViewById(R.id.text)
        retrieveButton = this.findViewById(R.id.retrieveButton)
        confirmButton = this.findViewById(R.id.confirmNetworkButton)
        successView = this.findViewById(R.id.successView)
        publishButton = this.findViewById(R.id.publishButton)
        stepsView = this.findViewById(R.id.stepsText)

        // when the user presses the syncbutton, this method will get called
        retrieveButton.setOnClickListener({ requestWeather() })
        confirmButton.setOnClickListener({ syncWithPi() })
        publishButton.setOnClickListener({ publish() })

        queue = Volley.newRequestQueue(this)
        gson = Gson()

        val serverUri = "tcp://192.168.4.1"
        val clientId = "EmergingTechMQTTClient"

        val subscribeTopic = "steps"

        mqttAndroidClient = MqttAndroidClient(getApplicationContext(), serverUri, clientId);

        mqttAndroidClient.setCallback(object: MqttCallbackExtended {

            // when the client is successfully connected to the broker, this method gets called
            override fun connectComplete(reconnect: Boolean, serverURI: String?) {
                println("Connection Complete!!")
                successView.text = "Connected!"
                // this subscribes the client to the subscribe topic
                mqttAndroidClient.subscribe(subscribeTopic, 0)

            }

            // this method is called when a message is received that fulfills a subscription
            override fun messageArrived(topic: String?, message: MqttMessage?) {
                println(message)
                stepsView.text = String(message!!.payload)
            }

            override fun connectionLost(cause: Throwable?) {
                println("Connection Lost")
                successView.text = "Disconnected! :("
            }

            // this method is called when the client succcessfully publishes to the broker
            override fun deliveryComplete(token: IMqttDeliveryToken?) {
                println("Delivery Complete")
            }
        })
    }

    fun requestWeather(){
        val url = StringBuilder("https://api.openweathermap.org/data/2.5/weather?id=4671654&appid=6430c66feae1f80696ed7f2705d73fd6").toString()
        val stringRequest = object : StringRequest(com.android.volley.Request.Method.GET, url,
                com.android.volley.Response.Listener<String> { response ->
//                    textView.text = response
                    mostRecentWeatherResult = gson.fromJson(response, WeatherResult::class.java)
                    textView.text = mostRecentWeatherResult.weather.get(0).main
                    weatherData = mostRecentWeatherResult.weather.get(0).main
                },
                com.android.volley.Response.ErrorListener { println("******That didn't work!") }) {}
        // Add the request to the RequestQueue.
        queue.add(stringRequest)
    }

    fun syncWithPi(){
        successView.text = "Connecting"
        println("+++++++ Connecting...")
        mqttAndroidClient.connect()
    }

    fun publish() {
        val publishTopic = "weather"
        val message = MqttMessage()
        message.payload = (weatherData).toByteArray()
        mqttAndroidClient.publish(publishTopic, message)
        println("Message published")
    }
}

class WeatherResult(val id: Int, val name: String, val cod: Int, val coord: Coordinates, val main: WeatherMain, val weather: Array<Weather>)
class Coordinates(val lon: Double, val lat: Double)
class Weather(val id: Int, val main: String, val description: String, val icon: String)
class WeatherMain(val temp: Double, val pressure: Double, val humidity: Int, val temp_min: Double, val temp_max: Double)

