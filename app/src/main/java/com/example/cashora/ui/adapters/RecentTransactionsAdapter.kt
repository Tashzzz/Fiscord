package com.example.cashora.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.cashora.databinding.ItemRecentTransactionBinding
import com.example.cashora.data.entity.Transaction
import java.text.SimpleDateFormat
import java.util.Locale

class RecentTransactionsAdapter(
    private val transactions: List<Transaction>,
    private val currency: String
) : RecyclerView.Adapter<RecentTransactionsAdapter.RecentTransactionViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecentTransactionViewHolder {
        val binding = ItemRecentTransactionBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return RecentTransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecentTransactionViewHolder, position: Int) {
        holder.bind(transactions[position])
    }

    override fun getItemCount(): Int = transactions.size

    inner class RecentTransactionViewHolder(private val binding: ItemRecentTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private val dateFormatter = SimpleDateFormat("dd MMM", Locale.getDefault())

        fun bind(transaction: Transaction) {
            binding.tvTitle.text = transaction.description
            binding.tvCategory.text = transaction.category
            binding.tvDate.text = dateFormatter.format(transaction.date)

            val amountText = if (transaction.type == "EXPENSE") {
                "- $currency ${transaction.amount}"
            } else {
                "+ $currency ${transaction.amount}"
            }

            binding.tvAmount.text = amountText
            binding.tvAmount.setTextColor(
                if (transaction.type == "EXPENSE") {
                    binding.root.context.getColor(android.R.color.holo_red_dark)
                } else {
                    binding.root.context.getColor(android.R.color.holo_green_dark)
                }
            )
        }
    }
}