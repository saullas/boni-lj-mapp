package com.example.boniljmap.utils

import android.content.Context
import android.graphics.Typeface
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class MyTextViewRegular(context: Context, attributeSet: AttributeSet) : AppCompatTextView(context, attributeSet) {
	init {
		typeface = Typeface.createFromAsset(context.assets, "Montserrat-Regular.ttf")
	}
}