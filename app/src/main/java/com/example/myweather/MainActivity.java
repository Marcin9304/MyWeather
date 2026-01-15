package com.example.myweather;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private TextView tvLocation, tvStatus, tvDbTest;
    private Button btnLogin;
    private FusedLocationProviderClient fusedLocationClient;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    // Nasza nowa baza danych
    private WeatherDatabase dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvLocation = findViewById(R.id.tvLocation);
        tvStatus = findViewById(R.id.tvStatus);
        tvDbTest = findViewById(R.id.tvDatabaseTest);
        btnLogin = findViewById(R.id.btnLogin);

        // Inicjalizacja GPS
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Inicjalizacja Bazy Danych
        dbHelper = new WeatherDatabase(this);

        setupBiometrics();
        // Automatyczny start logowania
        biometricPrompt.authenticate(promptInfo);

        btnLogin.setOnClickListener(v -> biometricPrompt.authenticate(promptInfo));
    }

    private void setupBiometrics() {
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                tvStatus.setText("Zalogowano! Pobieram dane...");
                getLocationAndTestDb();
            }
            @Override
            public void onAuthenticationError(int err, @NonNull CharSequence str) {
                tvStatus.setText("Błąd: " + str);
            }
        });
        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("MyWeather")
                .setSubtitle("Autoryzacja")
                .setNegativeButtonText("Anuluj")
                .build();
    }

    private void getLocationAndTestDb() {
        // 1. Sprawdzenie uprawnień GPS
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 100);
            return;
        }

        // 2. Pobranie lokalizacji
        fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        tvLocation.setText("GPS: " + location.getLatitude() + ", " + location.getLongitude());

                        // 3. TEST BAZY DANYCH
                        double testTemp = 5.0;
                        String messageFromDb = dbHelper.getMessageForTemp(testTemp);

                        tvDbTest.setText("Test Bazy (dla 5°C):\n" + messageFromDb);

                    } else {
                        tvLocation.setText("Błąd GPS (włącz mapy na emulatorze)");
                    }
                });
    }

    @Override
    public void onRequestPermissionsResult(int r, @NonNull String[] p, @NonNull int[] g) {
        super.onRequestPermissionsResult(r, p, g);
        if (r == 100 && g.length > 0 && g[0] == PackageManager.PERMISSION_GRANTED) {
            getLocationAndTestDb();
        }
    }
}