package com.example.pymonitormobile;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Headers;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ApiRequest {

    String pi_data_url = "http://localhost:5000/api/pi_data"; // GET
    String sensor_data_url = "http://localhost:5000/api/sensor_data"; // GET
    String meteo_api_data_url = "http://api.openweathermap.org/data/2.5/weather?q=Wavre&appid=bbf7bb6b18f44d80ef2ff76f90601099&units=metric"; // GET

    Activity activity;

    /**
     * //--------------
     * // CONSTRUCTOR
     * //--------------
     *
     * @param activity
     */
    public ApiRequest(Activity activity, String raspberry_ip) {
        this.activity = activity;
        this.pi_data_url = pi_data_url.replace("localhost", raspberry_ip);
        this.sensor_data_url = sensor_data_url.replace("localhost", raspberry_ip);
    }

    /**
     * ---------------------------------------------
     * Method that gets data from the raspberry pi
     * ---------------------------------------------
     * @param raspberryPi
     */
    public void Get_pi_data(final Pi raspberryPi) {
        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder().url(pi_data_url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println(pi_data_url);
                System.out.println("Get-Pi-Data : Request Failed");
                Toast.makeText(activity, "Le raspberry ne répond pas", Toast.LENGTH_SHORT);
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            String responseBody = response.body().string();
                            JSONObject Jobject = new JSONObject(responseBody);
                            JSONObject Jobject_disk = Jobject.getJSONObject("disk_space");
                            JSONObject Jobject_ram = Jobject.getJSONObject("ram_space");

                            String temperature = Jobject.getString("temperature");
                            String disk_available = Jobject_disk.getString("available_space");
                            String disk_total = Jobject_disk.getString("total_space");
                            String ram_available = Jobject_ram.getString("available_space");
                            String ram_total = Jobject_ram.getString("total_space");

                            raspberryPi.setTemperature(temperature);
                            raspberryPi.setDisk_total_space(disk_total);
                            raspberryPi.setDisk_avail_space(disk_available);
                            raspberryPi.setRam_total_space(ram_total);
                            raspberryPi.setRam_avail_space(ram_available);

                            Intent intent = new Intent(activity, Page2.class);
                            intent.putExtra("MyClass", raspberryPi);
                            activity.startActivity(intent);
                            activity.finish();// Kills curent activity

                        }
                        catch (JSONException | IOException ignored) { }
                    }
                });
            }
        });
    }
    /**
     * ---------------------------------------
     * Method that gets data from the sensor
     * ---------------------------------------
     * @param sensor
     */
    public void Get_sensor_data(final TextView sensor_temp, final TextView sensor_hum) {
        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder().url(sensor_data_url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Get-Sensor-Data : Request Failed");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String responseBody = null;
                        try {
                            responseBody = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Response Body : " + responseBody);
                        try {
                            JSONObject Jobject = new JSONObject(responseBody);

                            String humidity = Jobject.getString("humidity");
                            String humidity_formatted = humidity.replace(".",",") + "%";
                            String temperature = Jobject.getString("temperature");
                            String temperature_formatted = temperature.replace(".",",") + "°C";

                            sensor_temp.setText(temperature_formatted);
                            sensor_hum.setText(humidity_formatted);

                        }
                        catch (JSONException ignored) { }
                    }
                });

            }
        });
    }

    public void Get_api_data(final TextView internet_temp, final TextView internet_hum) {
        OkHttpClient client = new OkHttpClient();

        final Request request = new Request.Builder().url(meteo_api_data_url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                System.out.println("Get-Api-Data : Request Failed");
            }

            @Override
            public void onResponse(Call call, final Response response) throws IOException {
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        String responseBody = null;
                        try {
                            responseBody = response.body().string();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        System.out.println("Response Body : " + responseBody);

                        try {
                            JSONObject mainObject = new JSONObject(responseBody);

                            JSONObject object_current = mainObject.getJSONObject("main");

                            String temperature = object_current.getString("temp");
                            String temperature_formatted = temperature.replace(".",",") + "°C";

                            String humidity = object_current.getString("humidity");
                            String humidity_formatted = humidity.replace(".",",") + "%";

                            internet_temp.setText(temperature_formatted);
                            internet_hum.setText(humidity_formatted);

                        }
                        catch (JSONException ignored) { }
                    }
                });

            }
        });
    }
}
