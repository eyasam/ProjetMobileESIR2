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
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.projetmobileesir2.Modes.MultiplayerGame.MultiplayerGameActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.UUID;

/**
 * gère la connexion à un serveur Bluetooth, l'envoi initial du message READY,
 * la réception de messages asynchrones, et le démarrage de l'activité multijoueur.
 */
public class BluetoothClientThread extends Thread {

    private final BluetoothSocket socket;
    private final Context context;
    private static final UUID APP_UUID = UUID.fromString("83e2bb59-c086-4d86-a59d-a3cce53e64e2");

    /**
     * initialise un socket Bluetooth vers le device distant.
     */
    public BluetoothClientThread(BluetoothDevice device, Context context) {
        this.context = context;
        BluetoothSocket tmp = null;

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED) {
            try {
                tmp = device.createRfcommSocketToServiceRecord(APP_UUID);
            } catch (IOException e) {
                Log.e("APPPP_BT", "Erreur création socket", e);
            }
        }

        socket = tmp;
    }

    /**
     * démarre la connexion au serveur, envoie READY, écoute les messages entrants
     * et lance l'activité multijoueur.
     */
    @Override
    public void run() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                != PackageManager.PERMISSION_GRANTED) {
            Log.e("APPPP_BT", "Permission BLUETOOTH_CONNECT refusée");
            return;
        }

        try {
            Thread.sleep(500);
            socket.connect();
            Log.d("APPPP_BT", "Connexion réussie au serveur !");
            BluetoothConnectionHolder.setSocket(socket);

            OutputStream os = socket.getOutputStream();
            os.write("READY\n".getBytes());
            os.flush();

            listenForMessages();

            Intent intent = new Intent(context, MultiplayerGameActivity.class);
            intent.putExtra("isHost", false);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            Log.d("APPPP_BT_DEBUG", "Client va lancer MultiplayerGameActivity");
            context.startActivity(intent);

            if (context instanceof Activity) ((Activity) context).finish();

        } catch (IOException | InterruptedException e) {
            Log.e("APPPP_BT", "Connexion échouée", e);
            try {
                socket.close();
            } catch (IOException closeException) {
                Log.e("APPPP_BT", "Erreur fermeture socket", closeException);
            }
        }
    }

    /**
     * ecoute en continu les messages Bluetooth entrants via un BufferedReader,
     * puis les diffuse localement à l'app via un Intent.
     */
    private void listenForMessages() {
        new Thread(() -> {
            try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.d("APPPP_BT_RECEIVE", "Reçu sur socket : " + line);
                    Intent intent = new Intent("BLUETOOTH_MESSAGE");
                    intent.putExtra("message", line);
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                }
            } catch (IOException e) {
                Log.e("APPPP_BT", "Erreur réception message", e);
            }
        }).start();
    }

}
