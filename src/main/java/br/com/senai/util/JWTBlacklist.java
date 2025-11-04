package br.com.senai.util;

import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JWTBlacklist {

    private final Set<String> blacklist = new HashSet<>();

    public void addToken(String token) {
        blacklist.add(token);
    }

    public boolean contains(String token) {
        return blacklist.contains(token);
    }

    public Set<String> getAllTokens() {
        return new HashSet<>(blacklist);
    }

    public void removeToken(String token) {
        blacklist.remove(token);
    }
}
