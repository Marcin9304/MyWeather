package com.example.myweather;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import org.json.JSONException;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    // Elementy widoku
    private TextView tvTemp, tvDesc, tvAdvice, tvLocation, tvHistory;
    private Button btnLogin;

    // Narzędzia
    private FusedLocationProviderClient fusedLocationClient;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;
    private WeatherDatabase dbHelper;

    // TODO: TUTAJ WKLEJ SWÓJ KLUCZ API!
    private final String API_KEY = "87902ba9b3eef5dfaee2df02ad629766";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Przypisanie widoków
        tvTemp = findViewById(R.id.tvTemp);
        tvDesc = findViewById(R.id.tvDesc);
        tvAdvice = findViewById(R.id.tvAdvice);
        tvLocation = findViewById(R.id.tvLocation);
        tvHistory = findViewById(R.id.tvHistory);
        btnLogin = findViewById(R.id.btnLogin);

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        dbHelper = new WeatherDatabase(this); // Połączenie z bazą

        setupBiometrics();

        // Start aplikacji - od razu pytamy o palec
        biometricPrompt.authenticate(promptInfo);

        btnLogin.setOnClickListener(v -> biometricPrompt.authenticate(promptInfo));
    }

    private void setupBiometrics() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(MainActivity.this, "Zalogowano!", Toast.LENGTH_SHORT).show();
                getLocation(); // Po sukcesie -> uruchom GPS
            }
            @Override
            public void onAuthenticationError(int err, @NonNull CharSequence str) {
                tvAdvice.setText("Błąd logowania: " + str);
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder().setTitle("MyWeather").setSubtitle("Autoryzacja").setNegativeButtonText("Anuluj").build();
    }

    private void getLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }
        tvDesc.setText("Pobieranie GPS...");
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        tvLocation.setText("Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
                        // Mając GPS, pobieramy pogodę z Internetu
                        getWeather(location.getLatitude(), location.getLongitude());
                    } else {
                        tvDesc.setText("Błąd GPS (włącz mapy Google)");
                    }
                });
    }

    private void getWeather(double lat, double lon) {
        String url = "https://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&appid=" + API_KEY + "&units=metric&lang=pl";

        RequestQueue queue = Volley.newRequestQueue(this);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        // 1. Parsowanie danych z Internetu

                        // --- NOWOŚĆ: POBIERANIE NAZWY MIASTA ---
                        String cityName = response.getString("name");

                        double temp = response.getJSONObject("main").getDouble("temp");
                        String desc = response.getJSONArray("weather").getJSONObject(0).getString("description");

                        // 2. Wyświetlenie danych
                        tvTemp.setText(Math.round(temp) + "°C");
                        tvDesc.setText(desc);

                        // --- NOWOŚĆ: PODMIANA WSPÓŁRZĘDNYCH NA MIASTO ---
                        tvLocation.setText(cityName);

                        // 3. LOGIKA BAZY DANYCH
                        String adviceFromDb = dbHelper.getMessageForTemp(temp);
                        tvAdvice.setText(adviceFromDb);

                        dbHelper.addHistory(temp);
                        tvHistory.setText(dbHelper.getLastReadings());

                    } catch (JSONException e) { tvDesc.setText("Błąd JSON"); }
                }, error -> tvDesc.setText("Błąd Internetu (Sprawdź klucz API)"));

        queue.add(jsonObjectRequest);
    }

    @Override
    public void onRequestPermissionsResult(int r, @NonNull String[] p, @NonNull int[] g) {
        super.onRequestPermissionsResult(r, p, g);
        if (r == 100 && g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) getLocation();
    }
}

