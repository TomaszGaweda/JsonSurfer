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

package org.jsfr.json.path;

/**
 * Created by Leo on 2015/4/1.
 */
public class ArraySlicing extends ChildNode {

    private final Integer lowerBound;
    private final Integer upperBound;

    protected ArraySlicing(String key, Integer lowerBound, Integer upperBound) {
        super(key);
        this.lowerBound = lowerBound;
        this.upperBound = upperBound;
    }

    protected ArraySlicing(Integer lowerBound, Integer upperBound) {
        this(null, lowerBound, upperBound);
    }

    @Override
    public boolean match(PathOperator pathOperator) {
        if (!super.match(pathOperator)) {
            return false;
        }
        if (pathOperator instanceof ArrayIndex) {
            int index = ((ArrayIndex) pathOperator).getArrayIndex();
            if (lowerBound == null && upperBound == null) {
                return true;
            } else if (lowerBound == null) {
                return index < upperBound;
            } else if (upperBound == null) {
                return index >= lowerBound;
            } else {
                return lowerBound <= index && index < upperBound;
            }
        } else {
            throw new IllegalStateException("unexpected path operator: " + pathOperator);
        }
    }

    @Override
    public Type getType() {
        return Type.ARRAY;
    }
}
