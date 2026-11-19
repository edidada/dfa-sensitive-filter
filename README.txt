# 

高性能敏感词过滤：DFA(Trie) + Aho-Corasick 双引擎，**非正则实现，杜绝 ReDoS**。

## 特性
- Aho-Corasick：O(n + 匹配数)，大文本首选
- DFA(Trie)：词少时自动切换
- 中文/英文，大小写/全半角归一化
- 线程安全，支持热更新词库
-  JMH 基准：比逐词 `contains` 快 XX 倍，比正则快 XX 倍且稳定


## 性能
...（benchmark.md 链接 + 表格）

## 架构
RE → NFA → DFA 原理、AC 的 fail 指针、为什么不用正则（ReDoS 案例）

├── README.md
├── LICENSE
├── pom.xml                        # 父 POM
├── .github/workflows/ci.yml
│
├── core/                          # 核心算法
│   └── src/
│       ├── main/java/com/yourname/sw/
│       │   ├── SensitiveWordFilter.java
│       │   ├── SensitiveWordConfig.java
│       │   ├── MatchResult.java
│       │   ├── engine/
│       │   │   ├── Engine.java
│       │   │   ├── DfaTrieEngine.java
│       │   │   └── AhoCorasickEngine.java
│       │   ├── trie/TrieNode.java
│       │   ├── ac/AcNode.java
│       │   └── normalize/CharNormalizer.java
│       └── test/java/com/yourname/sw/...
│
├── spring-boot-starter/
│   └── src/main/java/.../SensitiveWordAutoConfiguration.java
│
├── benchmark/                     # JMH
│   └── src/main/java/.../FilterBenchmark.java
│
├── docs/
│   ├── architecture.md
│   └── benchmark.md
│
└── examples/demo/
    └── src/main/java/.../Demo.java
5. 核心类职责说明（写代码前先定契约）
文件	职责
SensitiveWordFilter	对外门面：init(words)、contains(text)、findAll(text)、replace(text, mode)
Engine	接口：addWord、build、match(text) -> List<MatchResult>
DfaTrieEngine	Trie 逐字符走，O(n·L) 最坏，适合少量词 + 短文本
AhoCorasickEngine	构建 fail 指针，O(n + 匹配数)，适合大量词 + 大文本（默认推荐）
CharNormalizer	全角→半角、大小写归一、繁简（可选）
MatchResult	word、start、end、engine
SensitiveWordConfig	ignoreCase、fullWidthNormalize、wordBoundary、engineType、maxTextLength
6. 关键设计决策（提前写进 docs/architecture.md）
默认引擎 = Aho-Corasick，词少（<100）时自动切 DFA Trie。

归一化在匹配前做一次，避免每个词重复处理。

英文词边界：wordBoundary=true 时，"ass" 不匹配 "class"，用 Character.isLetterOrDigit 判断前后。

不做正则：明确在 README 写"不基于正则，避免 ReDoS"，这是卖点。

线程安全：构建后不可变，match 无锁；热更新用 AtomicReference<Engine>。

要求：
词库：支持 List<String>、文件、classpath，提供 scripts/gen-wordlist.py 去重归一。
把 DfaTrieEngine + AhoCorasickEngine + 测试跑通；

加 JMH benchmark，和 Pattern 逐词匹配、String.contains 做对比，出数据；

抽 spring-boot-starter，让 Web 项目一行配置接入；