package com.ai.blackjackanalyzer

import android.graphics.Bitmap
import android.util.Log
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

class CardRecognizer(private val onCardDetected: (List<String>) -> Unit) {

    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    fun recognizeCardsFromBitmap(bitmap: Bitmap) {
        val image = InputImage.fromBitmap(bitmap, 0)
        
        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                val detectedCards = mutableListOf<String>()
                
                // စာသားများထဲမှ ဖဲချပ်အမှတ်အသားများ (A, K, Q, J, 2-10) ကို ရှာဖွေခြင်း
                for (block in visionText.textBlocks) {
                    val lineText = block.text.trim().uppercase()
                    
                    // ဖဲချပ်တန်ဖိုးများ ဖြစ်နိုင်ချေရှိသည်များကို စစ်ဆေးခြင်း
                    if (isValidCard(lineText)) {
                        detectedCards.add(lineText)
                    }
                }
                
                if (detectedCards.isNotEmpty()) {
                    onCardDetected(detectedCards)
                }
            }
            .addOnFailureListener { e ->
                Log.e("CardRecognizer", "OCR Failed: ${e.message}")
            }
    }

    private fun isValidCard(text: String): Boolean {
        val validValues = listOf("A", "2", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K")
        return validValues.contains(text) || text.contains("10")
    }
}
