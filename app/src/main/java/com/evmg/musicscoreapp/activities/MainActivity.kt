package com.evmg.musicscoreapp.activities

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.evmg.musicscoreapp.R
import com.evmg.musicscoreapp.adapter.MusicScoreAdapter
import com.evmg.musicscoreapp.model.Score
import com.evmg.musicscoreapp.service.DetectionClusterInstances
import com.evmg.musicscoreapp.service.ScoreDb
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var scoreDb: ScoreDb
    lateinit var musicScoreAdapter: MusicScoreAdapter
    var filterString: String = ""
    val visibleScores = arrayListOf<Score>()
    val allScores = arrayListOf<Score>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        DetectionClusterInstances.init(this)
        DetectionClusterInstances.init(applicationContext)

        setContentView(R.layout.activity_main)
        setSupportActionBar(findViewById(R.id.toolbar))
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { _ ->
            val intent = Intent(this, CreateOrUpdateScoreActivity::class.java)
            startActivity(intent)
        }
        recyclerView = findViewById<RecyclerView>(R.id.recyclerView)
        scoreDb = ScoreDb(this)
        refetchScores()
        applyFilterAndSetSongs()
        musicScoreAdapter = MusicScoreAdapter(this, this, visibleScores)
        scoreDb.subscribe(this) {
            refetchScores()
            applyFilterAndSetSongs()
            musicScoreAdapter.notifyDataSetChanged()
        }
        recyclerView.adapter = musicScoreAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        recyclerView.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))
    }

    override fun onDestroy() {
        super.onDestroy()
        scoreDb.unsuscribe(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)

        val manager = getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchItem = menu.findItem(R.id.search_bar)
        val searchView = searchItem?.actionView as SearchView
        searchView.setSearchableInfo(manager.getSearchableInfo(componentName))
        searchView.queryHint = "Find title" // getString(R.string.hint_search_bar)
        searchView.maxWidth = Int.MAX_VALUE

        searchItem.setOnMenuItemClickListener {
            true
        }

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(querytxt: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(querytxt: String?): Boolean {
                filterString = querytxt ?: ""
                applyFilterAndSetSongs()
                musicScoreAdapter.notifyDataSetChanged()
                return false
            }
        })

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == 1){
            recreate()
        }
    }

    fun applyFilterAndSetSongs() {
        visibleScores.clear()
        visibleScores.addAll(allScores.filter {
            it.title.toLowerCase(Locale.US).contains(filterString.toLowerCase(Locale.US))
        })
    }

    fun refetchScores() {
        allScores.clear()
        allScores.addAll(scoreDb.getAllScores())
    }
}