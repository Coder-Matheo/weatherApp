package rob.myappcompany.weatherapp;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZoneId;


public class MainActivity extends AppCompatActivity {

    private BroadcastReceiver minupdateReceiver;
    private FusedLocationProviderClient fusedLocationProviderClient;

    TextView resultTextView;
    //should Input editText

    ImageView iconWeatherImageView;
    TextView tempTextView;
    TextView TimeTextView;
    TextView minmaxTextView;
    TextView windTextView;
    private Button btn;
    private Switch daynightSwitch;
    private ConstraintLayout backLayout;
    private LocationManager locationManager;
    private LocationListener locationListener;


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull  String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)== PackageManager.PERMISSION_GRANTED){
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        backLayout = findViewById(R.id.backLayout);
        resultTextView = findViewById(R.id.descriptionWeatherTextView);


        iconWeatherImageView = findViewById(R.id.iconWeatherImageView);


        tempTextView = findViewById(R.id.tempTextView);
        TimeTextView = findViewById(R.id.TimeTextView);
        minmaxTextView = findViewById(R.id.minmaxTextView);
        windTextView = findViewById(R.id.windTextView);
        daynightSwitch = (Switch) findViewById(R.id.DayNightswitch);

        backLayout.setBackgroundColor(Color.parseColor("#2f3543"));
        tempTextView.setTextColor(Color.WHITE);
        resultTextView.setTextColor(Color.WHITE);
        TimeTextView.setTextColor(Color.WHITE);
        windTextView.setTextColor(Color.WHITE);
        minmaxTextView.setTextColor(Color.WHITE);
        daynightSwitch.setTextColor(Color.WHITE);



        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            @Override
            public void onLocationChanged(@NonNull Location location) {
                int longit = (int) (location.getLongitude());
                int latit = (int) (location.getLatitude());


                try {
                    DownloadTask task= new DownloadTask();
                    task.execute("https://api.openweathermap.org/data/2.5/weather?lat="+latit+"&lon="+longit+"&appid=5051c69d380f0a500aa44174975c8b25");

                }catch (Exception e){
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), "Coud not find weather1", Toast.LENGTH_SHORT).show();

                }
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) { }

            @Override
            public void onProviderEnabled(@NonNull String provider) { }

            @Override
            public void onProviderDisabled(@NonNull String provider) { }
        };
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }else {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, locationListener);
        }



        //Fisrt execute function

        getDateTime();
        getDayNightToggel();
    }

    //Update Display
    public void startMinuteUpdate(){
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_TIME_TICK);
        minupdateReceiver = new BroadcastReceiver(){

            @Override
            public void onReceive(Context context, Intent intent) {

                getDateTime();
                getDayNightToggel();
            }
        };
        registerReceiver(minupdateReceiver, intentFilter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        startMinuteUpdate();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(minupdateReceiver);
    }




    public void getDayNightToggel(){

        daynightSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked){
                    System.out.println("IS CHECKED");

                    backLayout.setBackgroundColor(Color.parseColor("#e2eaf2"));
                    tempTextView.setTextColor(Color.BLACK);
                    resultTextView.setTextColor(Color.BLACK);
                    TimeTextView.setTextColor(Color.BLACK);
                    windTextView.setTextColor(Color.BLACK);
                    minmaxTextView.setTextColor(Color.BLACK);
                    daynightSwitch.setTextColor(Color.BLACK);

                }else {
                    backLayout.setBackgroundColor(Color.parseColor("#2f3543"));
                    tempTextView.setTextColor(Color.WHITE);
                    resultTextView.setTextColor(Color.WHITE);
                    TimeTextView.setTextColor(Color.WHITE);
                    windTextView.setTextColor(Color.WHITE);
                    minmaxTextView.setTextColor(Color.WHITE);
                    daynightSwitch.setTextColor(Color.WHITE);

                }
            }
        });

    }

    public void getDateTime(){

        try {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                LocalDateTime localDate = LocalDateTime.now(ZoneId.of("GMT+02:30"));
                LocalDateTime localTime = LocalDateTime.now(ZoneId.of("GMT+02:00"));
                TimeTextView.setText(String.valueOf(localTime.getHour()+":"+localTime.getMinute()));

            }


        }catch (Exception e){
            e.printStackTrace();

        }

    }

    public class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            String result = "";
            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(urls[0]);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                InputStreamReader reader = new InputStreamReader(in);
                int data = reader.read();

                while (data != -1) {
                    char current = (char) data;
                    result += current;
                    data = reader.read();
                }
                return result;
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Could not find weather", Toast.LENGTH_SHORT).show();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            try {
                JSONObject jsonObject = new JSONObject(s);



                float tempNum = jsonObject.getJSONObject("main").getInt("temp");
                float tempMinNum = jsonObject.getJSONObject("main").getInt("temp_min");
                float tempMaxNum = jsonObject.getJSONObject("main").getInt("temp_max");
                int WindNum = jsonObject.getJSONObject("wind").getInt("deg");

                int val = (int) ((int) tempNum - 273.15f);
                int valmintemp = (int) ((int) tempMinNum - 273.15f);
                int valmaxtemp = (int) (tempMaxNum -273.15f);


                tempTextView.setText(String.valueOf( val)+"°");
                windTextView.setText(String.valueOf(WindNum)+" mph");
                minmaxTextView.setText(String.valueOf(valmintemp+ "/"+valmaxtemp+"°"));

                String weatherInfo = jsonObject.getString("weather");

                JSONArray arrweather = new JSONArray(weatherInfo);
                String message = "";

                for (int i = 0; i < arrweather.length(); i++) {
                    JSONObject jsonPart = arrweather.getJSONObject(i);

                    String main = jsonPart.getString("main");
                    String description = jsonPart.getString("description");
                   String iconlog = jsonPart.getString("main");

                    if (!main.equals("") && !description.equals("")) {
                        message += description + " \r\n";
                    }

                    String bezeichIcon = jsonPart.getString("icon");


                    if (bezeichIcon.equals("01d")){
                        iconWeatherImageView.setImageResource(R.drawable.icon01d);

                    }else if (bezeichIcon.equals("01n")){
                        iconWeatherImageView.setImageResource(R.drawable.icon01n);
                    }else if (bezeichIcon.equals("02d")){
                        iconWeatherImageView.setImageResource(R.drawable.icon02d);
                    }else if (bezeichIcon.equals("02n")){
                        iconWeatherImageView.setImageResource(R.drawable.icon02n);
                    }else if (bezeichIcon.equals("03d")){
                        iconWeatherImageView.setImageResource(R.drawable.icon03d);
                    }else if (bezeichIcon.equals("04d")){
                        iconWeatherImageView.setImageResource(R.drawable.icon04d);
                    }else if (bezeichIcon.equals("04n")){
                        iconWeatherImageView.setImageResource(R.drawable.icon04d);
                    }else if (bezeichIcon.equals("09d")){
                        iconWeatherImageView.setImageResource(R.drawable.icon09d);
                    }else if (bezeichIcon.equals("10d")){
                        iconWeatherImageView.setImageResource(R.drawable.icon10d);
                    }else if (bezeichIcon.equals("10n")){
                        iconWeatherImageView.setImageResource(R.drawable.icon10n);
                    }else if (bezeichIcon.equals("11d")){
                        iconWeatherImageView.setImageResource(R.drawable.icon11d);
                    }else if (bezeichIcon.equals("11n")){
                        iconWeatherImageView.setImageResource(R.drawable.icon11n);
                    }else if (bezeichIcon.equals("13d")){
                        iconWeatherImageView.setImageResource(R.drawable.icon13d);
                    }else if (bezeichIcon.equals("13n")){
                        iconWeatherImageView.setImageResource(R.drawable.icon13n);
                    }else if (bezeichIcon.equals("50d")){
                        iconWeatherImageView.setImageResource(R.drawable.icon50d);
                    }else if (bezeichIcon.equals("50n")){
                        iconWeatherImageView.setImageResource(R.drawable.icon50n);
                    }

                }
                if (!message.equals("")) {
                    resultTextView.setText(message);
                } else {
                    Toast.makeText(getApplicationContext(), "Coud not find weather", Toast.LENGTH_SHORT).show();
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getApplicationContext(), "Coud not find weather", Toast.LENGTH_SHORT).show();

            }

        }


    }
}