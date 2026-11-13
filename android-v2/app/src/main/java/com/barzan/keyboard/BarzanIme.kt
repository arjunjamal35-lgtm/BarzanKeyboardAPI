package com.barzan.keyboard

import android.inputmethodservice.InputMethodService
import android.view.View
import android.view.inputmethod.EditorInfo
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.widget.*
import android.view.Gravity
import org.json.JSONObject
import java.net.URL
import javax.net.ssl.HttpsURLConnection
import kotlin.concurrent.thread

class BarzanIme : InputMethodService() {
 private lateinit var root:LinearLayout
 private var emoji=false
 private val rows=listOf("١٢٣٤٥٦٧٨٩٠", "چپڤق فغعهخحج".replace(" ",""), "شسیبلاتنمکگ", "ئڕرێڵەوزۆد")
 private val emojis=listOf("😀😂😍😊🥰❤️💚👍👏🔥🌹🎉☀️🌍😎🙏")
 private fun prefs()=getSharedPreferences("barzan",MODE_PRIVATE)
 private fun bg(color:Int,r:Float=9f)=GradientDrawable().apply { setColor(color); cornerRadius=r*resources.displayMetrics.density }
 override fun onCreateInputView():View { root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }; render(); return root }
 private fun addKey(line:LinearLayout,text:String,weight:Float=1f,action:()->Unit) {
 val dark=prefs().getBoolean("dark",true)
 val button=TextView(this).apply { this.text=text; textSize=22f; gravity=Gravity.CENTER; setTextColor(if(dark) Color.WHITE else Color.BLACK); background=bg(if(dark) Color.rgb(55,58,62) else Color.WHITE); setOnClickListener { action() } }
 val p=LinearLayout.LayoutParams(0,(49*resources.displayMetrics.density).toInt(),weight).apply { setMargins(2,3,2,3) }; line.addView(button,p)
 }
 private fun line():LinearLayout=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER }
 private fun render() {
 val dark=prefs().getBoolean("dark",true); root.removeAllViews(); root.setPadding(4,6,4,8); root.setBackgroundColor(if(dark) Color.rgb(28,29,31) else Color.rgb(223,227,231))
 val toolbar=line(); root.addView(toolbar)
 addKey(toolbar,"✨ چاککردن",2f){ ai("correct") }; addKey(toolbar,"🌐 وەرگێڕان",2f){ ai("translate") }; addKey(toolbar,"😊"){ emoji=!emoji;render() }; addKey(toolbar,"☀️"){ prefs().edit().putBoolean("dark",!dark).apply();render() }
 if(emoji) { emojis.chunked(8).forEach { chars->val row=line();root.addView(row);chars.forEach { c->addKey(row,c){commit(c)} } } }
 else { rows.forEach { letters->val row=line();root.addView(row);letters.forEach { c->addKey(row,c.toString()){commit(c.toString())} } } }
 val bottom=line();root.addView(bottom)
 addKey(bottom,"؟٣٢١",1.2f){ emoji=false; showNumbers() };addKey(bottom,",",0.6f){commit("،")};addKey(bottom,"🌐",0.8f){ switchToNextInputMethod(false) };addKey(bottom,"کوردی",4f){commit(" ")};addKey(bottom,".",0.6f){commit(".")};addKey(bottom,"⌫",0.9f){ currentInputConnection?.deleteSurroundingText(1,0) };addKey(bottom,"↵",1f){currentInputConnection?.performEditorAction(EditorInfo.IME_ACTION_DONE)}
 }
 private fun showNumbers(){root.removeAllViews();val row=line();root.addView(row);"١٢٣٤٥٦٧٨٩٠".forEach { c->addKey(row,c.toString()){commit(c.toString())} };val back=line();root.addView(back);addKey(back,"کوردی"){render()};addKey(back,"⌫"){currentInputConnection?.deleteSurroundingText(1,0)} }
 private fun commit(s:String){currentInputConnection?.commitText(s,1)}
 private fun ai(mode:String){
 val info=currentInputEditorInfo ?: return
 if(info.inputType and android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD && info.inputType and android.text.InputType.TYPE_MASK_VARIATION != 0) { Toast.makeText(this,"لە خانەی پاسۆرد AI ناچالاکە",Toast.LENGTH_SHORT).show();return }
 val endpoint=prefs().getString("endpoint","").orEmpty()
 if(!endpoint.startsWith("https://")){ Toast.makeText(this,"لە ڕێکخستنەکاندا HTTPS API دابنێ",Toast.LENGTH_LONG).show();return }
 val connection=currentInputConnection ?:return
 val selected=connection.getSelectedText(0)?.toString().orEmpty()
 if(selected.isBlank()){Toast.makeText(this,"سەرەتا دەقەکە دیاری بکە",Toast.LENGTH_LONG).show();return}
 val lang=prefs().getString("lang","en") ?: "en"
 Toast.makeText(this,"چاوەڕوان بە...",Toast.LENGTH_SHORT).show()
 thread {
 try {
 val http=URL(endpoint).openConnection() as HttpsURLConnection
 http.requestMethod="POST";http.connectTimeout=12000;http.readTimeout=20000;http.doOutput=true;http.setRequestProperty("Content-Type","application/json; charset=utf-8")
 val payload=JSONObject().put("action",mode).put("text",selected).put("targetLanguage",lang).toString()
 http.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
 val result=if(http.responseCode in 200..299) JSONObject(http.inputStream.bufferedReader().use { it.readText() }).optString("result") else ""
 http.disconnect()
 android.os.Handler(mainLooper).post { if(result.isNotBlank()) { val current= currentInputConnection?.getSelectedText(0)?.toString(); if(current==selected) currentInputConnection?.commitText(result,1) else Toast.makeText(this,"دەقەکە گۆڕاوە؛ دووبارە هەوڵ بدە",Toast.LENGTH_LONG).show() } else Toast.makeText(this,"وەڵامی API دروست نییە",Toast.LENGTH_LONG).show() }
 }catch(e:Exception){android.os.Handler(mainLooper).post { Toast.makeText(this,"پەیوەندی AI سەرکەوتوو نەبوو",Toast.LENGTH_LONG).show() } }
 }
 }
}
