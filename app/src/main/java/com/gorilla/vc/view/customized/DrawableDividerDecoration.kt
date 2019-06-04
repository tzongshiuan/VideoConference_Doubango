package com.gorilla.vc.view.customized

import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView

/**
 * The divider decoration for [RecyclerView]
 *
 * Note: This decoration extracts orientation from [RecyclerView.LayoutManager] and
 * therefore it supports only [LinearLayoutManager]!
 */
class DrawableDividerDecoration(private val dividerDrawable: Drawable?,
                @DividerMode.Type private val dividerMode: Int) : RecyclerView.ItemDecoration() {

    private val bounds = Rect()

    override fun onDrawOver(canvas: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        val layoutManager: RecyclerView.LayoutManager? = parent.layoutManager

        layoutManager?.let {
            when (layoutManager) {
                is LinearLayoutManager -> {
                    when (layoutManager.orientation) {
                        RecyclerView.HORIZONTAL -> decorateHorizontally()
                        RecyclerView.VERTICAL -> decorateVertically(canvas, parent)
                        else -> throw IllegalStateException("Unknown orientation")
                    }
                }
                else -> throw IllegalStateException(
                        "Cannot support ${javaClass.simpleName} for ${layoutManager.javaClass.simpleName}")
            }
        }
    }

    private fun decorateVertically(canvas: Canvas, parent: RecyclerView) {
        canvas.save()

        val left: Int
        val right: Int
        if (parent.clipToPadding) {
            left = parent.paddingLeft
            right = parent.width - parent.paddingRight
            canvas.clipRect(left, parent.paddingTop, right,
                    parent.height - parent.paddingBottom)
        } else {
            left = 0
            right = parent.width
        }

        val childCount = parent.childCount
        for (i in 0 until childCount) {
            val child = parent.getChildAt(i)
            parent.getDecoratedBoundsWithMargins(child, bounds)

            // Beginning
            if (i == 0 &&
                    dividerMode.and(DividerMode.SHOW_DIVIDER_BEGINNING) == DividerMode.SHOW_DIVIDER_BEGINNING) {
                val top = bounds.top + Math.round(child.translationY)
                val bottom = top + dividerDrawable!!.intrinsicHeight

                dividerDrawable.setBounds(left, top, right, bottom)
                dividerDrawable.draw(canvas)
            }

            // Middle
            if (i in 0..(childCount - 2) &&
                    dividerMode.and(DividerMode.SHOW_DIVIDER_MIDDLE) == DividerMode.SHOW_DIVIDER_MIDDLE) {
                val bottom = bounds.bottom + Math.round(child.translationY)
                val top = bottom - dividerDrawable!!.intrinsicHeight

                dividerDrawable.setBounds(left, top, right, bottom)
                dividerDrawable.draw(canvas)
            }

            // End
            if (i == childCount - 1 &&
                    dividerMode.and(DividerMode.SHOW_DIVIDER_END) == DividerMode.SHOW_DIVIDER_END) {
                val bottom = bounds.bottom + Math.round(child.translationY)
                val top = bottom - dividerDrawable!!.intrinsicHeight

                dividerDrawable.setBounds(left, top, right, bottom)
                dividerDrawable.draw(canvas)
            }
        }

        canvas.restore()
    }

    private fun decorateHorizontally() {
        // TODO horizontal
    }
}