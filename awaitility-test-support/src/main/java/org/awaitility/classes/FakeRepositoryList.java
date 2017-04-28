package org.awaitility.classes;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class FakeRepositoryList {

    private List<String> state = new CopyOnWriteArrayList<String>();

    public void add(String string) {
        state.add(string);
    }

    public List<String> state() {
        return state;
    }
}
