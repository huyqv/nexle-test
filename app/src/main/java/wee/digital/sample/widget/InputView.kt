package wee.digital.sample.widget

import android.app.Activity
import android.content.Context
import android.content.res.TypedArray
import android.graphics.Paint
import android.graphics.Rect
import android.os.*
import android.text.InputFilter
import android.text.method.PasswordTransformationMethod
import android.util.AttributeSet
import android.util.TypedValue
import android.view.*
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewbinding.ViewBinding
import wee.digital.sample.R
import wee.digital.sample.databinding.InputBinding
import wee.digital.sample.util.addClickListener

class InputView : AppCustomView<InputBinding>,
    OnFocusChangeListener {


    constructor(context: Context, attrs: AttributeSet? = null) : super(context, attrs)

    /**
     * Init
     */
    override fun inflating(): (LayoutInflater, ViewGroup?, Boolean) -> ViewBinding {
        return InputBinding::inflate
    }

    override fun onInitialize(context: Context, types: TypedArray) {
        title = types.title
        onEditTextInitialize(vb.inputEditText, types)
        optional = types.getBoolean(R.styleable.AppCustomView_optional, false)
    }

    private fun onEditTextInitialize(it: AppCompatEditText, types: TypedArray) {
        it.onFocusChangeListener = this
        it.paintFlags = it.paintFlags and Paint.UNDERLINE_TEXT_FLAG.inv()
        it.setTextSize(
            TypedValue.COMPLEX_UNIT_PX,
            types.getDimension(
                R.styleable.AppCustomView_android_textSize,
                getPixels(R.dimen.textSize15)
            )
        )
        it.maxLines = 1

        // Text filter
        val sFilters = arrayListOf<InputFilter>()

        val textAllCaps = types.getBoolean(R.styleable.AppCustomView_android_textAllCaps, false)
        if (textAllCaps) sFilters.add(InputFilter.AllCaps())

        val sMaxLength = types.getInt(R.styleable.AppCustomView_android_maxLength, 256)
        sFilters.add(InputFilter.LengthFilter(sMaxLength))

        val array = arrayOfNulls<InputFilter>(sFilters.size)
        it.filters = sFilters.toArray(array)

        // Input type
        val attrInputType = types.getInt(
            R.styleable.AppCustomView_android_inputType,
            EditorInfo.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        )
        when (attrInputType) {
            EditorInfo.TYPE_NULL -> {
                editText.inputType = EditorInfo.TYPE_NULL
                disableFocus()
            }
            EditorInfo.TYPE_CLASS_NUMBER -> {
                editText.inputType = EditorInfo.TYPE_CLASS_NUMBER
            }
            EditorInfo.TYPE_CLASS_TEXT or EditorInfo.TYPE_TEXT_VARIATION_PASSWORD,
            EditorInfo.TYPE_CLASS_NUMBER or EditorInfo.TYPE_NUMBER_VARIATION_PASSWORD -> {
                editText.inputType = attrInputType
                editText.transformationMethod = PasswordMethod()
            }
            else -> {
                it.inputType =
                    attrInputType or EditorInfo.TYPE_TEXT_FLAG_NO_SUGGESTIONS or EditorInfo.IME_FLAG_NO_PERSONALIZED_LEARNING
            }
        }

        it.maxLines = types.getInt(R.styleable.AppCustomView_android_maxLines, 1)

        // Ime option
        val imeOption = types.getInt(R.styleable.AppCustomView_android_imeOptions, -1)
        if (imeOption != -1) it.imeOptions = imeOption

        it.privateImeOptions = "nm,com.google.android.inputmethod.latin.noMicrophoneKey"

        // Gesture
        it.setOnLongClickListener {
            return@setOnLongClickListener true
        }
        it.setTextIsSelectable(false)
        it.customSelectionActionModeCallback = object : ActionMode.Callback {
            override fun onPrepareActionMode(mode: ActionMode, menu: Menu) = false
            override fun onDestroyActionMode(mode: ActionMode) {}
            override fun onCreateActionMode(mode: ActionMode, menu: Menu) = false
            override fun onActionItemClicked(mode: ActionMode, item: MenuItem) = false
        }
        it.isLongClickable = false
        it.setOnCreateContextMenuListener { menu, _, _ -> menu.clear() }
        it.setText(types.text)
    }


    /**
     * [View] implements
     */
    override fun setEnabled(enabled: Boolean) {
        super.setEnabled(enabled)
        if (enabled) {
            enableFocus()
        } else {
            disableFocus()
        }
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        if (null == listener) {
            enableFocus()
            editText.setOnClickListener(null)
        } else {
            disableFocus()
            isClickable = true
            editText.addClickListener {
                listener.onClick(this)
            }
        }
    }

    override fun performClick(): Boolean {
        return editText.performClick()
    }

    override fun onDetachedFromWindow() {
        vb.inputViewLayout.clearAnimation()
        onFocusChange?.clear()
        super.onDetachedFromWindow()
    }

    override fun onSaveInstanceState(): Parcelable? {
        val superState = super.onSaveInstanceState()
        superState?.let {
            val state = SaveState(superState)
            state.text = text
            return state
        } ?: run {
            return superState
        }
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        super.onRestoreInstanceState(state)
        when (state) {
            is SaveState -> {
                super.onRestoreInstanceState(state.superState)
                Handler(Looper.getMainLooper()).post {
                    val s = state.text
                    text = s
                }
            }
        }
    }

    /**
     * [InputView] properties
     */
    private val editText: EditText get() = vb.inputEditText
    private var tempHasFocus: Boolean? = null
    private var isTextSilent: Boolean = false
    val isTextEmpty: Boolean get() = text.isNullOrEmpty()
    val hasError: Boolean get() = !error.isNullOrEmpty()
    val textLength: Int get() = text?.length ?: 0

    var text: String?
        get() {
            isTextSilent = true
            val s = editText.text?.toString()
            if (this.hasFocus()) {
                editText.setSelection(s?.length ?: 0)
            }
            return s
            isTextSilent = false
            return s
        }
        set(value) {
            isTextSilent = true
            editText.setText(value)
            onFocusChange(null, hasFocus())
            isTextSilent = false
        }

    val trimText: String
        get() {
            if (text.isNullOrEmpty()) return ""
            return text!!.replace("\n", " ")
                .replace("\\s+".toRegex(), " ")
                .trim()
                .trimIndent()
        }

    var title: String?
        get() = vb.inputTextViewTitle.text?.toString()
        set(value) {
            vb.inputTextViewTitle.text = value
        }


    var error: String?
        get() = vb.inputTextViewError.text?.toString()
        set(value) {
            vb.inputTextViewError.text = value
        }


    var inputType: Int = 0
        set(value) {
            editText.inputType = value
        }

    var maxLengths: Int = 0
        set(value) {
            val sFilters = arrayListOf<InputFilter>()
            sFilters.add(InputFilter.LengthFilter(value))
            val array = arrayOfNulls<InputFilter>(sFilters.size)
            editText.filters = sFilters.toArray(array)
        }


    /**
     * On text change
     */
    var onTextChanged: ((String) -> Unit)? = null

    /**
     *  Optional
     */
    var optional: Boolean = false
        set(value) {
            if (value) {
                vb.viewNoneOptional.text = "*"
            } else {
                vb.viewNoneOptional.text = null
            }
        }

    /**
     * Focus/Focusable
     */
    var onLostFocus: ((String?) -> Unit)? = null

    private var lostFocusText: String? = null

    private var onFocusChange: MutableList<(Boolean) -> Unit>? = null

    override fun onFocusChange(v: View?, hasFocus: Boolean) {
        if (hasFocus == tempHasFocus) return
        onFocusChange?.forEach { it(hasFocus) }
        if (!hasFocus) {
            lostFocusText = text
            onLostFocus?.invoke(text)
        }
    }

    override fun hasFocusable(): Boolean {
        return false
    }

    override fun isFocused(): Boolean {
        return false
    }

    override fun hasFocus(): Boolean {
        return editText.hasFocus()
    }

    override fun clearFocus() {
        editText.clearFocus()
        hideKeyboard()
    }

    override fun requestFocus(direction: Int, previouslyFocusedRect: Rect?): Boolean {
        editText.post {
            if (!editText.isFocused) {
                editText.requestFocus(FOCUS_DOWN)
                showKeyboard()
            }
        }
        return true
    }

    fun addOnFocusChangeListener(block: (Boolean) -> Unit) {
        if (onFocusChange == null) onFocusChange = mutableListOf()
        onFocusChange?.add(block)
    }

    fun disableFocus() {
        editText.also {
            it.isFocusable = false
            it.isCursorVisible = false
        }
    }

    fun enableFocus() {
        editText.also {
            it.isFocusable = true
            it.isCursorVisible = true
            it.isEnabled = true
            it.isFocusableInTouchMode = true
        }
    }

    /**
     * Update ui on error, focus, enable, text change
     */
    var isViewSilent: Boolean = false

    /**
     * Utils
     */
    fun addActionDoneListener(block: (String?) -> Unit) {
        editText.imeOptions = EditorInfo.IME_ACTION_DONE
        editText.isSingleLine = true
        editText.setImeActionLabel("Next", EditorInfo.IME_ACTION_DONE)
        editText.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (editText.imeOptions == actionId) {
                    editText.post {
                        isSelected = false
                        block(text.toString())
                    }
                    return true
                }
                return false
            }
        })
    }

    fun clear() {
        text = null
    }

    fun showKeyboard() {
        editText.post {
            (context as? Activity)?.window?.also {
                val controller = WindowInsetsControllerCompat(it, it.decorView)
                controller.hide(WindowInsetsCompat.Type.ime())
            }
        }
    }

    fun hideKeyboard() {
        post {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
            imm?.hideSoftInputFromWindow(editText.windowToken, 0)
        }
    }

    fun filter(filterChars: CharArray) {
        val arrayList = arrayListOf<InputFilter>()
        editText.filters?.apply { arrayList.addAll(this) }
        arrayList.add(InputFilter { source, start, end, _, _, _ ->
            if (end > start) {
                for (index in start until end) {
                    if (!String(filterChars).contains(source[index].toString())) {
                        return@InputFilter ""
                    }
                }
            }
            null
        })
        editText.filters = arrayList.toArray(arrayOfNulls<InputFilter>(arrayList.size))
    }

    fun require() {
        error = "$title is required"
    }

    inner class SaveState : AbsSavedState {

        var text: String? = null

        constructor(superState: Parcelable) : super(superState)

        @RequiresApi(Build.VERSION_CODES.N)
        constructor(source: Parcel, loader: ClassLoader?) : super(source, loader) {
            text = source.readString()
        }

        override fun writeToParcel(dest: Parcel, flags: Int) {
            super.writeToParcel(dest, flags)
            text?.also { dest.writeString(it) }
        }
    }

    class PasswordMethod : PasswordTransformationMethod() {

        override fun getTransformation(source: CharSequence, view: View): CharSequence {
            return PasswordCharSequence(source)
        }

        private inner class PasswordCharSequence(private val source: CharSequence) : CharSequence {

            override val length: Int get() = source.length

            override fun get(index: Int): Char {
                return when {
                    index <= length -> '⚉'
                    else -> source[index]
                }
            }

            override fun subSequence(startIndex: Int, endIndex: Int): CharSequence {
                return source.subSequence(startIndex, endIndex)
            }
        }
    }

}
