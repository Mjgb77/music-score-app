package com.evmg.musicscoreapp.objectparsing

class DisjointSet(N: Int) {
    private val P = Array(N) { -1 } // if < 0 then negative size, else parentId
    fun find(x: Int): Int {
        return if (P[x] < 0) x else { P[x] = find(P[x]); P[x] }
    }
    fun join(x: Int, y: Int): Boolean {
        var px = find(x)
        var py = find(y)
        if(px == py) return false
        if(P[py] < P[px]) px = py.also { py = px }
        P[px] += P[py]
        P[py] = px
        return true
    }
}
