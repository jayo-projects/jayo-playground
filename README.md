[![License](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?logo=apache&style=flat-square)](https://www.apache.org/licenses/LICENSE-2.0)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?logo=openjdk&logoColor=white&style=flat-square)](https://www.java.com/en/download/help/whatis_java.html)
[![Kotlin](https://img.shields.io/badge/kotlin-2.1.0-blue.svg?logo=kotlin&style=flat-square)](http://kotlinlang.org)

# Jayo Playground

A project to test and choose between various implementations for Jayo related subjects, with unit tests and JMH
benchmarks.

Benchmark                                           (executorType)  (taskRunnerVersion)   Mode  Cnt     Score      Error  Units
TaskRunnerBenchmark.taskRunner1Execute                     virtual                    0  thrpt    5   682.455 ±   18.581  ops/s
TaskRunnerBenchmark.taskRunner1Execute                     virtual                    1  thrpt    5   730.053 ±   38.032  ops/s
TaskRunnerBenchmark.taskRunner1Execute                     virtual                    2  thrpt    5  1001.282 ±   26.057  ops/s
TaskRunnerBenchmark.taskRunner1Execute                     virtual                    3  thrpt    5  1735.006 ±   48.599  ops/s
TaskRunnerBenchmark.taskRunner1Execute                     virtual                    4  thrpt    5  1763.359 ±   21.171  ops/s
TaskRunnerBenchmark.taskRunner1Execute                     virtual                    5  thrpt    5  1778.049 ±   61.905  ops/s
TaskRunnerBenchmark.taskRunner1Execute                     virtual                    6  thrpt    5  2713.770 ±   70.777  ops/s
TaskRunnerBenchmark.taskRunner1Execute                    platform                    0  thrpt    5   271.201 ±   22.730  ops/s
TaskRunnerBenchmark.taskRunner1Execute                    platform                    1  thrpt    5   278.007 ±   19.627  ops/s
TaskRunnerBenchmark.taskRunner1Execute                    platform                    2  thrpt    5   353.131 ±   34.587  ops/s
TaskRunnerBenchmark.taskRunner1Execute                    platform                    3  thrpt    5   492.062 ±   45.165  ops/s
TaskRunnerBenchmark.taskRunner1Execute                    platform                    4  thrpt    5   482.964 ±   66.702  ops/s
TaskRunnerBenchmark.taskRunner1Execute                    platform                    5  thrpt    5   464.303 ±   58.107  ops/s
TaskRunnerBenchmark.taskRunner1Execute                    platform                    6  thrpt    5  2369.841 ±  302.160  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute                virtual                    0  thrpt    5   714.787 ±   49.920  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute                virtual                    1  thrpt    5   712.214 ±   24.760  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute                virtual                    2  thrpt    5   813.587 ±   23.006  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute                virtual                    3  thrpt    5  1030.245 ±   29.380  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute                virtual                    4  thrpt    5  1052.442 ±   42.154  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute                virtual                    5  thrpt    5  1039.818 ±   26.519  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute                virtual                    6  thrpt    5  1034.981 ±   19.068  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute               platform                    0  thrpt    5   349.434 ±   32.900  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute               platform                    1  thrpt    5   316.756 ±   47.972  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute               platform                    2  thrpt    5   395.112 ±   25.594  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute               platform                    3  thrpt    5   499.873 ±   23.388  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute               platform                    4  thrpt    5   548.084 ±   87.853  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute               platform                    5  thrpt    5   538.193 ±  173.195  ops/s
TaskRunnerBenchmark.taskRunner2QueueExecute               platform                    6  thrpt    5   516.275 ±   54.078  ops/s
TaskRunnerBenchmark.taskRunner3QueueDelayedExecute         virtual                    0  thrpt    5     0.485 ±    0.003  ops/s
TaskRunnerBenchmark.taskRunner3QueueDelayedExecute         virtual                    1  thrpt    5     0.486 ±    0.002  ops/s
TaskRunnerBenchmark.taskRunner3QueueDelayedExecute         virtual                    2  thrpt    5     0.487 ±    0.002  ops/s
TaskRunnerBenchmark.taskRunner3QueueDelayedExecute         virtual                    3  thrpt    5     0.486 ±    0.004  ops/s
TaskRunnerBenchmark.taskRunner3QueueDelayedExecute         virtual                    4  thrpt    5     0.487 ±    0.003  ops/s
TaskRunnerBenchmark.taskRunner3QueueDelayedExecute         virtual                    5  thrpt    5     0.486 ±    0.001  ops/s
TaskRunnerBenchmark.taskRunner3QueueDelayedExecute         virtual                    6  thrpt    5     0.486 ±    0.004  ops/s
TaskRunnerBenchmark.taskRunner3QueueDelayedExecute        platform                    0  thrpt    5     0.490 ±    0.001  ops/s
TaskRunnerBenchmark.taskRunner3QueueDelayedExecute        platform                    1  thrpt    5     0.490 ±    0.002  ops/s
TaskRunnerBenchmark.taskRunner3QueueDelayedExecute        platform                    2  thrpt    5     0.490 ±    0.001  ops/s
TaskRunnerBenchmark.taskRunner3QueueDelayedExecute        platform                    3  thrpt    5     0.490 ±    0.004  ops/s
TaskRunnerBenchmark.taskRunner3QueueDelayedExecute        platform                    4  thrpt    5     0.490 ±    0.001  ops/s
TaskRunnerBenchmark.taskRunner3QueueDelayedExecute        platform                    5  thrpt    5     0.490 ±    0.001  ops/s
TaskRunnerBenchmark.taskRunner3QueueDelayedExecute        platform                    6  thrpt    5     0.490 ±    0.001  ops/s
TaskRunnerBenchmark.taskRunner4QueueSchedule               virtual                    0  thrpt    5  2936.698 ±  246.517  ops/s
TaskRunnerBenchmark.taskRunner4QueueSchedule               virtual                    1  thrpt    5  2756.079 ±  428.454  ops/s
TaskRunnerBenchmark.taskRunner4QueueSchedule               virtual                    2  thrpt    5  3353.215 ± 1474.426  ops/s
TaskRunnerBenchmark.taskRunner4QueueSchedule               virtual                    3  thrpt    5  3472.097 ±  960.435  ops/s
TaskRunnerBenchmark.taskRunner4QueueSchedule               virtual                    4  thrpt    5  3277.657 ± 1210.014  ops/s
TaskRunnerBenchmark.taskRunner4QueueSchedule               virtual                    5  thrpt    5  3421.987 ±  872.097  ops/s
TaskRunnerBenchmark.taskRunner4QueueSchedule               virtual                    6  thrpt    5  3952.743 ±  168.387  ops/s
TaskRunnerBenchmark.taskRunner4QueueSchedule              platform                    0  thrpt    5   561.063 ±   71.015  ops/s
TaskRunnerBenchmark.taskRunner4QueueSchedule              platform                    1  thrpt    5   552.500 ±   47.636  ops/s
TaskRunnerBenchmark.taskRunner4QueueSchedule              platform                    2  thrpt    5   736.636 ±   84.648  ops/s
TaskRunnerBenchmark.taskRunner4QueueSchedule              platform                    3  thrpt    5   732.123 ±   53.417  ops/s
TaskRunnerBenchmark.taskRunner4QueueSchedule              platform                    4  thrpt    5   733.729 ±   44.041  ops/s
TaskRunnerBenchmark.taskRunner4QueueSchedule              platform                    5  thrpt    5   744.170 ±   38.064  ops/s
TaskRunnerBenchmark.taskRunner4QueueSchedule              platform                    6  thrpt    5   749.977 ±   86.638  ops/s
TaskRunnerBenchmark.taskRunner5Mixed                       virtual                    0  thrpt    5   521.194 ±   27.470  ops/s
TaskRunnerBenchmark.taskRunner5Mixed                       virtual                    1  thrpt    5   554.723 ±   45.559  ops/s
TaskRunnerBenchmark.taskRunner5Mixed                       virtual                    2  thrpt    5   714.290 ±   12.418  ops/s
TaskRunnerBenchmark.taskRunner5Mixed                       virtual                    3  thrpt    5  1251.448 ±  415.393  ops/s
TaskRunnerBenchmark.taskRunner5Mixed                       virtual                    4  thrpt    5  1049.965 ±    6.587  ops/s
TaskRunnerBenchmark.taskRunner5Mixed                       virtual                    5  thrpt    5  1061.972 ±   45.005  ops/s
TaskRunnerBenchmark.taskRunner5Mixed                       virtual                    6  thrpt    5  1272.176 ±   53.709  ops/s
TaskRunnerBenchmark.taskRunner5Mixed                      platform                    0  thrpt    5   311.051 ±   49.587  ops/s
TaskRunnerBenchmark.taskRunner5Mixed                      platform                    1  thrpt    5   321.363 ±   50.575  ops/s
TaskRunnerBenchmark.taskRunner5Mixed                      platform                    2  thrpt    5   394.752 ±  104.477  ops/s
TaskRunnerBenchmark.taskRunner5Mixed                      platform                    3  thrpt    5   751.210 ±  667.895  ops/s
TaskRunnerBenchmark.taskRunner5Mixed                      platform                    4  thrpt    5   790.384 ±  915.124  ops/s
TaskRunnerBenchmark.taskRunner5Mixed                      platform                    5  thrpt    5   610.559 ±   46.401  ops/s
TaskRunnerBenchmark.taskRunner5Mixed                      platform                    6  thrpt    5   908.255 ±  149.374  ops/s


Benchmark                                     (bufferVersion)       (encoding)  (length)   Mode  Cnt         Score         Error  Units
BufferReaderUtf8Benchmark.readUtf8StringJayo                2            ascii        20  thrpt    5  17542554.311 ±  165310.342  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2            ascii      2000  thrpt    5   2952176.617 ±   66783.015  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2            ascii    200000  thrpt    5     18094.124 ±     376.262  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2           latin1        20  thrpt    5  17163549.961 ±   65250.204  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2           latin1      2000  thrpt    5    435118.204 ±    5585.269  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2           latin1    200000  thrpt    5      4184.647 ±     168.241  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2  utf8MostlyAscii        20  thrpt    5  11582642.587 ±   55047.060  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2  utf8MostlyAscii      2000  thrpt    5    289146.319 ±    5694.462  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2  utf8MostlyAscii    200000  thrpt    5      2791.027 ±     101.624  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2             utf8        20  thrpt    5   7844215.496 ±  172418.425  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2             utf8      2000  thrpt    5    133638.194 ±    1629.691  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2             utf8    200000  thrpt    5      1415.808 ±      22.119  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2           2bytes        20  thrpt    5   9863133.619 ±   89145.433  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2           2bytes      2000  thrpt    5    208860.474 ±    3760.048  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                2           2bytes    200000  thrpt    5      1992.219 ±      28.944  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3            ascii        20  thrpt    5  16815262.520 ±  178817.799  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3            ascii      2000  thrpt    5   3192384.070 ±   80327.727  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3            ascii    200000  thrpt    5     17837.082 ±    1081.708  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3           latin1        20  thrpt    5  16786834.341 ±  125170.662  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3           latin1      2000  thrpt    5    398999.270 ±    2618.102  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3           latin1    200000  thrpt    5      4258.623 ±      60.010  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3  utf8MostlyAscii        20  thrpt    5  10669904.841 ±   52654.265  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3  utf8MostlyAscii      2000  thrpt    5    293499.897 ±    2846.643  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3  utf8MostlyAscii    200000  thrpt    5      2732.843 ±      54.185  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3             utf8        20  thrpt    5   8899905.360 ±   28679.335  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3             utf8      2000  thrpt    5    133835.262 ±     743.730  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3             utf8    200000  thrpt    5      1373.469 ±      17.629  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3           2bytes        20  thrpt    5   9356648.983 ±  259571.536  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3           2bytes      2000  thrpt    5    210427.920 ±    1648.711  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                3           2bytes    200000  thrpt    5      1987.817 ±      74.893  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4            ascii        20  thrpt    5  26934773.263 ±  276567.919  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4            ascii      2000  thrpt    5   3147958.580 ±  118155.648  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4            ascii    200000  thrpt    5     18142.377 ±     588.055  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4           latin1        20  thrpt    5  28240087.650 ±  130620.148  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4           latin1      2000  thrpt    5    409497.707 ±    2951.465  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4           latin1    200000  thrpt    5      4193.708 ±      72.286  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4  utf8MostlyAscii        20  thrpt    5  14076612.844 ±   76261.659  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4  utf8MostlyAscii      2000  thrpt    5    303162.429 ±    1505.324  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4  utf8MostlyAscii    200000  thrpt    5      2808.656 ±      54.959  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4             utf8        20  thrpt    5   9594652.503 ±  375558.230  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4             utf8      2000  thrpt    5    133667.471 ±    3665.843  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4             utf8    200000  thrpt    5      1370.458 ±      35.238  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4           2bytes        20  thrpt    5  12420201.419 ±  101956.689  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4           2bytes      2000  thrpt    5    210186.511 ±    2595.160  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                4           2bytes    200000  thrpt    5      1990.326 ±      40.171  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                5            ascii        20  thrpt    5  27604221.499 ±  114768.206  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                5            ascii      2000  thrpt    5   3251724.267 ±   82511.287  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                5            ascii    200000  thrpt    5     18093.571 ±     749.283  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                5           latin1        20  thrpt    5  27550219.051 ±  148092.564  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                5           latin1      2000  thrpt    5    437544.236 ±   11441.897  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                5           latin1    200000  thrpt    5      4117.678 ±     127.087  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                5  utf8MostlyAscii        20  thrpt    5  13654917.123 ±  118165.668  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                5  utf8MostlyAscii      2000  thrpt    5    291225.060 ±    4912.486  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                5  utf8MostlyAscii    200000  thrpt    5      2711.739 ±      84.887  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                5             utf8        20  thrpt    5  11053851.516 ±  610300.244  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                5             utf8      2000  thrpt    5    134132.546 ±     506.236  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                5             utf8    200000  thrpt    5      1366.541 ±      15.706  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                5           2bytes        20  thrpt    5  12124551.457 ±  171391.840  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                5           2bytes      2000  thrpt    5    207732.437 ±    4086.417  ops/s
BufferReaderUtf8Benchmark.readUtf8StringJayo                5           2bytes    200000  thrpt    5      1988.498 ±      45.488  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2            ascii        20  thrpt    5  22496139.817 ±   81562.549  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2            ascii      2000  thrpt    5   5152880.602 ±  256742.534  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2            ascii    200000  thrpt    5     46820.749 ±    3387.495  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2           latin1        20  thrpt    5  22400225.524 ±  133528.948  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2           latin1      2000  thrpt    5    533313.883 ±    7358.182  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2           latin1    200000  thrpt    5      7942.110 ±      78.201  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2  utf8MostlyAscii        20  thrpt    5  17357572.169 ±   83611.289  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2  utf8MostlyAscii      2000  thrpt    5    667544.640 ±    6979.722  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2  utf8MostlyAscii    200000  thrpt    5      6433.705 ±     113.198  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2             utf8        20  thrpt    5  15131683.231 ±   35081.726  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2             utf8      2000  thrpt    5    328137.225 ±    5491.747  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2             utf8    200000  thrpt    5      3481.474 ±      55.085  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2           2bytes        20  thrpt    5  16867542.695 ±   49254.555  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2           2bytes      2000  thrpt    5    542055.409 ±    8208.177  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     2           2bytes    200000  thrpt    5      4864.507 ±     114.991  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3            ascii        20  thrpt    5  20327352.747 ± 7270907.227  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3            ascii      2000  thrpt    5   5206949.785 ±  280772.852  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3            ascii    200000  thrpt    5     45650.923 ±    4138.020  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3           latin1        20  thrpt    5  20025528.584 ± 7780590.093  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3           latin1      2000  thrpt    5    529524.199 ±   13166.956  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3           latin1    200000  thrpt    5      8102.588 ±      99.458  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3  utf8MostlyAscii        20  thrpt    5  15903840.085 ±   82807.374  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3  utf8MostlyAscii      2000  thrpt    5    614714.203 ±    2942.554  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3  utf8MostlyAscii    200000  thrpt    5      6114.615 ±      50.789  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3             utf8        20  thrpt    5  13949953.108 ±  179842.646  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3             utf8      2000  thrpt    5    326268.138 ±   13805.383  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3             utf8    200000  thrpt    5      3452.066 ±      41.189  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3           2bytes        20  thrpt    5  14873136.090 ±  121068.694  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3           2bytes      2000  thrpt    5    535902.687 ±    8648.408  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     3           2bytes    200000  thrpt    5      4781.483 ±     130.987  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4            ascii        20  thrpt    5  35271664.931 ±  354257.226  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4            ascii      2000  thrpt    5   5296204.996 ±  203520.852  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4            ascii    200000  thrpt    5     46495.092 ±    4956.088  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4           latin1        20  thrpt    5  35585379.774 ±  175761.635  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4           latin1      2000  thrpt    5    537140.596 ±   15651.271  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4           latin1    200000  thrpt    5      7020.324 ±     128.880  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4  utf8MostlyAscii        20  thrpt    5  24019237.409 ±  116896.506  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4  utf8MostlyAscii      2000  thrpt    5    625667.459 ±    3456.602  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4  utf8MostlyAscii    200000  thrpt    5      6162.451 ±      61.090  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4             utf8        20  thrpt    5  20004613.690 ±   95399.475  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4             utf8      2000  thrpt    5    329308.272 ±    1859.539  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4             utf8    200000  thrpt    5      3369.226 ±      47.696  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4           2bytes        20  thrpt    5  22971485.503 ±  583253.613  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4           2bytes      2000  thrpt    5    547396.912 ±    8595.250  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     4           2bytes    200000  thrpt    5      4850.299 ±      79.248  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     5            ascii        20  thrpt    5  35253177.098 ±  249314.258  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     5            ascii      2000  thrpt    5   5241790.839 ±   96435.530  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     5            ascii    200000  thrpt    5     45302.203 ±    2694.484  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     5           latin1        20  thrpt    5  35283088.533 ±  122987.167  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     5           latin1      2000  thrpt    5    543254.348 ±   17212.444  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     5           latin1    200000  thrpt    5      7177.544 ±     561.233  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     5  utf8MostlyAscii        20  thrpt    5  23966924.212 ±   96475.682  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     5  utf8MostlyAscii      2000  thrpt    5    619784.985 ±   12559.189  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     5  utf8MostlyAscii    200000  thrpt    5      6120.251 ±     110.557  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     5             utf8        20  thrpt    5  19851908.265 ±  433107.712  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     5             utf8      2000  thrpt    5    326078.893 ±    5611.661  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     5             utf8    200000  thrpt    5      3366.554 ±      40.863  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     5           2bytes        20  thrpt    5  23109248.005 ±   50699.845  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     5           2bytes      2000  thrpt    5    539577.560 ±    4042.343  ops/s
BufferReaderUtf8Benchmark.writeUtf8Jayo                     5           2bytes    200000  thrpt    5      4834.373 ±     100.209  ops/s

Benchmark                       (readerVersion)   Mode  Cnt  Score   Error  Units
SlowReaderBenchmark.readerJayo                2  thrpt    5  1.374 ± 0.043  ops/s
SlowReaderBenchmark.readerJayo                3  thrpt    5  0.722 ± 0.035  ops/s
SlowReaderBenchmark.readerJayo                4  thrpt    5  0.705 ± 0.052  ops/s
SlowReaderBenchmark.readerJayo                5  thrpt    5  0.707 ± 0.075  ops/s

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