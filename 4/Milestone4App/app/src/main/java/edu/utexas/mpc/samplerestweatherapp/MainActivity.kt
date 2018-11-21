package edu.utexas.mpc.samplerestweatherapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.provider.Settings
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.android.volley.RequestQueue
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.gson.Gson
import org.eclipse.paho.android.service.MqttAndroidClient
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended
import org.eclipse.paho.client.mqttv3.MqttMessage
import com.squareup.picasso.Picasso
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {


    // I'm using lateinit for these widgets because I read that repeated calls to findViewById
    // are energy intensive
    lateinit var textView: TextView
    lateinit var weatherImage: ImageView
    lateinit var retrieveButton: Button
    lateinit var confirmButton: Button
    lateinit var successView: TextView
    lateinit var publishButton: Button
    lateinit var stepsView: TextView

    lateinit var weatherData: String
    lateinit var min_temp : String
    lateinit var max_temp : String
    lateinit var humidity : String
    lateinit var payload : String

    lateinit var queue: RequestQueue
    lateinit var gson: Gson
    lateinit var mostRecentWeatherResult: WeatherResult
    lateinit var forecastWeatherResult: Forecast

    lateinit var mqttAndroidClient: MqttAndroidClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        textView = this.findViewById(R.id.text)
        weatherImage = this.findViewById(R.id.imageView)
        retrieveButton = this.findViewById(R.id.retrieveButton)
        confirmButton = this.findViewById(R.id.confirmNetworkButton)
        successView = this.findViewById(R.id.successView)
        publishButton = this.findViewById(R.id.publishButton)
        stepsView = this.findViewById(R.id.stepsText)

        // when the user presses the syncbutton, this method will get called
        retrieveButton.setOnClickListener({ requestWeather() })
        confirmButton.setOnClickListener({ syncWithPi() })
        publishButton.setOnClickListener({ publish() })
        publishButton.isEnabled = false

        queue = Volley.newRequestQueue(this)
        gson = Gson()
        payload = ""

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
                var elements = String(message!!.payload).split(',')
                stepsView.text = elements[3]
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
        val currentWeatherURL = StringBuilder("https://api.openweathermap.org/data/2.5/weather?id=4671654&appid=6430c66feae1f80696ed7f2705d73fd6").toString()
        val stringRequest1 = object : StringRequest(com.android.volley.Request.Method.GET, currentWeatherURL,
                com.android.volley.Response.Listener<String> { response ->
//                    textView.text = response
                    mostRecentWeatherResult = gson.fromJson(response, WeatherResult::class.java)
                    Picasso.with(applicationContext)
                            .load("http://openweathermap.org/img/w/"+mostRecentWeatherResult.weather.get(0).icon+".png")
                            .resize(300, 300)
                            .into(weatherImage);
                    min_temp = (mostRecentWeatherResult.main.temp_min.toString())
                    max_temp = (mostRecentWeatherResult.main.temp_max.toString())
                    humidity = (mostRecentWeatherResult.main.humidity.toString())

                    textView.text = mostRecentWeatherResult.weather.get(0).main
                    weatherData = mostRecentWeatherResult.weather.get(0).main
                    payload = min_temp + "#" + max_temp + "#" + humidity
                },
                com.android.volley.Response.ErrorListener { println("******That didn't work!") }) {}

        val forecastURL = StringBuilder("https://api.openweathermap.org/data/2.5/forecast?id=4671654&appid=6430c66feae1f80696ed7f2705d73fd6").toString()
        val stringRequest2 = object : StringRequest(com.android.volley.Request.Method.GET, forecastURL,
                com.android.volley.Response.Listener<String> { response ->
                    //                    textView.text = response
                    forecastWeatherResult = gson.fromJson(response, Forecast::class.java)

                    min_temp = (forecastWeatherResult.list.get(0).main.temp_min.toString())
                    max_temp = (forecastWeatherResult.list.get(0).main.temp_max.toString())
                    humidity = (forecastWeatherResult.list.get(0).main.humidity.toString())

                    textView.text = mostRecentWeatherResult.weather.get(0).main
                    weatherData = mostRecentWeatherResult.weather.get(0).main
                    payload += "," + min_temp + "#" + max_temp + "#" + humidity
                },
                com.android.volley.Response.ErrorListener { println("******That didn't work!") }) {}

        // Add the request to the RequestQueue.
        queue.add(stringRequest1)
        queue.add(stringRequest2)

        Thread.sleep(500)
        val intent = Intent(Settings.ACTION_WIFI_SETTINGS)
        startActivity(intent);
    }

    fun syncWithPi(){

        val builder = AlertDialog.Builder(this@MainActivity)

        // Set the alert dialog title
        builder.setTitle("Confirm Network Change")

        // Display a message on alert dialog
        builder.setMessage("Are you connected to pi network?")

        // Set a positive button and its click listener on alert dialog
        builder.setPositiveButton("YES"){dialog, which ->
            // Do something when user press the positive button
            Toast.makeText(applicationContext,"Thank you for confirming. Connecting to pi",Toast.LENGTH_SHORT).show()


            successView.text = "Connecting"
            println("+++++++ Connecting...")
            mqttAndroidClient.connect()
            publishButton.isEnabled = true
        }


        // Display a neutral button on alert dialog
        builder.setNeutralButton("No"){_,_ ->
            Toast.makeText(applicationContext,"Please connect to pi netwrok before proceeding",Toast.LENGTH_SHORT).show()
        }

        // Finally, make the alert dialog using builder
        val dialog: AlertDialog = builder.create()

        // Display the alert dialog on app interface
        dialog.show()


    }

    fun publish() {
        val publishTopic = "weather"
        val message = MqttMessage()
        message.payload = (payload).toByteArray()
        mqttAndroidClient.publish(publishTopic, message)
        println("Message published")

    }
}

class WeatherResult(val id: Int, val name: String, val cod: Int, val coord: Coordinates, val main: WeatherMain, val weather: Array<Weather>)
class Coordinates(val lon: Double, val lat: Double)
class Weather(val id: Int, val main: String, val description: String, val icon: String)
class WeatherMain(val temp: Double, val pressure: Double, val humidity: Int, val temp_min: Double, val temp_max: Double)

class Forecast(val cod: String, val list: Array<ForecastItem>, val city: City, val message: String, val cnt: String)
class City(val name: String)
class ForecastItem(val dt: Int, val main: ForecastMain)
class ForecastMain(val temp: Double, val pressure: Double, val humidity: Int, val temp_min: Double, val temp_max: Double)


