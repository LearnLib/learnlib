/* Copyright (C) 2013-2026 TU Dortmund University
 * This file is part of LearnLib <https://learnlib.de>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.learnlib.statistic;

import java.util.Objects;

import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A key to be used when interacting with {@link StatisticsService}s. Conceptionally, a {@link StatisticsKey} consists
 * of a base {@code key} and an optional {@code id} to denote {@link #isSubkeyOf(StatisticsKey) hierarchical}
 * relationships. {@link StatisticsKey}s can be enhanced with (optional) descriptions for displaying purposes. However,
 * the descriptions are not part of a keys {@link Object#equals(Object) identity}.
 *
 * @see StatisticsService
 */
public final class StatisticsKey {

    private final String key;
    private final @Nullable String description;
    private final @Nullable String id;

    /**
     * Convenience constructor for {@link #StatisticsKey(String, String)} which sets {@code description} to
     * {@code null}.
     *
     * @param key
     *         the base key
     */
    public StatisticsKey(String key) {
        this(key, null);
    }

    /**
     * Convenience constructor for {@link #StatisticsKey(String, String, String)} which sets {@code id} to
     * {@code null}.
     *
     * @param key
     *         the base key
     * @param description
     *         the (optional) description
     */
    public StatisticsKey(String key, @Nullable String description) {
        this(key, description, null);
    }

    /**
     * Constructor for creating a {@link StatisticsKey} with the given base key and id, as well as a description.
     *
     * @param key
     *         the base key
     * @param description
     *         the (optional) description
     * @param id
     *         the (optional) id used for denoting subkeys
     */
    public StatisticsKey(String key, @Nullable String description, @Nullable String id) {
        this.key = key;
        this.description = description;
        this.id = id;
    }

    /**
     * Returns a copy of this key with its {@code id} set to the provided value.
     *
     * @param id
     *         the id of the new key
     *
     * @return the key with an updated id
     */
    public StatisticsKey withId(@Nullable String id) {
        return Objects.equals(this.id, id) ? this : new StatisticsKey(this.key, this.description, id);
    }

    /**
     * Returns the effective representation of this key, i.e., the base key potentially augmented with an id.
     *
     * @return the effective representation of this key
     */
    public String getKey() {
        return this.id == null ? this.key : this.key + "-" + this.id;
    }

    /**
     * Returns the effective description of this key, i.e., the base description potentially augmented with an id.
     *
     * @return the effective description of this key or {@code null} if the base description is {@code null} as well
     */
    public @Nullable String getDescription() {
        if (this.description != null) {
            if (this.id == null) {
                return this.description;
            } else {
                return this.description + " (for '" + this.id + "')";
            }
        }
        return null;
    }

    /**
     * Returns whether {@code this} key is a subkey of the given one. A subkey relationship holds iff both base keys are
     * equal and {@code this} key's {@code id} is either undefined to equal to the given key's {@code id}.
     *
     * @param that
     *         the other key to compare to
     *
     * @return {@code true} if {@code this} is a subkey of the provided one, {@code false} otherwise
     */
    public boolean isSubkeyOf(StatisticsKey that) {
        return this.key.equals(that.key) && (this.id == null || this.id.equals(that.id));
    }

    @Override
    public boolean equals(@Nullable Object o) {
        return o == this ||
               o instanceof StatisticsKey that && this.key.equals(that.key) && Objects.equals(this.id, that.id);
    }

    @Override
    public int hashCode() {
        int result = Objects.hashCode(key);
        result = 31 * result + Objects.hashCode(id);
        return result;
    }

    @Override
    public String toString() {
        String description = getDescription();
        if (description != null) {
            return description;
        } else {
            return getKey();
        }
    }
}


