package com.circle.testexample.data;

import rx.Observable;

public class Api {

    public Observable<Boolean> login(String username, String password) {
        return Observable.just(true);
    }
}
