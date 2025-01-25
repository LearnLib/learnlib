/* Copyright (C) 2013-2025 TU Dortmund University
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
package de.learnlib.datastructure.discriminationtree.model;

import java.util.AbstractMap;
import java.util.AbstractSet;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import net.automatalib.common.util.HashUtil;
import org.checkerframework.checker.nullness.qual.KeyFor;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * Primitive implementation for boolean maps.
 *
 * @param <V>
 *         value type
 */
public class BooleanMap<V> extends AbstractMap<Boolean, V> {

    private V falseValue;
    private V trueValue;

    public BooleanMap() {}

    public BooleanMap(V falseValue, V trueValue) {
        this.falseValue = falseValue;
        this.trueValue = trueValue;
    }

    @Override
    public int size() {
        return 2;
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public boolean containsValue(@Nullable Object value) {
        return Objects.equals(falseValue, value) || Objects.equals(trueValue, value);
    }

    @Override
    public boolean containsKey(@Nullable Object key) {
        return key != null && key.getClass() == Boolean.class;
    }

    @Override
    public @Nullable V get(@Nullable Object key) {
        if (key == null || key.getClass() != Boolean.class) {
            return null;
        }
        boolean bval = (Boolean) key;
        return get(bval);
    }

    public V get(boolean key) {
        return key ? trueValue : falseValue;
    }

    @Override
    public V put(@Nullable Boolean key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("BooleanMap disallows null keys");
        }
        boolean bval = key;
        return put(bval, value);
    }

    public V put(boolean key, V value) {
        V old;
        if (key) {
            old = trueValue;
            trueValue = value;
        } else {
            old = falseValue;
            falseValue = value;
        }
        return old;
    }

    @Override
    public @Nullable V remove(@Nullable Object key) {
        if (key == null || key.getClass() != Boolean.class) {
            return null;
        }
        throw new UnsupportedOperationException("BooleanMap disallows removal");
    }

    @Override
    public void putAll(Map<? extends Boolean, ? extends V> m) {
        if (m.containsKey(null)) {
            throw new IllegalArgumentException("BooleanMap disallows null keys");
        }
        if (m.containsKey(Boolean.FALSE)) {
            this.falseValue = m.get(Boolean.FALSE);
        }
        if (m.containsKey(Boolean.TRUE)) {
            this.trueValue = m.get(Boolean.TRUE);
        }
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException("BooleanMap disallows removal");
    }

    @Override
    public Set<@KeyFor("this") Boolean> keySet() {
        return BooleanSet.INSTANCE;
    }

    @Override
    public Collection<V> values() {
        return Arrays.asList(falseValue, trueValue);
    }

    @Override
    public Set<Map.Entry<@KeyFor("this") Boolean, V>> entrySet() {
        Set<Map.Entry<Boolean, V>> entries = new HashSet<>(HashUtil.capacity(2));
        entries.add(new Entry(false));
        entries.add(new Entry(true));
        return entries;
    }

    private static final class BooleanSet extends AbstractSet<Boolean> {

        private static final List<Boolean> VALUES = Arrays.asList(Boolean.FALSE, Boolean.TRUE);

        private static final BooleanSet INSTANCE = new BooleanSet();

        @Override
        public Iterator<Boolean> iterator() {
            return VALUES.iterator();
        }

        @Override
        public int size() {
            return 2;
        }

        @Override
        public boolean isEmpty() {
            return false;
        }

        @Override
        public boolean contains(@Nullable Object o) {
            return o != null && o.getClass() == Boolean.class;
        }
    }

    private final class Entry implements Map.Entry<Boolean, V> {

        private final boolean key;

        Entry(boolean key) {
            this.key = key;
        }

        @Override
        public Boolean getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return get(key);
        }

        @Override
        public V setValue(V value) {
            return put(key, value);
        }

        @Override
        public int hashCode() {
            return Boolean.hashCode(key);
        }

        @Override
        public boolean equals(@Nullable Object o) {
            if (this == o) {
                return true;
            }
            if (!(o instanceof BooleanMap.Entry)) {
                return false;
            }

            final BooleanMap<?>.Entry that = (BooleanMap<?>.Entry) o;
            return Objects.equals(key, that.key);
        }
    }
}
