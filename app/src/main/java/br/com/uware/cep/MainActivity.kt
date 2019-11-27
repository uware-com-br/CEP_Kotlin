package br.com.uware.cep

import android.content.Context
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import br.com.uware.cep.functions.Anime
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.view.*
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : AppCompatActivity() {
    val anime = Anime()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        etCEP.addTextChangedListener(MaskEditUtil.mask(etCEP,"#####-###"))
        btnSearch.setOnClickListener {
            if(etCEP.text.toString().length != 9){
                Toast.makeText(this,"CEP precisa ter 8 Digitos", Toast.LENGTH_LONG).show()
            }
            else{
                getCep(etCEP.text.toString())
            }
            it.hideKeyboard()
        }
    }
    fun getCep(cep: String){
        val url= "https://viacep.com.br/ws/"+cep+"/json/"
        anime.tradeView(tvResp, pbCep)
        MyAsyncTask().execute(url)
    }
    object MaskEditUtil {
        fun mask(ediTxt: EditText, mask: String): TextWatcher {
            return object : TextWatcher {
                var isUpdating: Boolean = false
                var old = ""
                override fun afterTextChanged(s: Editable) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                    val str = unmask(s.toString())
                    var mascara = ""
                    if (isUpdating) {
                        old = str
                        isUpdating = false
                        return
                    }
                    var i = 0
                    for (m in mask.toCharArray()) {
                        if (m != '#' && str.length > old.length) {
                            mascara += m
                            continue
                        }
                        try {
                            mascara += str[i]
                        } catch (e: Exception) {
                            break
                        }
                        i++
                    }
                    isUpdating = true
                    ediTxt.setText(mascara)
                    ediTxt.setSelection(mascara.length)
                }
            }
        }
        fun unmask(s: String): String {
            return s.replace("[-]".toRegex(), "")
        }
    }
    inner class MyAsyncTask: AsyncTask<String, String, String>() {
        override fun onPreExecute() {
        }
        override fun doInBackground(vararg params: String?): String {
            try {
                val url = URL(params[0])
                val urlConnection = url.openConnection() as HttpURLConnection
                urlConnection.connectTimeout = 7000
                var instring = ConvertStreamString(urlConnection.inputStream)
                publishProgress(instring)
            } catch (ex: Exception) {
                Log.d("Erro: ", ex.toString())
            }
            return ""
        }
        override fun onProgressUpdate(vararg params: String?) {
            try {
                var json = JSONObject(params[0])
                val cep = json.getString("cep")
                val logradouro = json.getString("logradouro")
                val bairro = json.getString("bairro")
                val cidade = json.getString("localidade")
                val estado = json.getString("uf")
                anime.tradeView(pbCep,tvResp)
                tvResp.text =
                    "Dados\ncep: " + cep + "\nRua: " + logradouro + "\nBairro: " + bairro + "\nCidade: " + cidade + "\nEstado: " + estado
            } catch (ex: Exception) {
                Log.d("Erro: ", ex.toString())
            }
        }
        fun ConvertStreamString(inputStream: InputStream): String {
            val reader = BufferedReader(inputStream.reader())
            val content = StringBuilder()
            var line = reader.readLine()
            reader.use { reader ->
                while (line != null) {
                    content.append(line)
                    line = reader.readLine()
                }
            }
            return content.toString()
        }
    }

    // Hide Keyboard
    fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
}
