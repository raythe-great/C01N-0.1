package com.c01n.app

import android.app.Activity
import android.app.AlertDialog
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private const val GOLD = 0xFFD4AF37.toInt()
private const val SILVER = 0xFFC0C0C0.toInt()
private const val DARK = 0xFF111318.toInt()
private const val CARD = 0xFF1D2026.toInt()
private const val WHITE = 0xFFF5F5F5.toInt()
private const val MUTED = 0xFFB0B3B8.toInt()
private const val GREEN = 0xFF4CAF50.toInt()
private const val RED = 0xFFEF5350.toInt()

data class Transaction(
    val id: Long,
    val title: String,
    val amount: Long,
    val income: Boolean,
    val date: Long
)

class MainActivity : Activity() {

    private val transactions = mutableListOf<Transaction>()
    private lateinit var content: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = DARK
        window.navigationBarColor = DARK

        loadTransactions()
        showHome()
    }

    private fun showHome() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(DARK)
            setPadding(dp(20), dp(18), dp(20), dp(12))
        }

        val scroll = ScrollView(this)
        content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        scroll.addView(content)
        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        val logo = TextView(this).apply {
            text = "C01N"
            textSize = 32f
            typeface = Typeface.DEFAULT_BOLD
        }

        val logoText = android.text.SpannableString("C01N")
        logoText.setSpan(
            android.text.style.ForegroundColorSpan(GOLD),
            0, 1,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        logoText.setSpan(
            android.text.style.ForegroundColorSpan(SILVER),
            1, 4,
            android.text.Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )
        logo.text = logoText
        content.addView(logo)

        content.addView(label("Keuangan pribadi", MUTED, 14f))
        content.addView(space(20))

        val summary = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            setBackgroundColor(CARD)
        }

        summary.addView(label("TOTAL SALDO", MUTED, 13f))
        summary.addView(label(
            rupiah(balance()),
            GOLD,
            28f,
            true
        ))
        summary.addView(space(18))

        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
        }

        val incomeColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        incomeColumn.addView(label("Pemasukan", MUTED, 13f))
        incomeColumn.addView(label(rupiah(totalIncome()), GREEN, 17f, true))

        val expenseColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }
        expenseColumn.addView(label("Pengeluaran", MUTED, 13f))
        expenseColumn.addView(label(rupiah(totalExpense()), RED, 17f, true))

        row.addView(incomeColumn, LinearLayout.LayoutParams(0, -2, 1f))
        row.addView(expenseColumn, LinearLayout.LayoutParams(0, -2, 1f))
        summary.addView(row)

        content.addView(summary)
        content.addView(space(24))
        content.addView(label("Riwayat Transaksi", WHITE, 20f, true))
        content.addView(space(12))

        if (transactions.isEmpty()) {
            content.addView(
                label("Belum ada transaksi. Tambahkan transaksi pertamamu.",
                    MUTED, 14f)
            )
        } else {
            transactions.sortedByDescending { it.date }.forEach {
                content.addView(transactionRow(it))
                content.addView(space(8))
            }
        }

        val addButton = Button(this).apply {
            text = "+ Tambah Transaksi"
            textSize = 16f
            setTextColor(DARK)
            backgroundTintList = android.content.res.ColorStateList.valueOf(GOLD)
            setOnClickListener { showAddDialog() }
        }

        root.addView(
            addButton,
            LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                dp(54)
            )
        )

        setContentView(root)
    }

    private fun transactionRow(item: Transaction): View {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14), dp(12), dp(10), dp(12))
            setBackgroundColor(CARD)
        }

        val details = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
        }

        details.addView(label(item.title, WHITE, 15f, true))

        val date = SimpleDateFormat(
            "dd MMM yyyy, HH:mm",
            Locale("id", "ID")
        ).format(Date(item.date))

        details.addView(label(date, MUTED, 12f))

        val amountColumn = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.END
        }

        val sign = if (item.income) "+" else "−"
        val color = if (item.income) GREEN else RED

        amountColumn.addView(
            label("$sign${rupiah(item.amount)}", color, 14f, true)
        )

        val delete = TextView(this).apply {
            text = "Hapus"
            textSize = 12f
            setTextColor(MUTED)
            gravity = Gravity.END
            setPadding(0, dp(6), 0, 0)
            setOnClickListener {
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Hapus transaksi?")
                    .setMessage(item.title)
                    .setNegativeButton("Batal", null)
                    .setPositiveButton("Hapus") { _, _ ->
                        transactions.removeAll { it.id == item.id }
                        saveTransactions()
                        showHome()
                    }
                    .show()
            }
        }

        amountColumn.addView(delete)

        row.addView(
            details,
            LinearLayout.LayoutParams(0, -2, 1f)
        )
        row.addView(amountColumn)

        return row
    }

    private fun showAddDialog() {
        val form = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(8), dp(20), 0)
        }

        val type = RadioGroup(this).apply {
            orientation = RadioGroup.HORIZONTAL
        }

        val incomeRadio = RadioButton(this).apply {
            text = "Pemasukan"
            id = View.generateViewId()
        }

        val expenseRadio = RadioButton(this).apply {
            text = "Pengeluaran"
            id = View.generateViewId()
        }

        type.addView(incomeRadio)
        type.addView(expenseRadio)
        type.check(incomeRadio.id)

        val titleInput = EditText(this).apply {
            hint = "Nama transaksi"
            setSingleLine(true)
        }

        val amountInput = EditText(this).apply {
            hint = "Nominal (Rp)"
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
            setSingleLine(true)
        }

        form.addView(type)
        form.addView(titleInput)
        form.addView(amountInput)

        val dialog = AlertDialog.Builder(this)
            .setTitle("Tambah Transaksi")
            .setView(form)
            .setNegativeButton("Batal", null)
            .setPositiveButton("Simpan", null)
            .create()

        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener {
                    val title = titleInput.text.toString().trim()
                    val amount = amountInput.text.toString().toLongOrNull()

                    if (title.isBlank()) {
                        titleInput.error = "Nama transaksi wajib diisi"
                        return@setOnClickListener
                    }

                    if (amount == null || amount <= 0) {
                        amountInput.error = "Nominal harus lebih dari 0"
                        return@setOnClickListener
                    }

                    transactions.add(
                        Transaction(
                            id = System.currentTimeMillis(),
                            title = title,
                            amount = amount,
                            income = type.checkedRadioButtonId == incomeRadio.id,
                            date = System.currentTimeMillis()
                        )
                    )

                    saveTransactions()
                    dialog.dismiss()
                    showHome()
                }
        }

        dialog.show()
    }

    private fun label(
        text: String,
        color: Int,
        size: Float,
        bold: Boolean = false
    ): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = size
            setTextColor(color)
            if (bold) typeface = Typeface.DEFAULT_BOLD
        }
    }

    private fun space(height: Int): View {
        return View(this).apply {
            layoutParams = LinearLayout.LayoutParams(1, dp(height))
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun totalIncome(): Long =
        transactions.filter { it.income }.sumOf { it.amount }

    private fun totalExpense(): Long =
        transactions.filter { !it.income }.sumOf { it.amount }

    private fun balance(): Long = totalIncome() - totalExpense()

    private fun rupiah(value: Long): String {
        return "Rp " + NumberFormat
            .getNumberInstance(Locale("id", "ID"))
            .format(value)
    }

    private fun loadTransactions() {
        val json = getSharedPreferences("c01n", MODE_PRIVATE)
            .getString("transactions", "[]") ?: "[]"

        try {
            val array = JSONArray(json)

            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                transactions.add(
                    Transaction(
                        id = obj.getLong("id"),
                        title = obj.getString("title"),
                        amount = obj.getLong("amount"),
                        income = obj.getBoolean("income"),
                        date = obj.getLong("date")
                    )
                )
            }
        } catch (_: Exception) {
            transactions.clear()
        }
    }

    private fun saveTransactions() {
        val array = JSONArray()

        transactions.forEach { item ->
            array.put(
                JSONObject().apply {
                    put("id", item.id)
                    put("title", item.title)
                    put("amount", item.amount)
                    put("income", item.income)
                    put("date", item.date)
                }
            )
        }

        getSharedPreferences("c01n", MODE_PRIVATE)
            .edit()
            .putString("transactions", array.toString())
            .apply()
    }
}
