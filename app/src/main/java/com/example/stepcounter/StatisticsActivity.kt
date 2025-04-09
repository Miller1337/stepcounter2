package com.example.stepcounter
import android.graphics.Color
import android.content.Context
import android.os.Bundle
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class StatisticsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_statistics)
        populateStatisticsTable()
    }

    private fun populateStatisticsTable() {
        val tableLayout = findViewById<TableLayout>(R.id.tableStatistics)
        tableLayout.removeAllViews()


        val headerRow = TableRow(this)
        val headers = listOf("Дата", "Шаги", "Км", "Ккал")
        for (header in headers) {
            val tvHeader = TextView(this).apply {
                text = header
                setPadding(16, 8, 16, 8)
                setTextColor(Color.BLACK)
            }
            headerRow.addView(tvHeader)
        }
        tableLayout.addView(headerRow)



        val statisticsPref = getSharedPreferences("StatisticsPrefs", Context.MODE_PRIVATE)
        val dataString = statisticsPref.getString("data", "") ?: ""

        if (dataString.isNotEmpty()) {
            val lines = dataString.split("\n")
            for (line in lines) {

                val columns = line.split(",").take(4)
                if (columns.size >= 4) {
                    val row = TableRow(this)
                    for (value in columns) {
                        val tv = TextView(this).apply {
                            text = value.trim()
                            setPadding(16, 8, 16, 8)
                            setTextColor(Color.BLACK)

                        }
                        row.addView(tv)
                    }
                    tableLayout.addView(row)
                }
            }
        }
    }


}
