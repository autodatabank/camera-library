//package kr.co.kadb.cameralibrary.presentation.ui.shoot.many
//
//import androidx.databinding.DataBindingComponent
//import androidx.databinding.DataBindingUtil
//import android.view.LayoutInflater
//import android.view.ViewGroup
//import androidx.recyclerview.widget.DiffUtil
//import kr.co.kadb.cameralibrary.R
//import kr.co.kadb.cameralibrary.databinding.ViewShootingListItemBinding
//import kr.co.kadb.cameralibrary.vendor.aosp.AppExecutors
//import kr.co.kadb.cameralibrary.vendor.aosp.ChoiceModeDataBoundListAdapter
//
///**
// * Created by oooobang on 2020. 2. 3..
// * Adapter.
// */
//internal class ShootingManyAdapter(
//		appExecutors: AppExecutors,
//		private val dataBindingComponent: DataBindingComponent,
//		private var zoomAction: ((String) -> Unit)? = null
//) : ChoiceModeDataBoundListAdapter<String, ViewShootingListItemBinding>(
//		appExecutors = appExecutors,
//		diffCallback = object : DiffUtil.ItemCallback<String>() {
//			override fun areItemsTheSame(oldItem: String, newItem: String): Boolean {
//				return oldItem == newItem
//			}
//
//			override fun areContentsTheSame(oldItem: String, newItem: String): Boolean {
//				return oldItem == newItem
//			}
//		},
//		choiceMode = ChoiceMode.MULTIPLE) {
//	// 선택 Items.
//	var selectedItems: MutableList<String> = mutableListOf()
//
//	override fun createBinding(parent: ViewGroup, viewType: Int): ViewShootingListItemBinding {
//		return DataBindingUtil
//				.inflate(LayoutInflater.from(parent.context),
//						R.layout.view_shooting_list_item,
//						parent,
//						false,
//						dataBindingComponent)
//	}
//
//	override fun bind(binding: ViewShootingListItemBinding, item: String, position: Int) {
//		binding.item = item
//		binding.sequence = selectedItems.indexOf(item) + 1
//		binding.isSelected = selectedItems.contains(item)
//
//		// Row 선택.
//		binding.root.setOnClickListener {
//			if (selectedItems.contains(item)) {
//				selectedItems.remove(item)
//			} else {
//				selectedItems.add(item)
//			}
//			notifyDataSetChanged()
//		}
//		// 확대.
//		binding.buttonZoom.setOnClickListener {
//			zoomAction?.invoke(item)
//		}
//	}
//
//	override fun submitList(update: List<String>?) {
//		super.submitList(update)
//
//		// 기본 전체 선택.
//		update?.let {
//			selectedItems = it.toMutableList()
//			notifyDataSetChanged()
//		}
//	}
//
//	fun setZoomClickAction(action: (String) -> Unit) {
//		this.zoomAction = action
//	}
//}
