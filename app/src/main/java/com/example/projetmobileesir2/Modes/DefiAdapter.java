package com.example.projetmobileesir2.Modes;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.projetmobileesir2.R;

import java.util.List;

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
            case "Défi Secouer":
                iconImageView.setImageResource(R.drawable.ic_shake); // Mets une icône qui existe
                break;
            case "Défi Gyroscope":
                iconImageView.setImageResource(R.drawable.ic_gyroscope);
                break;
            case "Défi Quizz":
                iconImageView.setImageResource(R.drawable.ic_guess);
                break;
            case "Défi Mot":
                iconImageView.setImageResource(R.drawable.ic_enigme);
                break;
            case "Défi Snake":
                iconImageView.setImageResource(R.drawable.ic_snake);
                break;
            case "Défi Fruit":
                iconImageView.setImageResource(R.drawable.ic_fruit);
                break;
            default:
                iconImageView.setImageResource(R.drawable.ic_unknown);
                break;
        }

        return rowView;
    }
}
