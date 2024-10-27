import java.awt.Color;
import java.util.*;

public class Tag {
    private String name;
    private final Set<Tag> subtags;
    private Color color;

    public Tag(String name) {
        this.name = name;
        this.subtags = new HashSet<>();
        this.color = Color.GRAY; // Default color
    }

    public Tag(String name, Color color) {
        this.name = name;
        this.subtags = new HashSet<>();
        this.color = color;
    }

    // Getters and setters for color
    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

    public String getName() {
        return name;
    }

    public Set<Tag> getSubtags() {
        return subtags;
    }

    public void addSubtag(Tag tag) {
        subtags.add(tag);
    }

    public void removeSubtag(Tag tag) {
        subtags.remove(tag);
    }

    @Override
    public String toString() {
        // Serialize the tag including color information
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

        // Extract name and color
        int colorIdx = s.indexOf("::");
        String namePart = s;
        Color color = Color.GRAY; // Default color

        if (colorIdx != -1) {
            namePart = s.substring(0, colorIdx);
            String colorStr = s.substring(colorIdx + 2, s.indexOf('{', colorIdx + 2) != -1 ? s.indexOf('{', colorIdx + 2) : s.length());
            color = new Color(Integer.parseInt(colorStr));
        }

        int idx = s.indexOf('{');
        if (idx == -1) {
            return new Tag(namePart, color);
        } else {
            Tag tag = new Tag(namePart, color);
            String subtagsStr = s.substring(s.indexOf('{') + 1, s.lastIndexOf('}'));
            String[] subtagsArr = splitSubtags(subtagsStr);
            for (String subtagStr : subtagsArr) {
                Tag subtag = fromString(subtagStr);
                if (subtag != null) {
                    tag.addSubtag(subtag);
                }
            }
            return tag;
        }
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Tag tag = (Tag) o;

        if (!name.equals(tag.name)) return false;
        return subtags.equals(tag.subtags);
    }

    @Override
    public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + subtags.hashCode();
        return result;
    }
}
