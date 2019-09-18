package com.navigation.sharedelement;

import android.view.View;
import android.widget.ImageView;

import com.navigation.R;

import androidx.recyclerview.widget.RecyclerView;

/**
 * ViewHolder for kitten cells in our grid
 *
 * @author bherbst
 */
public class KittenViewHolder extends RecyclerView.ViewHolder {
    ImageView image;

    public KittenViewHolder(View itemView) {
        super(itemView);
        image = (ImageView) itemView.findViewById(R.id.image);
    }
}
