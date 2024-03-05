package com.example.aiish

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.ImageView
import android.widget.TextView

class MyArrayAdapter(context: Context, resource: Int, objects: MutableList<AppInfo>) : ArrayAdapter<AppInfo>(context, resource,objects)
{
    private val originalList: List<AppInfo> = ArrayList(objects)
    private var filteredList: MutableList<AppInfo> = ArrayList(objects)

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            val inflater = LayoutInflater.from(context)
            itemView = inflater.inflate(R.layout.row_layout, parent, false)
        }
        else {
            val inflater = LayoutInflater.from(context)
            itemView = inflater.inflate(R.layout.row_layout, parent, false)
        }
        val curiteam=getItem(position)
        val textView = itemView?.findViewById<TextView>(R.id.rowtextview);
        val imageview =itemView?.findViewById<ImageView>(R.id.icon_holder);
        imageview?.setImageDrawable(null);
        imageview?.setImageDrawable(curiteam?.appIcon);
        textView?.text=curiteam?.appName;
        return itemView!!
    }
    override fun getCount(): Int {
        return filteredList.size
    }

    override fun getItem(position: Int): AppInfo? {
        return filteredList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filterResults = FilterResults()

                if (constraint.isNullOrBlank()) {
                    filteredList = ArrayList(originalList)
                } else {
                    val query = constraint.toString().toLowerCase().trim()

                    filteredList = originalList.filter { appInfo ->
                        appInfo.appName.toLowerCase().contains(query)
                    }.toMutableList()
                }

                filterResults.values = filteredList
                filterResults.count = filteredList.size
                return filterResults
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                filteredList = results?.values as? MutableList<AppInfo> ?: ArrayList()
                notifyDataSetChanged()
            }
        }
    }
}