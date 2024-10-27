import java.util.*;

public class Tag {
    private final String name;
    private final Set<Tag> subtags;

    public Tag(String name) {
        this.name = name;
        this.subtags = new HashSet<>();
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
        if (subtags.isEmpty()) {
            return name;
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append(name).append("{");
            Iterator<Tag> it = subtags.iterator();
            while (it.hasNext()) {
                sb.append(it.next().toString());
                if (it.hasNext()) {
                    sb.append(",");
                }
            }
            sb.append("}");
            return sb.toString();
        }
    }

    public static Tag fromString(String s) {
        // Parse the tag string to reconstruct the Tag object
        if (s == null || s.isEmpty()) {
            return null;
        }
        int idx = s.indexOf('{');
        if (idx == -1) {
            return new Tag(s);
        } else {
            String name = s.substring(0, idx);
            Tag tag = new Tag(name);
            String subtagsStr = s.substring(idx + 1, s.lastIndexOf('}'));
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
