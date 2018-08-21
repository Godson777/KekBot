package com.godson.kekbot.objects;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class UDictionary {
    private List<String> tags = new ArrayList<>();
    @SerializedName("list")
    private List<Definition> definitions;
    private List<String> sounds;

    public List<Definition> getDefinitions() {
        return definitions;
    }

    public class Definition {
        private String definition;
        private String permalink;
        @SerializedName("thumbs_up")
        private int thumbsUp;
        private String author;
        private String word;
        private String defid;
        @SerializedName("current_vote")
        private String currentVote;
        private String example;
        @SerializedName("thumbs_down")
        private int thumbsDown;

        public String getDefinition() {
            return definition;
        }

        public String getExample() {
            return example;
        }

        public String getPermalink() {
            return permalink;
        }

        public String getWord() {
            return word;
        }

        public int getThumbsUp() {
            return thumbsUp;
        }

        public int getThumbsDown() {
            return thumbsDown;
        }
    }
}
