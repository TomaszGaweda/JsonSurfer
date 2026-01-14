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

package org.jsfr.json;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.dataformat.smile.SmileFactory;
import org.jsfr.json.provider.JacksonProvider;
import org.junit.Before;
import tools.jackson.dataformat.smile.SmileMapper;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

public class JacksonSmileParserTest extends JsonSurferTest<ObjectNode, ArrayNode, JsonNode> {

    @Before
    public void setUp() {
        provider = new JacksonProvider();
        surfer = new JsonSurfer(new JacksonParser(new SmileMapper()), provider);
    }

    @Override
    protected InputStream read(String resourceName) {
        ObjectMapper om = new ObjectMapper();
        JsonNode node = om.readTree(this.readAsString(resourceName));
        SmileFactory f = new SmileFactory();
        ObjectMapper cborMapper = new ObjectMapper(f);
        byte[] smileData = cborMapper.writeValueAsBytes(node);
        return new ByteArrayInputStream(smileData);
    }

    @Override
    public void testCollectAllFromString() {
        // skip non-byte-based source
    }

    @Override
    public void testCollectOneFromString() {
        // skip non-byte-based source
    }

    @Override
    public void testWildcardAtRoot() {
        // skip non-byte-based source
    }

}
