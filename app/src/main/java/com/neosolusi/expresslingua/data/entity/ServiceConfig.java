package com.neosolusi.expresslingua.data.entity;

public class ServiceConfig {
    private String current_version;
    private String url_panduan;
    private int maks_kata;

    public String getCurrent_version() {
        return current_version;
    }

    public void setCurrent_version(String current_version) {
        this.current_version = current_version;
    }

    public String getUrl_panduan() {
        return url_panduan;
    }

    public void setUrl_panduan(String url_panduan) {
        this.url_panduan = url_panduan;
    }

    public int getMaks_kata() {
        return maks_kata;
    }

    public void setMaks_kata(int maks_kata) {
        this.maks_kata = maks_kata;
    }
}
