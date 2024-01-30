package com.assessment.demo.utilities;

import java.util.regex.Pattern;

public class EmailUtils {
    private static final Pattern EMAIL = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean isEmail(String s) {
        return EMAIL.matcher(s).matches();
    }
}
