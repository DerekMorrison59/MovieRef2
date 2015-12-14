package com.derekmorrison.movieref2;

public class Globals{
    private static Globals instance;

    // Global variables
    private boolean RefreshNeeded = true;
    private boolean hasDataConnection = false;
    private boolean showFavorites = false;
    private boolean manualRefresh = false;
    private boolean badApiKey = false;
    private boolean twoPane = false;
    private boolean isNewList = true;

    // Restrict the constructor from being instantiated
    private Globals(){}

    public boolean anyReasonToUpdate() {
        return isNewList || badApiKey || manualRefresh || RefreshNeeded;
    }
    public void setRefreshNeeded(boolean b){
        this.RefreshNeeded =b;
    }
    public boolean getRefreshNeeded(){
        return this.RefreshNeeded;
    }

    public void setDataConnection(boolean b){
        this.hasDataConnection=b;
    }
    public boolean getDataConnection(){
        return this.hasDataConnection;
    }

    public void setShowFavorites(boolean b){
        this.showFavorites=b;
    }
    public boolean getShowFavorites(){
        return this.showFavorites;
    }

    public void setManualRefresh(boolean b){
        this.manualRefresh=b;
    }
    public boolean getMnualRefresh(){
        return this.manualRefresh;
    }

    public void setTwoPane(boolean b){
        this.twoPane=b;
    }
    public boolean getTwoPane(){
        return this.twoPane;
    }

    public void setBadApiKey(boolean b){
        this.badApiKey=b;
    }
    public boolean getBadApiKey(){
        return this.badApiKey;
    }

    public void setIsNewList(boolean b){
        this.isNewList =b;
    }
    public boolean getIsNewList(){
        return this.isNewList;
    }

    public static synchronized Globals getInstance(){
        if(instance==null){
            instance=new Globals();
        }
        return instance;
    }
}
