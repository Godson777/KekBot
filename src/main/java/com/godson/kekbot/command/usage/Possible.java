package com.godson.kekbot.command.usage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.godson.kekbot.Utils;

/**
 * Represents a possibility in a usage Tag
 */
public abstract class Possible {

    protected static final Map<String, Class<? extends Number>> minMaxTypeClasses = new HashMap<>(3);
    static {
        minMaxTypeClasses.put("string", Integer.class);
        minMaxTypeClasses.put("int", Integer.class);
        minMaxTypeClasses.put("float", Float.class);
    }
    protected static final Set<String> minMaxTypes = minMaxTypeClasses.keySet();
    protected static final List<String> regexTypes = Arrays.asList("pattern", "regex");

    public final String name;
    public final String type;
    public final Number min;
    public final Number max;
    public final Pattern regex;

    protected Possible(String[] parts) {
        assert parts.length == 6;

        name = parts[1];
        assert name != null;

        type = parts[2] == null ? "literal" : parts[2].toLowerCase();
        assert type != null;

        final boolean canHaveMinMax = minMaxTypes.contains(type);
        if (parts[3] == null) {
            min = null;
        } else {
            if (canHaveMinMax) min = resolveLimit(parts[3], type, "min");
            else throw new IllegalArgumentException("Type " + type + " cannot have a minimum value");
        }
        if (parts[4] == null) {
            max = null;
        } else {
            if (canHaveMinMax) max = resolveLimit(parts[4], type, "max");
            else throw new IllegalArgumentException("Type " + type + " cannot have a maximum value");
        }

        if (regexTypes.contains(type)) {
            if (parts[5] == null) throw new IllegalArgumentException("Regex types must include a regular expression");
            if (parts[6] == null) regex = Pattern.compile(parts[5]);
            else regex = Pattern.compile(parts[5], Utils.resolvePatternFlags(parts[6]));
        } else {
            if (parts[5] != null || parts[6] != null) throw new IllegalArgumentException("Type " + type + " is not a regex type, yet a regular expression was provided");
            regex = null;
        }
    }

    public static Possible makeFromMatchResult(MatchResult regexResult) {
        assert regexResult.groupCount() == 6;

        final String[] parts = new String[6];
        for (int i = 1; i <= 6; i++) parts[i] = regexResult.group(i);
        final String typeOrNull = parts[2];

        if (minMaxTypes.contains(typeOrNull)) {
            switch (minMaxTypeClasses.get(typeOrNull).getSimpleName()) {
                case "Integer": return new StringOrIntPossible(parts);
                default: assert false : "Forgot to implement this min and max type";
            }
        }
        return new OtherPossible(parts);
    }

    private static class OtherPossible extends Possible {
        public OtherPossible(String[] parts) { super(parts); }
    }

    private static class StringOrIntPossible extends Possible {
        public StringOrIntPossible(String[] parts) {
            super(parts);
            // TODO: int checking
            // final boolean canHaveMinMax = minMaxTypes.contains(type);
            // min = parts[3] == null ? null : resolveLimit(parts[3], type, "min");
            // if (parts[4] == null) {
            //     max = null;
            // } else {
            //     if (canHaveMinMax) max = resolveLimit(parts[4], type, "max");
            //     else throw new IllegalArgumentException("Type " + type + " cannot have a maximum value");
            // }
        }

        private static Number resolveLimit(String limit, String type, String limitType) {
            Integer tempLimit;
            try {
                tempLimit = Integer.valueOf(limit);
            } catch (NumberFormatException e) {
                throw new NumberFormatException(limitType + " must be an integer for this type.");
                //throw new NumberFormatException(limitType + " must be an integer");
            }
            return tempLimit;
        }
    }
}
