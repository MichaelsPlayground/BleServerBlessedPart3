# Changes to part 1 of the server

1) added 4 switches and a (Material) EditText to the activity_main.xml

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

    <com.google.android.material.switchmaterial.SwitchMaterial
        android:id="@+id/swMainSubscriptionsEnabled"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginStart="16dp"
        android:layout_marginTop="4dp"
        android:layout_marginEnd="16dp"
        android:checked="false"
        android:clickable="false"
        android:text="Subscriptions are enabled"
        android:textSize="18sp" />
                
    <com.google.android.material.textfield.TextInputLayout
        android:id="@+id/etMainConnectionLogDecoration"
        style="@style/Widget.MaterialComponents.TextInputLayout.OutlinedBox"
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:layout_marginStart="8dp"
        android:layout_marginTop="16dp"
        android:layout_marginEnd="8dp"
        android:hint="connection log"
        android:visibility="visible"
        app:boxCornerRadiusBottomEnd="5dp"
        app:boxCornerRadiusBottomStart="5dp"
        app:boxCornerRadiusTopEnd="5dp"
        app:boxCornerRadiusTopStart="5dp"
        app:layout_constraintEnd_toEndOf="parent"
        app:layout_constraintStart_toStartOf="parent"
        app:layout_constraintTop_toTopOf="parent">
        <com.google.android.material.textfield.TextInputEditText
            android:id="@+id/etMainConnectionLog"
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:ellipsize="end"
            android:focusable="false"
            android:text=""
            android:textSize="14sp"
            android:visibility="visible"
            tools:ignore="KeyboardInaccessibleWidget" />
    </com.google.android.material.textfield.TextInputLayout>        
```

2) added switches and EditText to MainActivity.java

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
    public static final String BLUETOOTH_SERVER_ADVERTISER = "androidcrypto.bluetoothserver.advertiser";
    public static final String BLUETOOTH_SERVER_ADVERTISER_EXTRA = "androidcrypto.bluetoothserver.advertiser.extra";
```

in BluetoothServer.java in BluetoothPeripheralManagerCallback add a Broadcast-Intent with 
values ON or OFF:

```plaintext
        @Override
        public void onAdvertisingStarted(@NotNull AdvertiseSettings settingsInEffect) {
            Intent intent = new Intent(BLUETOOTH_SERVERADVERTISER);
            intent.putExtra(BLUETOOTH_SERVER_ADVERTISER_EXTRA, "ON");
            sendToMain(intent);
        }

        @Override
        public void onAdvertiseFailure(@NotNull AdvertiseError advertiseError) {
            Intent intent = new Intent(BLUETOOTH_SERVERADVERTISER);
            intent.putExtra(BLUETOOTH_SERVER_ADVERTISER_EXTRA, "OFF");
            sendToMain(intent);
        }

        @Override
        public void onAdvertisingStopped() {
            Intent intent = new Intent(BLUETOOTH_SERVER_ADVERTISER);
            intent.putExtra(BLUETOOTH_SERVER_ADVERTISER_EXTRA, "OFF");
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
            String advertiserStatus = intent.getStringExtra(BluetoothServer.SERVER_ADVERTISER_EXTRA);
            if (advertiserStatus == null) return;
            if (advertiserStatus.equals("ON")) {
                advertisingActive.setChecked(true);
            } else {
                advertisingActive.setChecked(false);
            }
        }
    };
```

accompanied with a register on startup and unregister on destroy of the app:

```plaintext
onCreate:
  registerReceiver(advertiserStateReceiver, new IntentFilter((BluetoothServer.BLUETOOTH_SERVER_ADVERTISER)));

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(advertiserStateReceiver);
    }

```

6) now add the functionality for the DeviceConnected switch

in BluetoothServer.java add new constants

```plaintext
    public static final String BLUETOOTH_SERVER_CONNECTION = "androidcrypto.bluetoothserver.connection";
    public static final String BLUETOOTH_SERVER_CONNECTION_EXTRA = "androidcrypto.bluetoothserver.connection.extra";
```

in BluetoothServer.java in BluetoothPeripheralManagerCallback add a Broadcast-Intent with
values ON or OFF:

```plaintext
        @Override
        public void onCentralConnected(@NotNull BluetoothCentral central) {
            for (Service serviceImplementation : serviceImplementations.values()) {
                serviceImplementation.onCentralConnected(central);
            }
            Intent intent = new Intent(BLUETOOTH_SERVER_CONNECTION);
            intent.putExtra(BLUETOOTH_SERVER_CONNECTION_EXTRA, "connected to MAC: " + central.getAddress());
            sendToMain(intent);
        }

        @Override
        public void onCentralDisconnected(@NotNull BluetoothCentral central) {
            for (Service serviceImplementation : serviceImplementations.values()) {
                serviceImplementation.onCentralDisconnected(central);
            }
            Intent intent = new Intent(BLUETOOTH_SERVER_CONNECTION);
            intent.putExtra(BLUETOOTH_SERVER_CONNECTION_EXTRA, "DISCONNECTED from MAC: " + central.getAddress());
            sendToMain(intent);
        }
```

