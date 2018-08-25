package com.neosolusi.expresslingua.features.group.group;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.data.entity.Group;
import com.neosolusi.expresslingua.data.entity.Member;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;
import io.realm.Realm;

import static com.neosolusi.expresslingua.AppConstants.BASE_URL_USER_PROFILE;

public class GroupAdapter extends RecyclerView.Adapter<GroupAdapter.ViewHolder> {

    private List<Member> mDataset;
    private Realm mDb;
    private Context mContext;
    private GroupMemberClickListener mListener;

    public GroupAdapter(Context context, GroupMemberClickListener listener) {
        setHasStableIds(true);
        mDataset = new ArrayList<>();
        mDb = Realm.getDefaultInstance();
        mContext = context;
        mListener = listener;
    }

    public void update(List<Member> members) {
        mDataset = members;
        notifyDataSetChanged();
    }

    @Override public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_row_member, parent, false);
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

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private CircleImageView imageView;
        private TextView textName, textStatus;

        public ViewHolder(View itemView) {
            super(itemView);

            imageView = itemView.findViewById(R.id.image_member);
            textName = itemView.findViewById(R.id.text_member_name);
            textStatus = itemView.findViewById(R.id.text_member_status);

            itemView.setOnClickListener(this);
        }

        public void bindTo(Member member) {
            textName.setText(member.getUser_id());
            textStatus.setVisibility(View.GONE);

            Group group = mDb.where(Group.class).equalTo("id", member.getGroup_id()).findFirst();
            if (group != null && group.getAdmin() != null) {
                if (member.getPermission() > 0) {
                    textStatus.setBackground(ContextCompat.getDrawable(mContext, R.drawable.shape_rounded_corner_blue));
                    textStatus.setTextColor(ContextCompat.getColor(mContext, R.color.white));
                    textStatus.setVisibility(View.VISIBLE);
                    textStatus.setText("Admin");
                }
            }

            if (member.getUrl() != null && !member.getUrl().trim().isEmpty()) {
                Picasso.get().load(BASE_URL_USER_PROFILE  + member.getUrl()).placeholder(R.drawable.ic_person).into(imageView);
            }
        }

        @Override public void onClick(View view) {
            mListener.onMemberClick(mDataset.get(getAdapterPosition()));
        }
    }

    public interface GroupMemberClickListener {
        void onMemberClick(Member member);
    }

}
