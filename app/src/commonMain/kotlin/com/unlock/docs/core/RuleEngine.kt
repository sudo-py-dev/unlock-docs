package com.unlock.docs.core

object RuleEngine {
    fun applyRules(word: String): List<String> {
        val results = mutableListOf<String>()
        results.add(word) // Original

        // Rule 1: Capitalize first letter
        if (word.isNotEmpty() && word[0].isLowerCase()) {
            results.add(word.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() })
        }

        // Rule 2: ALL CAPS
        if (word.any { it.isLowerCase() }) {
            results.add(word.uppercase())
        }

        // Rule 3: Append 123
        results.add(word + "123")

        // Rule 4: Leetspeak basic (a -> 4, e -> 3, i -> 1, o -> 0)
        val leet = word.lowercase()
            .replace('a', '4')
            .replace('e', '3')
            .replace('i', '1')
            .replace('o', '0')
        if (leet != word.lowercase()) {
            results.add(leet)
        }

        return results.distinct()
    }
}
