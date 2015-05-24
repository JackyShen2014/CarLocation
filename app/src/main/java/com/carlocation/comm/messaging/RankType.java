package com.carlocation.comm.messaging;

/**
 * Created by 28851620 on 5/5/2015.
 * Used to indicates the rank of IM txt Msg or TaskMsg.
 *
 * @author Jacky Shen
 */
public enum  RankType {
    EMERGENCY(0),
    NORMAL(1),
    UNKNOWN(-1);

    private int code;

    RankType(int code) {
        this.code = code;
    }

    public static RankType valueOf(int code){
        switch (code){
            case 0: return EMERGENCY;
            case 1: return NORMAL;
            default:return UNKNOWN;
        }

    }
}
