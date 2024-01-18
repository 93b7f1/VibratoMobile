package com.example.vibrato

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import okhttp3.*
import java.io.IOException

class Dashboard : AppCompatActivity() {

    private val TAG = "Dashboard"
    private lateinit var linearLayout: LinearLayout

    private lateinit var horizontalScrollView: HorizontalScrollView
    private lateinit var tableLayout: TableLayout
    private var id: Int = 0
    private var username: String? = null
    private var tipoUsuario: String? = null


    override fun onBackPressed() {
        super.onBackPressed()
        val intent = Intent(this, MusicianHome::class.java)
        startActivity(intent)
        finish()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)
        val sharedPreferences = getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        id = sharedPreferences.getInt("id", 0)
        username = sharedPreferences.getString("username", null)
        tipoUsuario = sharedPreferences.getString("tipoUsuario", null)

        linearLayout = findViewById(R.id.tabelas)
        horizontalScrollView = HorizontalScrollView(this)
        tableLayout = createTableLayout()
        horizontalScrollView.addView(tableLayout)
        linearLayout.addView(horizontalScrollView)

        val headerRow = createTableRow("#1D1D1D", 5.dp,)
        addTextViewToRow(headerRow, "Title","#FFFFFF")
        addTextViewToRow(headerRow, "Views","#FFFFFF")
        addTextViewToRow(headerRow, "Plays","#FFFFFF")
        addTextViewToRow(headerRow, "Redirects","#FFFFFF")
        addTextViewToRow(headerRow, "Shares","#FFFFFF")
        tableLayout.addView(headerRow)

        buscarDadosDaApiEAtualizarTabela(id)
    }

    private fun createTableLayout(): TableLayout {
        val tableLayout = TableLayout(this)
        tableLayout.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        tableLayout.setPadding(10.dp, 10.dp, 10.dp, 0.dp)
        return tableLayout
    }

    private fun createTableRow(backgroundColor: String, padding: Int): TableRow {
        val row = TableRow(this)
        val params = TableRow.LayoutParams(
            TableRow.LayoutParams.MATCH_PARENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        row.layoutParams = params
        row.setBackgroundColor(Color.parseColor(backgroundColor))
        row.setPadding(padding, padding, padding, padding)
        return row
    }


    private fun createTextView(text: String, textSize: Float, textStyle: Int, textColor: Int): TextView {
        val textView = TextView(this)
        textView.layoutParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        textView.text = text
        textView.textSize = textSize
        textView.setTextColor(textColor)
        textView.setTypeface(null, textStyle)
        return textView
    }

    private fun addTextViewToRow(row: TableRow, text: String, textColor: String) {
        val color = Color.parseColor(textColor)
        val textView = createTextView(text, 20f, 0, color)
        val marginParams = TableRow.LayoutParams(
            TableRow.LayoutParams.WRAP_CONTENT,
            TableRow.LayoutParams.WRAP_CONTENT
        )
        marginParams.setMargins(5.dp, 10.dp, 5.dp, 0)
        textView.layoutParams = marginParams

        row.addView(textView)
    }
    private val Int.dp: Int
        get() = (this * resources.displayMetrics.density).toInt()

    private fun buscarDadosDaApiEAtualizarTabela(userId: Int) {
        val client = OkHttpClient()

        val request = Request.Builder()
            .url("https://vibrato.azurewebsites.net/echo/artista/$userId")
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                try {
                    val json = response.body?.string()
                    if (!json.isNullOrBlank()) {
                        val listaEcho =
                            Gson().fromJson(json, Array<Echo>::class.java).toList()
                        processarERenderizarDados(listaEcho)
                    }
                } catch (e: IOException) {
                    Log.e(TAG, "Erro ao processar a resposta", e)
                } finally {
                    response.close()
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "Erro na chamada da API", e)
            }
        })
    }

    private fun processarERenderizarDados(listaEcho: List<Echo>) {
        runOnUiThread {

            listaEcho.forEach { echo ->
                val echoRow = createTableRow("#F0F3F5", 10.dp)
                addTextViewToRow(echoRow, echo.tituloMusica,"#000000")
                addTextViewToRow(echoRow, echo.visualizacao.toString(),"#000000")
                addTextViewToRow(echoRow, echo.streams.toString(),"#000000")
                addTextViewToRow(echoRow, echo.redirecionamento.toString(),"#000000")
                addTextViewToRow(echoRow, echo.curtidas.toString(),"#000000")
                tableLayout.addView(echoRow)
            }
        }
    }
}
