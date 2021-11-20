package ua.goit.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Category {

    private static final Map<Long, Category> exitingCategories = new HashMap<>();

    private static long count = 0;

    private Long id;
    private String name;

    public Category() {
        id = count++;
        exitingCategories.put(this.id, this);
    }

    public Category(String name) {
        this.id = count++;
        this.name = name;

        exitingCategories.put(this.id, this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        exitingCategories.remove(this.id);
        this.id = id;
        exitingCategories.put(id, this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Map<Long, Category> getExitingCategories() {
        return exitingCategories;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Category category = (Category) o;
        return Objects.equals(getId(), category.getId()) && Objects.equals(getName(), category.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
