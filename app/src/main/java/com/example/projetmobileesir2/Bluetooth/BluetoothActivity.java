package com.example.projetmobileesir2.Bluetooth;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.projetmobileesir2.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * permet d'activer le Bluetooth
 * scanner les appareils à proximité
 * établir une connexion client/serveur
 */

public class BluetoothActivity extends AppCompatActivity {

    // private static final int REQUEST_ENABLE_BT = 1;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothServerThread serverThread;

    private ActivityResultLauncher<Intent> enableBluetoothLauncher;

    private ListView deviceListView;
    private ArrayAdapter<String> deviceListAdapter;
    private final List<String> deviceDisplayList = new ArrayList<>();
    private final Set<String> foundDeviceAddresses = new HashSet<>();

    private final ActivityResultLauncher<String[]> permissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(), result -> {
                boolean allGranted = true;
                for (Boolean granted : result.values()) {
                    allGranted = allGranted && granted;
                }
                if (allGranted) {
                    startClassicScan();
                } else {
                    Toast.makeText(this, "Permissions refusées", Toast.LENGTH_SHORT).show();
                }
            });
    @SuppressLint("MissingPermission")
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if (device != null) {

                    String name = "Inconnu";
                    String address = "";

                    if (hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
                        if (device.getName() != null) {
                            name = device.getName();
                        }
                        address = device.getAddress();
                    } else {
                        Log.w("BT", "Permission manquante pour getName / getAddress");
                        return;
                    }

                    if (!foundDeviceAddresses.contains(address)) {
                        String display = name + " - " + address;
                        foundDeviceAddresses.add(address);
                        runOnUiThread(() -> {
                            deviceDisplayList.add(display);
                            deviceListAdapter.notifyDataSetChanged();
                        });
                        Log.d("BT", "Appareil trouvé : " + display);
                    }
                }
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        // initialisation Bluetooth
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth non supporté", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // demander à rendre l'appareil visible pour les autres
        Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300); // 5 minutes
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) == PackageManager.PERMISSION_GRANTED) {
            startActivity(discoverableIntent);
        } else {
            Log.e("BT", "Permission BLUETOOTH_ADVERTISE manquante !");
            // Tu peux aussi appeler permissionLauncher ici si besoin
        }

        deviceListView = findViewById(R.id.deviceListView);
        deviceListAdapter = new ArrayAdapter<>(this, R.layout.list_item_device, R.id.deviceName, deviceDisplayList);
        deviceListView.setAdapter(deviceListAdapter);

        // clic sur appareil détecté = tentative de connexion
        deviceListView.setOnItemClickListener((parent, view, position, id) -> {
            String item = deviceDisplayList.get(position);
            String[] parts = item.split(" - ");
            if (parts.length == 2) {
                String macAddress = parts[1];
                BluetoothDevice device = bluetoothAdapter.getRemoteDevice(macAddress);
                BluetoothClientThread clientThread = new BluetoothClientThread(device, this);
                clientThread.start();
            }
        });

        findViewById(R.id.startScanButton).setOnClickListener(v -> checkPermissionsAndStart());
        findViewById(R.id.stopScanButton).setOnClickListener(v -> stopClassicScan());

        // bluetooth enable launcher
        enableBluetoothLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
                        Toast.makeText(this, "Bluetooth activé", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Bluetooth non activé", Toast.LENGTH_SHORT).show();
                    }
                });

        // lancer le serveur Bluetooth
        serverThread = new BluetoothServerThread(bluetoothAdapter, this);
        serverThread.start();
    }


    private void checkPermissionsAndStart() {
        List<String> neededPermissions = new ArrayList<>();

        if (!hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            neededPermissions.add(Manifest.permission.BLUETOOTH_SCAN);
        }
        if (!hasPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
            neededPermissions.add(Manifest.permission.BLUETOOTH_CONNECT);
        }
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            neededPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!hasPermission(Manifest.permission.BLUETOOTH_ADVERTISE)) {
            neededPermissions.add(Manifest.permission.BLUETOOTH_ADVERTISE);
        }

        if (neededPermissions.isEmpty()) {
            startClassicScan();
        } else {
            permissionLauncher.launch(neededPermissions.toArray(new String[0]));
        }
    }

    @SuppressLint("MissingPermission")
    private void startClassicScan() {
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            enableBluetoothLauncher.launch(enableBtIntent);
            return;
        }

        if (hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }

            deviceDisplayList.clear();
            foundDeviceAddresses.clear();
            deviceListAdapter.notifyDataSetChanged();

            IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
            registerReceiver(bluetoothReceiver, filter);

            bluetoothAdapter.startDiscovery();
            Toast.makeText(this, "Scan classique lancé...", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "Permission manquante pour le scan", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    private void stopClassicScan() {
        if (bluetoothAdapter != null && hasPermission(Manifest.permission.BLUETOOTH_SCAN)) {
            if (bluetoothAdapter.isDiscovering()) {
                bluetoothAdapter.cancelDiscovery();
            }
        }

        try {
            unregisterReceiver(bluetoothReceiver);
        } catch (IllegalArgumentException ignored) {
        }

        Toast.makeText(this, "Scan arrêté", Toast.LENGTH_SHORT).show();
    }

    private boolean hasPermission(String permission) {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopClassicScan();
    }

}