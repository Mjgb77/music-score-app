package com.example.musicscoreapp.adapter

import android.content.Context
import android.widget.ArrayAdapter
import android.widget.Filter

class NoFilterArrayAdapter<T>(context: Context, resourceId: Int, val elements: Array<out T>) :
    ArrayAdapter<T>(context, resourceId, elements) {

    override fun getFilter(): Filter {
        return NoFilter()
    }

    inner class NoFilter : Filter() {
        override fun performFiltering(arg0: CharSequence?): FilterResults {
            val result = FilterResults()
            result.values = elements
            result.count = elements.size
            return result
        }

        override fun publishResults(arg0: CharSequence?, arg1: FilterResults?) {
            notifyDataSetChanged()
        }
    }
}