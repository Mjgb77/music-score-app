package com.example.musicscoreapp.objectparsing

import android.graphics.RectF
import com.example.musicscoreapp.midi.*
import com.example.musicscoreapp.model.Recognition
import com.example.musicscoreapp.model.StaffRecognition
import kotlin.math.max
import kotlin.math.min

object StaffToMidi {

    fun getBarline(lRecognition: List<MusicalObject>): Barline? {
        return lRecognition.firstOrNull { it is Barline } as Barline?
    }

    fun distRect(a: RectF, b: RectF): Float {
        val vertical =
            if (a.bottom <= b.top) {
                b.top - a.bottom
            }
            else if (b.bottom <= a.top) {
                a.bottom - b.top
            } else
                max(a.top, b.top) - min(a.bottom, b.bottom)
        val horizontal =
            if (a.right <= b.left) {
                b.left - a.right
            }
            else if (b.right <= a.left) {
                a.left - b.right
            } else
                max(a.left, b.left) - min(a.right, b.right)
        println(vertical)
        println(horizontal)
        return max(vertical / min(a.height(), b.height()), horizontal / min(a.width(), b.width()))
    }

    fun processStaff(staffRecognition: StaffRecognition, track: Track, factor: Int = 960) {
        val lRecognition = staffRecognition.objects.sortedBy { it.location.left }

        val disjoinSystem = DisjointSet(lRecognition.size)
        for (i in lRecognition.withIndex()) {
            var cand: Int? = null
            for (j in lRecognition.withIndex()) {
                if (i.index == j.index) continue
                if (Matcher.canJoin(j.value, i.value)) {
                    val d = distRect(i.value.location, j.value.location)
                    if (d > 2) continue
                    if (cand == null || d < distRect(i.value.location, lRecognition[cand].location)) {
                        cand = j.index
                    }
                }
            }
            if (cand != null) {
                disjoinSystem.join(i.index, cand)
            }
        }
        val map = mutableMapOf<Int, ArrayList<Recognition>>()
        for (i in lRecognition.withIndex()) {
            val groupId = disjoinSystem.find(i.index)
            map.computeIfAbsent(groupId) { arrayListOf() }
            map[groupId]!!.add(i.value)
        }
        val musicalObjects = map.values.map { g -> MusicalObjectFactory.parse(g) }.flatten().sortedBy { it.posX() }

        val barlineRef = getBarline(musicalObjects) ?: return
        val pitchContext = PitchContext(barlineRef = barlineRef.bar.location)

//    var deltaRest = delta;
        for (obj in musicalObjects) {
            pitchContext.process(obj)
            if (obj is Note) {
                val pitch = obj.getPitch(pitchContext)
                val duration = (obj.getDuration() * factor).toInt()

                track.addEvent(0, NoteOn(pitch.toByte()))
                track.addEvent(duration, NoteOff(pitch.toByte()))
            } else if (obj is Rest) {
                val duration = (obj.getDuration() * factor).toInt()
//            deltaRest += duration
                track.addEvent(duration, NoteOn(0))
                track.addEvent(0, NoteOff(0))
            }
        }
//    return delta
    }

    fun staffToMidi(staffs: List<StaffRecognition>, tempo: Int = 120, instrument: Byte = 0x1): ByteArray {
        val myTrack = Track()
        myTrack.addEvent(0, ProgramChange(instrument))

        for (staff in staffs) {
            processStaff(staff, myTrack)
        }

        val output = MidiWriter()
            .writeHeader()
            .writeTrack(Track().addEvent(0, Tempo(tempo)))
            .writeTrack(myTrack)
            .toByteArray()
        return output
    }
}
