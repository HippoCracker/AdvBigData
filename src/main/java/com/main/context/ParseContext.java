package com.main.context;

/**
 * Created by zongzesheng on 11/27/16.
 */
public interface ParseContext {

    DocumentContext parse(String json);

    DocumentContext flat();

    DocumentContext restore();
}
