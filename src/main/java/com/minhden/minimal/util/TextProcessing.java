package com.minhden.minimal.util;

import java.util.ArrayList;
import java.util.List;

public class TextProcessing {
    public static String removeXMLDeclaration(String xmlString) {
        // Remove the XML declaration using substring
        int index = xmlString.indexOf("?>");
        if (index != -1) {
            xmlString = xmlString.substring(index + 2).trim();
        }
        return xmlString;
    }

    public static List<Integer> search(String text, String pattern) {
        List<Integer> matches = new ArrayList<>();

        int[] lps = computeLPSArray(pattern);

        int i = 0; // index for text[]
        int j = 0; // index for pattern[]

        while (i < text.length()) {
            if (pattern.charAt(j) == text.charAt(i)) {
                i++;
                j++;
            }

            if (j == pattern.length()) {
                matches.add(i - j);
                j = lps[j - 1];
            } else if (i < text.length() && pattern.charAt(j) != text.charAt(i)) {
                if (j != 0) {
                    j = lps[j - 1];
                } else {
                    i++;
                }
            }
        }

        return matches;
    }

    private static int[] computeLPSArray(String pattern) {
        int[] lps = new int[pattern.length()];
        int len = 0;
        int i = 1;
        lps[0] = 0;

        while (i < pattern.length()) {
            if (pattern.charAt(i) == pattern.charAt(len)) {
                len++;
                lps[i] = len;
                i++;
            } else {
                if (len != 0) {
                    len = lps[len - 1];
                } else {
                    lps[i] = len;
                    i++;
                }
            }
        }

        return lps;
    }
}
