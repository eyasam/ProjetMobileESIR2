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

    /**
     * Retourne le nombre d'éléments dans la liste des défis
     * @return
     */
    @Override
    public int getCount() {
        return defis.size();
    }

    /**
     * Retourne l'élément à une position donnée dans la liste des défis
     * @param position
     * @return
     */
    @Override
    public Object getItem(int position) {
        return defis.get(position);
    }

    /**
     * Retourne l'ID de l'élément à une position donnée
     * @param position
     * @return
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * Création de la vue pour chaque item dans la liste
     * Récupèration des références des vues dans le layout item_defi.xml
     * Récupèration du défi correspondant à la position
     * Remplissage du TextView avec le nom du défi
     * Sélection de  l'icône appropriée en fonction du défi
     * Renvoie la vue de l'élément à afficher dans la liste
     * @param position
     * @param convertView
     * @param parent
     * @return
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;

        if (rowView == null) {
            LayoutInflater inflater = LayoutInflater.from(context);
            rowView = inflater.inflate(R.layout.item_defi, parent, false);
        }

        TextView defiNameTextView = rowView.findViewById(R.id.defiNameTextView);
        ImageView iconImageView = rowView.findViewById(R.id.iconImageView);

        String defi = defis.get(position); //Récupèration du nom du défi à cette position dans la liste

        defiNameTextView.setText(defi); // Affichage le nom du défi dans le TextView


        switch (defi) {
            case "Shake It Up!":
                iconImageView.setImageResource(R.drawable.ic_shake); // Mets une icône qui existe
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
