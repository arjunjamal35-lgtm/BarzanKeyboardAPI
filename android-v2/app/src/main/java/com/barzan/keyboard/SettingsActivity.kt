package com.barzan.keyboard

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import android.view.inputmethod.InputMethodManager

class SettingsActivity : Activity() {
 private val navy=Color.rgb(9,16,37)
 private val panel=Color.rgb(22,35,67)
 private val cyan=Color.rgb(31,190,231)
 private fun dp(n:Int)=(n*resources.displayMetrics.density).toInt()
 private fun shape(color:Int,stroke:Int=Color.rgb(43,64,105))=GradientDrawable().apply{setColor(color);cornerRadius=dp(15).toFloat();setStroke(dp(1),stroke)}
 private val langs=listOf(Triple("🇹🇯  کوردی (سۆرانی)","ku","Kurdish (Sorani)"),Triple("🇹🇯  کوردی (بەهدینی)","ckb-bhd","Kurdish (Bahdini)"),Triple("🇸🇦  عەرەبی","ar","Arabic"),Triple("🇺🇸  English","en","English"),Triple("🇹🇷  Türkçe","tr","Turkish"),Triple("🇩🇪  Deutsch","de","German"),Triple("🇫🇷  Français","fr","French"),Triple("🇷🇺  Русский","ru","Russian"),Triple("🇪🇸  Español","es","Spanish"),Triple("🇨🇳  中文","zh","Chinese"))
 override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);showHome()}
 private fun base(title:String):LinearLayout{
  val body=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(15),dp(16),dp(15),dp(25));setBackgroundColor(navy)}
  val heading=TextView(this).apply{text=title;textSize=23f;setTypeface(null,Typeface.BOLD);setTextColor(Color.WHITE);gravity=Gravity.CENTER;setPadding(0,dp(8),0,dp(20))}
  body.addView(heading)
  setContentView(ScrollView(this).apply{isFillViewport=true;addView(body)})
  return body
 }
 private fun text(s:String,size:Float=17f)=TextView(this).apply{text=s;textSize=size;setTextColor(Color.WHITE);gravity=Gravity.CENTER_VERTICAL or Gravity.RIGHT;setPadding(dp(13),dp(9),dp(13),dp(9))}
 private fun item(root:LinearLayout,title:String,sub:String="",action:()->Unit){
  val group=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=shape(panel);setPadding(dp(7),dp(3),dp(7),dp(3));setOnClickListener{action()}}
  group.addView(text("‹    "+title,18f));if(sub.isNotEmpty())group.addView(text(sub,12f).apply{setTextColor(0xff9fb3d7.toInt())})
  root.addView(group,LinearLayout.LayoutParams(-1,dp(if(sub.isEmpty())60 else 76)).apply{bottomMargin=dp(9)})
 }
 private fun showHome(){
  val root=base("Barzan Keyboard")
  root.addView(ImageView(this).apply{setImageResource(R.drawable.barzan_logo)},LinearLayout.LayoutParams(dp(100),dp(100)).apply{gravity=Gravity.CENTER_HORIZONTAL})
  root.addView(text("کیبۆردی بەرزان • V2 Pro",16f).apply{gravity=Gravity.CENTER;setTextColor(cyan)})
  item(root,"⌨  چالاککردنی کیبۆرد"){startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))}
  item(root,"✓  هەڵبژاردنی کیبۆرد"){(getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager).showInputMethodPicker()}
  item(root,"▦  ڕێکخستنەکانی کیبۆرد","ڕووکاری دوگمەکان و زمانەکان"){showLanguages()}
  item(root,"文  وەرگێڕان","زمانی ئامانجی وەرگێڕان"){showTranslation()}
  item(root,"✦  یاریدەدەری زیرەک (AI)","چاککردنەوەی دەق و وەرگێڕان"){showAI()}
  item(root,"🌐  زمانەکان","زیادکردن و چالاککردنی زمانەکان"){showLanguages()}
  item(root,"◉  ڕووکار (Theme)","شینی تاریک و دۆخی ڕووناک"){showTheme()}
  item(root,"☷  پێشکەوتوو","ڕێکخستنی API"){showAI()}
  item(root,"ⓘ  دەربارە","Barzan Keyboard V2 Pro • بەرزان جەمال"){AlertDialog.Builder(this).setTitle("Barzan Keyboard V2 Pro").setMessage("کیبۆردی بەرزان — وەشانی ٢").setPositiveButton("باشە",null).show()}
 }
 private fun showLanguages(){
  val root=base("‹   زمانەکان")
  val prefs=getSharedPreferences("barzan",MODE_PRIVATE)
  val all=Switch(this).apply{text="چالاککردنی هەموو زمانەکان";textSize=17f;setTextColor(Color.WHITE);isChecked=langs.all{prefs.getBoolean("enabled_"+it.second,true)}}
  root.addView(all,LinearLayout.LayoutParams(-1,dp(60)))
  val toggles=mutableListOf<Switch>()
  langs.forEach { (name,code,sub)->
   val group=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=shape(panel);setPadding(dp(12),dp(2),dp(12),dp(2))}
   val toggle=Switch(this).apply{text=name;textSize=17f;setTextColor(Color.WHITE);isChecked=prefs.getBoolean("enabled_"+code,true);setOnCheckedChangeListener{_,checked->prefs.edit().putBoolean("enabled_"+code,checked).apply()}}
   toggles.add(toggle);group.addView(toggle,LinearLayout.LayoutParams(-1,dp(48)))
   group.addView(text(sub,12f).apply{setTextColor(0xff9fb3d7.toInt())})
   root.addView(group,LinearLayout.LayoutParams(-1,dp(75)).apply{bottomMargin=dp(6)})
  }
  all.setOnCheckedChangeListener{_,checked->toggles.forEach{it.isChecked=checked}}
  item(root,"‹   گەڕانەوە"){showHome()}
 }
 private fun showTranslation(){
  val root=base("‹   وەرگێڕان")
  val prefs=getSharedPreferences("barzan",MODE_PRIVATE)
  langs.forEach{(name,code,_)->item(root,(if(prefs.getString("lang","ku")==code) "✓  " else "")+name){prefs.edit().putString("lang",code).apply();showTranslation()}}
  item(root,"‹   گەڕانەوە"){showHome()}
 }
 private fun showTheme(){
  val root=base("‹   ڕووکار")
  val prefs=getSharedPreferences("barzan",MODE_PRIVATE)
  item(root,"🌙  شینی تاریک / مۆر"){prefs.edit().putBoolean("dark",true).apply();Toast.makeText(this,"دۆخی تاریک چالاک کرا",Toast.LENGTH_SHORT).show()}
  item(root,"☀  دۆخی ڕووناک"){prefs.edit().putBoolean("dark",false).apply();Toast.makeText(this,"دۆخی ڕووناک چالاک کرا",Toast.LENGTH_SHORT).show()}
  item(root,"‹   گەڕانەوە"){showHome()}
 }
 private fun showAI(){
  val root=base("‹   یاریدەدەری زیرەک (AI)")
  val prefs=getSharedPreferences("barzan",MODE_PRIVATE)
  root.addView(text("AI تەنها دەقی دیاریکراو بە داواکاری تۆ بۆ سێرڤەر دەنێرێت.",15f))
  val edit=EditText(this).apply{setText(prefs.getString("endpoint","https://barzankeyboardapi-psi.vercel.app/api/keyboard"));setSingleLine(true);setTextColor(Color.WHITE);setHintTextColor(cyan);hint="https://.../api/keyboard";background=shape(panel);setPadding(dp(12),0,dp(12),0)}
  root.addView(edit,LinearLayout.LayoutParams(-1,dp(60)).apply{bottomMargin=dp(14)})
  item(root,"✓  پاشەکەوتکردنی API"){val url=edit.text.toString().trim();if(url.startsWith("https://")){prefs.edit().putString("endpoint",url).apply();Toast.makeText(this,"پاشەکەوت کرا",Toast.LENGTH_SHORT).show()}else Toast.makeText(this,"ناونیشانی HTTPS پێویستە",Toast.LENGTH_LONG).show()}
  item(root,"‹   گەڕانەوە"){showHome()}
 }
}
