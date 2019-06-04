package com.gorilla.vc.view.customized.customSearchView

import android.text.SpannableString

interface SearchMethod<T> {
    fun isMatchForSummit(keyWord: String, data: T): Boolean
    fun isMatchForSuggestion(keyWord: String, data: T): Boolean
    fun getSearchSuggestionText(keyword: String, data: T): SpannableString
    fun onSummit()
    fun onSearchResult(keyword:String,list:ArrayList<T>)
}