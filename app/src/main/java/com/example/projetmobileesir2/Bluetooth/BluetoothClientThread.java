package com.example.projetmobileesir2.Bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.projetmobileesir2.Defis.DefiActivity;

import java.io.IOException;
import java.io.OutputStream;
import java.util.UUID;

public class BluetoothClientThread extends Thread {
    private final BluetoothSocket socket;
    private final BluetoothDevice device;
    private static final UUID APP_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");
    private final Context context;

    public BluetoothClientThread(BluetoothDevice device, Context context) {
        this.device = device;
        this.context = context;
        BluetoothSocket tmp = null;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
            try {
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
            } catch (IOException e) {
                Log.e("BT", "Erreur création socket", e);
            }
        } else {
            Log.e("BT", "Permission BLUETOOTH_CONNECT non accordée !");
        }
        socket = tmp;
    }

    public void run() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            Log.e("BT", "Pas de permission pour connecter");
            return;
        }

        try {
            Thread.sleep(500); // Petit délai pour éviter les soucis de timing
            socket.connect();
            Log.d("BT", "Connexion réussie au serveur !");

            OutputStream outputStream = socket.getOutputStream();
            outputStream.write("READY\n".getBytes());
            outputStream.flush(); // Très important pour garantir que le message est bien envoyé

            if (context instanceof Activity) {
                ((Activity) context).runOnUiThread(() -> {
                    Intent intent = new Intent(context, DefiActivity.class);
                    context.startActivity(intent);
                });
            }

        } catch (IOException | InterruptedException connectException) {
            Log.e("BT", "Connexion échouée", connectException);
            try {
                socket.close();
            } catch (IOException closeException) {
                Log.e("BT", "Erreur fermeture socket", closeException);
            }
        }
    }

    public void cancel() {
        try {
            socket.close();
        } catch (IOException e) {
            Log.e("BT", "Erreur fermeture socket", e);
        }
    }
}
