[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?logo=apache&style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white&style=flat-square)](https://www.java.com/en/download/help/whatis_java.html)
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.0-blue.svg?logo=kotlin&style=flat-square)](http://kotlinlang.org)

# Jayo Playground

A project to test and choose between various implementations for Jayo related subjects, with unit tests and JMH
benchmarks.

Benchmark                                     (executorType)  (schedulerVersion)   Mode  Cnt     Score     Error  Units
TaskRunnerBenchmark.taskRunner1Execute               virtual                   0  thrpt    5  1023.773 ±  33.712  ops/s
TaskRunnerBenchmark.taskRunner1Execute               virtual                   1  thrpt    5  1095.511 ±  61.180  ops/s
TaskRunnerBenchmark.taskRunner1Execute               virtual                   2  thrpt    5  1293.925 ±  29.673  ops/s
TaskRunnerBenchmark.taskRunner1Execute               virtual                   3  thrpt    5  2096.507 ±  96.352  ops/s
TaskRunnerBenchmark.taskRunner1Execute               virtual                   4  thrpt    5  2064.115 ±  37.897  ops/s
TaskRunnerBenchmark.taskRunner1Execute               virtual                   5  thrpt    5  2075.674 ± 100.090  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute          virtual                   0  thrpt    5  1060.094 ±  50.561  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute          virtual                   1  thrpt    5   974.611 ±  34.275  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute          virtual                   2  thrpt    5  1114.535 ±  42.528  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute          virtual                   3  thrpt    5  1408.607 ±  23.957  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute          virtual                   4  thrpt    5  1317.542 ±  97.594  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute          virtual                   5  thrpt    5  1354.228 ±  81.826  ops/s
TaskRunnerBenchmark.taskRunner3QueueSchedule         virtual                   0  thrpt    5  1448.570 ±  35.487  ops/s
TaskRunnerBenchmark.taskRunner3QueueSchedule         virtual                   1  thrpt    5  1419.600 ±  53.860  ops/s
TaskRunnerBenchmark.taskRunner3QueueSchedule         virtual                   2  thrpt    5  1574.917 ±  40.142  ops/s
TaskRunnerBenchmark.taskRunner3QueueSchedule         virtual                   3  thrpt    5  1550.014 ±  31.399  ops/s
TaskRunnerBenchmark.taskRunner3QueueSchedule         virtual                   4  thrpt    5  1566.980 ±  45.472  ops/s
TaskRunnerBenchmark.taskRunner3QueueSchedule         virtual                   5  thrpt    5  1561.587 ±  59.623  ops/s
TaskRunnerBenchmark.taskRunner4Mixed                 virtual                   0  thrpt    5   943.135 ±  25.627  ops/s
TaskRunnerBenchmark.taskRunner4Mixed                 virtual                   1  thrpt    5   957.676 ±  24.779  ops/s
TaskRunnerBenchmark.taskRunner4Mixed                 virtual                   2  thrpt    5  1126.062 ±  46.710  ops/s
TaskRunnerBenchmark.taskRunner4Mixed                 virtual                   3  thrpt    5  1012.821 ±  50.674  ops/s
TaskRunnerBenchmark.taskRunner4Mixed                 virtual                   4  thrpt    5  1180.798 ± 123.451  ops/s
TaskRunnerBenchmark.taskRunner4Mixed                 virtual                   5  thrpt    5  1198.274 ± 152.797  ops/s

## Build

You need a JDK 21 to build Jayo HTTP.

1. Clone this repo

```bash
git clone git@github.com:jayo-projects/jayo-http.git
```

2. Build the project

```bash
./gradlew clean build
```

## License

[Apache-2.0](https://opensource.org/license/apache-2-0)

Copyright (c) 2025-present, pull-vert and Jayo contributors