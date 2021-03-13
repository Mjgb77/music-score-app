package com.evmg.musicscoreapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.evmg.musicscoreapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.util.zip.ZipInputStream

class ImportActivity : AppCompatActivity() {
    lateinit var scoreDir: Path
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.actitvity_import)

        val appDir = getExternalFilesDir("Temp")
        lifecycleScope.launch(Dispatchers.IO) {
            intent.data?.also {
                val hashMap = HashMap<String, ByteArray>()
                ZipInputStream(contentResolver.openInputStream(it)).use { zip ->
                    while (true) {
                        val entry = zip.nextEntry ?: break
                        hashMap[entry.name] = zip.readBytes()
                        zip.closeEntry()
                    }
                }
                val obj = JSONObject(String(hashMap["meta.json"]!!))
                scoreDir = Files.createTempDirectory(
                    appDir?.toPath(),
                    "score_"
                )

                val directory = scoreDir.toFile()

                for (e in hashMap) {
                    val nFile = File(directory, e.key)
                    FileOutputStream(nFile).use { it.write(e.value) }
                }
                contentResolver.openInputStream(it)?.copyTo(FileOutputStream(File(directory, "export.dscore")))
            }
            withContext(Dispatchers.Main) {
                val intentDetailsActivity =
                    Intent(this@ImportActivity, ScoreDetailsActivity::class.java)
                intentDetailsActivity.putExtra(
                    ScoreDetailsActivity.SCORE_PATH,
                    scoreDir.toFile().absolutePath
                )
                intentDetailsActivity.putExtra(
                    ScoreDetailsActivity.TYPE,
                    ScoreDetailsActivity.TYPE_IMPORT
                )
                startActivity(intentDetailsActivity)
                finish()
            }
        }
    }
}
