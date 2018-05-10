package com.godson.kekbot.command.usage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.godson.kekbot.ThrowableString;

/**
 * Represents one tag, one argument, in a usage
 */
public class Tag {

    /**
     * Matches usage tags
     */
    private static final Pattern pattern = Pattern.compile("^([^:]+)(?::([^{}/]+))?(?:{([^,]+)?(?:,(.+))?})?(?:\\/(.+)\\/(\\w+)?)?$", Pattern.CASE_INSENSITIVE);

    public static enum Required {
        OPTIONAL, SEMI_REQUIRED, REQUIRED
    }

    /**
     * The type of tag (0 optional, 1 semi-required, 2 required)
     */
    public final Required required;
    
    /**
     * The "possibilities" of this tag
     */
    public final Possible[] possibles;

    /**
     * Whether this tag is repeating
     *
     * This is set by the Usage instance that contains this Tag.
     */
    public boolean repeat = false;

    public Tag(String contents, int position, Required required) throws ThrowableString {
        this.required = required;
        this.possibles = Tag.parseContents(contents, position);
    }

    /**
     * Parse the contents of a Tag into usable Possibles
     */
    private static Possible[] parseContents(String contents, int position) throws ThrowableString {
        final List<String> literals = new ArrayList<>();
        final List<String> types = new ArrayList<>();
        final List<String> rawPossibles = Tag._parseRawContents(contents);

        final Possible[] possibles = new Possible[rawPossibles.size()];
        final boolean moreThanOnePossible = rawPossibles.size() > 1;
        final int lastIndex = rawPossibles.size() - 1;
        for (int i = 0; i < rawPossibles.size(); i++) {
            final String tag = rawPossibles.get(i);
            final String current = String.format(
                "%s: at tag #%d at bound #%d",
                rawPossibles, position, i + 1
            );
            Possible possible;
            final Matcher matcher = Tag.pattern.matcher(tag);
            matcher.find();
            try {
                possible = Possible.makeFromMatchResult(matcher.toMatchResult());
            } catch (ThrowableString e) {
                throw new ThrowableString(current + ": " + e.getMessage(), e);
            }
            if (possible.type.equals("literal")) {
                if (literals.contains(possible.name)) throw new ThrowableString(current + ": there can't be two literals with the same text.");
                literals.add(possible.name);
            } else if (moreThanOnePossible) {
                if (i != lastIndex && possible.type.equals("String")) throw new ThrowableString(current + ": the String type is vague, you must specify it as the last type");
                if (types.contains(possible.type)) throw new ThrowableString(current + ": there can't be two possibles with the same type (" + possible.type + ")");
                types.add(possible.type);
            }
            possibles[i] = possible;
        }
        return possibles;
    }

    /**
     * Parse the contents of a Tag into strings representing each Possible
     */
    private static List<String> _parseRawContents(String contents) {
        final List<String> rawPossibles = new ArrayList<>();
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
                rawPossibles.add(currentType);
                currentType = "";
            }
        }
        // Add the last (or only) type
        rawPossibles.add(currentType);
        return rawPossibles;
    }
}