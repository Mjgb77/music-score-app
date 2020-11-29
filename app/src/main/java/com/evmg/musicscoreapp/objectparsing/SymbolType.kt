package com.evmg.musicscoreapp.objectparsing

enum class SymbolType(val label: String, val factor: Float) {
    WHOLE("whole", 4F),
    HALF("half", 2F),
    WHITE("white", 2F),
    QUARTER("quarter", 1F),
    BLACK("black", 1F),
    EIGHT("eight", 0.5F),
    SIXTEENTH("sixteenth", 0.25F);

    companion object {
        fun valueByLabel(label: String): SymbolType {
            return values().first {it.label == label}
        }
    }
}
