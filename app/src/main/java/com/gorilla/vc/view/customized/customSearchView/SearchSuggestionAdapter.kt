package com.gorilla.vc.view.customized.customSearchView

import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.graphics.drawable.StateListDrawable
import android.support.v4.widget.CursorAdapter
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.gorilla.vc.R

@Suppress("DEPRECATION")
class SearchSuggestionAdapter(context: Context?, c: Cursor?, val searchView: CustomSearchView) : CursorAdapter(context, c) {

    private val mLayoutInflater = LayoutInflater.from(context)

    override fun newView(context: Context?, cursor: Cursor?, parent: ViewGroup?): View {
        return mLayoutInflater.inflate(R.layout.search_suggestion, parent, false)
    }

    override fun bindView(view: View?, context: Context?, cursor: Cursor?) {
        if (cursor==null){
            return
        }
        val textView: TextView = view!!.findViewById(R.id.titleTextView)
        val text = cursor.getString(cursor.getColumnIndexOrThrow(CustomSearchView.COLUMN_ITEM_RESULT))
        textView.text = Html.fromHtml(text)
        view.setBackgroundColor(Color.WHITE)
        textView.setOnClickListener {
            val selectedKeyword = removeBlankChar(textView.text.toString())
            searchView.setQuery(selectedKeyword,true)
        }
        textView.setBackgroundDrawable(object :StateListDrawable(){
            override fun onStateChange(stateSet: IntArray): Boolean {
                if(checkIsPressed(stateSet)){
                    view.setBackgroundColor(Color.LTGRAY)
                }else{
                    view.setBackgroundColor(Color.WHITE)
                }
                return super.onStateChange(stateSet)
            }

            private fun checkIsPressed(stateSet: IntArray): Boolean {
                for (state in stateSet)
                    if (state == android.R.attr.state_pressed)
                        return true
                return false
            }
        })
    }

    private fun removeBlankChar(s:String):String{
        val newLineIndex = s.lastIndexOf('\n')
        return s.substring(0,newLineIndex-1)
    }

}