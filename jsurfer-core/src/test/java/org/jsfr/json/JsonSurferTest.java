/*
 * The MIT License
 *
 * Copyright (c) 2015 WANG Lingsong
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package org.jsfr.json;

import com.google.common.io.Resources;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.jsfr.json.provider.JavaCollectionProvider;
import org.jsfr.json.provider.JsonProvider;
import org.jsfr.json.provider.JsonSimpleProvider;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.isA;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class JsonSurferTest {

    protected final static Logger LOGGER = LoggerFactory.getLogger(JsonSurferTest.class);

    protected JsonSurfer surfer;
    protected JsonProvider provider;
    private JsonPathListener print = new JsonPathListener() {
        @Override
        public void onValue(Object value, ParsingContext context) {
            System.out.println(value);
        }
    };

    @Before
    public void setUp() throws Exception {
        provider = JsonSimpleProvider.INSTANCE;
        surfer = JsonSurfer.simple();
    }

    @Test
    public void testSampleJson() throws Exception {
        JsonPathListener mockListener = mock(JsonPathListener.class);
        surfer.builder().bind("$.store.book[0].category", mockListener)
                .bind("$.store.book[0]", mockListener)
                .bind("$.store.car", mockListener)
                .bind("$.store.bicycle", mockListener)
                .surf(read("sample.json"));

        Object book = provider.createObject();
        provider.put(book, "category", provider.primitive("reference"));
        provider.put(book, "author", provider.primitive("Nigel Rees"));
        provider.put(book, "title", provider.primitive("Sayings of the Century"));
        provider.put(book, "price", provider.primitive(8.95));
        verify(mockListener).onValue(eq(book), any(ParsingContext.class));

        verify(mockListener).onValue(eq(provider.primitive("reference")), any(ParsingContext.class));

        Object cars = provider.createArray();
        provider.add(cars, provider.primitive("ferrari"));
        provider.add(cars, provider.primitive("lamborghini"));
        verify(mockListener).onValue(eq(cars), any(ParsingContext.class));

        Object bicycle = provider.createObject();
        provider.put(bicycle, "color", provider.primitive("red"));
        provider.put(bicycle, "price", provider.primitive(19.95d));
        verify(mockListener).onValue(eq(bicycle), any(ParsingContext.class));
    }

    @Test
    public void testSample2() throws Exception {
        JsonPathListener mockListener = mock(JsonPathListener.class);
        surfer.builder()
                .bind("$[0].aiRuleEditorOriginal.+.barrierLevel", mockListener)
                .surf(read("sample2.json"));
        verify(mockListener).onValue(eq(provider.primitive("0.8065")), any(ParsingContext.class));
    }

    @Test
    public void testStoppableParsing() throws Exception {
        JsonPathListener mockListener = mock(JsonPathListener.class);
        doNothing().when(mockListener)
                .onValue(anyObject(), argThat(new TypeSafeMatcher<ParsingContext>() {

                    @Override
                    public boolean matchesSafely(ParsingContext parsingContext) {
                        parsingContext.stopParsing();
                        return true;
                    }

                    @Override
                    public void describeTo(Description description) {
                    }
                }));

        surfer.builder()
                .bind("$.store.book[0,1,2]", mockListener)
                .bind("$.store.book[3]", mockListener)
                .surf(read("sample.json"));
        verify(mockListener, times(1))
                .onValue(anyObject(), any(ParsingContext.class));

    }

    @Test
    public void testChildNodeWildcard() throws Exception {
        JsonPathListener mockListener = mock(JsonPathListener.class);
        surfer.builder()
                .bind("$.store.*", mockListener)
                .surf(read("sample.json"));
        verify(mockListener, times(3))
                .onValue(anyObject(), any(ParsingContext.class));
    }

    @Test
    public void testAnyIndex() throws Exception {
        JsonPathListener mockListener = mock(JsonPathListener.class);
        surfer.builder()
                .bind("$.store.book[*]", mockListener)
                .surf(read("sample.json"));
        verify(mockListener, times(4))
                .onValue(anyObject(), any(ParsingContext.class));
    }

    private String read(String resourceName) throws IOException {
        return Resources.toString(Resources.getResource(resourceName), StandardCharsets.UTF_8);
    }

    @Test
    public void testWildcardCombination() throws Exception {
        JsonPathListener mockListener = mock(JsonPathListener.class);
        surfer.builder().bind("$.store.book[*].*", mockListener)
                .surf(read("sample.json"));
        verify(mockListener, times(18)).onValue(anyObject(),
                any(ParsingContext.class));
    }

    @Test
    public void testArraySlicing() throws Exception {
        JsonPathListener mock1 = mock(JsonPathListener.class);
        JsonPathListener mock2 = mock(JsonPathListener.class);
        JsonPathListener mock3 = mock(JsonPathListener.class);
        JsonPathListener mock4 = mock(JsonPathListener.class);
        surfer.builder()
                .bind("$[:2]", mock1)
                .bind("$[0:2]", mock2)
                .bind("$[2:]", mock3)
                .bind("$[:]", mock4)
                .surf(read("array.json"));
        verify(mock1, times(2)).onValue(anyObject(), any(ParsingContext.class));
        verify(mock2, times(2)).onValue(anyObject(), any(ParsingContext.class));
        verify(mock3, times(3)).onValue(anyObject(), any(ParsingContext.class));
        verify(mock4, times(5)).onValue(anyObject(), any(ParsingContext.class));
    }

    @Test
    public void testParsingArray() throws Exception {
        JsonPathListener wholeArray = mock(JsonPathListener.class);
        JsonPathListener stringElement = mock(JsonPathListener.class);
        JsonPathListener numberElement = mock(JsonPathListener.class);
        JsonPathListener booleanElement = mock(JsonPathListener.class);
        JsonPathListener nullElement = mock(JsonPathListener.class);
        JsonPathListener objectElement = mock(JsonPathListener.class);

        surfer.builder().bind("$", wholeArray)
                .bind("$[0]", stringElement)
                .bind("$[1]", numberElement)
                .bind("$[2]", booleanElement)
                .bind("$[3]", nullElement)
                .bind("$[4]", objectElement)
                .surf(read("array.json"));
        
        Object object = provider.createObject();
        provider.put(object, "key", provider.primitive("value"));
        Object array = provider.createArray();
        provider.add(array, provider.primitive("abc"));
        provider.add(array, provider.primitive(8.88));
        provider.add(array, provider.primitive(true));
        provider.add(array, provider.primitiveNull());
        provider.add(array, object);
        verify(wholeArray).onValue(eq(array), any(ParsingContext.class));
        verify(stringElement).onValue(eq(provider.primitive("abc")), any(ParsingContext.class));
        verify(numberElement).onValue(eq(provider.primitive(8.88)), any(ParsingContext.class));
        verify(booleanElement).onValue(eq(provider.primitive(true)), any(ParsingContext.class));
        verify(nullElement).onValue(eq(provider.primitiveNull()), any(ParsingContext.class));
        verify(objectElement).onValue(eq(object), any(ParsingContext.class));

    }

    @Test
    public void testDeepScan() throws Exception {
        JsonPathListener mockListener = mock(JsonPathListener.class);
        surfer.builder().bind("$..author", mockListener)
                .bind("$..store..bicycle..color", mockListener)
                .surf(read("sample.json"));
        verify(mockListener).onValue(eq(provider.primitive("Nigel Rees")), any(ParsingContext.class));
        verify(mockListener).onValue(eq(provider.primitive("Evelyn Waugh")), any(ParsingContext.class));
        verify(mockListener).onValue(eq(provider.primitive("Herman Melville")), any(ParsingContext.class));
        verify(mockListener).onValue(eq(provider.primitive("J. R. R. Tolkien")), any(ParsingContext.class));
        verify(mockListener).onValue(eq(provider.primitive("red")), any(ParsingContext.class));

    }

    @Test
    public void testDeepScan2() throws Exception {
        JsonPathListener mockListener = mock(JsonPathListener.class);
        surfer.builder().bind("$..store..price", mockListener)
                .surf(read("sample.json"));
        verify(mockListener).onValue(eq(provider.primitive(8.95)), any(ParsingContext.class));
        verify(mockListener).onValue(eq(provider.primitive(12.99)), any(ParsingContext.class));
        verify(mockListener).onValue(eq(provider.primitive(8.99)), any(ParsingContext.class));
        verify(mockListener).onValue(eq(provider.primitive(22.99)), any(ParsingContext.class));
        verify(mockListener).onValue(eq(provider.primitive(19.95)), any(ParsingContext.class));
    }

    @Test
    public void testAny() throws Exception {
        JsonPathListener mockListener = mock(JsonPathListener.class);
        surfer.builder().bind("$.store..bicycle..*", mockListener)
                .surf(read("sample.json"));
        verify(mockListener).onValue(eq(provider.primitive("red")), any(ParsingContext.class));
        verify(mockListener).onValue(eq(provider.primitive(19.95)), any(ParsingContext.class));
    }

    @Test
    public void testFindEverything() throws Exception {
        surfer.builder()
                .bind("$..*", new JsonPathListener() {
                    @Override
                    public void onValue(Object value, ParsingContext context) {
                        LOGGER.trace("value: {}", value);
                    }
                })
                .surf(read("sample.json"));
    }

    @Test
    public void testIndexesAndChildrenOperator() throws Exception {
        JsonPathListener mockListener = mock(JsonPathListener.class);
        surfer.builder().bind("$..book[1,3][author,title]", mockListener)
                .surf(read("sample.json"));
        verify(mockListener).onValue(eq(provider.primitive("Evelyn Waugh")), any(ParsingContext.class));
        verify(mockListener).onValue(eq(provider.primitive("Sword of Honour")), any(ParsingContext.class));
        verify(mockListener).onValue(eq(provider.primitive("J. R. R. Tolkien")), any(ParsingContext.class));
        verify(mockListener).onValue(eq(provider.primitive("The Lord of the Rings")), any(ParsingContext.class));
    }

    @Test
    public void testCollectAllRaw() throws Exception {
        Collection<Object> values = surfer.collectAll(read("sample.json"), "$..book[1,3][author,title]");
        assertEquals(4, values.size());
        Iterator<Object> itr = values.iterator();
        itr.next();
        assertEquals("Sword of Honour", itr.next());
    }

    @Test
    public void testCollectOneRaw() throws Exception {
        Object value = surfer.collectOne(read("sample.json"), "$..book[1,3][author,title]");
        assertEquals("Evelyn Waugh", value);
    }

    @Test
    public void testCollectAll() throws Exception {
        Collection<String> values = surfer.collectAll(read("sample.json"), String.class, "$..book[1,3][author, title]");
        assertEquals(4, values.size());
        assertEquals("Evelyn Waugh", values.iterator().next());
    }

    @Test
    public void testCollectOne() throws Exception {
        String value = surfer.collectOne(read("sample.json"), String.class, "$..book[1,3][author,title]");
        assertEquals("Evelyn Waugh", value);
    }

    @Test
    public void testGetCurrentFieldName() throws Exception {
        surfer.builder()
                .bind("$.store.book[0].title", new JsonPathListener() {
                    @Override
                    public void onValue(Object value, ParsingContext context) throws Exception {
                        assertEquals(context.getCurrentFieldName(), "title");
                    }
                })
                .surf(read("sample.json"));
    }

    @Test
    public void testGetCurrentArrayIndex() throws Exception {
        surfer.builder().bind("$.store.book[3]", new JsonPathListener() {
            @Override
            public void onValue(Object value, ParsingContext context) throws Exception {
                assertEquals(context.getCurrentArrayIndex(), 3);
            }
        }).surf(read("sample.json"));
    }

    @Test
    public void testExample1() throws Exception {
        surfer.builder().bind("$.store.book[*].author", print).surf(read("sample.json"));
    }

    @Test
    public void testExample2() throws Exception {
        surfer.builder().bind("$..author", print).surf(read("sample.json"));
    }

    @Test
    public void testExample3() throws Exception {
        surfer.builder().bind("$.store.*", print).surf(read("sample.json"));
    }

    @Test
    public void testExample4() throws Exception {
        surfer.builder().bind("$.store..price", print).surf(read("sample.json"));
    }

    @Test
    public void testExample5() throws Exception {
        surfer.builder().bind("$..book[2]", print).surf(read("sample.json"));
    }

    @Test
    public void testExample6() throws Exception {
        surfer.builder().bind("$..book[0,1]", print).surf(read("sample.json"));
    }

    @Test
    public void testStoppable() throws Exception {
        surfer.builder().bind("$..book[0,1]", new JsonPathListener() {
            @Override
            public void onValue(Object value, ParsingContext parsingContext) {
                parsingContext.stopParsing();
                System.out.println(value);
            }
        }).surf(read("sample.json"));
    }

    @Test
    public void testPlugableProvider() throws Exception {
        JsonPathListener mockListener = mock(JsonPathListener.class);
        surfer.builder().withJsonProvider(JavaCollectionProvider.INSTANCE)
                .bind("$.store", mockListener)
                .surf(read("sample.json"));
        verify(mockListener).onValue(isA(HashMap.class), any(ParsingContext.class));
    }

    @Test
    public void testErrorStrategySuppressException() throws Exception {
        
        JsonPathListener mock = mock(JsonPathListener.class);
        doNothing().doThrow(Exception.class).doThrow(Exception.class).when(mock).onValue(anyObject(), any(ParsingContext.class));

        surfer.builder().bind("$.store.book[*]", mock)
                .withErrorStrategy(new ErrorHandlingStrategy() {
                    @Override
                    public void handleParsingException(Exception e) {
                        // suppress exception
                    }

                    @Override
                    public void handleExceptionFromListener(Exception e, ParsingContext context) {
                        // suppress exception
                    }
                })
                .surf(read("sample.json"));
        verify(mock, times(4)).onValue(anyObject(), any(ParsingContext.class));
    }

    @Test
    public void testErrorStrategyThrowException() throws Exception {
        
        JsonPathListener mock = mock(JsonPathListener.class);
        doNothing().doThrow(Exception.class).doThrow(Exception.class).when(mock).onValue(anyObject(), any(ParsingContext.class));
        try {
            surfer.builder().bind("$.store.book[*]", mock).surf(read("sample.json"));
        } catch (Exception e) {
            // catch mock exception
        }
        verify(mock, times(2)).onValue(anyObject(), any(ParsingContext.class));
    }

}
