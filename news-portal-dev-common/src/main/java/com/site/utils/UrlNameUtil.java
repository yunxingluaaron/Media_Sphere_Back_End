package com.site.utils;

import org.apache.commons.lang3.StringUtils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UrlNameUtil {

    /**
     * Validate url name
     * @param urlName
     * @return
     */
    public static boolean verifyUrlName(String urlName){
        if (urlName == null) {
            return false;
        }

        // UrlName cannot contain blank, and its length should between 6 - 16
        String regOnlyBlank = "^[^\\s]{6,16}$";
        Pattern onlyBlank = Pattern.compile(regOnlyBlank);
        Matcher onlyBlankMatcher = onlyBlank.matcher(urlName);

        boolean rs = onlyBlankMatcher.matches();
        return rs;
    }

    public static void main(String[] args) {
        boolean res =
                verifyUrlName("MyPortfolio");
        System.out.println(res);
    }

}
