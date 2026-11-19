package com.yourname.sw;

/** How {@link SensitiveWordFilter#replace(String, ReplaceMode)} masks a hit. */
public enum ReplaceMode {
    /** Replace every matched character with '*' (length preserved). */
    CHAR,
    /** Replace the whole matched word with a fixed mask "***". */
    WORD
}
