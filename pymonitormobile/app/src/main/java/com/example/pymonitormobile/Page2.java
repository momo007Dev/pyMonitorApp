package com.example.pymonitormobile;

import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.eazegraph.lib.charts.PieChart;
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