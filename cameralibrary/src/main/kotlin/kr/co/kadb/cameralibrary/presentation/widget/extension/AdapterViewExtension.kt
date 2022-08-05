@file:Suppress("unused")

package kr.co.kadb.cameralibrary.presentation.widget.extension

import android.view.View
import android.widget.AdapterView

/**
 * Created by oooobang on 2018. 4. 4..
 */
fun AdapterView<*>.setOnItemSelectedListener(
		nothingSelected: ((AdapterView<*>?) -> Unit)? = null,
		itemSelected: ((AdapterView<*>?, View?, Int, Long) -> Unit)? = null) {
	onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
		override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
			itemSelected?.invoke(parent, view, position, id)
		}
		override fun onNothingSelected(parent: AdapterView<*>?) {
			nothingSelected?.invoke(parent)
		}
	}
}