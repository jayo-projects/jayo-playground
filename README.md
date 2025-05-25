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


Benchmark                                     (bufferVersion)  (encoding)  (length)   Mode  Cnt         Score         Error  Units
BufferReaderUtf8Benchmark.readUtf8StringJayo                2       ascii        20  thrpt    5  17549905.491 ±  170505.311  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2       ascii      2000  thrpt    5   2890159.808 ±   59153.562  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2       ascii    200000  thrpt    5     18326.260 ±     432.070  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2      latin1        20  thrpt    5  17575169.248 ±   68575.983  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2      latin1      2000  thrpt    5    429078.120 ±   16415.456  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2      latin1    200000  thrpt    5      4136.580 ±     221.592  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2        utf8        20  thrpt    5   7434844.792 ±  192799.729  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2        utf8      2000  thrpt    5    133393.868 ±    1984.872  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2        utf8    200000  thrpt    5      1377.903 ±      35.783  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2      2bytes        20  thrpt    5   9832472.969 ±  391910.460  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2      2bytes      2000  thrpt    5    208964.644 ±    5084.656  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2      2bytes    200000  thrpt    5      1981.369 ±      72.531  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3       ascii        20  thrpt    5  16829840.126 ±   68368.443  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3       ascii      2000  thrpt    5   3177488.063 ±   41837.828  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3       ascii    200000  thrpt    5     17446.632 ±     743.659  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3      latin1        20  thrpt    5  16695026.427 ±  145978.797  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3      latin1      2000  thrpt    5    395702.542 ±   23054.062  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3      latin1    200000  thrpt    5      4126.629 ±      33.263  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3        utf8        20  thrpt    5   8818198.563 ±   80215.870  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3        utf8      2000  thrpt    5    134101.439 ±    1226.810  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3        utf8    200000  thrpt    5      1382.301 ±      15.066  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3      2bytes        20  thrpt    5   9402472.266 ±   54778.644  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3      2bytes      2000  thrpt    5    208080.377 ±    5896.360  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3      2bytes    200000  thrpt    5      1983.116 ±      40.516  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4       ascii        20  thrpt    5  27892934.238 ±   43926.765  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4       ascii      2000  thrpt    5   3222602.599 ±  103958.616  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4       ascii    200000  thrpt    5     17909.919 ±     816.847  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4      latin1        20  thrpt    5  27997076.726 ±  107733.508  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4      latin1      2000  thrpt    5    415249.262 ±    3923.064  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4      latin1    200000  thrpt    5      4132.402 ±      71.969  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4        utf8        20  thrpt    5  10961925.331 ±  117576.397  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4        utf8      2000  thrpt    5    137749.884 ±    3261.610  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4        utf8    200000  thrpt    5      1387.471 ±      23.375  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4      2bytes        20  thrpt    5  12435757.765 ±   88510.542  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4      2bytes      2000  thrpt    5    211249.721 ±     532.344  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4      2bytes    200000  thrpt    5      1985.584 ±     129.595  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2       ascii        20  thrpt    5  22423038.601 ±  577607.776  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2       ascii      2000  thrpt    5   4823505.290 ±  191264.886  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2       ascii    200000  thrpt    5     46436.205 ±    2165.423  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2      latin1        20  thrpt    5  22273732.578 ± 1081946.656  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2      latin1      2000  thrpt    5    533423.713 ±    8219.477  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2      latin1    200000  thrpt    5      7863.637 ±     133.476  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2        utf8        20  thrpt    5  15289189.491 ±   81149.378  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2        utf8      2000  thrpt    5    327368.702 ±    4016.736  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2        utf8    200000  thrpt    5      3449.874 ±      65.787  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2      2bytes        20  thrpt    5  16572261.969 ±  151686.238  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2      2bytes      2000  thrpt    5    542983.264 ±    6885.212  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2      2bytes    200000  thrpt    5      4850.015 ±      30.037  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3       ascii        20  thrpt    5  20324388.264 ± 7304171.057  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3       ascii      2000  thrpt    5   5281799.149 ±  131450.601  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3       ascii    200000  thrpt    5     44041.171 ±    4078.852  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3      latin1        20  thrpt    5  20281118.324 ± 7728729.487  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3      latin1      2000  thrpt    5    734967.295 ±   15121.556  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3      latin1    200000  thrpt    5      8041.150 ±      72.846  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3        utf8        20  thrpt    5  13835172.804 ±  124983.002  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3        utf8      2000  thrpt    5    326202.641 ±    5417.517  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3        utf8    200000  thrpt    5      3470.423 ±      56.121  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3      2bytes        20  thrpt    5  15380026.061 ±   74528.497  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3      2bytes      2000  thrpt    5    536334.399 ±   11794.999  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3      2bytes    200000  thrpt    5      4835.698 ±      26.828  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4       ascii        20  thrpt    5  35307165.992 ±  277920.793  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4       ascii      2000  thrpt    5   5302389.867 ±  127679.391  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4       ascii    200000  thrpt    5     46097.623 ±     529.802  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4      latin1        20  thrpt    5  35316784.983 ±  205262.347  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4      latin1      2000  thrpt    5    535708.966 ±    3741.175  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4      latin1    200000  thrpt    5      7996.563 ±     310.062  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4        utf8        20  thrpt    5  19833876.578 ±   47839.081  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4        utf8      2000  thrpt    5    327351.257 ±    1768.877  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4        utf8    200000  thrpt    5      3471.941 ±      48.888  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4      2bytes        20  thrpt    5  22972417.373 ±  122227.955  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4      2bytes      2000  thrpt    5    536503.078 ±    2840.698  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4      2bytes    200000  thrpt    5      4830.929 ±      77.738  ops/s

## Build

You need a JDK 24 to build Jayo HTTP.

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