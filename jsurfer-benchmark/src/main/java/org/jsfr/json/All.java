package org.jsfr.json;

import org.openjdk.jmh.runner.Runner;
import org.openjdk.jmh.runner.RunnerException;
import org.openjdk.jmh.runner.options.Options;
import org.openjdk.jmh.runner.options.OptionsBuilder;

public final class All {

    private All() {
    }

    public static void main(String[] args) throws RunnerException {
        Options opt = new OptionsBuilder()
                .include(BenchmarkCollectObject.class.getSimpleName())
                .include(BenchmarkCollectSingleValue.class.getSimpleName())
                .include(BenchmarkParseLargeJson.class.getSimpleName())
                .include(BenchmarkParseLongText.class.getSimpleName())
                .include(BenchmarkParseWithoutStreamingLargeJson.class.getSimpleName())
                .build();
        new Runner(opt).run();
    }
}
