/*
 * MIT License
 *
 * Copyright (c) 2019 WANG Lingsong
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.jsfr.json.provider;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.jsfr.json.provider.CastUtil.castJavaObject;

public class JacksonCoreProvider implements JsonProvider<Map<Object, Object>, List<Object>, Object> {

    /**
     * Immutable shared instance
     */
    public static final JacksonCoreProvider INSTANCE = new JacksonCoreProvider();

    public JacksonCoreProvider() {
    }

    @Override
    public Map<Object, Object> createObject() {
        return new HashMap<>();
    }

    @Override
    public List<Object> createArray() {
        return new ArrayList<>();
    }

    @Override
    public void put(Map<Object, Object> object, String key, Object value) {
        object.put(key, value);
    }

    @Override
    public void add(List<Object> array, Object value) {
        array.add(value);
    }

    @Override
    public Object resolve(Map<Object, Object> object, String key) {
        return object.get(key);
    }

    @Override
    public Object resolve(List<Object> array, int index) {
        return array.get(index);
    }

    @Override
    public Object primitive(boolean value) {
        return value;
    }

    @Override
    public Object primitive(int value) {
        return value;
    }

    @Override
    public Object primitive(double value) {
        return value;
    }

    @Override
    public Object primitive(long value) {
        return value;
    }

    @Override
    public Object primitive(String value) {
        return value;
    }

    @Override
    public Object primitive(BigInteger value) {
        return value;
    }

    @Override
    public Object primitiveNull() {
        return null;
    }

    @Override
    public <T> T cast(Object value, Class<T> tClass) {
        return castJavaObject(value, tClass);
    }

    @Override
    public boolean isPrimitiveNull(Object value) {
        return value == null;
    }

    @Override
    public boolean isPrimitiveNumber(Object value) {
        return value instanceof Number;
    }

    @Override
    public boolean isPrimitiveBoolean(Object value) {
        return value instanceof Boolean;
    }

    @Override
    public boolean isPrimitiveString(Object value) {
        return value instanceof String;
    }

}
