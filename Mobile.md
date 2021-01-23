# Mobile

3 main files :

* Page1.java :
  * Checks if user is connected to internet
  * Attempts to find the raspberry pi (with a static address - to optimise)
  * If everything is going good => Go to next page (Page2)
* Page2.java
  * Fetchs data from backend about available RAM space
  * Display / build a pie graph with RAM data received from backend
  * Has 2 refresh buttons that when clicked, sends an API request for getting temperature and humidity from sensor
* ApiRequest.java => Handles all API request

---

## Page1.java

```java
package com.example.pymonitormobile;

import android.app.Activity;
(...) // Skipped some imports
import androidx.appcompat.app.AppCompatActivity;

public class Page1 extends AppCompatActivity {

    Networking networking = new Networking();
    Pi raspberry = new Pi();
    String raspberry_static_ip = "192.168.0.100";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page1);

        WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(WIFI_SERVICE);
        final String ipAddress = Formatter.formatIpAddress(wifiManager.getConnectionInfo().getIpAddress());
        //ipAdress is the IP of android device => Could be used to get the subnet and then ping everyone,...

        TextView label_status = findViewById(R.id.label_status_internet); // Connection Internet
        final TextView label_connection = findViewById(R.id.label_status_connection);

        Button find_rapsberry_btn = findViewById(R.id.btn_find_pi);
        final Button pi_monitor = findViewById(R.id.btn_pi_monitor);

        set_invisible(pi_monitor); // Hides the py_monitor button

        if (!networking.isConnectedToInternet(this)){
            set_status_label_raspberry(label_status, false, 1);
            //label_status.setText(Html.fromHtml("<u>Connection Internet</u> :<span style=\"color:red\"> Pas connecté </span>"));
        }
        else {
            set_status_label_raspberry(label_status, true, 1);
            //label_status.setText(Html.fromHtml("<u>Connection Internet</u> :<span style=\"color:#00cc00\"> Connecté </span>"));
        }

        set_status_label_raspberry(label_connection, false, 2);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        find_rapsberry_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (networking.isConnectedToInternet(Page1.this)) { // If user is connected to internet.
                    Page1.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (!networking.isHostUp(raspberry_static_ip)){
                                Toast.makeText(Page1.this, "Raspberry pi non trouvé !", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                raspberry.setIp(raspberry_static_ip);
                                set_status_label_raspberry(label_connection, true, 2);
                                set_visible(pi_monitor);
                            }
                        }
                    });
                }
                else { Toast.makeText(Page1.this, "Pas connecté à internet !", Toast.LENGTH_SHORT).show(); }
            }
        });

        pi_monitor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ApiRequest apiRequest = new ApiRequest(Page1.this, raspberry.getIp());
                apiRequest.Get_pi_data(raspberry);
            }
        });
    }

    private void set_status_label_raspberry (TextView t, boolean connected, int type){ // Type 1 ="internet" ou 2 => "raspbery
        String label ="";
        if (type == 1){ label="<u>Connection Internet</u> :"; }
        else if (type ==2){ label="<u>Raspberry Pi</u> :"; }

        if (connected){
            t.setText(Html.fromHtml(label+"<span style=\"color:#00cc00\"> Connecté </span>"));
        }
        else {
            t.setText(Html.fromHtml(label+"<span style=\"color:red\"> Pas connecté </span>"));
        }
    }

    private void set_invisible (Button btn){
        btn.setVisibility(View.INVISIBLE);
    }

    private void set_visible (Button btn){
        btn.setVisibility(View.VISIBLE);
    }

    private void launch_page2(Activity activity){
        Intent intent = new Intent(activity, Page2.class);
        intent.putExtra("MyClass", raspberry);
        activity.startActivity(intent);
        activity.finish();// Kills curent activity
    }
}
```

---

## Page2.java

