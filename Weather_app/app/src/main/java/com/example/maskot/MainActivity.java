package com.example.maskot;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;

import android.os.Looper;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    //  private EditText city ;
    private TextView wind, temp, weather, city;
    private ImageButton raschet;

    private ImageView img;
    private double latitude, longitude;
    private String cityName;

    LocationRequest locationRequest;


    @SuppressLint({"ResourceAsColor", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        city = findViewById(R.id.city);
        wind = findViewById(R.id.wind);
        temp = findViewById(R.id.temp);
        weather = findViewById(R.id.weather);
        raschet = findViewById(R.id.raschet);
        img = (ImageView) findViewById(R.id.imageDeer);
        locationRequest = LocationRequest.create();
        locationRequest = LocationRequest.create();
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);


        raschet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // проверяем предоставлено ли разрешение
                if (getApplicationContext().checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(MainActivity.this, "Разрешение уже получено", Toast.LENGTH_SHORT).show();
                    Toast.makeText(MainActivity.this, "Получам данные , пожалуйста подождите", Toast.LENGTH_LONG).show();
                    if (GpsEnable()) {
                        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                .requestLocationUpdates(locationRequest, new LocationCallback() {
                                    @Override
                                    public void onLocationResult(@NonNull LocationResult locationResult) {
                                        super.onLocationResult(locationResult);
                                        LocationServices.getFusedLocationProviderClient(MainActivity.this)
                                                .removeLocationUpdates(this);
                                        if (locationResult != null && locationResult.getLocations().size() > 0) {

                                            int index = locationResult.getLocations().size() - 1;
                                            latitude = locationResult.getLocations().get(index).getLatitude();   // широта
                                            longitude = locationResult.getLocations().get(index).getLongitude(); // долгота

                                            String key = "235b5cf0d0862de0784d2e0dcd62cc93";
                                            String url1 = "https://api.openweathermap.org/data/2.5/weather?q=" + getGeocoder() + "&appid=" + key + "&units=metric&lang=ru";
                                            new GetUrlData().execute(url1);
                                        }
                                    }
                                }, Looper.getMainLooper());

                    } else {
                        turnOnGps();
                    }
                } else {
                    requestPermissions(new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, 2);
                }

            }
        });
    }

    // просим включить GPS
    private void turnOnGps() {


        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder().addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(getApplicationContext())
                .checkLocationSettings(builder.build());

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {

                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    Toast.makeText(MainActivity.this, "GPS уже включен", Toast.LENGTH_SHORT).show();

                } catch (ApiException e) {

                    switch (e.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:

                            try {
                                ResolvableApiException resolvableApiException = (ResolvableApiException) e;
                                resolvableApiException.startResolutionForResult(MainActivity.this, 2);
                            } catch (IntentSender.SendIntentException ex) {
                                ex.printStackTrace();
                            }
                            break;

                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            //Device does not have location
                            break;
                    }
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        // проверяем предоставлено ли разрешение
        if (requestCode == 2) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Toast.makeText(MainActivity.this, "Разрешение успешно получено", Toast.LENGTH_SHORT).show();
                // запрашиваем разрешение
            } else {
                Toast.makeText(MainActivity.this, "Отказано в разрешении", Toast.LENGTH_SHORT).show();
                // System.out.println("Похоже я сломвлся");


            }
        }

    }

    private boolean GpsEnable() {
        LocationManager locationManager = null;
        boolean gpsOn = false;

        if (locationManager == null) {
            locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        }
        gpsOn = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        return gpsOn;

    }

    public String getGeocoder() {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            cityName = addresses.get(0).getLocality();
            return cityName;

        } catch (Exception e) {
            System.out.println("Ошибка " + e);
            return null;
        }

    }


    //  Обрабатываем запрос / создаем соединение
    class GetUrlData extends AsyncTask<String, String, String> {


        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            wind.setText("Ожидайте...");
            temp.setText("Ожидайте...");
            weather.setText("Ожидайте...");

        }

        @Override
        protected String doInBackground(String... strings) {
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                URL url = new URL(strings[0]);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));

                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
                return buffer.toString();
            } catch (IOException e) {
                e.getMessage();
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (IOException e) {
                    e.getMessage();
                }
            }
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                try {
                    super.onPostExecute(result);

                    JSONObject json = new JSONObject(result);
                    temp.setText("" + json.getJSONObject("main").getDouble("temp") + "°C");
                    wind.setText("Ветер: " + json.getJSONObject("wind").getDouble("speed") + "м/с");
                    weather.setText(json.getJSONArray("weather").getJSONObject(0).getString("description"));
                    city.setText(getGeocoder());

                } catch (JSONException e) {
                    e.getMessage();
                }
            } else {
                temp.setText("404");
                wind.setText("not found");

                weather.setText("X_X");
            }

        }
    }

}

