package com.example.pymonitormobile;

import android.app.Activity;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.text.Html;
import android.text.format.Formatter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.TimeUnit;

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
