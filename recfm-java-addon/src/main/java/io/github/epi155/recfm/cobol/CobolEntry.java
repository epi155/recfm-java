package io.github.epi155.recfm.cobol;

import lombok.val;

import java.nio.CharBuffer;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public abstract class CobolEntry {
    private static final String EMPTY_STRING = "";
    private static final DecimalFormat LEVEL_FORMAT = new DecimalFormat("00");
    protected static final DecimalFormat OFFSET_FORMAT = new DecimalFormat("#,###,##0");
    protected static final DecimalFormat LENGTH_FORMAT = new DecimalFormat("##,##0");
    private final int level;
    private final String name;
    private int indentLevel;
    private int indentPic;

    protected CobolEntry(int level, String name) {
        this.level = level;
        this.name = camelToKebab(name);
    }

    public static void beautify(List<CobolEntry> entries) {
        indentLevels(entries);
        indentPictu(entries);
        indentLength(entries);
    }

    private static void indentLength(List<CobolEntry> entries) {
        int max = 0;
        for (CobolEntry entry: entries) {
            if (entry instanceof CobolValue) {
                int size = ((CobolValue) entry).getLength();
                if (size > max) max = size;
            }
        }
        NumberFormat picFormat;
        if (max<10) {
            picFormat = new DecimalFormat("0");
        } else if ( max < 100) {
            picFormat = new DecimalFormat("00");
        } else if ( max < 1000) {
            picFormat = new DecimalFormat("000");
        } else if ( max < 10000) {
            picFormat = new DecimalFormat("0000");
        } else if ( max < 100000) {
            picFormat = new DecimalFormat("00000");
        } else if ( max < 1000000) {
            picFormat = new DecimalFormat("000000");
        } else if ( max < 10000000) {
            picFormat = new DecimalFormat("0000000");
        } else {
            picFormat = NumberFormat.getInstance();
        }
        for (CobolEntry entry: entries) {
            if (entry instanceof CobolValue) {
                ((CobolValue) entry).setFormat(picFormat);
            }
        }
    }

    private static void indentPictu(List<CobolEntry> entries) {
        int max = 0;
        for (val entry: entries) {
            int pos = entry.indentLevel + entry.name.length();
            if (pos > max) max = pos;
        }
        for (val entry: entries) {
            entry.indentPic = max - (entry.indentLevel + entry.name.length());
        }
    }

    private static void indentLevels(List<CobolEntry> entries) {
        SortedSet<Integer> levels = new TreeSet<>();
        for (val entry: entries) {
            levels.add(entry.level);
        }
        Map<Integer,Integer> indentMap = new HashMap<>();
        int indent=0;
        for(val level: levels) {
            indentMap.put(level, indent);
            indent += 3;
        }
        for (val entry: entries) {
            entry.indentLevel = indentMap.get(entry.level);
        }
    }

    public abstract String code();

    private String camelToKebab(String name) {
        StringBuilder sb = new StringBuilder();
        char[] ac = name.toCharArray();
        int k=0;
        for(char c: ac) {
            if (Character.isUpperCase(c) && k>0) {
                sb.append('-').append(c);
            } else {
                sb.append(Character.toUpperCase(c));
            }
            k++;
        }
        return sb.toString();
    }
    protected String spaceLevel() {
        if (indentLevel > 0) {
            return space(indentLevel);
        } else {
            return EMPTY_STRING;
        }
    }
    protected String spacePic() {
        if (indentPic > 0) {
            return space(indentPic);
        } else {
            return EMPTY_STRING;
        }
    }
    protected String level() {
        return LEVEL_FORMAT.format(level);
    }
    protected String name() {
        return name;
    }

    private String space(int width) {
        return CharBuffer.allocate(width).toString().replace('\u0000', ' ');
    }
}
