package com.example.myweather;

import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import java.util.concurrent.Executor;

public class MainActivity extends AppCompatActivity {
    private TextView tvStatus;
    private Button btnLogin;
    private Executor executor;
    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvStatus = findViewById(R.id.tvStatus);
        btnLogin = findViewById(R.id.btnLogin);
        // Konfiguracja biometrii
        executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(MainActivity.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        tvStatus.setText("Zalogowano pomyślnie!\nSystem gotowy.");
                        tvStatus.setTextColor(0xFF2E7D32); // Zielony kolor

                        Toast.makeText(getApplicationContext(), "Sukces!",
                                Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        tvStatus.setText("Błąd: " + errString);
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("MyWeather Security")
                .setSubtitle("Przyłóż palec")
                .setNegativeButtonText("Anuluj")
                .build();

        btnLogin.setOnClickListener(v -> biometricPrompt.authenticate(promptInfo));

        // Start automatyczny przy uruchomieniu
        biometricPrompt.authenticate(promptInfo);
    }
}