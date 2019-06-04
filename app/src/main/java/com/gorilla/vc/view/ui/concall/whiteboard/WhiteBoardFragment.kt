package com.gorilla.vc.view.ui.concall.whiteboard

import android.app.AlertDialog
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.databinding.BindingAdapter
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.v4.app.Fragment
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.PopupMenu
import com.gorilla.vc.R
import com.gorilla.vc.databinding.ConcallWhiteboardFragmentBinding
import com.gorilla.vc.di.Injectable
import com.gorilla.vc.model.VcManager
import com.gorilla.vc.utils.VcViewModelFactory
import com.gorilla.vc.view.ui.concall.ConcallViewModel
import com.gorilla.vc.view.ui.concall.whiteboard.spectrum.SpectrumDialog
import java.io.ByteArrayOutputStream
import javax.inject.Inject

class WhiteBoardFragment : Fragment(), Injectable {

    private val TAG = WhiteBoardFragment::class.simpleName

    private var mBinding: ConcallWhiteboardFragmentBinding? = null

    @Inject
    lateinit var concallViewModel: ConcallViewModel

    @Inject
    lateinit var vcManager: VcManager

    @Inject
    lateinit var factory: VcViewModelFactory

    private var seqNum = 0

    private var dataHistory: ArrayList<String>? = ArrayList()

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        concallViewModel = ViewModelProviders.of(this, factory)
                                             .get(ConcallViewModel::class.java)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = ConcallWhiteboardFragmentBinding.inflate(inflater, container, false)

        initUI()
        initListeners()

