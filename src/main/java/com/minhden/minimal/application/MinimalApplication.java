package com.minhden.minimal.application;

public class MinimalApplication {
    private static MinimalApplication instance;


    private MinimalApplication() {

    }

    public static MinimalApplication getInstance() {
        if (instance == null) {
            instance = new MinimalApplication();
        }

        return instance;
    }

    public static void start() {
        instance = new MinimalApplication();
        BookManager.getInstance();
        String dataPath = UserManager.getInstance().getUserDataPath();
        System.out.println(dataPath);
    }
}
