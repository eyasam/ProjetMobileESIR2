package com.example.projetmobileesir2.Bluetooth;

import java.io.OutputStream;

/**
 * Classe utilitaire pour envoyer des messages via un OutputStream Bluetooth brut.
 * (Utilisé éventuellement dans des cas plus directs où le socket n’est pas dans le holder.)
 */
public class BluetoothMessageSender {
    public static OutputStream output;

    /**
     * Envoie un message texte via le flux de sortie Bluetooth.
     * Ne fait rien si le flux est null.
     */
    public static void send(String message) {
        try {
            if (output != null) {
                output.write((message + "\n").getBytes());
                output.flush();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
