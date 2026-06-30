package com.willadalton.springbatch.service;

import com.willadalton.springbatch.domain.PersonKey;
import org.springframework.stereotype.Component;

import java.util.LinkedHashSet;
import java.util.Set;

@Component
public class CurrentFileState {

    private final Set<PersonKey> keys = new LinkedHashSet<>();

    public synchronized void clear() {
        keys.clear();
    }

    public synchronized void add(PersonKey key) {
        keys.add(key);
    }

    public synchronized Set<PersonKey> snapshot() {
        return Set.copyOf(keys);
    }
}
