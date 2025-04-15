package com.example.projetmobileesir2.Bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.projetmobileesir2.Defis.DefiActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.UUID;

public class BluetoothServerThread extends Thread {
    private final BluetoothServerSocket serverSocket;
    private final Context context;

    private static final UUID APP_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    public BluetoothServerThread(BluetoothAdapter bluetoothAdapter, Context context) {
        this.context = context;
        BluetoothServerSocket tmp = null;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("JeuESIR", APP_UUID);
            } catch (IOException e) {
                Log.e("BT", "Erreur serveur socket", e);
            }
        } else {
            Log.e("BT", "Permission BLUETOOTH_CONNECT non accordée !");
        }

        serverSocket = tmp;
    }

    public void run() {
        if (serverSocket == null) {
            Log.e("BT", "ServerSocket non initialisé, arrêt du thread serveur.");
            return;
        }

        BluetoothSocket socket = null;
        try {
            Log.d("BT", "En attente de connexion...");
            socket = serverSocket.accept();

            if (socket != null) {
                Log.d("BT", "Connexion acceptée !");

                try {
                    InputStream inputStream = socket.getInputStream();
                    BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                    String message = reader.readLine();

                    if ("READY".equals(message)) {
                        Log.d("BT", "Message READY reçu !");
                        if (context instanceof Activity) {
                            ((Activity) context).runOnUiThread(() -> {
                                Intent intent = new Intent(context, DefiActivity.class);
                                context.startActivity(intent);
                            });
                        }
                    }

                } catch (IOException e) {
                    Log.e("BT", "Erreur lecture du message", e);
                }
            }

        } catch (IOException e) {
            Log.e("BT", "Erreur accept()", e);
        } finally {
            try {
                if (socket != null) socket.close();
                serverSocket.close();
            } catch (IOException e) {
                Log.e("BT", "Erreur fermeture socket serveur", e);
            }
        }
    }

    public void cancel() {
        try {
            serverSocket.close();
        } catch (IOException e) {
            Log.e("BT", "Erreur fermeture serveur socket", e);
        }
    }
}
