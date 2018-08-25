package com.neosolusi.expresslingua.data.network;

import com.google.gson.annotations.SerializedName;

public class Wrapper<T>
{
    @SerializedName("status")
    public String status;

    @SerializedName("msg")
    public String message;

    @SerializedName("data")
    public T data;
}
