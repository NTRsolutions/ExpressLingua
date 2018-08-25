package com.neosolusi.expresslingua.data.entity;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Notification extends RealmObject
{
    @PrimaryKey
    private String id;
    private String jenis_pesan;
    private String isi_pesan;
    private String aktif;
    private Date created_at;
    private Date expired_at;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getJenis_pesan()
    {
        return jenis_pesan;
    }

    public void setJenis_pesan(String jenis_pesan)
    {
        this.jenis_pesan = jenis_pesan;
    }

    public String getIsi_pesan()
    {
        return isi_pesan;
    }

    public void setIsi_pesan(String isi_pesan)
    {
        this.isi_pesan = isi_pesan;
    }

    public String getAktif()
    {
        return aktif;
    }

    public void setAktif(String aktif)
    {
        this.aktif = aktif;
    }

    public Date getCreated_at()
    {
        return created_at;
    }

    public void setCreated_at(Date created_at)
    {
        this.created_at = created_at;
    }

    public Date getExpired_at()
    {
        return expired_at;
    }

    public void setExpired_at(Date expired_at)
    {
        this.expired_at = expired_at;
    }
}
