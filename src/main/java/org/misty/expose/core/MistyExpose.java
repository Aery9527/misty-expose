package org.misty.expose.core;

import java.util.Objects;

public class MistyExpose {

    public static final String FULL_NAME_FORMAT = "MistyExpose(%s)(%s)";

    public static final String FULL_NAME_WITH_CLASS_FORMAT = FULL_NAME_FORMAT + "(%s)";

    private final String name;

    private final String version;

    private final String description;

    private final String fullName;

    private final String fullNameWithClass;

    public MistyExpose(String name, String version) {
        this(name, version, "");
    }

    public MistyExpose(String name, String version, String description) {
        check(name, "name", false);
        check(version, "version", false);
        check(description, "description", true);

        this.name = name;
        this.version = version;
        this.fullName = String.format(FULL_NAME_FORMAT, name, version);
        this.fullNameWithClass = String.format(FULL_NAME_WITH_CLASS_FORMAT, name, version, getClass().getName());
        this.description = description;
    }

    private void check(String target, String term, boolean allowEmpty) {
        if (target == null) {
            throw new IllegalArgumentException(term + " can't be null.");
        } else if (target.isEmpty() && !allowEmpty) {
            throw new IllegalArgumentException(term + " can't be empty.");
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
