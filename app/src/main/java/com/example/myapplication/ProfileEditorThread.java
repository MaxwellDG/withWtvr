package com.example.myapplication;

// USE THIS LATER ON FOR THE EDITING FIELDS STUFF //

public class ProfileEditorThread extends Thread {
    @Override
    public void run() {
        super.run();
    }

    public String editField(int requestCode){
        switch (requestCode){
            case 1:
                return "yea";
            case 2:
                return "yup";
        } return "nope";
    }
}
