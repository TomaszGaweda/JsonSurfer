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

import tools.jackson.core.FormatSchema;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.json.async.NonBlockingByteArrayJsonParser;
import org.jsfr.json.provider.JsonProvider;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ObjectReader;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class JacksonParser implements JsonParserAdapter {

    private static class JacksonNonblockingParser extends JacksonResumableParser implements NonBlockingParser {

        private final NonBlockingByteArrayJsonParser nonBlockingJsonParser;

        JacksonNonblockingParser(NonBlockingByteArrayJsonParser jsonParser, SurfingContext context) {
            super(jsonParser, context);
            this.nonBlockingJsonParser = jsonParser;
        }

        @Override
        public boolean feed(byte[] bytes, int start, int end) {
            try {
                if (nonBlockingJsonParser.needMoreInput() && !context.isStopped()) {
                    nonBlockingJsonParser.feedInput(bytes, start, end);
                    if (context.isPaused()) {
                        context.resume();
                        doParse();
                    } else {
                        parse();
                    }
                    return true;
                }
            } catch (IOException e) {
                context.getConfig().getErrorHandlingStrategy().handleParsingException(e);
            }
            return false;
        }

        @Override
        public void endOfInput() {
            nonBlockingJsonParser.endOfInput();
            resume();
        }

    }

    private static class JacksonResumableParser implements ResumableParser {

        protected SurfingContext context;
        private final JsonParser jsonParser;
        private final AbstractPrimitiveHolder stringHolder;
        private final AbstractPrimitiveHolder longHolder;
        private final AbstractPrimitiveHolder doubleHolder;
        private final StaticPrimitiveHolder staticHolder;

        JacksonResumableParser(final JsonParser jsonParser, SurfingContext context) {
            this.jsonParser = jsonParser;
            this.context = context;
            final JsonProvider jsonProvider = context.getConfig().getJsonProvider();
            this.stringHolder = new AbstractPrimitiveHolder(context.getConfig()) {
                @Override
                public Object doGetValue() {
                    return jsonProvider.primitive(jsonParser.getString());
                }

                @Override
                public void doSkipValue() {
                }
            };
            this.longHolder = new AbstractPrimitiveHolder(context.getConfig()) {
                @Override
                public Object doGetValue() {
                    if (jsonParser.getNumberType() == JsonParser.NumberType.BIG_INTEGER) {
                        return jsonProvider.primitive(jsonParser.getBigIntegerValue());
                    }  else {
                        return jsonProvider.primitive(jsonParser.getLongValue());
                    }
                }

                @Override
                public void doSkipValue() {
                }
            };
            this.doubleHolder = new AbstractPrimitiveHolder(context.getConfig()) {
                @Override
                public Object doGetValue() {
                    return jsonProvider.primitive(jsonParser.getDoubleValue());
                }

                @Override
                public void doSkipValue() {
                }
            };
            this.staticHolder = new StaticPrimitiveHolder();
        }

        @Override
        public boolean resume() {
            try {
                if (!context.isPaused()) {
                    return false;
                }
                context.resume();
                doParse();
                return true;
            } catch (Exception e) {
                context.getConfig().getErrorHandlingStrategy().handleParsingException(e);
                return false;
            }
        }

        @Override
        public void parse() {
            context.startJSON();
            try {
                doParse();
            } catch (Exception e) {
                context.getConfig().getErrorHandlingStrategy().handleParsingException(e);
            }
        }

        void doParse() throws IOException {
            JsonProvider jsonProvider = context.getConfig().getJsonProvider();
            while (!context.shouldBreak()) {
                JsonToken token = jsonParser.nextToken();
                if (token == null) {
                    context.endJSON();
                    break;
                }
                switch (token) {
                    case NOT_AVAILABLE:
                        context.pause();
                        break;
                    case START_OBJECT:
                        context.startObject();
                        break;
                    case END_OBJECT:
                        context.endObject();
                        break;
                    case START_ARRAY:
                        context.startArray();
                        break;
                    case END_ARRAY:
                        context.endArray();
                        break;
                    case PROPERTY_NAME:
                        context.startObjectEntry(jsonParser.currentName());
                        break;
                    case VALUE_STRING:
                        stringHolder.init();
                        context.primitive(stringHolder);
                        stringHolder.skipValue();
                        break;
                    case VALUE_NUMBER_INT:
                        longHolder.init();
                        context.primitive(longHolder);
                        longHolder.skipValue();
                        break;
                    case VALUE_NUMBER_FLOAT:
                        doubleHolder.init();
                        context.primitive(doubleHolder);
                        doubleHolder.skipValue();
                        break;
                    case VALUE_TRUE:
                        context.primitive(staticHolder.withValue(jsonProvider.primitive(true)));
                        break;
                    case VALUE_FALSE:
                        context.primitive(staticHolder.withValue(jsonProvider.primitive(false)));
                        break;
                    case VALUE_NULL:
                        context.primitive(staticHolder.withValue(jsonProvider.primitiveNull()));
                        break;
                    case VALUE_EMBEDDED_OBJECT:
                    default:
                        throw new IllegalStateException("Unexpected token: " + token);
                }
                if (context.getConfig().isCloseParserOnStop() && context.isStopped()) {
                    this.jsonParser.close();
                }
            }
        }

    }

    private static final ObjectMapper JSON_MAPPER = new JsonMapper();
    /**
     * Immutable shared instance
     */
    public static final JacksonParser INSTANCE = new JacksonParser(JSON_MAPPER);

    private final ObjectMapper objectMapper;
    private FormatSchema formatSchema;

    public JacksonParser(ObjectMapper mapper) {
        this.objectMapper = mapper;
    }

    public JacksonParser(ObjectMapper mapper, FormatSchema formatSchema) {
        this.objectMapper = mapper;
        this.formatSchema = formatSchema;
    }
    @Override
    public void parse(Reader reader, final SurfingContext context) {
        createResumableParser(reader, context).parse();
    }

    @Override
    public void parse(String json, SurfingContext context) {
        createResumableParser(json, context).parse();
    }

    @Override
    public void parse(InputStream inputStream, SurfingContext context) {
        createResumableParser(inputStream, context).parse();
    }

    @Override
    public ResumableParser createResumableParser(Reader reader, SurfingContext context) {
        try {
            final JsonParser jp = readerWithSchema().createParser(reader);
            return new JacksonResumableParser(jp, context);
        } catch (Exception e) {
            context.getConfig().getErrorHandlingStrategy().handleParsingException(e);
            return null;
        }
    }

    @Override
    public ResumableParser createResumableParser(String json, SurfingContext context) {
        try {
            final JsonParser jp = readerWithSchema().createParser(json);
            return new JacksonResumableParser(jp, context);
        } catch (Exception e) {
            context.getConfig().getErrorHandlingStrategy().handleParsingException(e);
            return null;
        }
    }

    private ObjectReader readerWithSchema() {
        ObjectReader reader = objectMapper.reader();
        if (formatSchema != null) {
            reader = reader.with(formatSchema);
        }
        return reader;
    }

    @Override
    public ResumableParser createResumableParser(InputStream json, SurfingContext context) {
        try {
            final JsonParser jp = readerWithSchema().createParser(json);
            return new JacksonResumableParser(jp, context);
        } catch (Exception e) {
            context.getConfig().getErrorHandlingStrategy().handleParsingException(e);
            return null;
        }
    }

    @Override
    public NonBlockingParser createNonBlockingParser(SurfingContext context) {
        try {
            ObjectReader reader = objectMapper.reader();

            if (formatSchema != null) {
                reader = reader.with(formatSchema);
            }
            var jp = (NonBlockingByteArrayJsonParser) reader.createNonBlockingByteArrayParser();
            return new JacksonNonblockingParser(jp, context);
        } catch (JacksonException e) {
            context.getConfig().getErrorHandlingStrategy().handleParsingException(e);
        }
        return null;
    }

}
