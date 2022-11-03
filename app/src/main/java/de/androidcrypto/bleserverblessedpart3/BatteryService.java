package de.androidcrypto.bleserverblessedpart3;

import static android.bluetooth.BluetoothGattCharacteristic.PERMISSION_READ;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_NOTIFY;
import static android.bluetooth.BluetoothGattCharacteristic.PROPERTY_READ;
import static android.bluetooth.BluetoothGattService.SERVICE_TYPE_PRIMARY;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.os.Handler;
import android.os.Looper;

import com.welie.blessed.BluetoothCentral;
import com.welie.blessed.BluetoothPeripheralManager;
import com.welie.blessed.GattStatus;
import com.welie.blessed.ReadResponse;

import org.jetbrains.annotations.NotNull;

import java.util.UUID;

import timber.log.Timber;

class BatteryService extends BaseService {

    private static final UUID BATTERY_SERVICE_UUID = UUID.fromString("0000180F-0000-1000-8000-00805f9b34fb");
    private static final UUID BATTERY_LEVEL_CHARACTERISTIC_UUID = UUID.fromString("00002A19-0000-1000-8000-00805f9b34fb");

    private @NotNull final BluetoothGattService service = new BluetoothGattService(BATTERY_SERVICE_UUID, SERVICE_TYPE_PRIMARY);
    private @NotNull final BluetoothGattCharacteristic characteristic = new BluetoothGattCharacteristic(BATTERY_LEVEL_CHARACTERISTIC_UUID, PROPERTY_READ | PROPERTY_NOTIFY, PERMISSION_READ);
    private @NotNull final Handler handler = new Handler(Looper.getMainLooper());
    private @NotNull final Runnable notifyRunnable = this::notifyBatteryLevel;
    private int currentBL = 100; // battery level goes from 100 to 0

    public BatteryService(@NotNull BluetoothPeripheralManager peripheralManager) {
        super(peripheralManager);
        service.addCharacteristic(characteristic);
        characteristic.addDescriptor(getClientCharacteristicConfigurationDescriptor());
    }

    @Override
    public void onCentralDisconnected(@NotNull BluetoothCentral central) {
        if (noCentralsConnected()) {
            stopNotifying();
        }
    }

    @Override
    public ReadResponse onCharacteristicRead(@NotNull BluetoothCentral central, @NotNull BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(BATTERY_LEVEL_CHARACTERISTIC_UUID)) {
            //return new ReadResponse(GattStatus.SUCCESS, new byte[]{0x00, 0x40});
            return new ReadResponse(GattStatus.SUCCESS, new byte[]{(byte) ((byte) currentBL & 0xFF)});
            //return new ReadResponse(GattStatus.SUCCESS, new byte[]{0x40 & 0xFF});
        }
        return super.onCharacteristicRead(central, characteristic);
    }

    @Override
    public void onNotifyingEnabled(@NotNull BluetoothCentral central, @NotNull BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(BATTERY_LEVEL_CHARACTERISTIC_UUID)) {
            notifyBatteryLevel();
        }
    }

    @Override
    public void onNotifyingDisabled(@NotNull BluetoothCentral central, @NotNull BluetoothGattCharacteristic characteristic) {
        if (characteristic.getUuid().equals(BATTERY_LEVEL_CHARACTERISTIC_UUID)) {
            stopNotifying();
        }
    }

    private void notifyBatteryLevel() {
        currentBL += (int) - 10;

        final byte[] value = new byte[]{(byte) ((byte) currentBL & 0xFF)};
        notifyCharacteristicChanged(value, characteristic);
        // stop the countdown on 0
        if (currentBL < 1) {
            handler.removeCallbacks(notifyRunnable);
        } else {
            handler.postDelayed(notifyRunnable, 1000);
        }
        Timber.i("new BLr: %d", currentBL);
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
