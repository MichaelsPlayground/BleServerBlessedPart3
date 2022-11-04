package de.androidcrypto.bleserverblessedpart3;

import static android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ;
import static android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY;

import static com.welie.blessed.BluetoothBytesParser.FORMAT_UINT8;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;

import com.welie.blessed.BluetoothBytesParser;
import com.welie.blessed.BluetoothCentral;
import com.welie.blessed.BluetoothPeripheralManager;
import com.welie.blessed.GattStatus;
import com.welie.blessed.ReadResponse;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import timber.log.Timber;

class BatteryService extends BaseService {

    // new in part 3
    public static final String BLUETOOTH_SERVER_BATTERY_LEVEL = "androidcrypto.bluetoothserver.batterylevel";
    public static final String BLUETOOTH_SERVER_BATTERY_LEVEL_EXTRA = "androidcrypto.bluetoothserver.batterylevel.extra";
    Context mContext;

    private static final UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    public static final UUID BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

    private @NotNull final BluetoothGattService service = new BluetoothGattService(BATTERY_SERVICE_UUID, SERVICE_TYPE_PRIMARY);
    private @NotNull final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID, PROPERTY_READ | PROPERTY_NOTIFY, PERMISSION_READ);
    private @NotNull final Handler handler = new Handler(Looper.getMainLooper());
    private @NotNull final Runnable notifyRunnable = this::notifyBatteryLevel;
    private int currentBL = 100; // battery level goes from 100 to 0

    public BatteryService(@NotNull BluetoothPeripheralManager peripheralManager, Context context) {
        super(peripheralManager);
        service.addCharacteristic(characteristic);
        characteristic.addDescriptor(getClientCharacteristicConfigurationDescriptor());
        mContext = context;
        // start the notifying on startup
        notifyBatteryLevel();
    }

    @Override
    public void onCentralDisconnected(@NotNull BluetoothCentral central) {
        if (noCentralsConnected()) {
            // note: as the battery service should run without any connection or interaction this could get commented out
            // stopNotifying();
        }
    }

    @Override
    public ReadResponse onCharacteristicRead(@NotNull BluetoothCentral central, @NotNull BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(BATTERY_LEVEL_CHARACTERISTIC_UUID)) {
            return new ReadResponse(GattStatus.SUCCESS, new byte[]{(byte) ((byte) currentBL & 0xFF)});
        }
        return super.onCharacteristicRead(central, characteristic);
    }

    @Override
    public void onNotifyingEnabled(@NotNull BluetoothCentral central, @NotNull BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(BATTERY_LEVEL_CHARACTERISTIC_UUID)) {
            // note: as the battery service should run without any connection or interaction this could get commented out
            // notifyBatteryLevel();
        }
    }

    @Override
    public void onNotifyingDisabled(@NotNull BluetoothCentral central, @NotNull BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(BATTERY_LEVEL_CHARACTERISTIC_UUID)) {
            // note: as the battery service should run without any connection or interaction this could get commented out
            // stopNotifying();
        }
    }

    private void notifyBatteryLevel() {
        currentBL += (int) - 1;

        final byte[] value = new byte[]{(byte) ((byte) currentBL & 0xFF)};
        notifyCharacteristicChanged(value, characteristic);
        sendBatteryLevelToUi(value);
        // stop the countdown on 0, in a real device this is not necessary as the device stops when battery is empty
        if (currentBL < 1) {
            handler.removeCallbacks(notifyRunnable);
        } else {
            handler.postDelayed(notifyRunnable, 1000);
        }

        Timber.i("new BL: %d", currentBL);
    }

    private void sendBatteryLevelToUi(byte[] value) {
        BluetoothBytesParser parser = new BluetoothBytesParser(value);
        String valueString = parser.getIntValue(FORMAT_UINT8).toString();
        Intent intent = new Intent(BLUETOOTH_SERVER_BATTERY_LEVEL);
        intent.putExtra(BLUETOOTH_SERVER_BATTERY_LEVEL_EXTRA, valueString);
        mContext.sendBroadcast(intent);
    }

    private void stopNotifying() {
        handler.removeCallbacks(notifyRunnable);
    }

    @Override
    public @NotNull BluetoothGattService getService() {
        return service;
    }

    @Override
    public String getServiceName() {
        return "Battery Service";
    }
}
