package com.main;

import com.main.common.Utils;

/**
 * Created by zongzesheng on 12/5/16.
 */
public class Path {

    private String rawPath;
    private String keyPattern;
    private String filterPattern;

    public Path(String jsonPath) {
        this.rawPath = jsonPath;
        parse();
    }

    private void parse() {
        int filterStart = rawPath.indexOf('[');
//        if (filterStart == -1) {
//            filterPattern = "";
//        } else {
//            filterPattern = "$" + rawPath.substring(filterStart);
//        }
        filterPattern = rawPath;

        parseKeyPattern(rawPath.substring(0, filterStart));
    }

    private void parseKeyPattern(String pattern) {
        pattern = pattern.trim();
        int significantCharStart = 0;
        for (int i = 0, len = pattern.length(); i < len; i++) {
            if (pattern.charAt(i) == '.' && i + 1 < len && pattern.charAt(i + 1) != '.') {
                significantCharStart = i;
                break;
            }
        }
        keyPattern = "*" + pattern.substring(significantCharStart);
    }

    public String getKeyPattern() {
        return keyPattern;
    }

    public String getFilterPattern() {
        return filterPattern;
    }
}
