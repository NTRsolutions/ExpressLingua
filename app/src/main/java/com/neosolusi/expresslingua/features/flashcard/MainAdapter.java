package com.neosolusi.expresslingua.features.flashcard;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.neosolusi.expresslingua.R;

import java.util.ArrayList;
import java.util.List;

public class MainAdapter extends RecyclerView.Adapter<MainAdapter.ViewHolder> {

    private List<String> mDataSet = new ArrayList<>();
    private OnClickListener mListener;

    public MainAdapter(OnClickListener listener) {
        mListener = listener;

        mDataSet.add("One Word");
        mDataSet.add("Multiple Words");
        mDataSet.add("One Sentence");
        mDataSet.add("Multiple Sentences");

        notifyDataSetChanged();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_flashcard, parent, false);

        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        CardType cardType;

        switch (mDataSet.get(position).toUpperCase()) {
            case "ONE SENTENCE":
                cardType = CardType.SINGLE_SENTENCE;
                break;
            case "MULTIPLE WORDS":
                cardType = CardType.MULTIPLE_WORDS;
                break;
            case "MULTIPLE SENTENCES":
                cardType = CardType.MULTIPLE_SENTENCES;
                break;
            default:
                cardType = CardType.SINGLE_WORD;
        }

        holder.bindTo(mDataSet.get(position), cardType, mListener);
    }

    @Override public int getItemCount() {
        return mDataSet.size();
    }

    public enum CardType {
        SINGLE_WORD, SINGLE_SENTENCE,
        MULTIPLE_WORDS, MULTIPLE_SENTENCES,
        CHALLENGES
    }

    public interface OnClickListener {
        void onClick(View view, CardType cardType);

        void onMasteringClick(View view, CardType cardType, int mastering);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView textTitle;
        CardView layoutMastering;
        Button buttonRed, buttonOrange, buttonYellow, buttonGreen;

        public ViewHolder(View itemView) {
            super(itemView);

            textTitle = itemView.findViewById(R.id.item_text_flashcard_title);
            layoutMastering = itemView.findViewById(R.id.layout_mastering);
            buttonRed = itemView.findViewById(R.id.button_red);
            buttonOrange = itemView.findViewById(R.id.button_orange);
            buttonYellow = itemView.findViewById(R.id.button_yellow);
            buttonGreen = itemView.findViewById(R.id.button_green);
        }

        public void bindTo(final String item, CardType cardType, final OnClickListener listener) {
            textTitle.setText(item);

            if (item.equalsIgnoreCase("One Word") || item.equalsIgnoreCase("One Sentence")) {
                layoutMastering.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> listener.onClick(v, cardType));
            buttonRed.setOnClickListener(v -> listener.onMasteringClick(v, cardType, 1));
            buttonOrange.setOnClickListener(v -> listener.onMasteringClick(v, cardType, 2));
            buttonYellow.setOnClickListener(v -> listener.onMasteringClick(v, cardType, 3));
            buttonGreen.setOnClickListener(v -> listener.onMasteringClick(v, cardType, 4));
        }
    }

}
