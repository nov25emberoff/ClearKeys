package com.clearkeys

import android.inputmethodservice.InputMethodService
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.LinearLayout

class ClearKeysService : InputMethodService() {

    private lateinit var cryptoEngine: CryptoEngine
    private lateinit var dictionaryManager: DictionaryManager
    private var isEncryptionEnabled = true
    private var composingText = StringBuilder()

    override fun onCreate() {
        super.onCreate()
        cryptoEngine = CryptoEngine()
        dictionaryManager = DictionaryManager(this)
        val dictionary = dictionaryManager.loadDictionary()
        if (dictionary.isEmpty()) {
            val default = dictionaryManager.buildDefaultDictionary()
            cryptoEngine.loadDictionary(default)
            dictionaryManager.saveDictionary(default)
        } else {
            cryptoEngine.loadDictionary(dictionary)
        }
    }

    override fun onCreateInputView(): View {
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(4, 4, 4, 4)
            setBackgroundColor(0xFF121212.toInt())
        }

        val row1 = createRow()
        addKeys(row1, "1234567890".toList())
        layout.addView(row1)

        val row2 = createRow()
        addKeys(row2, "qwertyuiop".toList())
        layout.addView(row2)

        val row3 = createRow()
        addKeys(row3, "asdfghjkl".toList())
        layout.addView(row3)

        val row4 = createRow()
        addSpecialKey(row4, "⇧") {}
        addKeys(row4, "zxcvbnm".toList())
        addSpecialKey(row4, "⌫") { handleBackspace() }
        layout.addView(row4)

        val row5 = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            )
        }

        addSpecialKey(row5, if (isEncryptionEnabled) "🔒" else "🔓") {
            isEncryptionEnabled = !isEncryptionEnabled
            (it as? Button)?.text = if (isEncryptionEnabled) "🔒" else "🔓"
        }

        addSpecialKey(row5, "␣", width = 0.5f) { commitText(" ", 1) }
        addSpecialKey(row5, "↵") { sendDefaultEditorAction(true) }
        layout.addView(row5)

        return layout
    }

    private fun createRow(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.HORIZONTAL
        layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
    }

    private fun addKeys(row: LinearLayout, keys: List<Char>) {
        keys.forEach { char ->
            val key = Button(this).apply {
                text = char.toString(); textSize = 16f
                setTextColor(0xFFFFFFFF.toInt()); setBackgroundColor(0xFF1E1E1E.toInt())
                layoutParams = LinearLayout.LayoutParams(0, 120, 1f).apply { setMargins(2, 2, 2, 2) }
                setOnClickListener { onKey(char) }
            }
            row.addView(key)
        }
    }

    private fun addSpecialKey(row: LinearLayout, label: String, width: Float = 0.15f, onClick: (View) -> Unit) {
        val key = Button(this).apply {
            text = label; textSize = 14f
            setTextColor(0xFFFFFFFF.toInt()); setBackgroundColor(0xFFE04F16.toInt())
            layoutParams = LinearLayout.LayoutParams(0, 120, width).apply { setMargins(2, 2, 2, 2) }
            setOnClickListener { onClick(it) }
        }
        row.addView(key)
    }

    private fun onKey(char: Char) {
        composingText.append(char)
        commitText(char.toString(), 1)
    }

    private fun handleBackspace() {
        if (composingText.isNotEmpty()) composingText.deleteCharAt(composingText.length - 1)
        sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
    }

    override fun onCommitText(text: CharSequence?, newCursorPosition: Int) {
        if (text == null) return
        val processed = if (isEncryptionEnabled) cryptoEngine.encodeText(text.toString())
        else cryptoEngine.decodeText(text.toString())
        super.onCommitText(processed, newCursorPosition)
    }

    fun updateDictionary(dictionary: Map<String, String>) {
        cryptoEngine.loadDictionary(dictionary)
        dictionaryManager.saveDictionary(dictionary)
    }

    fun getCryptoEngine(): CryptoEngine = cryptoEngine
    fun getDictionaryManager(): DictionaryManager = dictionaryManager
}
