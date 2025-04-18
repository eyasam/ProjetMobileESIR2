package com.example.projetmobileesir2.Bluetooth;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import com.example.projetmobileesir2.Modes.MultiplayerGameActivity;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.UUID;

/**
 * Attend une connexion entrante, lit un message initial "READY" puis démarre l’activité multijoueur.
 * Lance également une écoute continue des messages entrants.
 */
public class BluetoothServerThread extends Thread {

    private final BluetoothServerSocket serverSocket;
    private final Context context;
    private static final UUID APP_UUID = UUID.fromString("8ce255c0-200a-11e0-ac64-0800200c9a66");

    /**
     * Initialise le serveur Bluetooth et prépare le socket d’écoute.
     */
    public BluetoothServerThread(BluetoothAdapter bluetoothAdapter, Context context) {
        this.context = context;
        BluetoothServerSocket tmp = null;

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.BLUETOOTH_CONNECT)
                == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            try {
                tmp = bluetoothAdapter.listenUsingRfcommWithServiceRecord("JeuESIR", APP_UUID);
            } catch (IOException e) {
                Log.e("APPPP_BT", "Erreur création socket serveur", e);
            }
        }

        serverSocket = tmp;
    }

    /**
     * Attend une connexion client, lit un message READY, puis lance l'activité multijoueur.
     * Lance également l'écoute asynchrone des messages.
     */
    @Override
    public void run() {
        if (serverSocket == null) {
            Log.e("APPPP_BT", "ServerSocket null");
            return;
        }

        try {
            Log.d("APPPP_BT", "En attente de connexion...");
            BluetoothSocket socket = serverSocket.accept();

            if (socket != null) {
                Log.d("APPPP_BT", "Connexion acceptée !");
                BluetoothConnectionHolder.setSocket(socket);

                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String message = reader.readLine();

                if ("READY".equals(message)) {
                    Log.d("APPPP_BT", "Client est prêt, lancement activité multijoueur !");
                    listenForMessages(socket);

                    if (context instanceof Activity) {
                        ((Activity) context).runOnUiThread(() -> {
                            Intent intent = new Intent(context, MultiplayerGameActivity.class);
                            intent.putExtra("isHost", true);
                            context.startActivity(intent);
                        });
                    }
                } else {
                    Log.e("APPPP_BT", "Message inattendu du client : " + message);
                }
            }

        } catch (IOException e) {
            Log.e("APPPP_BT", "Erreur accept() ou lecture", e);
        } finally {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Log.e("APPPP_BT", "Erreur fermeture serverSocket", e);
            }
        }
    }

    /**
     * Démarre un thread en arrière-plan pour lire les messages Bluetooth entrants
     * et les diffuser à l’app via un Intent local.
     */
    private void listenForMessages(BluetoothSocket socket) {
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
