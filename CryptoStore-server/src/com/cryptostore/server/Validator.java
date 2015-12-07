package com.cryptostore.server;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Validator {
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_-]{3,12}$";
    private static final String PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{8,16}$";
    private static final String FILENAME_PATTERN = "^\\.\\/[\\w\\d\\s]{10}$";

    public static boolean validateUsername(final String username) {
        Pattern pattern = Pattern.compile(USERNAME_PATTERN);
        Matcher matcher = pattern.matcher(username);
        return matcher.matches();
    }

    public static boolean validatePassword(final String password) {
        Pattern pattern = Pattern.compile(PASSWORD_PATTERN);
        Matcher matcher = pattern.matcher(password);
        return matcher.matches();
    }

    public static boolean validateFilename(final String filename) {
        Pattern pattern = Pattern.compile(FILENAME_PATTERN);
        Matcher matcher = pattern.matcher(filename);
        return matcher.matches();
    }
}