package com.neosolusi.expresslingua.features.group.listgroup;

import android.content.Context;
import android.net.Uri;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Group;
import com.squareup.picasso.Picasso;

import java.io.File;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.RealmResults;

public class ListGroupAdapter extends RecyclerView.Adapter<ListGroupAdapter.ViewHolder> {

    private RealmResults<Group> mDataset;
    private OnGroupClickListener mListener;
    private Context mContext;

    public ListGroupAdapter(Context context, RealmResults<Group> groups, OnGroupClickListener listener) {
        setHasStableIds(true);
        mListener = listener;
        mContext = context;
        update(groups);
    }

    public void update(RealmResults<Group> groups) {
        mDataset = groups;
        mDataset.addChangeListener((update, changeSet) -> notifyDataSetChanged());
        notifyDataSetChanged();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_group, parent, false);
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

    public interface OnGroupClickListener {
        void onClick(Group group);
    }

    public class ViewHolder extends RecyclerView.ViewHolder {

        private CircleImageView imageView;
        private TextView textName;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_group_placeholder);
            textName = itemView.findViewById(R.id.text_group_name);
        }

        public void bindTo(Group group) {
            textName.setText(group.getName());

//            File audioPath = new File(mContext.getFilesDir(), "avatar");
//            File audioFile = new File(audioPath, group.getUrl());
//            Uri uri = Uri.fromFile(audioFile);
//            Picasso.get().load(uri).placeholder(R.drawable.ic_person).into(imageView);

            Picasso.get().load("http://dev.expresslingua.com/expresslingua_api/public/group/" + group.getUrl()).placeholder(R.drawable.ic_people).into(imageView);

            itemView.setOnClickListener(v -> mListener.onClick(group));
        }
    }
}