```java
package com.example.pymonitormobile;

import android.graphics.Color;
(...) // Skipped some imports
import org.eazegraph.lib.models.PieModel;

public class Page2 extends AppCompatActivity {

    // Create the object of TextView
    // and PieChart class
    TextView temp_sonde, hum_sonde;
    PieChart pieChart;
    double data [];

    Pi raspberry2;
    Sensor sensor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.page2);

        pieChart = findViewById(R.id.piechart);
        TextView ram_used = findViewById(R.id.label_ram_used);
        TextView ram_avail = findViewById(R.id.label_ram_avail);

        temp_sonde = findViewById(R.id.label_sonde_temp); // Set mesure temp for sensor
        hum_sonde = findViewById(R.id.label_sonde_hum); // Set humidity for sensor

        ImageButton refresh_sensor = findViewById(R.id.refresh_sensor);
        ImageButton refresh_api_data = findViewById(R.id.refresh_internet_data);

        final TextView internet_temp = findViewById(R.id.label_internet_temp);
        final TextView internet_hum = findViewById(R.id.label_internet_hum);

        raspberry2 = (Pi) getIntent().getSerializableExtra("MyClass");
        sensor = new Sensor();

        data = calculate(raspberry2.getRam_total_space(), raspberry2.getRam_avail_space());

        setData(ram_used, ram_avail, data);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        refresh_sensor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ApiRequest apiRequest = new ApiRequest(Page2.this, raspberry2.getIp());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        apiRequest.Get_sensor_data(temp_sonde, hum_sonde);
                    }
                });
            }
        });

        refresh_api_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final ApiRequest apiRequest = new ApiRequest(Page2.this, raspberry2.getIp());
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        apiRequest.Get_api_data(internet_temp, internet_hum);
                    }
                });
            }
        });
    }

    private double [] calculate (String total_ram, String avail_ram){
        double total = 0;
        double avail = 0;
        double tab [] = new double[2]; // 0  = used ram percent / 1 = avail ram percent

        String total_string = total_ram.substring(0, total_ram.length() - 2);
        String avail_string = avail_ram.substring(0, avail_ram.length() - 2);

        if (total_string.contains(",")){
            total_string = total_string.replace(",",".");
        }
        if (avail_string.contains(",")){
            avail_string = avail_string.replace(",",".");
        }
        total = Double.parseDouble(total_string);
        avail = Double.parseDouble(avail_string);


        double used = total - avail;
        double used_percent = (100/total) * used;
        double avail_percent = (100/total) * avail;
        tab[0] = Math.round(used_percent * 100.0) / 100.0;
        tab[1] = Math.round(avail_percent * 100.0) / 100.0;

        return tab;
    }

    private void setData(TextView ram_used, TextView ram_avail, double [] data) {

        ram_used.setText("RAM Utilisée : " + data[0] + "%");
        ram_avail.setText("RAM Disponible : " + data[1] + "%");

        // Set the data and color to the pie chart
        pieChart.addPieSlice(
                new PieModel(
                        "RAM Utilisée",
                        (float) data[0],
                        Color.parseColor("#FFA726")));
        pieChart.addPieSlice(
                new PieModel(
                        "RAM Disponible",
                        (float) data[1],
                        Color.parseColor("#66BB6A")));

        // To animate the pie chart
        pieChart.startAnimation();
    }
}
```

---

## ApiRequest.java

```java
package com.example.pymonitormobile;

import android.app.Activity;
(...) // Skipped some imports
import okhttp3.Response;)

public class ApiRequest {

    String pi_data_url = "http://localhost:5000/api/pi_data";
    String sensor_data_url = "http://localhost:5000/api/sensor_data";
    String meteo_api_data_url = "http://api.weatherapi.com/v1/current.json?key=3d50edc14ac74d1789f122358202012&q=Wavre";

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

                            JSONObject object_current = mainObject.getJSONObject("current");

                            String temperature = object_current.getString("temp_c");
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
```
