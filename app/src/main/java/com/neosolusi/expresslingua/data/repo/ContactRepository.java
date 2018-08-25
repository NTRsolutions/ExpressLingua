package com.neosolusi.expresslingua.data.repo;

import android.arch.lifecycle.LiveData;
import android.util.Log;

import com.neosolusi.expresslingua.AppExecutors;
import com.neosolusi.expresslingua.data.dao.ContactDao;
import com.neosolusi.expresslingua.data.entity.Contact;
import com.neosolusi.expresslingua.data.network.NetworkDataSource;

import java.util.ArrayList;
import java.util.List;

public class ContactRepository extends BaseRepository<Contact> {

    private static final String TAG = ContactRepository.class.getSimpleName();

    // Singleton Instantiation
    private static final Object LOCK = new Object();
    private static ContactRepository mInstance;
    private final ContactDao mDao;
    private final AppExecutors mExecutors;
    private final NetworkDataSource mNetworkSource;
    private boolean mInitialized = false;

    private ContactRepository(ContactDao dao, NetworkDataSource networkDataSource, AppExecutors executors) {
        super(dao);
        this.mDao = dao;
        this.mExecutors = executors;
        this.mNetworkSource = networkDataSource;

        LiveData<List<Contact>> networkData = mNetworkSource.getUploadedContact();
        networkData.observeForever(phones -> {
            if (phones == null) return;

            List<Contact> contacts = new ArrayList<>();
            for (Contact phone : phones) {
                Contact contact = mDao.findFirstCopyEqualTo("phone", phone.getPhone());
                if (contact != null) {
                    contact.setActive(true);
                    contacts.add(contact);
                }
            }

            if (isFetchNeeded()) {
                mDao.insertAsync(contacts);
            } else {
                mDao.copyOrUpdateAsync(contacts);
            }
        });
    }

    public synchronized static ContactRepository getInstance(ContactDao dao, NetworkDataSource networkDataSource, AppExecutors executors) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new ContactRepository(dao, networkDataSource, executors);
            }
        }

        return mInstance;
    }

    private synchronized void initializeData() {
        if (mInitialized) return;
        mInitialized = true;
    }

    public synchronized void compareToService(List<Contact> contacts) {
        mExecutors.networkIO().execute(() -> {
            if (!contacts.isEmpty()) mNetworkSource.compareContacts(contacts);
        });
    }

    @Override public boolean isFetchNeeded() {
        return 0 == mDao.count();
    }

    @Override public void wakeup() {
        initializeData();
        Log.d(TAG, "wakeup");
    }

}
