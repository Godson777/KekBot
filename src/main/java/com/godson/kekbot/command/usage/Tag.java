package com.godson.kekbot.command.usage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Represents one tag, one argument, in a usage
 */
public class Tag {

    /**
     * Matches usage tags
     */
    private static Pattern pattern = Pattern.compile("^([^:]+)(?::([^{}/]+))?(?:{([^,]+)?(?:,(.+))?})?(?:\\/(.+)\\/(\\w+)?)?$", Pattern.CASE_INSENSITIVE);

    /**
     * The type of tag (0 for optional, anything else for required)
     */
    public int required;
    
    /**
     * The "possibilities" of this tag
     * TODO typing
     */
    public Object[] possibles;

    /**
     * Whether this tag is repeating
     */
    public boolean repeat = false;

    public Tag(String contents, int position, int required) {
        this.required = required;
        this.possibles = Tag.parseContents(contents, position);
    }

    private static Possible[] parseContents(String contents, int position) {
        final List<String> literals = new ArrayList<>();
        final List<String> types = new ArrayList<>();
        final List<String> tags = Tag._parseRawContents(contents);
        return IntStream.range(0, tags.size()).mapToObj(i -> {
            final String tag = tags.get(i);
            final String current = String.format(
                "%s: at tag #%d at bound #%d",
                tags, position, i + 1
            );
            Possible possible;
            try {
                final Matcher matcher = Tag.pattern.matcher(tag);
                matcher.find();
                possible = new Possible(matcher.toMatchResult());
            } catch (Exception e) {
                //TODO: handle exception
            }
            // TODO
            return possible;
        }).toArray(Possible[]::new);
    }

    private static List<String> _parseRawContents(String contents) {
        final List<String> types = new ArrayList<>();
        // Whether we're currently parsing a regular expression, in a type
        boolean regex = false;
        // The type declaration, in this tag, that we're currently parsing
        String currentType = "";
        for (final char c : contents.toCharArray()) {
            // A '/' char means a regex is starting or ending
            if (c == '/') regex = !regex;
            if (c != '|' || regex) {
                // Add the current char, if we're not at the end ('|' chars are allowed in regex)
                currentType += c;
            } else {
                // A '|' char means we've reached the end of a type (and a new one is going to start)
                types.add(currentType);
                currentType = "";
            }
        }
        // Add the last (or only) type
        types.add(currentType);
        return types;
    }
}