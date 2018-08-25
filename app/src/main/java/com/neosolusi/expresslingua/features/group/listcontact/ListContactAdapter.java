package com.neosolusi.expresslingua.features.group.listcontact;

import android.content.Context;
import android.support.v4.content.ContextCompat;
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

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.OrderedCollectionChangeSet;
import io.realm.RealmResults;

public class ListContactAdapter extends RecyclerView.Adapter<ListContactAdapter.ViewHolder> {

    private Context mContext;
    private RealmResults<Contact> mDataset;
    private OnContactSelection mListener;
    private Set<Contact> mSelectedData;

    public ListContactAdapter(Context context, OnContactSelection listener) {
        setHasStableIds(true);
        mContext = context;
        mListener = listener;
        mSelectedData = new HashSet<>();
    }

    public void update(RealmResults<Contact> contacts) {
        if (mDataset != null) mDataset.removeAllChangeListeners();

        mDataset = contacts;
        mDataset.addChangeListener((updates, changeSet) -> {
            // `null`  means the async query returns the first time.
            if (changeSet == null) {
                notifyDataSetChanged();
                return;
            }

            // For deletions, the adapter has to be notified in reverse order.
            OrderedCollectionChangeSet.Range[] deletions = changeSet.getDeletionRanges();
            for (int i = deletions.length - 1; i >= 0; i--) {
                OrderedCollectionChangeSet.Range range = deletions[i];
                notifyItemRangeRemoved(range.startIndex, range.length);
            }

            OrderedCollectionChangeSet.Range[] insertions = changeSet.getInsertionRanges();
            for (OrderedCollectionChangeSet.Range range : insertions) {
                notifyItemRangeInserted(range.startIndex, range.length);
            }

            OrderedCollectionChangeSet.Range[] modification = changeSet.getChangeRanges();
            for (OrderedCollectionChangeSet.Range range : modification) {
                notifyItemRangeChanged(range.startIndex, range.length);
            }
        });
        notifyDataSetChanged();
    }

    public void remove(Contact contact) {
        mSelectedData.remove(contact);
        notifyItemChanged(mDataset.indexOf(contact));
    }

    public void add(Contact contact) {
        mSelectedData.add(contact);
        notifyDataSetChanged();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_contact, parent, false);
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

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView imageView, imageAttribute;
        private TextView textName, textPhone;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_contact_placeholder);
            imageAttribute = itemView.findViewById(R.id.image_contact_attribute);
            textName = itemView.findViewById(R.id.text_contact_name);
            textPhone = itemView.findViewById(R.id.text_contact_phone);
        }

        public void bindTo(Contact contact) {
            textName.setText(contact.getName());
            textPhone.setText(contact.getPhone());
            imageView.setImageDrawable(ContextCompat.getDrawable(mContext, R.drawable.ic_person));

            if (mSelectedData.contains(contact)) {
                imageAttribute.setVisibility(View.VISIBLE);
            } else {
                imageAttribute.setVisibility(View.GONE);
            }

            itemView.setOnClickListener(v -> {
                if (mSelectedData.contains(contact)) {
                    animateSelected(false);
                    imageAttribute.setVisibility(View.GONE);
                    mSelectedData.remove(contact);
                    mListener.onSelect(contact, false);
                } else {
                    imageAttribute.setVisibility(View.VISIBLE);
                    animateSelected(true);
//                    mSelectedData.add(contact);
                    mListener.onSelect(contact, true);
                }
            });

//            File audioPath = new File(mContext.getFilesDir(), "avatar");
//            File audioFile = new File(audioPath, group.getUrl());
//            Uri uri = Uri.fromFile(audioFile);
//            Picasso.get().load("").placeholder(R.drawable.ic_person).into(imageView);
        }

        private void animateSelected(boolean select) {
            float xAxisFrom, xAxisTo;
            float yAxisFrom, yAxisTo;

            if (select) {
                xAxisFrom = 0f; xAxisTo = 1f;
                yAxisFrom = 0f; yAxisTo = 1f;
            } else {
                xAxisFrom = 1f; xAxisTo = 0f;
                yAxisFrom = 1f; yAxisTo = 0f;
            }

            Animation anim = new ScaleAnimation(
                    xAxisFrom, xAxisTo, // Start and end values for the X axis scaling
                    yAxisFrom, yAxisTo, // Start and end values for the Y axis scaling
                    Animation.RELATIVE_TO_SELF, 0.5f, // Pivot point of X scaling
                    Animation.RELATIVE_TO_SELF, 0.5f); // Pivot point of Y scaling
            anim.setFillAfter(true); // Needed to keep the result of the animation
            anim.setDuration(200);
            anim.setInterpolator(new DecelerateInterpolator());
            imageAttribute.startAnimation(anim);
        }
    }

    public interface OnContactSelection {
        void onSelect(Contact contact, boolean isSelect);
    }
}
