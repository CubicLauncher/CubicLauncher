package com.cubiclauncher.launcher.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public final class GsonProvider {
    public static final Gson PRETTY = new GsonBuilder().setPrettyPrinting().create();


    private GsonProvider() {}
}