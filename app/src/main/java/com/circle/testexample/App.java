package com.circle.testexample;

import android.app.Application;

public class App extends Application {

    private static App sInstance;
    private Graph graph;

    @Override
    public void onCreate() {
        super.onCreate();

        sInstance = this;
        graph = Graph.Initializer.init();
    }

    public static App getInstance() {
        return sInstance;
    }

    public Graph graph() {
        return graph;
    }

}
