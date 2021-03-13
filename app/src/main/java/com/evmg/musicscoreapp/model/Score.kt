package com.evmg.musicscoreapp.model

import com.evmg.musicscoreapp.objectparsing.SheetMusic
import java.io.File
import java.util.*

class Score(
    val dir: String,
    val title: String,
    val tempo: Int,
    val instrument: Int,
    val sheets: List<SheetMusic>?,
    val createDate: Long = Date().time
)
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

