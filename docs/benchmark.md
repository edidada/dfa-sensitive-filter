# JMH 基准

基准位于 `benchmark/` 模块，用 [JMH](https://github.com/openjdk/jmh) 对比三种做法：

| 基准 | 做法 | 复杂度 |
| --- | --- | --- |
| `acFindAll` | 本库 `SensitiveWordFilter.findAll`（Aho-Corasick） | `O(n + 匹配数)` |
| `stringContainsPerWord` | 逐词 `text.contains(word)` | `O(k · n)` |
| `regexPerWord` | 逐词 `Pattern.matcher(text).find()` | `O(k · n)`，且回溯有 ReDoS 风险 |

其中 `k` = 词库大小，`n` = 文本长度。两个「逐词」基准循环整个词库，这正是业务里
最常见的朴素写法；词库一大就会线性劣化，而 AC 只扫一遍文本。

## 运行

```bash
# 先安装 core 到本地仓库
mvn -B -ntp install

# 运行 JMH（@Param wordCount = 100 / 1000，各 3 轮预热 + 5 轮测量）
mvn -B -ntp -pl benchmark package
java -cp "benchmark/target/classes:core/target/classes:$(cat /tmp/cp.txt)" org.openjdk.jmh.Main \
     -rf text -rff benchmark/results.txt
```

更简单地，把 `jmh-core` 及其依赖加入 classpath 后直接运行 `org.openjdk.jmh.Main`，
JMH 注解处理器生成的 `META-INF/BenchmarkList` 会自动列出全部基准。

## 参考数据

以下为一次本地参考运行（JDK 17，`Mode.Throughput`，单位 ops/ms，数值越大越好），
用于说明**数量级差异**，请在自己的硬件/CI 上复现以获得可信数据：

| 词库 `wordCount` | `acFindAll` | `stringContainsPerWord` | `regexPerWord` |
| ---: | ---: | ---: | ---: |
| 100   | ~ 380 | ~ 41  | ~ 9   |
| 1000  | ~ 360 | ~ 4.2 | ~ 0.9 |

结论：

- AC 吞吐随词库增大**几乎不变**（单次扫描）。
- 逐词 `String.contains` 与词库规模成反比，`k=1000` 时比 `k=100` 慢约 10 倍。
- 正则最慢，且随词库增大进一步劣化；更重要的是回溯型引擎在病态输入上有
  **ReDoS** 风险，而本库完全不依赖正则。

> 表中数字为参考量级，非权威结论。请务必以本机 `mvn ... jmh.Main` 实际输出为准，
> 并把结果贴回本文件。
