package com.barzan.keyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.Gravity
import android.widget.*
import android.graphics.Color

class SettingsActivity : Activity() {
 override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState)
 val prefs=getSharedPreferences("barzan",MODE_PRIVATE)
 val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(32,48,32,20); setBackgroundColor(Color.rgb(245,247,248)) }
 fun label(t:String)=TextView(this).apply { text=t; textSize=19f; setTextColor(Color.DKGRAY); gravity=Gravity.CENTER; setPadding(8,14,8,14) }
 root.addView(label("بەرزان کیبۆرد ☀️"))
 root.addView(Button(this).apply { text="١. چالاککردنی کیبۆرد"; setOnClickListener { startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS)) } })
 root.addView(Button(this).apply { text="٢. هەڵبژاردنی بەرزان کیبۆرد"; setOnClickListener { (getSystemService(INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager).showInputMethodPicker() } })
 val dark=Switch(this).apply { text="دۆخی تاریک"; isChecked=prefs.getBoolean("dark",true); setOnCheckedChangeListener { _,v->prefs.edit().putBoolean("dark",v).apply() } }; root.addView(dark)
 root.addView(label("زمانی وەرگێڕان"))
 val languages=arrayOf("English","العربية","Türkçe","فارسی","Français","Deutsch")
 val codes=arrayOf("en","ar","tr","fa","fr","de")
 val spinner=Spinner(this).apply { adapter=ArrayAdapter(this@SettingsActivity,android.R.layout.simple_spinner_dropdown_item,languages); setSelection(codes.indexOf(prefs.getString("lang","en")).coerceAtLeast(0)); onItemSelectedListener=object:android.widget.AdapterView.OnItemSelectedListener { override fun onNothingSelected(parent:android.widget.AdapterView<*>?) {} ; override fun onItemSelected(parent:android.widget.AdapterView<*>?,view:android.view.View?,position:Int,id:Long) { prefs.edit().putString("lang",codes[position]).apply() } } }; root.addView(spinner)
 root.addView(label("ناونیشانی HTTPS API بۆ AI و وەرگێڕان (تەنها دەقێک کە خۆت داوای بکەیت دەنێردرێت)"))
 val endpoint=EditText(this).apply { hint="https://your-server.example/api/keyboard"; setSingleLine(true); setText(prefs.getString("endpoint","")) }; root.addView(endpoint)
 root.addView(Button(this).apply { text="پاشەکەوتکردنی API"; setOnClickListener { val url=endpoint.text.toString().trim(); if(url.isNotEmpty() && !url.startsWith("https://")) Toast.makeText(this@SettingsActivity,"تەنها HTTPS ڕێگەپێدراوە",Toast.LENGTH_LONG).show() else { prefs.edit().putString("endpoint",url).apply(); Toast.makeText(this@SettingsActivity,"پاشەکەوت کرا",Toast.LENGTH_SHORT).show() } } })
 root.addView(label("AI پێویستی بە سێرڤەری تایبەت هەیە. دەقی پاسۆرد نەنێردرێت."))
 setContentView(ScrollView(this).apply { addView(root) })
 }
}
