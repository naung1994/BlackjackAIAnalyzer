package com.example.blackjackai

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.net.Uri
import android.view.Gravity
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.ComponentActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : ComponentActivity() {
    private lateinit var playerInput: EditText
    private lateinit var dealerInput: EditText
    private lateinit var result: TextView
    private lateinit var history: TextView
    private val prefs by lazy { getSharedPreferences("blackjack_history", MODE_PRIVATE) }

    private val picker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) readScreenshot(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        buildScreen()
        showHistory()
    }

    private fun buildScreen() {
        val bg = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(22, 22, 22, 22)
            setBackgroundColor(Color.rgb(9, 45, 34))
        }
        val scroll = ScrollView(this)
        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, 0, 0, 24)
        }
        fun label(text: String, size: Float = 16f) = TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(Color.WHITE)
            setPadding(0, 12, 0, 8)
        }
        fun edit(hint: String) = EditText(this).apply {
            this.hint = hint
            textSize = 17f
            setSingleLine(true)
            setTextColor(Color.BLACK)
            setHintTextColor(Color.DKGRAY)
            setBackgroundColor(Color.WHITE)
            setPadding(12, 8, 12, 8)
        }
        fun button(text: String, action: () -> Unit) = Button(this).apply {
            this.text = text
            setOnClickListener { action() }
        }

        content.addView(label("♠ BLACKJACK AI ANALYZER", 23f))
        content.addView(label("ရွေးထားသော Screenshot မှ ကတ်စာသားကို ဖတ်ပါ"))
        content.addView(button("📷 Screenshot ရွေးပြီး OCR ဖတ်ရန်") { picker.launch("image/*") })
        content.addView(label("Player ကတ်များ (ဥပမာ A,7)"))
        playerInput = edit("A,7")
        content.addView(playerInput)
        content.addView(label("Dealer ကတ်များ (ဥပမာ 6)"))
        dealerInput = edit("6")
        content.addView(dealerInput)
        content.addView(button("တွက်ချက်ပြီး အကြံပြုရန်") { analyzeAndSave() })
        result = label("ရလဒ်ကို ဒီနေရာမှာ ပြပါမယ်", 18f)
        content.addView(result)
        content.addView(button("မှတ်တမ်းရှင်းရန်") {
            prefs.edit().remove("items").apply()
            showHistory()
        })
        content.addView(label("မှတ်တမ်း", 20f))
        history = label("")
        content.addView(history)
        content.addView(label("မှတ်ချက်: OCR ဖတ်сэн кတ်များကို Player/Dealer အကွက်ထဲ သင်ကိုယ်တိုင် ခွဲစစ်ပါ။", 13f))
        scroll.addView(content)
        bg.addView(scroll)
        setContentView(bg)
    }

    private fun readScreenshot(uri: Uri) {
        try {
            val image = InputImage.fromFilePath(this, uri)
            val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
            recognizer.process(image)
                .addOnSuccessListener { visionText ->
                    val found = extractCards(visionText.text)
                    if (found.isEmpty()) {
                        result.text = "ကတ်အမှတ် မတွေ့ပါ။ ကတ်တွေ ပိုရှင်းတဲ့ Screenshot ရွေးပါ။"
                    } else {
                        // OCR ကစားပွဲပေါ်ရှိသူကို အလိုအလျောက် မခွဲနိုင်သဖြင့် Player/Dealer အကွက်တွင် စစ်ဆေးရန် ပြထားသည်။
                        playerInput.setText(found.joinToString(","))
                        result.text = "OCR ဖတ်မိသော ကတ်များ: ${found.joinToString(", ")}\nPlayer / Dealer အကွက်များကို စစ်ပြီး ပြင်ဆင်ပါ။"
                    }
                    recognizer.close()
                }
                .addOnFailureListener {
                    result.text = "OCR အမှား: ${it.localizedMessage}"
                    recognizer.close()
                }
        } catch (e: Exception) {
            result.text = "ပုံဖွင့်မရပါ: ${e.localizedMessage}"
        }
    }

    private fun extractCards(text: String): List<String> {
        val tokens = Regex("(?i)(?<![A-Z0-9])(10|[2-9]|A|J|Q|K)(?![A-Z0-9])")
            .findAll(text.uppercase()).map { it.value }.toList()
        return tokens.distinct()
    }

    private fun parseCards(raw: String): List<String> {
        val valid = setOf("A","2","3","4","5","6","7","8","9","10","J","Q","K")
        return raw.uppercase().split(Regex("[,\\s]+")).map { it.trim() }.filter { it in valid }
    }

    private fun total(cards: List<String>): Int {
        var sum = cards.sumOf {
            when (it) { "A" -> 11; "K","Q","J" -> 10; else -> it.toInt() }
        }
        var aces = cards.count { it == "A" }
        while (sum > 21 && aces > 0) { sum -= 10; aces-- }
        return sum
    }

    private fun advice(player: List<String>, dealer: List<String>): String {
        if (player.isEmpty() || dealer.isEmpty()) return "Player နဲ့ Dealer ကတ်တွေ ထည့်ပါ။"
        val p = total(player)
        val d = total(dealer)
        if (p > 21) return "BUST — 21 ကျော်နေပါပြီ။"
        if (p == 21) return "STAND — 21 ရရှိထားပါတယ်။"
        if (player.size == 2 && p == 11) return "HIT — အခြေခံ strategy အရ ကတ်ထပ်ဆွဲရန်။"
        if (p >= 17) return "STAND — ရပ်ရန်။"
        if (p <= 8) return "HIT — ကတ်ထပ်ဆွဲရန်။"
        if (p in 13..16 && d in 2..6) return "STAND — Dealer ကတ်နိမ့်နေသောကြောင့် ရပ်ရန်။"
        if (p in 12..16 && d in 7..11) return "HIT — Dealer ကတ်မြင့်နေသောကြောင့် ကတ်ထပ်ဆွဲရန်။"
        if (p in 9..11) return "HIT — အခြေခံ strategy အရ ကတ်ထပ်ဆွဲရန်။"
        return "HIT — အခြေခံ strategy အကြံပြုချက်။"
    }

    private fun analyzeAndSave() {
        val player = parseCards(playerInput.text.toString())
        val dealer = parseCards(dealerInput.text.toString())
        if (player.isEmpty() || dealer.isEmpty()) {
            result.text = "Player နဲ့ Dealer ကတ်တွေကို A,7 ပုံစံနဲ့ ထည့်ပါ။"
            return
        }
        val p = total(player)
        val d = total(dealer)
        val tip = advice(player, dealer)
        val line = "Player ${player.joinToString(",")} = $p | Dealer ${dealer.joinToString(",")} = $d\n$tip"
        result.text = line
        val arr = JSONArray(prefs.getString("items", "[]"))
        arr.put(JSONObject().put("entry", line))
        prefs.edit().putString("items", arr.toString()).apply()
        showHistory()
    }

    private fun showHistory() {
        if (!::history.isInitialized) return
        val arr = JSONArray(prefs.getString("items", "[]"))
        val lines = mutableListOf<String>()
        for (i in arr.length() - 1 downTo 0) lines.add("${arr.getJSONObject(i).optString("entry")}")
        history.text = if (lines.isEmpty()) "မှတ်တမ်း မရှိသေးပါ" else lines.joinToString("\n\n")
    }
}
