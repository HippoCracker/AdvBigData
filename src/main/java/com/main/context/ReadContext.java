package com.main.context;

import com.main.common.Configuration;

/**
 * Created by zongzesheng on 11/27/16.
 */
public interface ReadContext {

    Configuration configuration();

    <T> T json();

    <T> T flatJson();

    String jsonString();

    String flatJsonString();
}
