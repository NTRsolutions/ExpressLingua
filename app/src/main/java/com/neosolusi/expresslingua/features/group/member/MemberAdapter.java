package com.neosolusi.expresslingua.features.group.member;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.R;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_HEADER = 0;
    private static final int VIEW_TYPE_DETAIL = 1;

    private Context mContext;
    private List<MemberDetail> mDataSet;

    public MemberAdapter(Context context) {
        mContext = context;
    }

    public void update(List<MemberDetail> memberDetails) {
        mDataSet = memberDetails;
        notifyDataSetChanged();
    }

    @NonNull @Override public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutId = getLayoutIdByType(viewType);
        View view = LayoutInflater.from(mContext).inflate(layoutId, parent, false);
        view.setFocusable(true);

        if (viewType == VIEW_TYPE_HEADER) {
            return new HeaderAdapterViewHolder(view);
        } else {
            return new DetailAdapterViewHolder(view);
        }
    }

    @Override public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        switch (holder.getItemViewType()) {
            case VIEW_TYPE_HEADER:
                HeaderAdapterViewHolder headerViewHolder = (HeaderAdapterViewHolder) holder;
                headerViewHolder.bindTo(mDataSet.get(position));
                break;
            case VIEW_TYPE_DETAIL:
                DetailAdapterViewHolder detailViewHolder = (DetailAdapterViewHolder) holder;
                detailViewHolder.bindTo(mDataSet.get(position));
                break;
        }
    }

    @Override public int getItemCount() {
        return mDataSet == null ? 0 : mDataSet.size();
    }

    @Override public int getItemViewType(int position) {
        if (position == 0) {
            return VIEW_TYPE_HEADER;
        } else {
            return VIEW_TYPE_DETAIL;
        }
    }

    private int getLayoutIdByType(int viewType) {
        switch (viewType) {
            case VIEW_TYPE_HEADER: {
                return R.layout.item_row_member_header;
            }
            case VIEW_TYPE_DETAIL: {
                return R.layout.item_row_member_detail;
            }
            default:
                throw new IllegalArgumentException("Invalid view type, value of " + viewType);
        }
    }

    class HeaderAdapterViewHolder extends RecyclerView.ViewHolder {

        CircleImageView imageView;
        TextView textName;
        TextView textRedW, textOrangeW, textYellowW, textGreenW, textBlueW;
        TextView textRedS, textOrangeS, textYellowS, textGreenS, textBlueS;
        TextView textNotSeen, textSkipped, textIncorrect, textCorrect;
        TextView textNotSeenProgress, textSkippedProgress, textIncorrectProgress, textCorrectProgress;

        public HeaderAdapterViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.item_image_member);
            textName = itemView.findViewById(R.id.item_text_member_name);

            textRedW = itemView.findViewById(R.id.indikator_red);
            textOrangeW = itemView.findViewById(R.id.indikator_orange);
            textYellowW = itemView.findViewById(R.id.indikator_yellow);
            textGreenW = itemView.findViewById(R.id.indikator_green);
            textBlueW = itemView.findViewById(R.id.indikator_blue);

            textRedS = itemView.findViewById(R.id.sentence_indikator_red);
            textOrangeS = itemView.findViewById(R.id.sentence_indikator_orange);
            textYellowS = itemView.findViewById(R.id.sentence_indikator_yellow);
            textGreenS = itemView.findViewById(R.id.sentence_indikator_green);
            textBlueS = itemView.findViewById(R.id.sentence_indikator_blue);

            textNotSeen = itemView.findViewById(R.id.challenge_not_seen);
            textSkipped = itemView.findViewById(R.id.challenge_skipped);
            textIncorrect = itemView.findViewById(R.id.challenge_incorrect);
            textCorrect = itemView.findViewById(R.id.challenge_correct);

            textNotSeenProgress = itemView.findViewById(R.id.challenge_not_seen_progress);
            textSkippedProgress = itemView.findViewById(R.id.challenge_skipped_progress);
            textIncorrectProgress = itemView.findViewById(R.id.challenge_incorrect_progress);
            textCorrectProgress = itemView.findViewById(R.id.challenge_correct_progress);
        }

        void bindTo(MemberDetail detail) {
            textName.setText(detail.member.getUser_id());

            textRedW.setText(String.format(Locale.getDefault(), "%d", detail.progress.w_red));
            textOrangeW.setText(String.format(Locale.getDefault(), "%d", detail.progress.w_orange));
            textYellowW.setText(String.format(Locale.getDefault(), "%d", detail.progress.w_yellow));
            textGreenW.setText(String.format(Locale.getDefault(), "%d", detail.progress.w_green));
            textBlueW.setText(String.format(Locale.getDefault(), "%d", detail.progress.w_blue));

            textRedS.setText(String.format(Locale.getDefault(), "%d", detail.progress.s_red));
            textOrangeS.setText(String.format(Locale.getDefault(), "%d", detail.progress.s_orange));
            textYellowS.setText(String.format(Locale.getDefault(), "%d", detail.progress.s_yellow));
            textGreenS.setText(String.format(Locale.getDefault(), "%d", detail.progress.s_green));
            textBlueS.setText(String.format(Locale.getDefault(), "%d", detail.progress.s_blue));

            textNotSeen.setText(String.format(Locale.getDefault(), "%d", detail.progress.not_seen));
            textSkipped.setText(String.format(Locale.getDefault(), "%d", detail.progress.skipped));
            textIncorrect.setText(String.format(Locale.getDefault(), "%d", detail.progress.incorrect));
            textCorrect.setText(String.format(Locale.getDefault(), "%d", detail.progress.correct));

            textNotSeenProgress.setText(String.format(Locale.getDefault(), "%d%%", detail.progress.not_seen_percent));
            textSkippedProgress.setText(String.format(Locale.getDefault(), "%d%%", detail.progress.skipped_percent));
            textIncorrectProgress.setText(String.format(Locale.getDefault(), "%d%%", detail.progress.incorrect_percent));
            textCorrectProgress.setText(String.format(Locale.getDefault(), "%d%%", detail.progress.correct_percent));

            Picasso.get()
                    .load(AppConstants.BASE_URL_USER_PROFILE + detail.member.getUrl())
                    .placeholder(R.drawable.ic_person)
                    .into(imageView);
        }
    }

    class DetailAdapterViewHolder extends RecyclerView.ViewHolder {

        TextView textDifficulty, textWord, textSentence;
        ImageView imageView;

        public DetailAdapterViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.item_image_difficulty);
            textDifficulty = itemView.findViewById(R.id.item_text_member_difficulty);
            textWord = itemView.findViewById(R.id.item_text_member_word_count);
            textSentence = itemView.findViewById(R.id.item_text_member_sentence_count);
        }

        void bindTo(MemberDetail detail) {
            textDifficulty.setText(detail.difficulty);
            textWord.setText("" + detail.word);
            textSentence.setText("" + detail.sentence);

            switch (detail.difficulty.toLowerCase()) {
                case "sangat sulit":
                    imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.shape_indikator_red));
                    break;
                case "sulit":
                    imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.shape_indikator_orange));
                    break;
                case "agak mudah":
                    imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.shape_indikator_yellow));
                    break;
                case "mudah":
                    imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.shape_indikator_green));
                    break;
            }
        }
    }
}
