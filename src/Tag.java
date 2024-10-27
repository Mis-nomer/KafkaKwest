import java.awt.Color;
import java.util.*;

public class Tag {
    private final String name;
    private final Set<Tag> subtags;
    private Color color;

    public Tag(String name) {
        this.name = name;
        this.subtags = new LinkedHashSet<>();
        this.color = Color.GRAY; // Default color
    }

    public Tag(String name, Color color) {
        this.name = name;
        this.subtags = new LinkedHashSet<>();
        this.color = color;
    }

    // Getters and setters
    public String getName() {
        return name;
    }

    public Set<Tag> getSubtags() {
        return subtags;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public void addSubtag(Tag tag) {
        subtags.add(tag);
    }

    public void removeSubtag(Tag tag) {
        subtags.remove(tag);
    }

    @Override
    public String toString() {
        // Serialize the tag including color and subtags
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        sb.append("::").append(color.getRGB());
        if (!subtags.isEmpty()) {
            sb.append("{");
            Iterator<Tag> it = subtags.iterator();
            while (it.hasNext()) {
                sb.append(it.next().toString());
                if (it.hasNext()) {
                    sb.append(",");
                }
            }
            sb.append("}");
        }
        return sb.toString();
    }

    public static Tag fromString(String s) {
        // Parse the tag string to reconstruct the Tag object
        if (s == null || s.isEmpty()) {
            return null;
        }

        int colorIdx = s.indexOf("::");
        int subtagIdx = s.indexOf('{');

        String namePart = colorIdx != -1 ? s.substring(0, colorIdx) : s;
        String colorPart = colorIdx != -1 ? s.substring(colorIdx + 2, subtagIdx != -1 ? subtagIdx : s.length()) : String.valueOf(Color.GRAY.getRGB());
        Color color = new Color(Integer.parseInt(colorPart));

        Tag tag = new Tag(namePart, color);

        if (subtagIdx != -1) {
            String subtagsStr = s.substring(subtagIdx + 1, s.lastIndexOf('}'));
            String[] subtagsArr = splitSubtags(subtagsStr);
            for (String subtagStr : subtagsArr) {
                Tag subtag = fromString(subtagStr);
                if (subtag != null) {
                    tag.addSubtag(subtag);
                }
            }
        }
        return tag;
    }

    private static String[] splitSubtags(String s) {
        List<String> tags = new ArrayList<>();
        int braceLevel = 0;
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == ',' && braceLevel == 0) {
                tags.add(sb.toString());
                sb.setLength(0);
            } else {
                if (c == '{') braceLevel++;
                if (c == '}') braceLevel--;
                sb.append(c);
            }
        }
        tags.add(sb.toString());
        return tags.toArray(new String[0]);
    }

    // Override equals and hashCode if necessary
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;

        return name.equals(tag.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
