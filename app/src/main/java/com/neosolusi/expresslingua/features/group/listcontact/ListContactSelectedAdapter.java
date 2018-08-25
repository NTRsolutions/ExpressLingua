package com.neosolusi.expresslingua.features.group.listcontact;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;

import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Contact;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ListContactSelectedAdapter extends RecyclerView.Adapter<ListContactSelectedAdapter.ViewHolder> {

    private Context mContext;
    private List<Contact> mDataset;
    private OnContactRemove mListener;

    public ListContactSelectedAdapter(Context context, OnContactRemove listener) {
        setHasStableIds(true);
        mContext = context;
        mListener = listener;
    }

    public void update(List<Contact> contacts) {
        if (mDataset == null) {
            mDataset = contacts;
            notifyDataSetChanged();
        } else {
            DiffUtil.DiffResult result = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                @Override public int getOldListSize() {
                    return mDataset.size();
                }

                @Override public int getNewListSize() {
                    return contacts.size();
                }

                @Override public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                    return mDataset.get(oldItemPosition).compareTo(contacts.get(newItemPosition)) == 0;
                }

                @Override public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                    return true;
                }

                @Nullable @Override public Object getChangePayload(int oldItemPosition, int newItemPosition) {
                    return super.getChangePayload(oldItemPosition, newItemPosition);
                }
            }, false);
            result.dispatchUpdatesTo(this);
            mDataset = contacts;
        }
    }

    public void add(Contact contact) {
        if (mDataset == null) mDataset = new ArrayList<>();
        mDataset.add(contact);
        notifyItemInserted(mDataset.size());
    }

    public void remove(Contact contact) {
        notifyItemRemoved(mDataset.indexOf(contact));
        mDataset.remove(mDataset.indexOf(contact));
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_selected_contact, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindTo(mDataset.get(position));
    }

    @Override public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    @Override public long getItemId(int position) {
        return mDataset == null ? 0 : mDataset.get(position).getId();
    }

    @Override public int getItemViewType(int position) {
        return position;
    }

    public interface OnContactRemove {
        void onRemove(Contact contact);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView imageView, imageAttribute;
        private TextView textName;
        private ConstraintLayout content;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_contact_placeholder);
            textName = itemView.findViewById(R.id.text_contact_name);
            content = itemView.findViewById(R.id.content_selected_contact);
            imageAttribute = itemView.findViewById(R.id.image_contact_attribute);

//            Animation anim = new ScaleAnimation(
//                    0f, 1f, // Start and end values for the X axis scaling
//                    0f, 1f, // Start and end values for the Y axis scaling
//                    Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
//                    Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
//            anim.setFillAfter(true); // Needed to keep the result of the animation
//            anim.setDuration(200);
//            anim.setInterpolator(new DecelerateInterpolator());
//            content.startAnimation(anim);
        }

        public void bindTo(Contact contact) {
            textName.setText(contact.getName());
//            imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_person));
            imageAttribute.setOnClickListener(v -> {
//                Animation anim = new ScaleAnimation(
//                        1f, 0f, // Start and end values for the X axis scaling
//                        1f, 0f, // Start and end values for the Y axis scaling
//                        Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
//                        Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
//                anim.setFillAfter(true); // Needed to keep the result of the animation
//                anim.setDuration(200);
//                anim.setInterpolator(new DecelerateInterpolator());
//                content.startAnimation(anim);

                mListener.onRemove(contact);
            });
        }
    }
}
