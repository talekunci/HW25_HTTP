package ua.goit.model;

import java.util.*;

public class Pet {

    private static final Map<Long, Pet> exitingPets = new HashMap<>();

    private static long count = 0;

    private Long id;
    private Category category;
    private String name;
    private String[] photoUrls;
    private Tag[] tags;
    private String status;

    public Pet() {
        id = count++;
        exitingPets.put(this.id, this);
    }

    public Pet(Category category, String name, String[] photoUrls, Tag[] tags, String status) {
        this.id = count++;
        this.category = category;
        this.name = name;
        this.photoUrls = photoUrls;
        this.tags = tags;
        this.status = status;

        exitingPets.put(this.id, this);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        exitingPets.remove(this.id);
        this.id = id;
        exitingPets.put(id, this);
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String[] getPhotoUrls() {
        return photoUrls;
    }

    public void setPhotoUrls(String[] photoUrls) {
        this.photoUrls = photoUrls;
    }

    public Tag[] getTags() {
        return tags;
    }

    public void setTags(Tag[] tags) {
        this.tags = tags;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static Map<Long, Pet> getExitingPets() {
        return exitingPets;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Pet pet = (Pet) o;
        return Objects.equals(getId(), pet.getId())
                && Objects.equals(getCategory(), pet.getCategory())
                && Objects.equals(getName(), pet.getName())
                && Arrays.equals(getPhotoUrls(), pet.getPhotoUrls())
                && Arrays.equals(getTags(), pet.getTags())
                && Objects.equals(getStatus(), pet.getStatus());
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(getId(), getCategory(), getName(), getStatus());
        result = 31 * result + Arrays.hashCode(getPhotoUrls());
        result = 31 * result + Arrays.hashCode(getTags());
        return result;
    }

    @Override
    public String toString() {
        return "Pet{" +
                "id=" + id +
                ", category=" + category +
                ", name='" + name + '\'' +
                ", photoUrls=" + Arrays.toString(photoUrls) +
                ", tags=" + Arrays.toString(tags) +
                ", status='" + status + '\'' +
                '}';
    }
}
