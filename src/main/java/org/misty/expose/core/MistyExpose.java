package org.misty.expose.core;

import java.util.Objects;

public class MistyExpose {

    static {
        MistyExposeDetector.findBySPI(); // trigger MistyExpose name and version pattern check
    }

    public static final String NAME_REGEX_PATTERN = "[a-zA-Z]" + "[\\w-\\.$]*" + "[a-zA-Z0-9]";

    public static final String VERSION_REGEX_PATTERN = "[a-zA-Z0-9]" + "[\\w-\\.$]*" + "[a-zA-Z0-9]";

    public static final String FULL_NAME_FORMAT = "MistyExpose(%s)(%s)";

    public static final String FULL_NAME_WITH_CLASS_FORMAT = FULL_NAME_FORMAT + "(%s)";

    private final String name;

    private final String version;

    private final String description;

    private final String fullName;

    private final String fullNameWithClass;

    public MistyExpose(String name, String version) {
        this(name, version, "", null);
    }

    public MistyExpose(String name, String version, Class<?> type) {
        this(name, version, "", type);
    }

    public MistyExpose(String name, String version, String description) {
        this(name, version, description, null);
    }

    public MistyExpose(String name, String version, String description, Class<?> type) {
        check(name, "name", false, NAME_REGEX_PATTERN);
        check(version, "version", false, VERSION_REGEX_PATTERN);
        check(description, "description", true, "");

        String typeQualifiedName = type == null ? getClass().getName() : type.getName();

        this.name = name;
        this.version = version;
        this.fullName = String.format(FULL_NAME_FORMAT, name, version);
        this.fullNameWithClass = String.format(FULL_NAME_WITH_CLASS_FORMAT, name, version, typeQualifiedName);
        this.description = description;
    }

    private void check(String target, String term, boolean allowEmpty, String allowFormat) {
        if (target == null) {
            throw new IllegalArgumentException(term + " can't be null.");
        } else if (target.isEmpty() && !allowEmpty) {
            throw new IllegalArgumentException(term + " can't be empty.");
        }

        if (allowFormat.isEmpty()) {
            return;
        }

        if (!target.matches(allowFormat)) {
            throw new IllegalArgumentException(term + "(" + target + ") must match regex pattern \"" + allowFormat + "\"");
        }
    }

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MistyExpose)) return false;
        MistyExpose that = (MistyExpose) o;
        return Objects.equals(name, that.name) && Objects.equals(version, that.version);
    }

    @Override
    public final int hashCode() {
        return Objects.hash(name, version);
    }

    @Override
    public String toString() {
        return this.fullName;
    }

    public final String getName() {
        return name;
    }

    public final String getVersion() {
        return version;
    }

    public final String getDescription() {
        return description;
    }

    public final String getFullName() {
        return fullName;
    }

    public final String getFullNameWithClass() {
        return fullNameWithClass;
    }

}