in MainActivity.java add a BroadcastReceiver:

```plaintext
    private final BroadcastReceiver connectionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String connectionStatus = intent.getStringExtra(BluetoothServer.BLUETOOTH_SERVER_CONNECTION_EXTRA);
            if (connectionStatus == null) return;
            if (connectionStatus.contains("connected")) {
                deviceConnected.setChecked(true);
            } else {
                deviceConnected.setChecked(false);
            }
            //String newConnectionLog = connectionStatus + "\n" + connectionLog.getText().toString();
            //connectionLog.setText(newConnectionLog);
        }
    };
```

accompanied with a BroadcastReceiver register or unregister:

```plaintext
        registerReceiver(connectionStateReceiver, new IntentFilter((BluetoothServer.BLUETOOTH_SERVER_CONNECTION)));
        
        unregisterReceiver(connectionStateReceiver);
```

7) now we are adding a connection logfile 

in MainActivity.java BroadcastReceiver connectionStateReceiver remove the comment out 
on the last two lines:

```plaintext
    private final BroadcastReceiver connectionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String connectionStatus = intent.getStringExtra(BluetoothServer.BLUETOOTH_SERVER_CONNECTION_EXTRA);
            if (connectionStatus == null) return;
            if (connectionStatus.contains("connected")) {
                deviceConnected.setChecked(true);
            } else {
                deviceConnected.setChecked(false);
            }
            String newConnectionLog = connectionStatus + "\n" + connectionLog.getText().toString();
            connectionLog.setText(newConnectionLog);
        }
    };
```

8) adding the code for subscriptionsEnabled:

```plaintext
    MainAcrtivity:
    registerReceiver(subscriptionStateReceiver, new IntentFilter((BluetoothServer.BLUETOOTH_SERVER_SUBSCRIPTION)));
    unregisterReceiver(subscriptionStateReceiver);

    private final BroadcastReceiver subscriptionStateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String dataStatus = intent.getStringExtra(BluetoothServer.BLUETOOTH_SERVER_SUBSCRIPTION_EXTRA);
            if (dataStatus == null) return;
            if (dataStatus.contains("enabled")) {
                subscriptionsEnabled.setChecked(true);
            } else {
                subscriptionsEnabled.setChecked(false);
            }
            String newConnectionLog = dataStatus + "\n" 
                    + connectionLog.getText().toString();
            connectionLog.setText(newConnectionLog);
        }
    };
    
    BluetoothServer:
    public static final String BLUETOOTH_SERVER_SUBSCRIPTION = "androidcrypto.bluetoothserver.subscription";
    public static final String BLUETOOTH_SERVER_SUBSCRIPTION_EXTRA = "androidcrypto.bluetoothserver.subscription.extra";    
    
        @Override
        public void onNotifyingEnabled(@NotNull BluetoothCentral central, @NotNull BluetoothGattCharacteristic characteristic) {
            Service serviceImplementation = serviceImplementations.get(characteristic.getService());
            if (serviceImplementation != null) {
                serviceImplementation.onNotifyingEnabled(central, characteristic);
                // new in part 2
                Intent intent = new Intent(BLUETOOTH_SERVER_SUBSCRIPTION);
                intent.putExtra(BLUETOOTH_SERVER_SUBSCRIPTION_EXTRA, "subscription enabled for characteristic: " + characteristic.getUuid().toString());
                sendToMain(intent);
            }
        }

        @Override
        public void onNotifyingDisabled(@NotNull BluetoothCentral central, @NotNull BluetoothGattCharacteristic characteristic) {
            Service serviceImplementation = serviceImplementations.get(characteristic.getService());
            if (serviceImplementation != null) {
                serviceImplementation.onNotifyingDisabled(central, characteristic);
                // new in part 2
                Intent intent = new Intent(BLUETOOTH_SERVER_SUBSCRIPTION);
                intent.putExtra(BLUETOOTH_SERVER_SUBSCRIPTION_EXTRA, "subscription disabled for characteristic: " + characteristic.getUuid().toString());
                sendToMain(intent);
            }
        }
```

Now the MainActivity is showing the BluetoothEnabled, AdvertisingActive, DeviceConnect and SubscriptionsEnabled status and 
provides a logfile on the last connected and disconnected devices and subscriptions.

For a Battery Level Service see BluetoothLowEnergyInAndroidJavaPeripheral.