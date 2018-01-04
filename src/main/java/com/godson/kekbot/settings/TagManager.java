package com.godson.kekbot.settings;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TagManager {
    @SerializedName("list")
    private List<Tag> tags = new ArrayList<>();

    public TagManager() {}

    public TagManager(List<Tag> tags) {
        this.tags = tags;
    }

    public void addTag(Tag tag) {
        if (!getTagByName(tag.getName()).isPresent()) tags.add(tag);
        else throw new IllegalArgumentException("Attempted to add a tag with a name of an already existing tag!");
    }

    public boolean hasNoTags() {
        return tags.isEmpty();
    }

    public Optional<Tag> getTagByName(String name) {
        return tags.stream().filter(t -> t.getName().equals(name)).findFirst();
    }

    public List<Tag> getList() {
        return tags;
    }

    public void removeTag(Tag tag) {
        if (tags.contains(tag)) tags.remove(tag);
    }

    public void editTag(Tag tag, String newContents, String editedTime) {
        Optional<Tag> toEdit = tags.stream().filter(t -> t.equals(tag)).findFirst();
        if (toEdit.isPresent()) {
            tags.remove(tag);
            tags.add(toEdit.get().setContents(newContents).setEditedTime(editedTime));
        }
    }
}
