package com.carlocation.view;

/**
 * Created by 28851620 on 5/19/2015.
 * @author Jacky Shen
 */
public class GlobalHolder {
    private String mTerminalId;

    private GlobalHolder(){

    }

    private static final GlobalHolder globalHolder = new GlobalHolder();

    public static GlobalHolder getInstance(){
        return globalHolder;
    }

    public void setTerminalId(String terminalId){
        mTerminalId = terminalId;
    }

    public String getTerminalId(){
        return mTerminalId;
    }


}
