package com.navigation.sharedelement;

import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.navigation.R;

/**
 * Adapts Views containing kittens to RecyclerView cells
 *
 * @author bherbst
 */
public class KittenGridAdapter extends RecyclerView.Adapter<KittenViewHolder> {
    private final int mSize;
    private final KittenClickListener mListener;

    /**
     * Constructor
     * @param size The number of kittens to show
     * @param listener A listener for kitten click events
     */
    public KittenGridAdapter(int size, KittenClickListener listener) {
        mSize = size;
        mListener = listener;
    }

    @Override
    public KittenViewHolder onCreateViewHolder(ViewGroup container, int position) {
        LayoutInflater inflater = LayoutInflater.from(container.getContext());
        View cell = inflater.inflate(R.layout.item_grid, container, false);

        return new KittenViewHolder(cell);
    }

    @Override
    public void onBindViewHolder(final KittenViewHolder viewHolder, final int position) {
        switch (position % 6) {
            case 0:
                viewHolder.image.setImageResource(R.drawable.placekitten_1);
                break;
            case 1:
                viewHolder.image.setImageResource(R.drawable.placekitten_2);
                break;
            case 2:
                viewHolder.image.setImageResource(R.drawable.placekitten_3);
                break;
            case 3:
                viewHolder.image.setImageResource(R.drawable.placekitten_4);
                break;
            case 4:
                viewHolder.image.setImageResource(R.drawable.placekitten_5);
                break;
            case 5:
                viewHolder.image.setImageResource(R.drawable.placekitten_6);
                break;
        }

        // It is important that each shared element in the source screen has a unique transition name.
        // For example, we can't just give all the images in our grid the transition name "kittenImage"
        // because then we would have conflicting transition names.
        // By appending "_image" to the position, we can support having multiple shared elements in each
        // grid cell in the future.
        ViewCompat.setTransitionName(viewHolder.image, String.valueOf(position) + "_image");

        viewHolder.image.setOnClickListener(v -> mListener.onKittenClicked(viewHolder, position));
    }

    @Override
    public int getItemCount() {
        return mSize;
    }

}
