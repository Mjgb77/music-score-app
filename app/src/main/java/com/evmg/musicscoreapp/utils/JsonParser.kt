package com.evmg.musicscoreapp.utils

import android.graphics.RectF
import com.evmg.musicscoreapp.model.Recognition
import com.evmg.musicscoreapp.model.Score
import com.evmg.musicscoreapp.model.StaffRecognition
import com.evmg.musicscoreapp.model.StoredScore
import com.evmg.musicscoreapp.objectparsing.SheetMusic
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

object JsonParser {


    fun parseMetadata(dir: String, sheets: List<SheetMusic>, jsonObject: JSONObject): StoredScore {
        return StoredScore(
            score = Score(dir = dir,
                title = jsonObject["title"] as String,
                tempo = jsonObject["tempo"] as Int,
                instrument = jsonObject["instrument"] as Int,
                sheets = sheets,
                createDate = Date(jsonObject["createDate"] as Long).time
            ),
            recognitions = parseArrayOfArray(jsonObject.getJSONArray("recognition"), this::parseStaffRecognitionForPage)
        )
    }
    fun parseStaffRecognitionForPage(jsonObject: JSONArray): List<StaffRecognition> {
        return parseArray(jsonObject, this::parseStaffRecognition)
    }
    fun parseStaffRecognition(jsonObject: JSONObject): StaffRecognition {
        return StaffRecognition(
            staff = parseRecognition(jsonObject.getJSONObject("staff")),
            objects = parseArray(jsonObject.getJSONArray("objects"), this::parseRecognition)
        )
    }
    fun parseRecognition(jsonObject: JSONObject): Recognition {
        return Recognition(
            id = jsonObject.getString("id"),
            title = jsonObject.getString("title"),
            confidence = jsonObject.getDouble("confidence").toFloat(),
            detectedClass = jsonObject.getInt("detectedClass"),
            location = RectF(
                jsonObject.getDouble("left").toFloat(),
                jsonObject.getDouble("top").toFloat(),
                jsonObject.getDouble("right").toFloat(),
                jsonObject.getDouble("bottom").toFloat()
            )
        )
    }

    fun <T> parseArray(array: JSONArray, toObject: (JSONObject) -> T): List<T> {
        return (0 until array.length()).map {
            toObject(array.getJSONObject(it))
        }
    }

    fun <T> parseArrayOfArray(array: JSONArray, toObject: (JSONArray) -> T): List<T> {
        return (0 until array.length()).map {
            toObject(array.getJSONArray(it))
        }
    }

}