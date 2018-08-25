package com.neosolusi.expresslingua.features.group.listcontact;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.ViewModel;

import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.data.entity.Contact;
import com.neosolusi.expresslingua.data.entity.Group;
import com.neosolusi.expresslingua.data.entity.Member;
import com.neosolusi.expresslingua.data.repo.ContactRepository;
import com.neosolusi.expresslingua.data.repo.GroupRepository;
import com.neosolusi.expresslingua.data.repo.MemberRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;

public class ListContactViewModel extends ViewModel {
    private Realm mDatabase;
    private ContactRepository mContactRepo;
    private MemberRepository mMemberRepo;
    private GroupRepository mGroupRepo;
    private LiveData<RealmResults<Contact>> mContacts;

    public ListContactViewModel(ContactRepository contactRepository, MemberRepository memberRepository, GroupRepository groupRepository) {
        mDatabase = Realm.getDefaultInstance();
        mContactRepo = contactRepository;
        mMemberRepo = memberRepository;
        mGroupRepo = groupRepository;
        mContacts = mContactRepo.findAllEqualToAsync("isActive", true);
    }

    @Override protected void onCleared() {
        mDatabase.close();
    }

    public LiveData<RealmResults<Contact>> getContacts() {
        return mContacts;
    }

    public void addContacts(List<Contact> contacts) {
        List<Contact> filteredContact = new ArrayList<>();
        for (Contact contact : contacts) {
            if (mContactRepo.findFirstCopyEqualTo("phone", AppUtils.normalizePhone(contact.getPhone())) == null) {
                filteredContact.add(contact);
            }
        }
        mContactRepo.copyOrUpdate(filteredContact);
        mContactRepo.compareToService(mDatabase.copyFromRealm(mDatabase.where(Contact.class).equalTo("isActive", false).findAll()));
    }

    public Group findGroup(long id) {
        return mGroupRepo.findFirstEqualTo("id", id);
    }

    public void addGroup(Group group) {
        mGroupRepo.copyOrUpdate(group);
    }

    public void addMember(Member member) {
        mMemberRepo.copyOrUpdateAsync(member);
    }

    public Member findMember(String name, long groupId) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("user_id", name);
        map.put("group_id", groupId);

        return mMemberRepo.findFirstEqualTo(map);
    }

    public RealmResults<Member> findMembers(long groupId) {
        return mMemberRepo.findAllEqualTo("group_id", groupId);
    }

    public void removeMember(Member member) {
        mDatabase.executeTransaction(db -> member.deleteFromRealm());
    }

    public void clearGroupMembers(Group group) {
        mDatabase.executeTransaction(db -> {
            for (Member member : mMemberRepo.findAllEqualTo("group_id", group.getId())) {
                member.deleteFromRealm();
            }
        });
    }
}
