package com.neosolusi.expresslingua.data.entity;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class MemberProgress {
    public int id;
    public String userid;
    public int not_seen;
    public int skipped;
    public int incorrect;
    public int correct;

    @SerializedName("not_seen_%")
    public int not_seen_percent;
    @SerializedName("skipped_percent_%")
    public int skipped_percent;
    @SerializedName("incorrect_percent_%")
    public int incorrect_percent;
    @SerializedName("correct_percent_%")
    public int correct_percent;

    public int w_red;
    public int w_orange;
    public int w_yellow;
    public int w_green;
    public int w_blue;
    public int s_red;
    public int s_orange;
    public int s_yellow;
    public int s_green;
    public int s_blue;
    public Date datecreated;
    public Date datemodified;
}
