# dfa-sensitive-filter

高性能敏感词过滤：**DFA(Trie) + Aho-Corasick 双引擎**。
**不基于正则实现，杜绝 ReDoS（正则表达式拒绝服务）**。

> 词库匹配是一个「多模式串匹配」问题，用自动机一次扫描即可完成；
> 用 `Pattern`/正则去拼 `(a|b|c|...)` 既慢，又会在病态输入上回溯爆炸。
> 本库从根上不引入正则，所以没有这个问题。

## 特性

- **Aho-Corasick**：`O(n + 匹配数)`，大词库 / 大文本首选
- **DFA(Trie)**：词少时自动切换，常数更小、实现更轻
- **归一化**：中文/英文，大小写、全角→半角；归一化一次完成，索引可还原
- **线程安全**：构建后引擎不可变，`match` 无锁；热更新用 `AtomicReference<Engine>`
- **可配置**：`ignoreCase` / `fullWidthNormalize` / `wordBoundary` / `engineType` / `maxTextLength`
- **词库来源**：`List<String>`、文件、classpath，另附 `scripts/gen-wordlist.py` 去重归一
- **JMH 基准**：与逐词 `String.contains`、`Pattern` 正则对比，见 [docs/benchmark.md](docs/benchmark.md)

## 模块

| 模块 | 说明 |
| --- | --- |
| `core` | 核心算法与门面 `SensitiveWordFilter` |
| `spring-boot-starter` | 一行配置接入的自动装配 |
| `benchmark` | JMH 基准 |
| `examples/demo` | 可运行示例 |

## 快速开始

Maven（发布到中央仓库前请先 `mvn install` 到本地）：

```xml
<dependency>
  <groupId>com.yourname</groupId>
  <artifactId>core</artifactId>
  <version>0.1.0-SNAPSHOT</version>
</dependency>
```

```java
SensitiveWordFilter filter = new SensitiveWordFilter();
filter.init(Arrays.asList("badword", "敏感词", "赌博"));

filter.contains("这是个 BadWord");          // true
filter.findAll("禁止赌博和敏感词");          // List<MatchResult>
filter.replace("禁止赌博", ReplaceMode.CHAR); // "****"
```

词库也可以来自文件或 classpath：

```java
filter.init(Paths.get("/etc/sensitive/words.txt"));
filter.initFromClasspath("sensitive-words.txt");
```

热更新（并发安全）：

```java
filter.reload(loadLatestWords());
```

## 配置

```java
SensitiveWordConfig config = SensitiveWordConfig.builder()
        .ignoreCase(true)
        .fullWidthNormalize(true)
        .wordBoundary(true)                       // "ass" 不匹配 "class"
        .engineType(SensitiveWordConfig.EngineType.AUTO)
        .maxTextLength(1_000_000)
        .build();
SensitiveWordFilter filter = new SensitiveWordFilter(config);
```

| 配置 | 默认 | 说明 |
| --- | --- | --- |
| `ignoreCase` | `true` | 大小写归一 |
| `fullWidthNormalize` | `true` | 全角 ASCII → 半角 |
| `wordBoundary` | `false` | 英文词边界（仅对 ASCII 词生效，避免误伤中文） |
| `engineType` | `AUTO` | `AUTO` / `DFA` / `AC`；`AUTO` 时词数 < 100 选 DFA |
| `maxTextLength` | `1000000` | 单次输入长度上限，防御超大文本 |

`MatchResult` 的 `start/end` 为**半开区间** `[start, end)`，直接指向原始文本
（因归一化是严格等长的 1:1 映射），所以 `text.substring(start, end)` 即为命中片段。

## Spring Boot

引入 `spring-boot-starter` 后自动装配 `SensitiveWordFilter` Bean：

```yaml
sensitive-word:
  words:
    - badword
    - 敏感词
  # 或：location: classpath:sensitive-words.txt
  ignore-case: true
  word-boundary: false
  engine-type: AUTO
```

```java
@RestController
public class CheckController {
    private final SensitiveWordFilter filter;
    public CheckController(SensitiveWordFilter filter) { this.filter = filter; }

    @GetMapping("/check")
    public boolean check(@RequestParam String q) { return filter.contains(q); }
}
```

## 为什么不用正则

详见 [docs/architecture.md](docs/architecture.md)。典型 ReDoS：
`^(a+)+$` 匹配 `"aaaaaaaaaaaaaaaaaaaa!"` 时因灾难性回溯耗时随长度指数增长。
本库用 Trie / AC 自动机，单次线性扫描，不存在回溯。

## 构建

```bash
mvn -B -ntp verify
```

要求 JDK 17+。CI 见 `.github/workflows/ci.yml`。

## License

[MIT](LICENSE)
