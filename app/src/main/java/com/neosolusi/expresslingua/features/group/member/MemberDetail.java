package com.neosolusi.expresslingua.features.group.member;

import com.neosolusi.expresslingua.data.entity.Member;
import com.neosolusi.expresslingua.data.entity.MemberProgress;

public class MemberDetail {
    public Member member;
    public MemberProgress progress;
    public String difficulty;
    public int word;
    public int sentence;

    public MemberDetail(Member member, MemberProgress progress, String difficulty, int word, int sentence) {
        this.member = member;
        this.progress = progress;
        this.difficulty = difficulty;
        this.word = word;
        this.sentence = sentence;
    }
}
