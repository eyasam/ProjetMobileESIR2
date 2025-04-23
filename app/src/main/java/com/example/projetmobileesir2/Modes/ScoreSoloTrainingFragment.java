package com.example.projetmobileesir2.Modes;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.example.projetmobileesir2.Modes.Training.TrainingActivity;
import com.example.projetmobileesir2.Modes.SoloGame.SoloGameActivity;
import com.example.projetmobileesir2.R;

/**
 * Boîte de dialogue affichée à la fin d'un défi solo ou entraînement.
 * Montre le score avec une animation et propose de rejouer ou continuer
 */

public class ScoreSoloTrainingFragment extends DialogFragment {

    public static ScoreSoloTrainingFragment newInstance(int score, String mode) {
        ScoreSoloTrainingFragment fragment = new ScoreSoloTrainingFragment();
        Bundle args = new Bundle();
        args.putInt("score", score);
        args.putString("mode", mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = requireActivity();

        int score = getArguments().getInt("score");
        String mode = getArguments().getString("mode");

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        LayoutInflater inflater = requireActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.dialog_score, null);

        TextView tvScore = view.findViewById(R.id.tvFinalScore);
        tvScore.setText("Partie Terminée \n Score : " + score );

        Animation fadeIn = AnimationUtils.loadAnimation(context, android.R.anim.fade_in);
        fadeIn.setDuration(800);
        tvScore.startAnimation(fadeIn);

        builder.setView(view)
                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent;
                    if ("entrainement".equals(mode)) {
                        intent = new Intent(context, TrainingActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    } else {
                        intent = new Intent(context, SoloGameActivity.class);

                    }
                    context.startActivity(intent);
                    requireActivity().finish();
                });

        return builder.create();
    }
}
