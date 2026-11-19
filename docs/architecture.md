# 架构与设计决策

## 1. 从正则到自动机：RE → NFA → DFA

多模式串匹配（在一篇文本里找出词库中所有出现的词）本质是自动机问题，教科书
路径是：

```
正则表达式 (RE)
   │  Thompson 构造
   ▼
NFA（非确定有限自动机）   一个状态可有多条 ε / 字符出边
   │  子集构造 (subset construction)
   ▼
DFA（确定有限自动机）     每个状态对每个字符只有唯一出边
```

- **NFA**：由 `(word1|word2|...)` 编译而来，匹配时可能同时处于多个状态，需要回溯或
  维护状态集合。
- **DFA**：把「状态集合」本身当作一个新状态，匹配时每读入一个字符只走一条边，
  `O(n)` 且无回溯。

**正则实现的问题**：主流语言（Java/JS/Python/PCRE）的正则引擎是**回溯型 NFA**，
不是理论上的 DFA。像 `^(a+)+$` 这种嵌套量词，在输入 `"aaaaaaaaaaaaaaaaaaaa!"`
（末尾一个不匹配字符）上会触发**灾难性回溯**，耗时随 `a` 的个数指数增长 —— 这就是
**ReDoS**。把上万个敏感词拼成一个大正则，既慢又给攻击者留了拒绝服务的入口。

**本库的选择**：直接把词库建成一棵 Trie（本身就是一台 DFA），或在其上补 fail 指针
得到 Aho-Corasick 自动机。二者都是**单次线性扫描、无回溯**，因此：

> 不基于正则，避免 ReDoS —— 这是本库的核心卖点。

## 2. 两个引擎

### DFA(Trie) —— `DfaTrieEngine`

把每个词逐字符插入 Trie，匹配时从文本的每个起点沿 Trie 向下走，遇到终止节点即
产出一次命中。

- 复杂度 `O(n · L)`（`L` 为最长词长），最坏情况下每个起点都要走一段。
- 无 fail 指针，构建极快、对象少。
- **适合词少（< 100）+ 短文本**，常数优于 AC。

### Aho-Corasick —— `AhoCorasickEngine`

在 Trie 上用 BFS 为每个节点计算 **fail 指针**：`fail(v)` 指向「`v` 所代表串的
最长真后缀、且该后缀恰好也是某个词前缀」所对应的节点（类似 KMP 的 next 数组，
推广到 Trie）。匹配时失配就沿 fail 指针跳转，无需回退文本指针。

- 复杂度 `O(n + 匹配数)`，与词库规模基本无关。
- 构建时把 fail 链上的输出合并进当前节点（`output += fail.output`），匹配到某点时
  一次性报告该点及所有后缀词，天然支持重叠词（如 `a` / `aa` / `aaa`）。
- **适合大词库 + 大文本**，作为默认推荐引擎。

### 自动切换

`SensitiveWordConfig.EngineType.AUTO`（默认）：词数 `< 100` 选 DFA Trie，否则选
Aho-Corasick。阈值定义在 `SensitiveWordConfig.AUTO_DFA_THRESHOLD`。也可通过
`DFA` / `AC` 强制指定。

## 3. 关键设计决策

1. **默认引擎 = Aho-Corasick，词少时自动切 DFA Trie**（见上）。
2. **归一化在匹配前做一次**：对词库和输入文本都先做 `CharNormalizer`（全角→半角、
   大小写归一），避免每个词重复处理。
   - 归一化被设计成**严格等长的 1:1 字符映射**，因此匹配得到的下标可以直接用于
     **原始文本**，`MatchResult.word` 通过 `text.substring(start, end)` 还原为原始片段。
   - 繁简转换不是 1:1 映射，会破坏下标稳定性，故未内置（可作为前置步骤接入）。
3. **英文词边界**：`wordBoundary=true` 时 `"ass"` 不匹配 `"class"`，用
   `Character.isLetterOrDigit` 判断命中词前后字符。
   - 中文没有空格分词，若对中文也套用该规则会把 `"很敏感词啊"` 里的命中误删，
     因此边界规则**仅对纯 ASCII 字母/数字词生效**。
4. **线程安全 / 热更新**：引擎 `build()` 后完全不可变，`match()` 无锁；
   `SensitiveWordFilter` 用 `AtomicReference<Engine>` 保存当前引擎，`init()`/`reload()`
   先构建好完整新引擎再一次性 `set`，并发读永远看到一致快照。
5. **防御性上限**：`maxTextLength` 限制单次输入长度，避免超大文本撑爆内存。
6. **重叠命中**：`findAll` 返回全部命中；`replace` 按起点排序、同起点取最长，
   跳过被覆盖的短命中，保证替换区间不重叠。

## 4. 类职责

| 文件 | 职责 |
| --- | --- |
| `SensitiveWordFilter` | 门面：`init` / `contains` / `findAll` / `replace` / `reload` |
| `Engine` | 接口：`addWord` / `build` / `match` / `name` |
| `DfaTrieEngine` | Trie 逐字符走，`O(n·L)` |
| `AhoCorasickEngine` | fail 指针，`O(n + 匹配数)` |
| `CharNormalizer` | 等长归一化 |
| `MatchResult` | `word` / `start` / `end` / `engine` |
| `SensitiveWordConfig` | 全部可配置项 + 构建器 |
| `WordListLoader` | `List` / 文件 / classpath 词库加载、去重 |
