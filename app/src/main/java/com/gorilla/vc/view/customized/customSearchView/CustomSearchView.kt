package com.gorilla.vc.view.customized.customSearchView

import android.content.Context
import android.database.MatrixCursor
import android.graphics.Rect
import android.provider.BaseColumns
import android.support.v7.appcompat.R
import android.support.v7.widget.SearchView
import android.text.Html
import android.util.AttributeSet
import android.util.Log
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.observers.DisposableSingleObserver
import io.reactivex.schedulers.Schedulers

@Suppress("DEPRECATION")
class CustomSearchView @JvmOverloads constructor(context: Context, attrs: AttributeSet?,
             defStyleAttr: Int = R.attr.searchViewStyle) : SearchView(context, attrs, defStyleAttr) {
    private var mCompositeDisposable: CompositeDisposable = CompositeDisposable()
    companion object {
        var COLUMN_ITEM_RESULT = "item_result"
        var COLUMN_ITEM_DATA = "item_data"
    }

    fun <T> setSearchRecourse(resources: ArrayList<T>, searchMethod: SearchMethod<T>) {
        setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                searchMethod.onSummit()
                search(query,searchMethod,resources,true)
                clearFocus()
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                search(newText,searchMethod,resources,false)
                return true
            }
        })
    }

    private fun <T> search(keyword: String, searchMethod: SearchMethod<T>, resources: ArrayList<T>, isSubmitted: Boolean) {
        mCompositeDisposable.clear()
        val cursor = MatrixCursor(arrayOf(BaseColumns._ID,COLUMN_ITEM_RESULT, COLUMN_ITEM_DATA))
        if (suggestionsAdapter!=null)
            suggestionsAdapter.swapCursor(null)
        if(keyword.isEmpty()) {
            searchMethod.onSearchResult("",resources)
            return
        }
        mCompositeDisposable.add(Observable.fromArray(resources)
                .observeOn(Schedulers.computation())
                .flatMapIterable { items -> items }
                .filter { item ->
                    if (isSubmitted)
                        searchMethod.isMatchForSummit(keyword, item)
                    else
                        searchMethod.isMatchForSuggestion(keyword, item)
                }
                .map { item ->
                    cursor.addRow(arrayOf(cursor.count,  Html.toHtml(searchMethod.getSearchSuggestionText(keyword, item)), item))
                    item
                }
                .collect({ ArrayList<T>() }, { list, value -> list.add(value) })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(object : DisposableSingleObserver<ArrayList<T>>() {
                    override fun onSuccess(list: ArrayList<T>) {
                        if(isSubmitted){
                            searchMethod.onSearchResult(keyword,list)
                            return
                        }
                        if(suggestionsAdapter==null)
                            suggestionsAdapter = SearchSuggestionAdapter(context,cursor,this@CustomSearchView)
                        suggestionsAdapter.swapCursor(cursor)
                    }

                    override fun onError(e: Throwable) {
                        Log.i("error","$e")
                    }
                }))
    }

    override fun onDetachedFromWindow() {
        mCompositeDisposable.clear()
        super.onDetachedFromWindow()
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        return false
    }
}
