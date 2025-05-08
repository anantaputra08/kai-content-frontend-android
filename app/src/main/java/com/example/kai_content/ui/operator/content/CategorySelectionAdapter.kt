package com.example.kai_content.ui.operator.content

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.recyclerview.widget.RecyclerView
import com.example.kai_content.R
import com.example.kai_content.models.content.Category

class CategorySelectionAdapter(
    private var categories: List<Category> = emptyList(),
    private var selectedCategoryIds: MutableSet<Int> = mutableSetOf()
) : RecyclerView.Adapter<CategorySelectionAdapter.CategoryViewHolder>() {

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val categoryCheckbox: CheckBox = itemView.findViewById(R.id.category_checkbox)

        fun bind(category: Category) {
            // Remove listener before updating checked state to avoid triggering it
            categoryCheckbox.setOnCheckedChangeListener(null)

            // Set the text and checked state
            categoryCheckbox.text = category.name
            categoryCheckbox.isChecked = selectedCategoryIds.contains(category.id)

            // Set listener after updating checked state
            categoryCheckbox.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    selectedCategoryIds.add(category.id)
                } else {
                    selectedCategoryIds.remove(category.id)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_content_selection, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount(): Int = categories.size

    fun updateCategories(newCategories: List<Category>) {
        categories = newCategories
        notifyDataSetChanged()
    }

    fun updateSelectedCategories(selectedCategories: List<Category>) {
        selectedCategoryIds.clear()
        selectedCategoryIds.addAll(selectedCategories.map { it.id })
        notifyDataSetChanged()
    }

    fun getSelectedCategoryIds(): List<Int> {
        return selectedCategoryIds.toList()
    }

    fun clearSelections() {
        selectedCategoryIds.clear()
        notifyDataSetChanged()
    }
}
