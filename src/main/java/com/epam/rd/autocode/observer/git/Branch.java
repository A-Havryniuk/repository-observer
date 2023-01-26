package com.epam.rd.autocode.observer.git;

import java.util.Objects;

public class Branch {

    private final String name;

    public Branch(final String name) {
        Objects.requireNonNull(name);
        this.name = name;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Branch branch = (Branch) o;
        return name.equals(branch.name);
    }

    public String getName() {
        return name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name);
    }

    @Override
    public String toString() {
        return name;
    }
}
