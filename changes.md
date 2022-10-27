# Changes to part 1 of the server

1) added 3 switches to the activity_main.xml

```plaintext
    <com.google.android.material.switchmaterial.SwitchMaterial
        android:id="@+id/swMainBleEnabled"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginStart="16dp"
        android:layout_marginTop="16dp"
        android:layout_marginEnd="16dp"
        android:checked="false"
        android:clickable="false"
        android:text="Bluetooth is enabled"
        android:textSize="18sp" />

    <com.google.android.material.switchmaterial.SwitchMaterial
        android:id="@+id/swMainAdvertisingActive"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginStart="16dp"
        android:layout_marginTop="4dp"
        android:layout_marginEnd="16dp"
        android:checked="false"
        android:clickable="false"
        android:text="Advertising is active"
        android:textSize="18sp" />

    <com.google.android.material.switchmaterial.SwitchMaterial
        android:id="@+id/swMainDeviceConnected"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginStart="16dp"
        android:layout_marginTop="4dp"
        android:layout_marginEnd="16dp"
        android:checked="false"
        android:clickable="false"
        android:text="Device is connected"
        android:textSize="18sp" />
```

2) added switches to MainActivity.java

for switch BluetoothEnabled:

3) in onResume checked for BluetoothEnabled and set switch

```plaintext
    protected void onResume() {
        super.onResume();

        if (!isBluetoothEnabled()) {
            bluetoothEnabled.setChecked(false); // added in part 2
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        } else {
            bluetoothEnabled.setChecked(true); // added in part 2
            checkPermissions();
        }
    }
```

for switch AdvertisingActive:

4) in BluetoothServer.java added 2 constants

```plaintext
    // Intent constants
    public static final String BLUETOOTH_HANDLER_ADVERTISER = "androidcrypto.bluetoothhandler.advertiser";
    public static final String BLUETOOTH_HANDLER_ADVERTISER_EXTRA = "androidcrypto.bluetoothhandler.advertiser.extra";
```

in BluetoothServer.java in BluetoothPeripheralManagerCallback add a Broadcast-Intent with 
values ON or OFF:

```plaintext
        @Override
        public void onAdvertisingStarted(@NotNull AdvertiseSettings settingsInEffect) {
            Intent intent = new Intent(BLUETOOTH_HANDLER_ADVERTISER);
            intent.putExtra(BLUETOOTH_HANDLER_ADVERTISER_EXTRA, "ON");
            sendToMain(intent);
        }

        @Override
        public void onAdvertiseFailure(@NotNull AdvertiseError advertiseError) {
            Intent intent = new Intent(BLUETOOTH_HANDLER_ADVERTISER);
            intent.putExtra(BLUETOOTH_HANDLER_ADVERTISER_EXTRA, "OFF");
            sendToMain(intent);
        }

        @Override
        public void onAdvertisingStopped() {
            Intent intent = new Intent(BLUETOOTH_HANDLER_ADVERTISER);
            intent.putExtra(BLUETOOTH_HANDLER_ADVERTISER_EXTRA, "OFF");
            sendToMain(intent);
        }
```

and append a sendToMain method:

```plaintext
    /**
     * section for broadcast
     */

    private void sendToMain(@NotNull Intent intent) {
        mContext.sendBroadcast(intent);
    }
}
```

as the sendToMain method needs a context we have store the conext provided on 
BluetoothServer getInstance in a class variable:

```plaintext
private static Context mContext;

    public static synchronized BluetoothServer getInstance(Context context) {
        mContext = context; // new in part 2
        if (instance == null) {
            instance = new BluetoothServer(context.getApplicationContext());
        }
        return instance;
    }

```

5) now the advertising status is send in a Broadcast but we do need a Receiver to 
show the information on UI

in MainActivity.java add a BroadcastReceiver:

```plaintext
    /**
     * section for broadcast
     */

    private final BroadcastReceiver advertiserStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String advertiserStatus = intent.getStringExtra(BluetoothServer.BLUETOOTH_HANDLER_ADVERTISER_EXTRA);
            if (advertiserStatus == null) return;
            if (advertiserStatus.equals("ON")) {
                advertisingActive.setChecked(true);
            } else {
                advertisingActive.setChecked(false);
            }
        }
    };
```

acompanied with a register on startup and unregister on destroy of the app:

```plaintext
onCreate:
  registerReceiver(advertiserStateReceiver, new IntentFilter((BluetoothServer.BLUETOOTH_HANDLER_ADVERTISER)));

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(advertiserStateReceiver);
    }

```

