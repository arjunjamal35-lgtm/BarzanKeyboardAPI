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
 private var numbers=false
 private val navy=Color.rgb(9,16,37)
 private val purple=Color.rgb(91,35,202)
 private val cyan=Color.rgb(31,190,231)
 private var currentLang="ku"
 private val rows=listOf("١٢٣٤٥٦٧٨٩٠", "چپڤق فغعهخحج".replace(" ",""), "شسیبلاتنمکگ", "ئڕرێڵەوزۆد")
 private val emojis=listOf("😀","😂","😍","😊","🥰","❤️","💚","👍","👏","🔥","🌹","🎉","☀️","🌍","😎","🙏","😭","😅","😉","😘","🤔","💜","✨","🎂","😇","😴","🤩","🥳","😃","😜","💯","🙏🏻")
 private val latinRows=listOf("qwertyuiop","asdfghjkl","zxcvbnm")
 private val arabicRows=listOf("ضصثقفغعهخحج","شسيبلاتنمكط","ئءؤرلاىةوزظ")
 private val persianRows=listOf("ضصثقفغعهخحج","شسیبلاتنمکگ","ظطزرذدوپچژ")
 private fun prefs()=getSharedPreferences("barzan",MODE_PRIVATE)
 private fun bg(color:Int,r:Float=9f)=GradientDrawable().apply { setColor(color); cornerRadius=r*resources.displayMetrics.density }
 override fun onCreateInputView():View { root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }; render(); return root }
 private fun addKey(line:LinearLayout,text:String,weight:Float=1f,special:Boolean=false,action:()->Unit) {
  val dark=prefs().getBoolean("dark",true)
  val density=resources.displayMetrics.density
  val button=TextView(this).apply {
   this.text=text; textSize=if(text.length>9) 12f else if(text.length>3) 16f else 22f
   gravity=Gravity.CENTER; setTextColor(if(dark) Color.WHITE else navy)
   background=GradientDrawable(GradientDrawable.Orientation.TL_BR,
    if(special) intArrayOf(purple,Color.rgb(58,25,149))
    else if(dark) intArrayOf(Color.rgb(37,53,88),Color.rgb(25,36,66))
    else intArrayOf(Color.WHITE,Color.rgb(224,231,251))).apply {
      cornerRadius=13*density;setStroke((1*density).toInt(),if(special) Color.rgb(131,83,251) else if(dark) Color.rgb(61,77,118) else Color.rgb(180,192,225))
    }
   setOnClickListener { action() }
  }
  val params=LinearLayout.LayoutParams(0,(51*density).toInt(),weight).apply {setMargins((3*density).toInt(),(4*density).toInt(),(3*density).toInt(),(4*density).toInt())}
  line.addView(button,params)
 }
 private fun line():LinearLayout=LinearLayout(this).apply { orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER }
 private fun render() {
  val dark=prefs().getBoolean("dark",true)
  currentLang=prefs().getString("keyboard_lang","ku") ?: "ku"
  root.removeAllViews();root.setPadding(5,8,5,10)
  root.setBackgroundColor(if(dark) navy else Color.rgb(221,230,250))
  val toolbar=line();root.addView(toolbar)
  addKey(toolbar,"☻",0.8f){emoji=!emoji;numbers=false;render()}
  addKey(toolbar,"GIF",0.8f){Toast.makeText(this,"GIF لە وەشانی داهاتوودا",Toast.LENGTH_SHORT).show()}
  addKey(toolbar,"▤",0.8f){ val clip=(getSystemService(CLIPBOARD_SERVICE) as android.content.ClipboardManager).primaryClip;val t=clip?.getItemAt(0)?.coerceToText(this)?.toString();if(!t.isNullOrEmpty())commit(t) }
  addKey(toolbar,"文",0.8f){ai("translate")}
  addKey(toolbar,"✓",0.8f){ai("correct")}
  addKey(toolbar,"⚙",0.8f){val i=android.content.Intent(this,SettingsActivity::class.java);i.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK);startActivity(i)}

  if(emoji){emojis.chunked(8).forEach { chars->val r=line();root.addView(r);chars.forEach { c->addKey(r,c){commit(c)} } }}
  else {
   val activeRows=when {numbers->listOf("1234567890","@#&-_+()/","*\"':;!?%");currentLang in listOf("en","tr","de","fr","es")->latinRows;currentLang=="ar"->arabicRows;currentLang=="fa"->persianRows;else->rows}
   activeRows.forEachIndexed { index,letters->
    val r=line();root.addView(r)
    letters.forEach { c->addKey(r,c.toString()){commit(c.toString())} }
    if(index==activeRows.lastIndex) addKey(r,"⌫",1.25f){currentInputConnection?.deleteSurroundingText(1,0)}
   }
  }
  val bottom=line();root.addView(bottom)
  addKey(bottom,if(numbers)"ABC" else "?123",1.15f,true){numbers=!numbers;emoji=false;render()}
  addKey(bottom,"☻",0.8f){emoji=!emoji;numbers=false;render()}
  addKey(bottom,"🌐",0.8f){val langs=listOf("ku","ckb-bhd","ar","en","tr","de","fr","ru","es","zh");val enabled=langs.filter { prefs().getBoolean("enabled_"+it,true) };val cycle=if(enabled.isEmpty()) listOf("ku") else enabled;currentLang=cycle[(cycle.indexOf(currentLang).coerceAtLeast(0)+1)%cycle.size];prefs().edit().putString("keyboard_lang",currentLang).apply();emoji=false;numbers=false;render()}
  addKey(bottom,when(currentLang){"ku"->"کوردی (سۆرانی)";"ckb-bhd"->"کوردی (بەهدینی)";"ar"->"عەرەبی";"en"->"English";"tr"->"Türkçe";"de"->"Deutsch";"fr"->"Français";"ru"->"Русский";"es"->"Español";else->"Space"},3.8f){commit(" ")}
  addKey(bottom,"↵",1.1f,true){enterKey()}
 }
 private fun enterKey(){
  val action=currentInputEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
  if(action!=EditorInfo.IME_ACTION_NONE && action!=EditorInfo.IME_ACTION_UNSPECIFIED && action!=EditorInfo.IME_ACTION_DONE){currentInputConnection?.performEditorAction(action)}
  else if(action==EditorInfo.IME_ACTION_DONE){currentInputConnection?.performEditorAction(action)}
  else commit("\n")
 }
 private fun commit(s:String){currentInputConnection?.commitText(s,1)}
 private fun ai(mode:String){
 val info=currentInputEditorInfo ?: return
 if(info.inputType and android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD == android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD && info.inputType and android.text.InputType.TYPE_MASK_VARIATION != 0) { Toast.makeText(this,"لە خانەی پاسۆرد AI ناچالاکە",Toast.LENGTH_SHORT).show();return }
 val endpoint=prefs().getString("endpoint","https://barzankeyboardapi-psi.vercel.app/api/keyboard").orEmpty()
 if(!endpoint.startsWith("https://")){ Toast.makeText(this,"لە ڕێکخستنەکاندا HTTPS API دابنێ",Toast.LENGTH_LONG).show();return }
 val connection=currentInputConnection ?:return
 val selected=connection.getSelectedText(0)?.toString().orEmpty()
 if(selected.isBlank()){Toast.makeText(this,"سەرەتا دەقەکە دیاری بکە",Toast.LENGTH_LONG).show();return}
 val lang=prefs().getString("lang","ku") ?: "ku"
 Toast.makeText(this,"چاوەڕوان بە...",Toast.LENGTH_SHORT).show()
 thread {
 try {
 val http=URL(endpoint).openConnection() as HttpsURLConnection
 http.requestMethod="POST";http.connectTimeout=12000;http.readTimeout=20000;http.doOutput=true;http.setRequestProperty("Content-Type","application/json; charset=utf-8")
 val payload=JSONObject().put("action",mode).put("text",selected).put("targetLanguage",lang).toString()
 http.outputStream.use { it.write(payload.toByteArray(Charsets.UTF_8)) }
 val status=http.responseCode
 val response=(if(status in 200..299) http.inputStream else http.errorStream)?.bufferedReader()?.use { it.readText() }.orEmpty()
 val json=runCatching { JSONObject(response) }.getOrNull()
 val result=if(status in 200..299) listOf("result","translation","correctedText","text","output").mapNotNull { json?.optString(it)?.takeIf(String::isNotBlank) }.firstOrNull().orEmpty() else ""
 http.disconnect()
 android.os.Handler(mainLooper).post { if(result.isNotBlank()) { val current= currentInputConnection?.getSelectedText(0)?.toString(); if(current==selected) currentInputConnection?.commitText(result,1) else Toast.makeText(this,"دەقەکە گۆڕاوە؛ دووبارە هەوڵ بدە",Toast.LENGTH_LONG).show() } else Toast.makeText(this,if(status in 200..299) "وەڵامی API دەقی ئەنجامی تێدا نییە" else "هەڵەی API: HTTP $status",Toast.LENGTH_LONG).show() }
 }catch(e:Exception){android.os.Handler(mainLooper).post { Toast.makeText(this,"پەیوەندی سێرڤەر: ${e.javaClass.simpleName}",Toast.LENGTH_LONG).show() } }
 }
 }
}
