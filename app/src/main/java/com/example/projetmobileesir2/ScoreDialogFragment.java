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

    public static ScoreDialogFragment newInstance(int score, String mode) {
        ScoreDialogFragment fragment = new ScoreDialogFragment();
        Bundle args = new Bundle();
        args.putInt("score", score);
        args.putString("mode", mode);
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Context context = requireActivity();

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

        builder.setView(view)

                .setCancelable(false)
                .setPositiveButton("OK", (dialog, which) -> {
                    Intent intent;
                    if ("entrainement".equals(mode)) {
                        intent = new Intent(context, TrainingActivity.class);
                    } else {
                        intent = new Intent(context, SelectionDefiActivity.class);
                    }
                    context.startActivity(intent);
                    requireActivity().finish();  // Fermer l'activité du défi en cours
                });

        return builder.create();
    }
}
