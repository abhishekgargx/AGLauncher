package com.abhishek.aglauncher;

// Copyright 2020 , Abhishek Garg

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.Random;

public class PermissionHelper {

    private int PERMISSION_REQUEST_CODE;
    private Context context;
    // all permissions
    private String[] allPermissions = null;
    private Callback callback;
    private boolean shouldShowRequestPermissionRationale = true;
    private boolean askAgainIfPermissionDenied = false;
    private String permissionName = "this";
    private boolean showDenyDialog = false;
    private String denyDialogMsg = "This permission is necessary to run app smoothly.";

    public PermissionHelper(Context context, String... allPermissions) {
        this.context = context;
        this.allPermissions = allPermissions;
        PERMISSION_REQUEST_CODE = new Random().nextInt(900);
    }

    public static String[] getStoragePermissionList() {
        return new String[]{
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
    }

    public static String[] getLocationPermissionList() {
        return new String[]{
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
        };
    }

    public PermissionHelper setCallback(Callback callback) {
        this.callback = callback;
        return this;
    }

    public boolean checkAllPermissionGrantedOrNot() {
        for (String permission : allPermissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public void askForPermissions() {
        if (checkAllPermissionGrantedOrNot()) {
            doSuccessCall();
        } else {
            ActivityCompat.requestPermissions(getActivity(), allPermissions, PERMISSION_REQUEST_CODE);
        }
    }

    public PermissionHelper shouldShowDialogForNeverAskAgain(boolean show) {
        shouldShowRequestPermissionRationale = show;
        return this;
    }

    public PermissionHelper shouldAskAgainIfPermissionDenied(boolean allowed) {
        askAgainIfPermissionDenied = allowed;
        return this;
    }

    private void doFailureCall() {
        if (callback != null)
            callback.failure();
    }

    private void doSuccessCall() {
        if (callback != null)
            callback.success();
    }

    public PermissionHelper showCustomDenyDialogBeforeAskingAgain(String message) {
        showDenyDialog = true;
        if (message != null)
            denyDialogMsg = message;
        return this;
    }

    private Activity getActivity() {
        return (Activity) context;
    }

    public PermissionHelper setPermissionName(String permissionName) {
        this.permissionName = permissionName;
        return this;
    }

    public void handlePermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        if (requestCode == PERMISSION_REQUEST_CODE) {
            boolean everyPermissionGranted = true;
            boolean isNeverAskAgainSelected = false;

            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    everyPermissionGranted = false;
                    break;
                }
            }
            // check if never ask chosen for any permission
            for (String permission : allPermissions) {
                if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permission)) {
                    isNeverAskAgainSelected = true;
                    break;
                }
            }

            // all permission granted
            if (everyPermissionGranted) {
                doSuccessCall();
            }
            /*
             * if never ask again ticked
             * means we can not do anything
             * user have to go in app setting to manually gave permission
             */
            else if (isNeverAskAgainSelected) {
                if (shouldShowRequestPermissionRationale) {
                    singleBtnDialog(context, "You selected Never Ask again", "Hence you need to gave " + permissionName + " permission manually via app settings", new SingleBtnDialogListener() {
                        @Override
                        public void onOkayPress() {
                            doFailureCall();
                        }
                    });
                } else {
                    doFailureCall();
                }
            }
            // means user denied request, simply ask them again
            else {
                if (askAgainIfPermissionDenied) {
                    if (showDenyDialog) {
                        singleBtnDialog(context, "Alert", denyDialogMsg, new SingleBtnDialogListener() {
                            @Override
                            public void onOkayPress() {
                                askForPermissions();
                            }
                        });
                    } else {
                        askForPermissions();
                    }
                } else {
                    doFailureCall();
                }
            }
        }
    }

    private void singleBtnDialog(Context context,
                                 String title,
                                 String message,
                                 SingleBtnDialogListener singleBtnDialogListener) {
        if (context != null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            if (title != null) {
                builder.setTitle(title);
            }
            if (message != null) {
                builder.setMessage(message);
            }
            builder.setPositiveButton("Okay", (dialog, which) -> singleBtnDialogListener.onOkayPress());
            builder.create().show();
        }
    }

    public interface Callback {
        void success();

        void failure();
    }

    public interface SingleBtnDialogListener {

        void onOkayPress();

    }

}
