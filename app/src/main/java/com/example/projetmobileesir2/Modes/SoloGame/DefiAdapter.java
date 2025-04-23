package com.example.projetmobileesir2.Modes.SoloGame;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.projetmobileesir2.R;

import java.util.List;

/**
 * adapter pour afficher une liste de défis avec leur nom et icône dans le mode solo
 */

public class DefiAdapter extends BaseAdapter {

    private final Context context;
    private final List<String> defis;

    public DefiAdapter(Context context, List<String> defis) {
        this.context = context;
        this.defis = defis;
    }

    @Override
    public int getCount() {
        return defis.size();
    }

    @Override
    public Object getItem(int position) {
        return defis.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.item_defi, parent, false);
        }

        TextView defiNameTextView = rowView.findViewById(R.id.defiNameTextView);
        ImageView iconImageView = rowView.findViewById(R.id.iconImageView);

        String defi = defis.get(position);

        defiNameTextView.setText(defi);


        switch (defi) {
            case "Shake It Up!":
                iconImageView.setImageResource(R.drawable.ic_shake);
                break;
            case "Target Spin":
                iconImageView.setImageResource(R.drawable.ic_gyroscope);
                break;
            case "Guess It Right":
                iconImageView.setImageResource(R.drawable.ic_guess);
                break;
            case "Mind Maze":
                iconImageView.setImageResource(R.drawable.ic_enigme);
                break;
            case "Snake":
                iconImageView.setImageResource(R.drawable.ic_snake);
                break;
            case "Slice Dash":
                iconImageView.setImageResource(R.drawable.ic_fruit);
                break;
            default:
                iconImageView.setImageResource(R.drawable.ic_unknown);
                break;
        }

        return rowView;
    }
}
