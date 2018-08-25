package com.neosolusi.expresslingua.data.network;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class WrapperList<T>
{
    @SerializedName("status")
    public String status;

    @SerializedName("msg")
    public String message;

    @SerializedName("data")
    public List<T> data;
}
