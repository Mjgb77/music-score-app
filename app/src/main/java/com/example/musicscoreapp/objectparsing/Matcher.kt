package com.example.musicscoreapp.objectparsing

import android.graphics.RectF
import com.example.musicscoreapp.model.Recognition
import kotlin.math.max
import kotlin.math.min

object Matcher {
    private enum class Op { LEFT, SAME, RIGHT }

    private val relations = mapOf(
        Pair(Pair("head", "body"), listOf(Op.SAME)),
        Pair(Pair("head", "dot"), listOf(Op.SAME, Op.RIGHT)),
        Pair(Pair("head", "sign"), listOf(Op.LEFT)),
        Pair(Pair("sign", "sign"), listOf(Op.RIGHT)),
        Pair(Pair("clef", "sign"), listOf(Op.RIGHT)),
        Pair(Pair("rest", "dot"), listOf(Op.SAME)),
        Pair(Pair("head", "body"), listOf(Op.SAME))
    )

    fun canJoin(left: Recognition, right: Recognition): Boolean {
        val catLeft = left.title.split(".")[0]
        val catRight = right.title.split(".")[0]
        val candidateOrientations = arrayListOf<Op>()
        if (intersectionLeftX(left.location, right.location) < 0.1) {
            candidateOrientations.add(Op.RIGHT)
        }
        if (intersectionLeftX(right.location, left.location) < 0.1) {
            candidateOrientations.add(Op.LEFT)
        }
        if (intersectionX(left.location, right.location) > 0.9) {
            candidateOrientations.add(Op.SAME)
        }
        return relations[Pair(catLeft, catRight)]?.intersect(candidateOrientations)?.isNotEmpty() ?: false

    }

    private fun intersectionLeftX(left: RectF, right: RectF): Float {
        return (left.right - right.left) / min(left.width(), right.width())
    }

    private fun intersectionX(left: RectF, right: RectF): Float {
        return (min(left.right, right.right) - max(left.left, right.left)) / min(left.width(), right.width())
    }
}
