package com.carlocation.comm.messaging;

/**
 * Created by Jacky on 2015/4/20.
 * @author Jacky Shen
 */
public enum TerminalType {
    TERMINAL_CAR(0),
    TERMINAL_PLANE(1),
    TERMINAL_PC(2),
    TERMINAL_UNKNOWN(-1);

    private int code;

    TerminalType(int code) {
        this.code = code;
    }

    public static TerminalType valueOf(int code){
        switch (code){
            case 0:
                return TERMINAL_CAR;
            case 1:
                return TERMINAL_PLANE;
            case 2:
                return TERMINAL_PC;
            default:return TERMINAL_UNKNOWN;
        }
    }
}
