package com.leanpro.leanprover2

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputLayout
import java.io.*
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        ///views declaration
        var recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        var addNewButton: Button = findViewById(R.id.addNewButton)

        ///Opening User Data
        var userInfo = openUserInfo(filesDir)
        ///opening calculations
        var calculations: MutableList<Calculation> = mutableListOf()
        for (name in userInfo.calculation_names){
            var calculationFile = File(filesDir, "$name.cal")
            var calculationFIS = FileInputStream(calculationFile)
            var calculationOIS = ObjectInputStream(calculationFIS)
            var calculation = calculationOIS.readObject() as Calculation
            calculationOIS.close()
            calculationOIS.close()
            calculations.add(calculation)
        }
        ///creating adapter in oncreate cause i need access to userinfo
        class CustomAdapter(private var calculations: MutableList<Calculation>): RecyclerView.Adapter<CustomAdapter.ViewHolder>(){
            inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
                val calculationName: TextView = view.findViewById(R.id.calculationName)
                val editDate: TextView = view.findViewById(R.id.editDate)
                val openButton: TextView = view.findViewById(R.id.openButton)
                val card: CardView = view.findViewById(R.id.card)
            }
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val card = LayoutInflater.from(parent.context).inflate(R.layout.calculations_cards, parent, false)
                return ViewHolder(card)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                holder.calculationName.text = calculations[position].name
                holder.editDate.text = calculations[position].NUP.lastEdit.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")).toString()
                holder.openButton.setOnClickListener(){
                    val intent = Intent(this@MainActivity, CalculationActivity::class.java)
                    intent.putExtra("calculation", calculations[position])
                    startActivity(intent)
                    overridePendingTransition(0,0)
                }
                holder.card.setOnLongClickListener(OnLongClickListener { //
                    val dialogBuilder = MaterialAlertDialogBuilder(this@MainActivity, R.style.ThemeOverlay_App_MaterialAlertDialog_Centered)
                    dialogBuilder.setTitle("Удаление расчета")
                    dialogBuilder.setMessage("Вы действительно хотите удалить расчет? Отменить это действие будет невозможно")
                    dialogBuilder.setPositiveButton("Удалить") { dialog, which ->
                        userInfo.calculation_names.remove(calculations[position].name)
                        saveUserInfo(filesDir, userInfo)
                        var file = File(filesDir, calculations[position].name + ".cal")
                        file.delete()
                        calculations.removeAt(position)
                        notifyDataSetChanged()
                    }
                    dialogBuilder.setNegativeButton("Отмена"){ dialog, which ->}
                    dialogBuilder.show()
                    true
                })

            }

            override fun getItemCount(): Int {
                return calculations.size
            }

        }

        ///creating adapter and displaying data
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = CustomAdapter(calculations)

        ///creating calculation by button pressing
        addNewButton.setOnClickListener(){
            val dialogBuilder = MaterialAlertDialogBuilder(this, R.style.ThemeOverlay_App_MaterialAlertDialog_Centered)
            dialogBuilder.setTitle("Название расчета")
            dialogBuilder.setMessage("Введите название нового расчета")
            val input = layoutInflater.inflate(R.layout.basicinput, null, false)
            var inputLayout = input.findViewById(R.id.inputLayout) as TextInputLayout
            var textfield: EditText = input.findViewById(R.id.textField)
            var uniqCheck = false
            textfield.setOnEditorActionListener { v, actionId, event ->
                if(actionId == EditorInfo.IME_ACTION_DONE){
                    if (textfield.text.toString() !in userInfo.calculation_names){
                        uniqCheck = true
                        val imm = v.context.getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                        true
                    } else{
                        inputLayout.error = getString(R.string.uniqNameReq)
                        uniqCheck = false
                        false
                    }
                } else {
                    false
                }
            }
            textfield.addTextChangedListener(object : TextWatcher {
                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                override fun afterTextChanged(s: Editable) {
                    if (textfield.text.toString() in userInfo.calculation_names) {
                        inputLayout.error = getString(R.string.uniqNameReq)
                    } else {
                        inputLayout.error = null
                    }
                }
            })
            dialogBuilder.setView(input)
            dialogBuilder.setPositiveButton("Готово") { dialog, which ->
                if (uniqCheck) {
                    var newCalculation = Calculation(textfield.text.toString())
                    newCalculation.NUP.lastEdit
                    var calculationFile = File(filesDir, "${textfield.text.toString()}.cal")
                    var calculationFOS = FileOutputStream(calculationFile)
                    var calculationOOS = ObjectOutputStream(calculationFOS)
                    calculationOOS.writeObject(newCalculation)
                    userInfo.calculation_names.add(textfield.text.toString())
                    saveUserInfo(filesDir, userInfo)
                    calculations.add(newCalculation)
                    recyclerView.adapter = CustomAdapter(calculations)
                }
            }
            dialogBuilder.setNegativeButton("Отмена"){ dialog, which -> }
            dialogBuilder.show()
        }



    }


    private fun openUserInfo(filesDir: File): UserInfo{
        try {
            var userInfoFile = File(filesDir, "user.info")
            var userInfoFIS = FileInputStream(userInfoFile)
            var userInfoOIS = ObjectInputStream(userInfoFIS)
            var userInfo = userInfoOIS.readObject() as UserInfo
            userInfoOIS.close()
            userInfoFIS.close()
            return userInfo
        } catch (e: FileNotFoundException){
            var userInfo = UserInfo()
            var userInfoFile = File(filesDir, "user.info")
            var userInfoFOS = FileOutputStream(userInfoFile)
            var userInfoOOS = ObjectOutputStream(userInfoFOS)
            userInfoOOS.writeObject(userInfo)
            userInfoOOS.close()
            userInfoFOS.close()
            return userInfo
        }
    }
    private fun saveUserInfo(filesDir: File, userInfo: UserInfo){
        var userInfoFile = File(filesDir, "user.info")
        var userInfoFOS = FileOutputStream(userInfoFile)
        var userInfoOOS = ObjectOutputStream(userInfoFOS)
        userInfoOOS.writeObject(userInfo)
        userInfoOOS.close()
        userInfoFOS.close()
    }
    private fun hideKeyboard() {
        val imm: InputMethodManager =
            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.windowToken, 0)
    }
}


