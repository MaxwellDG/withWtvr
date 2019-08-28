package com.example.myapplication;

public class AvatarSelector {

    private int avatarNumber;
    private String avatarName;

    public AvatarSelector(int avatarNumber){
        this.avatarNumber = avatarNumber;
    }

    public int getAvatarPhoto(){
        switch (avatarNumber){
            case 1:
                avatarName = "the yellow square";
                return R.drawable.front_square;
            case 2:
                avatarName = "the cyan square";
                return R.drawable.front_square1;
        } return R.drawable.ic_account_box_black_24dp;
    }

    public String getAvatarName() {
        return avatarName;
    }

    public void setAvatarName(String avatarName) {
        this.avatarName = avatarName;
    }

    public int getAvatarNumber() {
        return avatarNumber;
    }

    public void setAvatarNumber(int avatarNumber) {
        this.avatarNumber = avatarNumber;
    }
}
