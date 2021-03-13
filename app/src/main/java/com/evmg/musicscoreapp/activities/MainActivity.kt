package com.evmg.musicscoreapp.activities

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.evmg.musicscoreapp.R
import com.evmg.musicscoreapp.adapter.MusicScoreAdapter
import com.evmg.musicscoreapp.model.Score
import com.evmg.musicscoreapp.service.DetectionClusterInstances
import com.evmg.musicscoreapp.service.ScoreDb
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var recyclerView: RecyclerView
    lateinit var scoreDb: ScoreDb
    lateinit var musicScoreAdapter: MusicScoreAdapter
    lateinit var layoutSortOrderChips: ViewGroup
    lateinit var layoutSort: LinearLayout
    var filterString: String = ""
    var sortCriteria = Criteria.DATE
    var sortOrder = Order.DESC
    enum class Criteria(val chipId: Int, val processor: (List<Score>) -> List<Score>) {
        TITLE(R.id.chip_sort_title, { it.sortedBy { it.title.toLowerCase(Locale.US) }}),
        DATE(R.id.chip_sort_date, { it.sortedBy { it.createDate } }),
        PAGES(R.id.chip_sort_title, { it.sortedBy { it.sheets?.size } })
    }
    enum class Order(val chipId: Int, val processor: (List<Score>) -> List<Score>) {
        ASC(R.id.chip_sort_asc, { it }),
        DESC(R.id.chip_sort_desc, { it.reversed() })
    }
    val visibleScores = arrayListOf<Score>()
    val allScores = arrayListOf<Score>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        DetectionClusterInstances.init(this)
        DetectionClusterInstances.init(applicationContext)

        setContentView(R.layout.activity_main)
        findViewById<FloatingActionButton>(R.id.fab).setOnClickListener { _ ->
            val intent = Intent(this, CreateOrUpdateScoreActivity::class.java)
            startActivity(intent)
        }
        layoutSort = findViewById(R.id.layout_chips)
        layoutSortOrderChips = findViewById(R.id.chipsForOrder)
        recyclerView = findViewById(R.id.recyclerView)

        scoreDb = ScoreDb(this)
        showSort()
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

        val showSortBy = menu.findItem(R.id.sort_bar)
        showSortBy?.setOnMenuItemClickListener{
            layoutSort.visibility = if (layoutSort.visibility == ViewGroup.GONE) ViewGroup.VISIBLE else ViewGroup.GONE
            if (layoutSort.visibility == ViewGroup.GONE) {
                showSort()
            }
            true
        }

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

    fun showSort() {
        for (c in Criteria.values()) findViewById<Chip>(c.chipId).isSelected = false
        for (c in Order.values()) findViewById<Chip>(c.chipId).isSelected = false
        findViewById<Chip>(sortCriteria.chipId).isSelected = true
        findViewById<Chip>(sortOrder.chipId).isSelected = true
        lifecycleScope.launch(Dispatchers.IO) {
            delay(100)
            withContext(Dispatchers.Main) {
                layoutSortOrderChips.visibility = ViewGroup.GONE
                layoutSort.visibility = ViewGroup.GONE
            }
        }
    }
    fun applySort(scores: List<Score>): List<Score> {
        return sortOrder.processor(sortCriteria.processor(scores))
    }
    fun applyFilterAndSetSongs() {
        visibleScores.clear()
        visibleScores.addAll(applySort(allScores).filter {
            it.title.toLowerCase(Locale.US).contains(filterString.toLowerCase(Locale.US))
        })
    }

    fun refetchScores() {
        allScores.clear()
        allScores.addAll(scoreDb.getAllScores())
    }

    val criteriaChips = listOf(R.id.chip_sort_date, R.id.chip_sort_title)
    val orderChips = listOf(R.id.chip_sort_asc, R.id.chip_sort_desc)
    var temp = Criteria.DATE
    fun chipClicked(view: View) {
        if (view is Chip) {
            if (criteriaChips.contains(view.id)) {
                for (other in criteriaChips.filter { view.id != it }) {
                    findViewById<Chip>(other).isSelected = false
                }
            }
            when(view.id) {
                R.id.chip_sort_date -> {
                    view.isSelected = true
                    for (c in Order.values()) findViewById<Chip>(c.chipId).isSelected = false
                    layoutSortOrderChips.visibility = ViewGroup.VISIBLE
                    temp = Criteria.DATE
                }
                R.id.chip_sort_title -> {
                    view.isSelected = true
                    for (c in Order.values()) findViewById<Chip>(c.chipId).isSelected = false
                    layoutSortOrderChips.visibility = ViewGroup.VISIBLE
                    temp = Criteria.TITLE
                }
                R.id.chip_sort_asc -> {
                    view.isSelected = true
                    sortCriteria = temp
                    sortOrder = Order.ASC
                    showSort()
                    applyFilterAndSetSongs()
                    musicScoreAdapter.notifyDataSetChanged()
                }
                R.id.chip_sort_desc -> {
                    view.isSelected = true
                    sortCriteria = temp
                    sortOrder = Order.DESC
                    showSort()
                    applyFilterAndSetSongs()
                    musicScoreAdapter.notifyDataSetChanged()
                }
            }
        }
    }
}