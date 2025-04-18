package com.example.projetmobileesir2.Bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Classe statique de gestion de la connexion Bluetooth active.
 * Fournit une interface simple pour envoyer des messages via le socket.
 */
public class BluetoothConnectionHolder {

    private static BluetoothSocket socket;

    /**
     * Définit le socket Bluetooth à utiliser pour toute communication.
     */
    public static void setSocket(BluetoothSocket s) {
        socket = s;
    }

    /**
     * Envoie un message texte via le socket Bluetooth.
     * Le message est suivi d’un saut de ligne pour faciliter la lecture côté réception.
     */
    public static void sendMessage(String msg) {
        if (socket == null) {
            Log.e("BT_SEND", "Socket NULL, message non envoyé : " + msg);
            return;
        }

        try {
            Log.d("BT_SEND", "Envoi message : " + msg);
            OutputStream os = socket.getOutputStream();
            os.write((msg + "\n").getBytes());
            os.flush();
        } catch (IOException e) {
            Log.e("BT_SEND", "Erreur d’envoi Bluetooth : " + msg, e);
        }
    }
}
