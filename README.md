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
BufferReaderUtf8Benchmark.readUtf8StringJayo                2       ascii        20  thrpt    5  14709570.274 ±  533670.157  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2       ascii      2000  thrpt    5    465540.080 ±    1829.241  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2       ascii    200000  thrpt    5      4480.107 ±      78.609  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2        utf8        20  thrpt    5   8962663.923 ±   52495.640  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2        utf8      2000  thrpt    5    120466.469 ±     858.183  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2        utf8    200000  thrpt    5      1208.783 ±      26.884  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2      2bytes        20  thrpt    5   9893906.468 ±  150083.781  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2      2bytes      2000  thrpt    5    212051.845 ±    2477.156  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2      2bytes    200000  thrpt    5      1936.826 ±      88.269  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3       ascii        20  thrpt    5  14760471.725 ± 4910975.245  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3       ascii      2000  thrpt    5   1344087.720 ±    3372.038  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3       ascii    200000  thrpt    5     10697.196 ±    1548.261  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3        utf8        20  thrpt    5   6809608.332 ±  423085.027  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3        utf8      2000  thrpt    5     77446.789 ±     529.153  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3        utf8    200000  thrpt    5       785.921 ±      44.044  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3      2bytes        20  thrpt    5   5905765.269 ±  538944.065  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3      2bytes      2000  thrpt    5     97457.982 ±    5576.906  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3      2bytes    200000  thrpt    5       884.385 ±       6.958  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4       ascii        20  thrpt    5  20265246.122 ±  214827.124  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4       ascii      2000  thrpt    5    640108.957 ±   56856.655  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4       ascii    200000  thrpt    5      5872.290 ±      98.809  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4        utf8        20  thrpt    5  10267687.811 ±   42618.482  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4        utf8      2000  thrpt    5    111381.272 ±    5206.741  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4        utf8    200000  thrpt    5      1191.089 ±       3.451  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4      2bytes        20  thrpt    5  11224004.345 ±  101899.688  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4      2bytes      2000  thrpt    5    228572.803 ±    1916.274  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4      2bytes    200000  thrpt    5      1858.515 ±      15.163  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2       ascii        20  thrpt    5  16959818.646 ±  477776.156  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2       ascii      2000  thrpt    5    518137.203 ±    5695.714  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2       ascii    200000  thrpt    5      5216.322 ±     153.977  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2        utf8        20  thrpt    5  12472078.056 ±  668325.728  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2        utf8      2000  thrpt    5    243709.669 ±    1667.865  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2        utf8    200000  thrpt    5      2649.412 ±       3.758  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2      2bytes        20  thrpt    5  17103201.626 ±  584836.058  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2      2bytes      2000  thrpt    5    548657.390 ±     653.546  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2      2bytes    200000  thrpt    5      4608.450 ±      40.882  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3       ascii        20  thrpt    5  17856663.919 ±  194839.077  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3       ascii      2000  thrpt    5   1679764.401 ±    8609.642  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3       ascii    200000  thrpt    5     17330.069 ±     140.147  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3        utf8        20  thrpt    5  10067923.982 ±  365582.945  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3        utf8      2000  thrpt    5    113567.975 ±    1579.054  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3        utf8    200000  thrpt    5      1175.067 ±       3.498  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3      2bytes        20  thrpt    5   8126674.125 ±  346160.542  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3      2bytes      2000  thrpt    5    130378.902 ±    8873.813  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3      2bytes    200000  thrpt    5      1196.793 ±       3.136  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4       ascii        20  thrpt    5  25228977.285 ±   12261.160  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4       ascii      2000  thrpt    5    733399.943 ±     953.770  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4       ascii    200000  thrpt    5      7406.330 ±      94.278  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4        utf8        20  thrpt    5  17655245.537 ±  152590.184  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4        utf8      2000  thrpt    5    214570.249 ±    1984.880  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4        utf8    200000  thrpt    5      2385.401 ±       9.000  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4      2bytes        20  thrpt    5  22384192.984 ±   93252.886  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4      2bytes      2000  thrpt    5    715470.290 ±   12150.263  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4      2bytes    200000  thrpt    5      4047.927 ±      40.675  ops/s

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