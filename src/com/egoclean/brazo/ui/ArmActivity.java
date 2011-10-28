package com.egoclean.brazo.ui;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import com.egoclean.brazo.R;
import com.egoclean.brazo.adk.OutputController;
import com.egoclean.brazo.ui.widget.ArmView;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class ArmActivity extends FragmentActivity {
    private static final String TAG = "ArmActivity";
    private static final String ACTION_USB_PERMISSION = "com.egoclean.brazo.action.USB_PERMISSION";

    private UsbManager mUsbManager;
    private PendingIntent mPermissionIntent;
    private boolean mPermissionRequestPending;

    private UsbAccessory mAccessory;
    private ParcelFileDescriptor mFileDescriptor;
    private FileInputStream mInputStream;
    private FileOutputStream mOutputStream;

    private static final byte LED_SERVO_COMMAND = 2;

    private OutputController outputController;
    private View mNotConnected;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mUsbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(
                ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        registerReceiver(mUsbReceiver, filter);

        outputController = new OutputController(this);

        setContentView(R.layout.main);
        ArmView armView = (ArmView) findViewById(R.id.arm_view);
        armView.setAngleListener(outputController);

        mNotConnected = findViewById(R.id.lnl_not_connected);
        if (mAccessory != null) {
			showControls();
		} else {
			hideControls();
		}
    }

    private void showControls() {
        mNotConnected.setVisibility(View.GONE);
    }

    private void hideControls() {
        mNotConnected.setVisibility(View.VISIBLE);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mInputStream != null && mOutputStream != null) {
            showControls();
            return;
        }

        UsbAccessory[] accessories = mUsbManager.getAccessoryList();
        UsbAccessory accessory = (accessories == null ? null : accessories[0]);
        if (accessory != null) {
            if (mUsbManager.hasPermission(accessory)) {
                openAccessory(accessory);
            } else {
                synchronized (mUsbReceiver) {
                    if (!mPermissionRequestPending) {
                        mUsbManager.requestPermission(accessory,
                                mPermissionIntent);
                        mPermissionRequestPending = true;
                    }
                }
            }
        } else {
            Log.d(TAG, "mAccessory is null");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        closeAccessory();
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(mUsbReceiver);
        super.onDestroy();
    }

    private void openAccessory(UsbAccessory accessory) {
        mFileDescriptor = mUsbManager.openAccessory(accessory);
        if (mFileDescriptor != null) {
            mAccessory = accessory;
            FileDescriptor fd = mFileDescriptor.getFileDescriptor();
            mInputStream = new FileInputStream(fd);
            mOutputStream = new FileOutputStream(fd);
            Log.d(TAG, "accessory opened");
            showControls();
        } else {
            Log.d(TAG, "accessory open fail");
            hideControls();
        }
    }

    private void closeAccessory() {
        hideControls();
        try {
            if (mFileDescriptor != null) {
                mFileDescriptor.close();
            }
        } catch (IOException e) {
        } finally {
            mFileDescriptor = null;
            mAccessory = null;
        }
    }

    public void sendCommand(byte target, int value) {
        byte[] buffer = new byte[3];
        if (value > 255)
            value = 255;

        buffer[0] = ArmActivity.LED_SERVO_COMMAND;
        buffer[1] = target;
        buffer[2] = (byte) value;
        if (mOutputStream != null && buffer[1] != -1) {
            try {
                mOutputStream.write(buffer);
            } catch (IOException e) {
                Log.e(TAG, "write failed", e);
            }
        }
    }

    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                    if (intent.getBooleanExtra(
                            UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        openAccessory(accessory);
                    } else {
                        Log.d(TAG, "permission denied for accessory "
                                + accessory);
                    }
                    mPermissionRequestPending = false;
                }
            } else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
                UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
                if (accessory != null && accessory.equals(mAccessory)) {
                    closeAccessory();
                }
            }
        }
    };
}