        return mBinding?.root
    }

    override fun onDestroyView() {
        if (vcManager.userId != null) {
            concallViewModel.setVideoSourceFromCamera(vcManager.userId!!.toInt())
        }

        super.onDestroyView()
    }

    private fun initUI() {
        mBinding?.isUsePencil = true
        mBinding?.isUseEraser = false
        mBinding?.isUseText = false
        mBinding?.drawingCanvas?.setFragment(this)
    }

    private fun initListeners() {
        initFunctionListener()
        initColorButtonListener()
        initInputTextListener()
    }

    /**
     * Set functions' listeners
     */
    private fun initFunctionListener() {
        mBinding?.pencilBtn?.setOnClickListener {
            Log.d(tag, "Pencil button click")
            if (!mBinding?.isUsePencil!!) {
                mBinding?.isUsePencil = true
                mBinding?.isUseEraser = false
                mBinding?.isUseText = false
            }
            mBinding?.drawingCanvas?.setPencil()
        }

        mBinding?.pencilBtn?.setOnLongClickListener { view ->
            Log.d(tag, "Pencil button long click")
            // Show width options for pencil
            val wrapper = ContextThemeWrapper(this.context, R.style.WhiteboardPopupMenu)
            val popup = PopupMenu(wrapper, view)
            popup.inflate(R.menu.menu_pencil)

            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.width_1dp -> mBinding?.drawingCanvas?.setPencilWidth(1)
                    R.id.width_2dp -> mBinding?.drawingCanvas?.setPencilWidth(2)
                    R.id.width_3dp -> mBinding?.drawingCanvas?.setPencilWidth(3)
                    R.id.width_5dp -> mBinding?.drawingCanvas?.setPencilWidth(5)
                    R.id.width_10dp -> mBinding?.drawingCanvas?.setPencilWidth(10)
                    else -> mBinding?.drawingCanvas?.setPencilWidth(3)
                }
                return@setOnMenuItemClickListener true
            }
            popup.show()

            return@setOnLongClickListener true
        }

        mBinding?.eraserBtn?.setOnClickListener {
            Log.d(tag, "Eraser button click")
            if (!mBinding?.isUseEraser!!) {
                mBinding?.isUsePencil = false
                mBinding?.isUseEraser = true
                mBinding?.isUseText = false
            }
            mBinding?.drawingCanvas?.setEraser()
        }

        mBinding?.eraserBtn?.setOnLongClickListener { view ->
            Log.d(tag, "Erase button long click")
            // Show width options for eraser
            val wrapper = ContextThemeWrapper(this.context, R.style.WhiteboardPopupMenu)
            val popup = PopupMenu(wrapper, view)
            popup.inflate(R.menu.menu_eraser)

            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.width_5dp -> mBinding?.drawingCanvas?.setEraserWidth(5)
                    R.id.width_10dp -> mBinding?.drawingCanvas?.setEraserWidth(10)
                    R.id.width_20dp -> mBinding?.drawingCanvas?.setEraserWidth(20)
                    R.id.width_30dp -> mBinding?.drawingCanvas?.setEraserWidth(30)
                    R.id.width_50dp -> mBinding?.drawingCanvas?.setEraserWidth(50)
                    else -> mBinding?.drawingCanvas?.setEraserWidth(20)
                }
                return@setOnMenuItemClickListener true
            }
            popup.show()

            return@setOnLongClickListener true
        }

        mBinding?.inputTextBtn?.setOnClickListener {
            Log.d(tag, "Input text button click")
            if (!mBinding?.isUseText!!) {
                mBinding?.isUsePencil = false
                mBinding?.isUseEraser = false
                mBinding?.isUseText = true
            }
            mBinding?.drawingCanvas?.setInputText()
        }

        mBinding?.inputTextBtn?.setOnLongClickListener { view ->
            Log.d(tag, "Input text button long click")
            // Show width options for pencil
            val wrapper = ContextThemeWrapper(this.context, R.style.WhiteboardPopupMenu)
            val popup = PopupMenu(wrapper, view)
            popup.inflate(R.menu.menu_input_text)

            popup.setOnMenuItemClickListener {
                when (it.itemId) {
                    R.id.text_size_small -> mBinding?.drawingCanvas?.setInputTextSize(DrawingView.TEXT_SIZE_SMALL)
                    R.id.text_size_medium -> mBinding?.drawingCanvas?.setInputTextSize(DrawingView.TEXT_SIZE_MEDIUM)
                    R.id.text_size_big -> mBinding?.drawingCanvas?.setInputTextSize(DrawingView.TEXT_SIZE_BIG)
                    else -> mBinding?.drawingCanvas?.setInputTextSize(DrawingView.TEXT_SIZE_MEDIUM)
                }
                return@setOnMenuItemClickListener true
            }
            popup.show()

            return@setOnLongClickListener true
        }

        mBinding?.colorBtn?.setOnClickListener {
            Log.d(tag, "Color spectrum button click")
            mBinding?.drawingCanvas?.getAllColors()
        }

        mBinding?.saveBtn?.setOnClickListener {
            Log.d(tag, "Save button click")
            showConfirmSaveDialog()
        }

        mBinding?.undoBtn?.setOnClickListener {
            Log.d(tag, "Undo button click")
            mBinding?.drawingCanvas?.undo()
        }

        mBinding?.trashCanBtn?.setOnClickListener {
            Log.d(tag, "Trash can button click")
            showConfirmClearDialog()
        }
    }

    /**
     * Set color button listeners
     */
    private fun initColorButtonListener() {
        mBinding?.colorBlackBtn?.setOnClickListener {
            Log.d(tag, "Black button click")
            mBinding?.drawingCanvas?.setColor(DrawingView.COLOR_BLACK)
        }

        mBinding?.colorGrayBtn?.setOnClickListener {
            Log.d(tag, "Gray button click")
            mBinding?.drawingCanvas?.setColor(DrawingView.COLOR_GRAY)
        }

        mBinding?.colorWhiteBtn?.setOnClickListener {
            Log.d(tag, "White button click")
            mBinding?.drawingCanvas?.setColor(DrawingView.COLOR_WHITE)
        }

        mBinding?.colorRedBtn?.setOnClickListener {
            Log.d(tag, "Red button click")
            mBinding?.drawingCanvas?.setColor(DrawingView.COLOR_RED)
        }

        mBinding?.colorOrangeBtn?.setOnClickListener {
            Log.d(tag, "Orange button click")
            mBinding?.drawingCanvas?.setColor(DrawingView.COLOR_ORANGE)
        }

        mBinding?.colorYellowBtn?.setOnClickListener {
            Log.d(tag, "Yellow button click")
            mBinding?.drawingCanvas?.setColor(DrawingView.COLOR_YELLOW)
        }

        mBinding?.colorGreenBtn?.setOnClickListener {
            Log.d(tag, "Green button click")
            mBinding?.drawingCanvas?.setColor(DrawingView.COLOR_GREEN)
        }
        mBinding?.colorBlueBtn?.setOnClickListener {
            Log.d(tag, "Blue button click")
            mBinding?.drawingCanvas?.setColor(DrawingView.COLOR_BLUE)
        }

        mBinding?.colorIndigoBtn?.setOnClickListener {
            Log.d(tag, "Indigo button click")
            mBinding?.drawingCanvas?.setColor(DrawingView.COLOR_INDIGO)
        }

        mBinding?.colorPurpleBtn?.setOnClickListener {
            Log.d(tag, "Purple button click")
            mBinding?.drawingCanvas?.setColor(DrawingView.COLOR_PURPLE)
        }

        // default color is black
        mBinding?.colorBlackBtn?.isSelected = true
    }

    /**
     * Set input text listener
     */
    private fun initInputTextListener() {
        mBinding?.inputText?.setRootFragment(this)

//        mBinding?.inputText?.setOnEditorActionListener { textView, actionId, event ->
//            Log.d(tag, "Input action id: $actionId")
//
//            if (actionId == EditorInfo.IME_ACTION_DONE) {
//                mBinding?.drawingCanvas?.commitDrawingText()
//                stopEditInputText()
//                return@setOnEditorActionListener false
//            }
//
//            return@setOnEditorActionListener true
//        }

        mBinding?.inputText?.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                mBinding?.drawingCanvas?.setInputTextContent(s.toString())
            }
        })

        val f = this
        mBinding?.confirmInput?.setOnClickListener {
            mBinding?.drawingCanvas?.commitDrawingText()
            stopEditInputText()

            val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(f.view?.windowToken, 0)
        }
    }

    fun onBackPressed() {
        stopEditInputText()
    }

    /**
     * Show all options of color to choose
     *
     * @param colors the options of color to paint
     */
    fun showAllColorOptions(colors: IntArray, curColor: Int) {
        SpectrumDialog.Builder(this.context)
                .setColors(colors)
                .setSelectedColor(colors[curColor])
                .setDismissOnColorSelected(true)
                .setOutlineWidth(2)
                .setOnColorSelectedListener { positiveResult, color ->
                    if (positiveResult) {
                        Log.d(TAG, "Color selected: #${Integer.toHexString(color).toUpperCase()}")
                        mBinding?.drawingCanvas?.setColor(colors.indexOf(color))
                    } else {
                        Log.d(TAG, "Dialog cancelled")
                    }
                }.build().show(fragmentManager, getString(R.string.select_color))
    }

    /**
     * Change color of the color button based the currently active color
     *
     * @param color the color to paint the button
     */
    fun setButtonColor(color: Int, index: Int) {
        Log.d(tag, "Select color index: $index")

        mBinding?.colorBtn?.setBackgroundColor(color)

        for (i in 0..mBinding?.colorLayout?.childCount!!) {
            val view = mBinding?.colorLayout?.getChildAt(i)
            if (view != null)
                (view as ImageView).isSelected = false
        }

        when (index) {
            DrawingView.COLOR_BLACK -> mBinding?.colorBlackBtn?.isSelected = true
            DrawingView.COLOR_GRAY -> mBinding?.colorGrayBtn?.isSelected = true
            DrawingView.COLOR_WHITE -> mBinding?.colorWhiteBtn?.isSelected = true
            DrawingView.COLOR_RED -> mBinding?.colorRedBtn?.isSelected = true
            DrawingView.COLOR_ORANGE -> mBinding?.colorOrangeBtn?.isSelected = true
            DrawingView.COLOR_YELLOW -> mBinding?.colorYellowBtn?.isSelected = true
            DrawingView.COLOR_GREEN -> mBinding?.colorGreenBtn?.isSelected = true
            DrawingView.COLOR_BLUE -> mBinding?.colorBlueBtn?.isSelected = true
            DrawingView.COLOR_INDIGO -> mBinding?.colorIndigoBtn?.isSelected = true
            DrawingView.COLOR_PURPLE -> mBinding?.colorPurpleBtn?.isSelected = true
        }
    }

    fun startEditInputText() {
        Log.d(TAG, "startEditInputText()")
        mBinding?.colorLayout?.visibility = View.GONE
        mBinding?.inputLayout?.visibility = View.VISIBLE

        mBinding?.inputText?.requestFocus()
        val imm = activity?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(mBinding?.inputText, 0)
    }

    private fun stopEditInputText() {
        Log.d(TAG, "stopEditInputText()")
        mBinding?.drawingCanvas?.stopEditInputText()

        mBinding?.colorLayout?.visibility = View.VISIBLE
        mBinding?.inputLayout?.visibility = View.GONE
        mBinding?.inputText?.setText("")
    }

    /**
     * Bring sequence #'s up to par when a new piece of data is produced.
     */
    private fun increaseSequenceNumber() {
        seqNum++
    }

    /**
     * Function to handle situation where user draws a new stroke or clear canvas.
     *
     * @param jsonData the json representation of the user's action
     */
    fun callback(jsonData: String) {
        dataHistory?.add(jsonData)  // Add action to history
        increaseSequenceNumber()
        //Log.d(TAG, "User drawing step generated: $jsonData")
    }

    /**
     * Function to confirm that the user want's to save the current whiteboard canvas as a image in
     * device gallery
     */
    private fun showConfirmSaveDialog() {
        Log.d(TAG, "showConfirmSaveDialog()")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d(TAG, "IF PART, Build.VERSION.SDK_INT >= Build.VERSION_CODES.O")
            this.activity?.window?.let { window ->
                val view = mBinding?.drawingCanvas
                val bitmap = Bitmap.createBitmap(view?.width!!, view.height, Bitmap.Config.ARGB_8888)
                val locationOfViewInWindow = IntArray(2)
                mBinding?.drawingCanvas?.getLocationInWindow(locationOfViewInWindow)
                try {
                    PixelCopy.request(window
                            , Rect(locationOfViewInWindow[0], locationOfViewInWindow[1],
                            locationOfViewInWindow[0] + view.width, locationOfViewInWindow[1] + view.height)
                            , bitmap
                            , {/*copyResult*/
                                val outputStream = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
                                WhiteBoardUtils.saveWhiteboardImage(this.context!!, outputStream)
                                outputStream.close()
                            }
                            , Handler())
                } catch (e: IllegalArgumentException) {
                    // PixelCopy may throw IllegalArgumentException, make sure to handle it
                    e.printStackTrace()
                }
            }
        } else {
            Log.d(TAG, "ELSE PART, Build.VERSION.SDK_INT < Build.VERSION_CODES.O")
            // Deprecated way since API 28
            //mBinding?.drawingCanvas?.isDrawingCacheEnabled
            //val outputStream = ByteArrayOutputStream()
            //mBinding?.drawingCanvas?.drawingCache?.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            //WhiteBoardUtils.saveWhiteboardImage(this.context!!, outputStream)
            //mBinding?.drawingCanvas?.destroyDrawingCache()
        }
    }

    /**
     * Ask user to confirm whether to clear canvas or not.
     */
    private fun showConfirmClearDialog() {
        val builder = AlertDialog.Builder(this.context)
                    .setIcon(R.mipmap.ic_dialog_alert)
                    .setTitle(getString(R.string.confirm_clear))
                    .setMessage(getString(R.string.clear_canvas_message))
                    .setNegativeButton(getString(R.string.cancel)) { _, _ -> } // do nothing
                    .setPositiveButton(getString(R.string.confirm)) { _, _ ->
                        Log.d(TAG, "Clear canvas")
                        mBinding?.drawingCanvas?.clear()
                    }
                    .create()

        builder.show()
    }

    companion object {
        @JvmStatic
        @BindingAdapter("convertPencilBackground")
        fun convertPencilBackground(view: ImageView, isUsePencil: Boolean) {
            if (isUsePencil) {
                view.setImageResource(R.mipmap.btn_pen_p)
            } else {
                view.setImageResource(R.drawable.btn_pencil)
            }
        }

        @JvmStatic
        @BindingAdapter("convertEraserBackground")
        fun convertEraserBackground(view: ImageView, isUseEraser: Boolean) {
            if (isUseEraser) {
                view.setImageResource(R.mipmap.btn_eraser_p)
            } else {
                view.setImageResource(R.drawable.btn_eraser)
            }
        }

        @JvmStatic
        @BindingAdapter("convertTextBackground")
        fun convertTextBackground(view: ImageView, isUseText: Boolean) {
            if (isUseText) {
                view.setImageResource(R.mipmap.btn_text_p)
            } else {
                view.setImageResource(R.drawable.btn_input_text)
            }
        }
    }
}