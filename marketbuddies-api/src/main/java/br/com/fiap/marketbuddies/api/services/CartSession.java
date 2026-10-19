package br.com.fiap.marketbuddies.api.services;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

import java.io.Serial;
import java.io.Serializable;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@SessionScope
public class CartSession implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    private final LinkedHashMap<Long, Entry> entries = new LinkedHashMap<>();

    public record Entry(String productName, int quantity) implements Serializable {
    }

    public int quantityOf(Long productId) {
        Entry entry = entries.get(productId);
        return entry == null ? 0 : entry.quantity();
    }

    public void put(Long productId, String productName, int quantity) {
        entries.put(productId, new Entry(productName, quantity));
    }

    public void remove(Long productId) {
        entries.remove(productId);
    }

    public void clear() {
        entries.clear();
    }

    public boolean isEmpty() {
        return entries.isEmpty();
    }

    public int count() {
        return entries.values().stream().mapToInt(Entry::quantity).sum();
    }

    public Map<Long, Entry> entries() {
        return Collections.unmodifiableMap(entries);
    }
}
