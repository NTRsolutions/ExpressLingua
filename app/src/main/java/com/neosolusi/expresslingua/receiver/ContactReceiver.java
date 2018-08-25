package com.neosolusi.expresslingua.receiver;

import android.content.ContentResolver;
import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.provider.ContactsContract;

import com.neosolusi.expresslingua.AppExecutors;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.data.entity.Contact;
import com.neosolusi.expresslingua.data.repo.ContactRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import io.realm.Realm;

public class ContactReceiver extends ContentObserver {

    private Context mContext;
    private ContactRepository mContactRepo;

    public ContactReceiver(Context context, ContactRepository contactRepository) {
        super(null);
        this.mContactRepo = contactRepository;
        this.mContext = context;

        if (mContactRepo.isFetchNeeded()) readContact();
    }

    @Override public boolean deliverSelfNotifications() {
        return true;
    }

    @Override public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        readContact();
    }

    private void readContact() {
        AppExecutors.getInstance().diskIO().execute(() -> {
            Realm database = Realm.getDefaultInstance();
            ContentResolver cr = mContext.getContentResolver();
            Cursor cursor = cr.query(
                    ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                    new String[]{
                            ContactsContract.Contacts._ID,
                            ContactsContract.Contacts.DISPLAY_NAME,
                            ContactsContract.CommonDataKinds.Phone.NUMBER,
                            ContactsContract.RawContacts.ACCOUNT_TYPE
                    },
                    ContactsContract.RawContacts.ACCOUNT_TYPE + " <> 'google' ",
                    null, null);

            if (cursor == null) return;

            if (cursor.getCount() > 0) {
                List<Contact> contacts = new ArrayList<>();
                while (cursor.moveToNext()) {
                    String Phone_number = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    Contact contact = new Contact();
                    contact.setId(UUID.randomUUID().getMostSignificantBits() & Long.MAX_VALUE);
                    contact.setPhone(AppUtils.normalizePhone(Phone_number));
                    contact.setName(name);
                    contact.setActive(false);
                    contacts.add(contact);
                }

                List<Contact> filteredContact = new ArrayList<>();
                for (Contact contact : contacts) {
                    Contact checkedContact = database.where(Contact.class).equalTo("phone", AppUtils.normalizePhone(contact.getPhone())).findFirst();
                    if (checkedContact == null) {
                        filteredContact.add(contact);
                    }
                }

                database.executeTransaction(db -> db.copyToRealmOrUpdate(filteredContact));
            }

            cursor.close();

            uploadContact(database.copyFromRealm(database.where(Contact.class).equalTo("isActive", false).findAll()));
            database.close();
        });
    }

    private void uploadContact(List<Contact> contacts) {
        mContactRepo.compareToService(contacts);
    }

}
