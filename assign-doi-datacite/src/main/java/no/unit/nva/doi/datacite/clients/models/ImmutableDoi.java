package no.unit.nva.doi.datacite.clients.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import nva.commons.utils.JacocoGenerated;

/**
 * Immutable implementation of {@link Doi}.
 *
 * <p>Use the builder to create immutable instances: {@code ImmutableDoi.builder()}.
 */
@SuppressWarnings({"all"})
@JacocoGenerated
public final class ImmutableDoi extends Doi {

    private static final String FORWARD_SLASH = "/";
    private final String prefix;
    private final String suffix;

    private ImmutableDoi(String prefix, String suffix) {
        this.prefix = prefix;
        this.suffix = suffix;
    }

    /**
     * Creates an immutable copy of a {@link Doi} value. Uses accessors to get values to initialize the new immutable
     * instance. If an instance is already immutable, it is returned as is.
     *
     * @param instance The instance to copy
     * @return A copied immutable Doi instance
     */
    public static ImmutableDoi copyOf(Doi instance) {
        if (instance instanceof ImmutableDoi) {
            return (ImmutableDoi) instance;
        }
        return ImmutableDoi.builder()
            .withPrefix(instance.getPrefix())
            .withSuffix(instance.getSuffix())
            .build();
    }

    /**
     * Creates a builder for {@link ImmutableDoi ImmutableDoi}.
     * <pre>
     * ImmutableDoi.builder()
     *    .prefix(String) // required {@link Doi#getPrefix() prefix}
     *    .suffix(String) // required {@link Doi#getSuffix() suffix}
     *    .build();
     * </pre>
     *
     * @return A new ImmutableDoi builder
     */
    public static ImmutableDoi.Builder builder() {
        return new ImmutableDoi.Builder();
    }

    /**
     * Copy the current immutable object by setting a value for the {@link Doi#getPrefix() prefix} attribute. An equals
     * check used to prevent copying of the same value by returning {@code this}.
     *
     * @param value A new value for prefix
     * @return A modified copy of the {@code this} object
     */
    public final ImmutableDoi withPrefix(String value) {
        String newValue = Objects.requireNonNull(value, "prefix");
        if (this.prefix.equals(newValue)) {
            return this;
        }
        return new ImmutableDoi(newValue, this.suffix);
    }

    /**
     * Copy the current immutable object by setting a value for the {@link Doi#getSuffix() suffix} attribute. An equals
     * check used to prevent copying of the same value by returning {@code this}.
     *
     * @param value A new value for suffix
     * @return A modified copy of the {@code this} object
     */
    public final ImmutableDoi withSuffix(String value) {
        String newValue = Objects.requireNonNull(value, "suffix");
        if (this.suffix.equals(newValue)) {
            return this;
        }
        return new ImmutableDoi(this.prefix, newValue);
    }

    /**
     * This instance is equal to all instances of {@code ImmutableDoi} that have equal attribute values.
     *
     * @return {@code true} if {@code this} is equal to {@code another} instance
     */
    @Override
    public boolean equals(Object another) {
        if (this == another) {
            return true;
        }
        return another instanceof ImmutableDoi
            && equalTo((ImmutableDoi) another);
    }

    /**
     * Computes a hash code from attributes: {@code prefix}, {@code suffix}.
     *
     * @return hashCode value
     */
    @Override
    public int hashCode() {
        int h = 5381;
        h += (h << 5) + prefix.hashCode();
        h += (h << 5) + suffix.hashCode();
        return h;
    }

    /**
     * Prints the immutable value {@code Doi} with attribute values.
     *
     * @return A string representation of the value
     */
    @Override
    public String toString() {
        return toIdentifier();
    }

    /**
     * Retreive prefix for the DOI.
     *
     * @return The value of the {@code prefix} attribute
     */
    @Override
    public String getPrefix() {
        return prefix;
    }

    /**
     * Retrieve suffix for the DOI.
     *
     * @return The value of the {@code suffix} attribute
     */
    @Override
    public String getSuffix() {
        return suffix;
    }

    private boolean equalTo(ImmutableDoi another) {
        return prefix.equals(another.prefix)
            && suffix.equals(another.suffix);
    }

    /**
     * Builds instances of type {@link ImmutableDoi ImmutableDoi}.
     *
     * <p>Initialize attributes and then invoke the {@link #build()} method to create an immutable instance.
     *
     * <p><em>{@code Builder} is not thread-safe and generally should not be stored in a field or collection,
     * but instead used immediately to create instances.</em>
     */
    public static final class Builder {

        private static final long INIT_BIT_PREFIX = 0x1L;
        private static final long INIT_BIT_SUFFIX = 0x2L;
        private long initBits = 0x3L;

        private String prefix;
        private String suffix;

        private Builder() {
        }

        /**
         * Initializes the value for the {@link Doi#getPrefix() prefix} attribute.
         *
         * @param prefix The value for prefix
         * @return {@code this} builder for use in a chained invocation
         */
        public final Builder withPrefix(String prefix) {
            checkNotIsSet(prefixIsSet(), "prefix");
            this.prefix = Objects.requireNonNull(prefix, "prefix");
            initBits &= ~INIT_BIT_PREFIX;
            return this;
        }

        /**
         * Initializes the value for the {@link Doi#getSuffix() suffix} attribute.
         *
         * @param suffix The value for suffix
         * @return {@code this} builder for use in a chained invocation
         */
        public final Builder withSuffix(String suffix) {
            checkNotIsSet(suffixIsSet(), "suffix");
            this.suffix = Objects.requireNonNull(suffix, "suffix");
            initBits &= ~INIT_BIT_SUFFIX;
            return this;
        }

        /**
         * Initializes the value for the {@link Doi#getPrefix()} and {@link Doi#getSuffix()} attributes.
         *
         * @param identifier The value (doi identifier: prefix/suffix) that can be parsed into prefix and suffix.
         * @return {@code this} builder for use in a chained invocation
         */
        public final Builder identifier(String identifier) {
            Objects.requireNonNull(identifier, "identifier");
            int indexOfDivider = identifier.indexOf(FORWARD_SLASH);
            if (indexOfDivider == -1) {
                throw new IllegalArgumentException("Invalid DOI identifier");
            }
            withPrefix(identifier.substring(0, indexOfDivider));
            withSuffix(identifier.substring(++indexOfDivider));
            return this;
        }

        /**
         * Builds a new {@link ImmutableDoi ImmutableDoi}.
         *
         * @return An immutable instance of Doi
         * @throws java.lang.IllegalStateException if any required attributes are missing
         */
        public ImmutableDoi build() {
            checkRequiredAttributes();
            return new ImmutableDoi(prefix, suffix);
        }

        private static void checkNotIsSet(boolean isSet, String name) {
            if (isSet) {
                throw new IllegalStateException("Builder of Doi is strict, attribute is already set: ".concat(name));
            }
        }

        private boolean prefixIsSet() {
            return (initBits & INIT_BIT_PREFIX) == 0;
        }

        private boolean suffixIsSet() {
            return (initBits & INIT_BIT_SUFFIX) == 0;
        }

        private void checkRequiredAttributes() {
            if (initBits != 0) {
                throw new IllegalStateException(formatRequiredAttributesMessage());
            }
        }

        private String formatRequiredAttributesMessage() {
            List<String> attributes = new ArrayList<>();
            if (!prefixIsSet()) {
                attributes.add("prefix");
            }
            if (!suffixIsSet()) {
                attributes.add("suffix");
            }
            return "Cannot build Doi, some of required attributes are not set " + attributes;
        }
    }
}