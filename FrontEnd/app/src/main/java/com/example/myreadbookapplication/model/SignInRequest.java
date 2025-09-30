package com.example.myreadbookapplication.model;

public class SignInRequest {
    private String emailSignIn;
    private String passwordSignIn;

    public SignInRequest(String emailSignIn, String passwordSignIn) {
        this.emailSignIn = emailSignIn;
        this.passwordSignIn = passwordSignIn;
    }

    public String getPasswordSignIn() {
        return passwordSignIn;
    }

    public void setPasswordSignIn(String passwordSignIn) {
        this.passwordSignIn = passwordSignIn;
    }

    public String getEmailSignIn() {
        return emailSignIn;
    }

    public void setEmailSignIn(String emailSignIn) {
        this.emailSignIn = emailSignIn;
    }

}
