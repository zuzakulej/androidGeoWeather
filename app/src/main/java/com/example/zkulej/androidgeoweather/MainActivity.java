package com.example.zkulej.androidgeoweather;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    TextView latView;
    TextView lonView;
    WebView weatherWebView;
    LocationManager locationManager;
    LocationProvider locationProvider;
    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 1;
    public static String OPENWEATHER_WEATHER_QUERY = "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&mode=html&appid=4526d487f12ef78b82b7a7d113faea64";

    // usage String.format(OPENWEATHER_WEATHER_QUERY, lat,lon)

    public String getContentFromUrl(String addr) {
        String content = null;

        Log.v("[GEO WEATHER ACTIVITY]", addr);
        HttpURLConnection urlConnection = null;
        URL url = null;
        try {
            url = new URL(addr);
            urlConnection = (HttpURLConnection) url.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

            StringBuilder stringBuilder = new StringBuilder();
            String line = null;
            while ((line = in.readLine()) != null)
            {
                stringBuilder.append(line + "\n");
            }
            content = stringBuilder.toString();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if(urlConnection!= null) urlConnection.disconnect();
        }
        return content;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        latView = findViewById(R.id.latView);
        lonView = findViewById(R.id.lonView);
        weatherWebView = findViewById(R.id.weatherWebView);

    }

    public void onLocationChanged(Location location) {
        final double lat = (location.getLatitude());
        final double lon = location.getLongitude();
        new Thread(new Runnable() {
            @Override
            public void run() {
                updateWeather(lat,lon);
            }
        }).start();
    };

    private void updateWeather(double lat, double lon){
        //get your own free ulr with data from OpenWeatherMap
        //string.format parses data to suitable url
        String weather = getContentFromUrl(String.format(OPENWEATHER_WEATHER_QUERY, lat,lon));
        Message m = myHandler.obtainMessage();
        Bundle b = new Bundle();
        b.putString("lat", String.valueOf(lat));
        b.putString("lon", String.valueOf(lon));
        b.putString("web", weather);
        m.setData(b);
        myHandler.sendMessage(m);
    }

    Handler myHandler = new Handler(){
        public void handleMessage(Message msg) {
            String lat = msg.getData().getString("lat");
            String lon = msg.getData().getString("lon");
            String web = msg.getData().getString("web");

            // referencje pobrane wcześniej w metodzie onCreate(...)
            latView.setText(lat);
            lonView.setText(lon);
            weatherWebView.loadDataWithBaseURL(null, web, "text/html", "utf-8", null);
        }
    };

//    //preferable way to implement without memory leakage:
//    private static class MyHandler extends Handler{
//        private final WeakReference<MainActivity> mActivity;
//        MyHandler(MainActivity activity) {
//            mActivity = new WeakReference<MainActivity>(activity);
//        }
//        @Override
//        public void handleMessage(Message msg) {
//            MainActivity activity = mActivity.get();
//            String
//                    String
//            String
//                    String
//            String
//                    lat = msg.getData().getString("lat");
//            lon = msg.getData().getString("lon");
//            woeid = msg.getData().getString("woeid");
//            web = msg.getData().getString("web");
//            city = msg.getData().getString("city");
////referencje pobrane wcześniej w metodzie onCreate(...)
//            activity.latTextView.setText(lat);
//            activity.lonTextView.setText(lon);
//            activity.woeidTextView.setText(woeid);
//            activity.cityTextView.setText(city);
//            activity.weatherWebView.loadDataWithBaseURL(null, web, "text/html", "utf-8", null);
//        }
//    }
//    Handler myHandler = new MyHandler(this);

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    accessLocation();
                }
            }
        }
    }

    private final LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            final double lat = (location.getLatitude());
            final double lon = location.getLongitude();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    updateWeather(lat,lon);
                }
            }).start();
//kod obslugi najlepiej w osobnym wątku...
        }

        @Override
        public void onStatusChanged(String s, int i, Bundle bundle) {

        }

        @Override
        public void onProviderEnabled(String s) {

        }

        @Override
        public void onProviderDisabled(String s) {

        }
    };

    @Override
    protected void onStart() {
        super.onStart();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);

        // MY_PERMISSIONS_REQUEST_LOCATION is an
        // app-defined int constant. The callback method gets the
        // result of the request.
        } else{
            accessLocation();
        }
        weatherWebView.loadUrl("http://api.openweathermap.org/data/2.5/weather?lat=49.2&lon=19.9&mode=html&appid=4526d487f12ef78b82b7a7d113faea64");
    }

    private void accessLocation(){
        this.locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        this.locationProvider = this.locationManager.getProvider(LocationManager.GPS_PROVIDER);
        if (locationProvider != null) {
            Toast.makeText(this, "Location listener registered!", Toast.LENGTH_SHORT).show();
            try {
                this.locationManager.requestLocationUpdates(locationProvider.getName(), 0, 0,
                        this.locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this,
                    "Location Provider is not avilable at the moment!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (locationProvider != null) {
            Toast.makeText(this, "Location listener unregistered!", Toast.LENGTH_SHORT).show();
            try {
                this.locationManager.removeUpdates(this.locationListener);
            } catch (SecurityException e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, "Location Provider is not avilable at the moment!",
                    Toast.LENGTH_SHORT).show();
        }
    }

}
