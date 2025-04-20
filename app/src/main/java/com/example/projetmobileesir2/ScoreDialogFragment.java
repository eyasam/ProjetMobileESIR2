package com.example.projetmobileesir2;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.projetmobileesir2.Modes.TrainingActivity;
import com.example.projetmobileesir2.R;
import com.example.projetmobileesir2.SelectionDefiActivity;

public class ScoreDialogFragment extends DialogFragment {

    /**
     * Crée une nouvelle instance de ScoreDialogFragment avec le score et le mode passés en arguments.
     *
     * @param score Le score final de l'utilisateur.
     * @param mode Le mode de jeu (par exemple, "entrainement" ou autre).
     * @return Un objet ScoreDialogFragment.
     */

    public static ScoreDialogFragment newInstance(int score, String mode) {
        ScoreDialogFragment fragment = new ScoreDialogFragment();
        Bundle args = new Bundle();
        args.putInt("score", score); // mettre le score dans les arguments
        args.putString("mode", mode); // mettre le mode dans les arguments
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Cette méthode est appelée pour créer le dialogue personnalisé.
     * on récupère les arguments passés lors de la création du fragment, on initialise et configure la TextView pour afficher le score
     * on crée une animation d'apparition pour le texte du score et on configure le dialogue avec la vue personnalisée
     * @param savedInstanceState Si le fragment est recréé, on peut récupérer l'état précédent ici.
     * @return Le dialogue avec l'interface graphique et le comportement définis.
     */

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = requireActivity(); // Récupère le contexte de l'activité actuelle

        int score = getArguments().getInt("score");
        String mode = getArguments().getString("mode");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_score, null);

        TextView tvScore = view.findViewById(R.id.tvFinalScore);
        tvScore.setText("Partie Terminée \uD83D\uDCA5 \n Score : " + score );

        // Animation d'apparition
        Animation fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        fadeIn.setDuration(800); // durée de l'animation
        tvScore.startAnimation(fadeIn);

        // Configuration du dialogue
        builder.setView(view)

                .setCancelable(false)// Le dialogue ne peut pas être annulé par un clic en dehors de OK
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent;
                    if ("entrainement".equals(mode)) {
                        // Si le mode est "entrainement", relance l'activité d'entraînement
                        intent = new Intent(context, TrainingActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    } else {
                        // Sinon, c'est en mode solo et on retourne à la sélection des défis
                        intent = new Intent(context, SelectionDefiActivity.class);

                    }
                    // Lancement de l'activity cible
                    context.startActivity(intent);
                    requireActivity().finish();  // Fermer l'activité du défi en cours
                });

        return builder.create();
    }
}
