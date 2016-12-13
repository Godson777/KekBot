package com.godson.kekbot.Settings;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.dv8tion.jda.core.entities.Guild;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class TagManager {
    private List<Tag> tags = new ArrayList<>();

    public TagManager() {}

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

    public List<Tag> getTags() {
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

    @Override
    public String toString() {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(this, this.getClass());
    }

    public void save(Guild guild) {
        File folder = new File("settings/" + guild.getId());
        File tags = new File("settings/" + guild.getId() + "/Tags.json");
        if (!folder.exists()) {
            folder.mkdirs();
        }
        try {
            FileWriter writer = new FileWriter(tags);
            writer.write(this.toString());
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
