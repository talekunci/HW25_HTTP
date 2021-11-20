package ua.goit.model;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class StoreInventory {
    private Map<String, Integer> inventory = new HashMap<>();

    public Map<String, Integer> getInventory() {
        return Collections.unmodifiableMap(inventory);
    }

//    public void
}
