package com.evmg.musicscoreapp.model

import java.util.*
import kotlin.collections.ArrayList

class StoredScore(
    val score: Score,
    val recognitions: List<List<StaffRecognition>>
) {
    fun getMetadataJson(): String{
        return """
            {
                "title": "${score.title}",
                "tempo": ${score.tempo},
                "instrument": ${score.instrument},
                "createDate": ${score.createDate},
                "recognition": ${getRecognitionJson()}
            }
        """.trimIndent()
    }

    private fun getRecognitionJson(): String {
        return """
            [
                ${recognitions.map(this::toJson).joinToString(""",
                    
                """)}
            ]
            """.trimIndent()
    }

    private fun toJson(rec: List<StaffRecognition>): String {
        return """
            [
                ${rec.map(this::toJson).joinToString(""",
                    
                """)}
            ]
            """.trimIndent()
    }

    fun toJson(rec: StaffRecognition): String {
        return """{
                "staff": ${toJson(rec.staff)},
                "objects": [
                    ${rec.objects.map(this::toJson).joinToString(""",
                        
                    """)}
                ]
            }
        """.trimIndent()
    }

    fun toJson(rec: Recognition): String {
        return """
            {
                "left": ${rec.location.left},
                "top": ${rec.location.top},
                "right": ${rec.location.right},
                "bottom": ${rec.location.bottom},              
                "confidence": ${rec.confidence},          
                "title": "${rec.title}",              
                "id": "${rec.id}",              
                "detectedClass": ${rec.detectedClass}
            }
        """.trimIndent()
    }
}
/*
Interno_Temporal_scores
-> [] 1, 2, 3, 4, 5
        asd  sdkjk sdkjksjd -> 1, 2, 3, 4
-> []

Pictures
    >
    >
    >
Scores V

-> View Activity
 leer -> directorio temporal

 -> editar -> salvar


 crear ->
    editar
              (importado? editando) ->  (salvado)
    Score -> (Temporal - Edit/View) (Fixed - View/List)
      Temporal (las imagenes son no determisticas) ESCRITURA
                (las imagenes estan en orden, porq fue importado) LECTURA

      Fixed (las imagenes tan en orden)
 */

