package ua.goit.model;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class Tag {

    private static final Map<Long, Tag> exitingTags = new HashMap<>();

    private static long count = 0;

    private Long id;
    private String name;

    public Tag() {
        id = count++;
        exitingTags.put(this.id, this);
    }

    public Tag(String name) {
        this.id = count++;
        this.name = name;

        exitingTags.put(this.id, this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        exitingTags.remove(this.id);
        this.id = id;
        exitingTags.put(id, this);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Map<Long, Tag> getExitingTags() {
        return exitingTags;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tag tag = (Tag) o;
        return Objects.equals(getId(), tag.getId()) && Objects.equals(getName(), tag.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName());
    }

    @Override
    public String toString() {
        return "Tag{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }
}
