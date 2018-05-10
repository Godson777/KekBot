package com.godson.kekbot.command.usage;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

import com.godson.kekbot.ThrowableString;
import com.godson.kekbot.Utils;

/**
 * Represents a possible type/value in a usage Tag
 */
public abstract class Possible {

    /**
     * A map of Possible types (ones that support min/max values) to the type their min/max values must have
     */
    protected static final Map<String, String> minMaxTypeClasses = new HashMap<>(3);
    static {
        minMaxTypeClasses.put("String", "int");
        minMaxTypeClasses.put("int", "int");
        minMaxTypeClasses.put("float", "float");
    }

    /**
     * The Possible types that can have min/max values
     */
    protected static final Set<String> minMaxTypes = minMaxTypeClasses.keySet();

    /**
     * The Possible types that can have regular expressions
     */
    protected static final List<String> regexTypes = Arrays.asList("Pattern", "regex");

    /**
     * The name of this Possible
     */
    public final String name;

    /**
     * The type of this Possible
     */
    public final String type;

    protected Possible(String[] parts) throws ThrowableString {
        assert parts.length == 6;

        name = parts[1];
        assert name != null;

        type = parts[2] == null ? "literal" : parts[2].toLowerCase();
        assert type != null;

        if (!minMaxTypes.contains(type)) {
            if (parts[3] != null) throw new ThrowableString("Type " + type + " cannot have a minimum value");
            if (parts[4] != null) throw new ThrowableString("Type " + type + " cannot have a maximum value");
        }

        if (!regexTypes.contains(type)) {
            if (parts[5] != null) throw new ThrowableString("Type " + type + " is not a regex type, yet a regular expression was provided");
            if (parts[6] != null) throw new ThrowableString("Type " + type + " is not a regex type, yet regular expression flags were provided");
        }
    }

    /**
     * Make a Possible, of the appropriate type
     */
    public static Possible makeFromMatchResult(MatchResult regexResult) throws ThrowableString {
        assert regexResult.groupCount() == 6;

        final String[] parts = new String[6];
        for (int i = 1; i <= 6; i++) parts[i] = regexResult.group(i);

        final String typeOrNull = parts[2];
        if (minMaxTypes.contains(typeOrNull)) {
            switch (minMaxTypeClasses.get(typeOrNull)) {
                case "int": return new StringOrIntPossible(parts);
                case "float": return new FloatPossible(parts);
                default: assert false : "Forgot to implement this min and max type";
            }
        } else if (regexTypes.contains(typeOrNull)) {
            return new RegexPossible(parts);
        }
        return new OtherPossible(parts);
    }

    /**
     * Represents a possible type/value in a usage Tag
     */
    public static class OtherPossible extends Possible {
        protected OtherPossible(String[] parts) throws ThrowableString {
            super(parts);
        }
    }

    /**
     * Represents a possible String/int type/value in a usage Tag
     */
    public static class StringOrIntPossible extends Possible {
        /**
         * The minimum value this Possible allows
         */
        public final int min;

        /**
         * The maximum value this Possible allows
         */
        public final int max;

        protected StringOrIntPossible(String[] parts) throws ThrowableString {
            super(parts);
            assert minMaxTypes.contains(type);
            min = parts[3] == null ? null : resolveLimit(parts[3], "min");
            max = parts[4] == null ? null : resolveLimit(parts[4], "max");
        }

        private static int resolveLimit(String limit, String limitType) throws ThrowableString {
            int tempLimit;
            try {
                tempLimit = Integer.parseInt(limit);
            } catch (NumberFormatException e) {
                throw (ThrowableString) new ThrowableString(limitType + " must be an integer for this type, if provided.", e);
            }
            return tempLimit;
        }
    }

    /**
     * Represents a possible float type/value in a usage Tag
     */
    public static class FloatPossible extends Possible {
        /**
         * The minimum value this Possible allows
         */
        public final float min;

        /**
         * The maximum value this Possible allows
         */
        public final float max;

        protected FloatPossible(String[] parts) throws ThrowableString {
            super(parts);
            assert minMaxTypes.contains(type);
            min = parts[3] == null ? null : resolveLimit(parts[3], "min");
            max = parts[4] == null ? null : resolveLimit(parts[4], "max");
        }

        private static float resolveLimit(String limit, String limitType) throws ThrowableString {
            float tempLimit;
            try {
                tempLimit = Float.parseFloat(limit);
            } catch (NumberFormatException e) {
                throw (ThrowableString) new ThrowableString(limitType + " must be a float for this type, if provided.", e);
            }
            return tempLimit;
        }
    }

    /**
     * Represents a possible Pattern type/value in a usage Tag
     */
    public static class RegexPossible extends Possible {
        /**
         * The regular expression this Possible matches against
         */
        public final Pattern regex;

        protected RegexPossible(String[] parts) throws ThrowableString {
            super(parts);
            if (parts[5] == null) throw new ThrowableString("Regex types must include a regular expression");
            if (parts[6] == null) regex = Pattern.compile(parts[5]);
            else regex = Pattern.compile(parts[5], Utils.resolvePatternFlags(parts[6]));
        }
    }
}
