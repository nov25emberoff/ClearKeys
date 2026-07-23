package com.clearkeys

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var dictionarySizeText: TextView
    private lateinit var panicBtn: Button
    private lateinit var qrImageView: ImageView
    private val dictionaryManager by lazy { DictionaryManager(this) }
    private var currentDictionary = mutableMapOf<String, String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 48, 32, 32)
            setBackgroundColor(0xFF121212.toInt())
        }

        layout.addView(TextView(this).apply {
            text = "ClearKeys"; textSize = 28f; setTextColor(0xFFFFFFFF.toInt())
        })

        layout.addView(TextView(this).apply {
            text = "Keyboard that defeats Chat Control"; textSize = 14f
            setTextColor(0xFFE04F16.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 4 }
        })

        statusText = TextView(this).apply {
            text = "Status: Not enabled"; textSize = 14f; setTextColor(0xFFB0B0B0.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 24 }
        }
        layout.addView(statusText)

        layout.addView(Button(this).apply {
            text = "ENABLE CLEARKEYS KEYBOARD"; textSize = 15f
            setTextColor(0xFFFFFFFF.toInt()); setBackgroundColor(0xFFE04F16.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 130
            ).apply { topMargin = 12 }
            setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) }
        })

        dictionarySizeText = TextView(this).apply {
            text = "Dictionary: loading..."; textSize = 12f; setTextColor(0xFF707070.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 8 }
        }
        layout.addView(dictionarySizeText)

        layout.addView(View(this).apply {
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 1
            ).apply { topMargin = 24; bottomMargin = 24 }
            setBackgroundColor(0xFF2A2A2A.toInt())
        })

        // Sync section
        layout.addView(TextView(this).apply {
            text = "Dictionary Sync"; textSize = 18f; setTextColor(0xFFFFFFFF.toInt())
        })
        layout.addView(TextView(this).apply {
            text = "Share dictionary with contacts via QR code."
            textSize = 12f; setTextColor(0xFF707070.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 4 }
        })

        val syncRow = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 12 }
        }

        syncRow.addView(Button(this).apply {
            text = "SHOW QR"; textSize = 14f; setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF1E1E1E.toInt())
            layoutParams = LinearLayout.LayoutParams(0, 110, 1f).apply { marginEnd = 8 }
            setOnClickListener { generateQRCode() }
        })

        syncRow.addView(Button(this).apply {
            text = "SCAN QR"; textSize = 14f; setTextColor(0xFFFFFFFF.toInt())
            setBackgroundColor(0xFF1E1E1E.toInt())
            layoutParams = LinearLayout.LayoutParams(0, 110, 1f).apply { marginStart = 8 }
            setOnClickListener { scanQRCode() }
        })
        layout.addView(syncRow)

        qrImageView = ImageView(this).apply {
            layoutParams = LinearLayout.LayoutParams(600, 600).apply {
                topMargin = 16; gravity = android.view.Gravity.CENTER_HORIZONTAL
            }
            visibility = android.view.View.GONE
        }
        layout.addView(qrImageView)

        // Panic section
        layout.addView(TextView(this).apply {
            text = "Emergency Panic Mode"; textSize = 18f; setTextColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 32 }
        })
        layout.addView(TextView(this).apply {
            text = "Instantly replaces all visible messages with innocent text."
            textSize = 12f; setTextColor(0xFF707070.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 4 }
        })

        panicBtn = Button(this).apply {
            text = "🛡 ACTIVATE PANIC MODE"; textSize = 16f
            setTextColor(0xFFFFFFFF.toInt()); setBackgroundColor(0xFFC62828.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 140
            ).apply { topMargin = 12 }
            setOnClickListener {
                if (PanicService.isPanicActive) {
                    PanicTrigger().stopPanic(this@SettingsActivity)
                    text = "🛡 ACTIVATE PANIC MODE"; setBackgroundColor(0xFFC62828.toInt())
                    Toast.makeText(this@SettingsActivity, "Panic mode stopped", Toast.LENGTH_SHORT).show()
                } else {
                    if (isAccessibilityServiceEnabled()) {
                        PanicTrigger().triggerPanic(this@SettingsActivity)
                        text = "⏹ STOP PANIC MODE"; setBackgroundColor(0xFF4CAF50.toInt())
                        Toast.makeText(this@SettingsActivity, "🛡 PANIC: All messages being cleaned", Toast.LENGTH_LONG).show()
                    } else {
                        Toast.makeText(this@SettingsActivity, "Enable ClearKeys in Accessibility settings first", Toast.LENGTH_LONG).show()
                        startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
                    }
                }
            }
        }
        layout.addView(panicBtn)

        layout.addView(Button(this).apply {
            text = "ENABLE ACCESSIBILITY SERVICE"; textSize = 14f
            setTextColor(0xFFE04F16.toInt()); setBackgroundColor(0xFF1E1E1E.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, 100
            ).apply { topMargin = 8 }
            setOnClickListener { startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }
        })

        layout.addView(TextView(this).apply {
            text = "ClearKeys © 2026\nMessages appear innocent to any scanner"
            textSize = 10f; setTextColor(0xFF404040.toInt())
            gravity = android.view.Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = 32 }
        })

        setContentView(layout)
        loadDictionary()
    }

    override fun onResume() {
        super.onResume()
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabled = imm.enabledInputMethodList.any { it.packageName == packageName }
        statusText.text = if (enabled) "Status: ✓ Enabled" else "Status: ✗ Not enabled"
        statusText.setTextColor(if (enabled) 0xFF4CAF50.toInt() else 0xFFE04F16.toInt())
    }

    private fun loadDictionary() {
        currentDictionary.clear()
        val saved = dictionaryManager.loadDictionary()
        if (saved.isEmpty()) {
            currentDictionary.putAll(dictionaryManager.buildDefaultDictionary())
            dictionaryManager.saveDictionary(currentDictionary)
        } else {
            currentDictionary.putAll(saved)
        }
        dictionarySizeText.text = "Dictionary: ${currentDictionary.size} word pairs loaded"
    }

    private fun generateQRCode() {
        try {
            val matrix = dictionaryManager.generateQRCode(currentDictionary, 600)
            val bitmap = Bitmap.createBitmap(600, 600, Bitmap.Config.RGB_565)
            for (x in 0 until 600) for (y in 0 until 600)
                bitmap.setPixel(x, y, if (matrix[x, y]) 0xFF000000.toInt() else 0xFFFFFFFF.toInt())
            qrImageView.setImageBitmap(bitmap)
            qrImageView.visibility = android.view.View.VISIBLE
            Toast.makeText(this, "QR code generated", Toast.LENGTH_LONG).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun scanQRCode() {
        val input = EditText(this).apply {
            hint = "Paste QR code content"; setTextColor(0xFFFFFFFF.toInt())
            setHintTextColor(0xFF606060.toInt()); setBackgroundColor(0xFF1E1E1E.toInt())
            setPadding(16, 16, 16, 16)
        }
        AlertDialog.Builder(this)
            .setTitle("Import Dictionary")
            .setView(input)
            .setPositiveButton("Import") { _, _ ->
                val dict = dictionaryManager.decodeQRCode(input.text.toString())
                if (dict != null) {
                    currentDictionary.clear(); currentDictionary.putAll(dict)
                    dictionaryManager.saveDictionary(currentDictionary); loadDictionary()
                    Toast.makeText(this, "Imported: ${dict.size} words", Toast.LENGTH_SHORT).show()
                } else Toast.makeText(this, "Invalid QR content", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null).show()
    }

    private fun isAccessibilityServiceEnabled(): Boolean {
        val enabledServices = Settings.Secure.getString(
            contentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES
        ) ?: return false
        return enabledServices.contains("$packageName/.PanicService")
    }
}
