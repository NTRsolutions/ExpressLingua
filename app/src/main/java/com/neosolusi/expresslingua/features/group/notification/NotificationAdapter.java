package com.neosolusi.expresslingua.features.group.notification;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Notification;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    private List<Notification> mDataset;

    public NotificationAdapter() {
        setHasStableIds(true);
        mDataset = new ArrayList<>();
    }

    public void update(List<Notification> members) {
        mDataset = members;
        notifyDataSetChanged();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override public void onBindViewHolder(ViewHolder holder, int position) {
        holder.bindTo(mDataset.get(position));
    }

    @Override public int getItemCount() {
        return mDataset == null ? 0 : mDataset.size();
    }

    @Override public long getItemId(int position) {
        return mDataset == null ? 0 : Long.valueOf(mDataset.get(position).getId());
    }

    @Override public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private TextView textTitle, textMessage;
        private Button buttonAccept, buttonReject;

        public ViewHolder(View itemView) {
            super(itemView);

//            imageView = itemView.findViewById(R.id.image_member);
//            textName = itemView.findViewById(R.id.text_member_name);
//            textStatus = itemView.findViewById(R.id.text_member_status);
        }

        public void bindTo(Notification notification) {
            textTitle.setText(notification.getIsi_pesan());
            textMessage.setText(notification.getJenis_pesan());
        }
    }
}
