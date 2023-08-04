package com.leanpro.leanprover2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import java.time.format.DateTimeFormatter
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class CalculationStructure : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculation_structure)

        var calculation: Calculation = intent.getSerializableExtra("calculation") as Calculation

        var bottomNavigationView: BottomNavigationView = findViewById(R.id.navigator)
        bottomNavigationView.selectedItemId = R.id.structure
        bottomNavigationView.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener{ item ->
            if (item.itemId == R.id.settings){
                var intent = Intent(this, CalculationActivity::class.java)
                intent.putExtra("calculation", calculation)
                startActivity(intent)
                overridePendingTransition(0,0)
            }
            if (item.itemId == R.id.humans){
                var intent = Intent(this, CalculationHumans::class.java)
                intent.putExtra("calculation", calculation)
                startActivity(intent)
                overridePendingTransition(0,0)
            }
            true
        })

        class OperationAdapter(private var operations: MutableSet<MutableMap.MutableEntry<Int, Calculation.Zone.Operation>>): RecyclerView.Adapter<OperationAdapter.ViewHolder>(){
            inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
                val operationNameTextField: TextInputEditText = view.findViewById(R.id.operationNameTextField)
                val cyclesLayout: TextInputLayout = view.findViewById(R.id.cyclesLayout)
                val cyclesTextField: TextInputEditText = view.findViewById(R.id.cyclesTextField)
                val currentTimeLayout: TextInputLayout = view.findViewById(R.id.currentTimeLayout)
                val currentTimeTextField: TextInputEditText = view.findViewById(R.id.currentTimeTextField)
                val currentTimeMeasureTextField: AutoCompleteTextView = view.findViewById(R.id.currentTimeMeasureTextField)
                val professionTextField: TextInputEditText = view.findViewById(R.id.professionTextField)
                val workersCountLayout: TextInputLayout = view.findViewById(R.id.workersCountLayout)
                val workersCountTextField: TextInputEditText = view.findViewById(R.id.workersCountTextField)
                val expandButton: Chip = view.findViewById(R.id.expandButton)
                val expandable: LinearLayout = view.findViewById(R.id.expandable)
                val card: MaterialCardView = view.findViewById(R.id.card)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val card = LayoutInflater.from(parent.context).inflate(R.layout.operation_layout, parent, false)
                return ViewHolder(card)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                var operation = operations.elementAt(position).value
                var zoneId = operation.zoneId
                var operationId = operation.id
                holder.operationNameTextField.setText(operation.name)
                holder.operationNameTextField.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun afterTextChanged(p0: Editable?) {
                        try {
                            calculation.zones[zoneId]!!.operations!!.get(operationId)?.name =
                                holder.operationNameTextField.text.toString()
                            saveCalculation(filesDir, calculation)
                        }catch (e:Exception){

                        }
                    }

                })
                holder.cyclesTextField.setText(operation.cycles.toString())
                holder.cyclesTextField.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun afterTextChanged(p0: Editable?) {
                        try{
                            calculation.zones[zoneId]!!.operations[operationId]!!.cycles = holder.cyclesTextField.text.toString().toInt()
                            calculation.calculate()
                            saveCalculation(filesDir, calculation)
                            holder.cyclesLayout.error = null
                        } catch (e: Exception){
                            holder.cyclesLayout.error = "Введите значение"
                        }
                    }

                })
                holder.currentTimeTextField.setText(operation.currentTime.inWholeSeconds.toString())
                holder.currentTimeTextField.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun afterTextChanged(p0: Editable?) {
                        try{
                            if (currentFocus == holder.currentTimeTextField) {
                                calculation.zones[zoneId]!!.operations[operationId]!!.currentTime =
                                    holder.currentTimeTextField.text.toString().toDouble()
                                        .toDuration(textToDurationUnit(holder.currentTimeMeasureTextField.text.toString()))
                                calculation.calculate()
                                saveCalculation(filesDir, calculation)
                                holder.currentTimeLayout.error = null
                            }
                        } catch (e: Exception){
                            holder.currentTimeLayout.error = "Введите значение"
                        }
                    }

                })
                holder.currentTimeMeasureTextField.addTextChangedListener(object : TextWatcher{
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun afterTextChanged(p0: Editable?) {
                        holder.currentTimeTextField.setText(durationToText(operation.currentTime, holder.currentTimeMeasureTextField.text.toString()).toString())
                    }

                })
                holder.professionTextField.setText(operation.profession)
                holder.professionTextField.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun afterTextChanged(p0: Editable?) {
                        try{
                            calculation.zones[zoneId]!!.operations[operationId]!!.profession = holder.professionTextField.text.toString()
                            calculation.calculate()
                            saveCalculation(filesDir, calculation)
                        } catch (e: Exception){
                        }
                    }

                })
                holder.workersCountTextField.setText(operation.workersCount.toString())
                holder.workersCountTextField.addTextChangedListener(object : TextWatcher {
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun afterTextChanged(p0: Editable?) {
                        try{
                            calculation.zones[zoneId]!!.operations[operationId]!!.workersCount = holder.workersCountTextField.text.toString().toInt()
                            calculation.calculate()
                            saveCalculation(filesDir, calculation)
                            holder.workersCountLayout.error = null
                        } catch (e: Exception){
                            holder.workersCountLayout.error = "Введите значение"
                        }
                    }

                })
                holder.expandButton.setOnClickListener(){
                    if (holder.expandable.visibility == View.GONE){holder.expandable.visibility = View.VISIBLE}
                    else{if(holder.expandable.visibility == View.VISIBLE){holder.expandable.visibility = View.GONE}}
                }
                holder.card.setOnLongClickListener(){
                    val dialogBuilder = MaterialAlertDialogBuilder(this@CalculationStructure, R.style.ThemeOverlay_App_MaterialAlertDialog_Centered)
                    dialogBuilder.setTitle("Удаление операции")
                    dialogBuilder.setMessage("Вы действительно хотите удалить оперцию? Отменить это действие будет невозможно")
                    dialogBuilder.setPositiveButton("Удалить") { dialog, which ->
                        calculation.zones[zoneId]!!.operations.remove(operation.id)
                        calculation.calculate()
                        saveCalculation(filesDir, calculation)
                        notifyItemRemoved(position)
                    }
                    dialogBuilder.setNegativeButton("Отмена"){ dialog, which ->}
                    dialogBuilder.show()
                    true
                }
            }

            override fun getItemCount(): Int {
                return operations.size
            }
        }

        class ProfessionsAdapter(private var humans: MutableSet<MutableMap.MutableEntry<String, Calculation.Human>>): RecyclerView.Adapter<ProfessionsAdapter.ViewHolder>(){
            inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
                val profession: TextInputEditText = view.findViewById(R.id.profession)
                val workingTime: TextInputEditText = view.findViewById(R.id.workingTime)
                val workingTimeMeasure: AutoCompleteTextView = view.findViewById(R.id.workingTimeMeasure)
                val workersCount: TextInputEditText = view.findViewById(R.id.workersCount)
                val expandButton: Chip = view.findViewById(R.id.expandButton)
                val expandable: LinearLayout = view.findViewById(R.id.expandable)
            }

            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val card = LayoutInflater.from(parent.context).inflate(R.layout.profession_layout, parent, false)
                return ViewHolder(card)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                var human = humans.elementAt(position).value
                holder.profession.setText(human.profession.toString())
                holder.workingTime.setText(human.workingTimeForProduct.inWholeSeconds.toString())
                holder.workingTimeMeasure.addTextChangedListener(object : TextWatcher{
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun afterTextChanged(p0: Editable?) {
                        holder.workingTime.setText(durationToText(human.workingTimeForProduct, holder.workingTimeMeasure.text.toString()).toString())
                    }

                })
                holder.workersCount.setText(String.format("%.3f", human.calculatedCount))
                holder.expandButton.setOnClickListener(){
                    if (holder.expandable.visibility == View.GONE){holder.expandable.visibility = View.VISIBLE}
                    else{if(holder.expandable.visibility == View.VISIBLE){holder.expandable.visibility = View.GONE}}
                }
            }

            override fun getItemCount(): Int {
                return humans.size
            }
        }

        class StructureAdapter(private var zones: MutableSet<MutableMap.MutableEntry<Int, Calculation.Zone>>): RecyclerView.Adapter<StructureAdapter.ViewHolder>(){
            inner class ViewHolder(view: View): RecyclerView.ViewHolder(view){
                val zoneNameTextField: TextInputEditText = view.findViewById(R.id.zoneNameTextField)
                val addOperationButton: ImageButton = view.findViewById(R.id.addOperationButton)
                val showOperationsButton: Button = view.findViewById(R.id.showOperationsButton)

                val operationsLayout: RecyclerView = view.findViewById(R.id.operationsLayout)
                val showProfessionsButton: Button = view.findViewById(R.id.showProfessionsButton)
                val professionsLayout: RecyclerView = view.findViewById(R.id.professionsLayout)

                var professionCard: MaterialCardView = view.findViewById(R.id.professionCard)
                var operationsCard: MaterialCardView = view.findViewById(R.id.operationsCard)
                val card: MaterialCardView = view.findViewById(R.id.card)
            }
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
                val card = LayoutInflater.from(parent.context).inflate(R.layout.structure_layout, parent, false)
                return ViewHolder(card)
            }

            override fun onBindViewHolder(holder: ViewHolder, position: Int) {
                var zone = zones.elementAt(position).value
                var zoneId = zone.id
                holder.zoneNameTextField.setText(zone.name)
                holder.zoneNameTextField.addTextChangedListener(object : TextWatcher{
                    override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

                    override fun afterTextChanged(p0: Editable?) {
                        calculation.zones[zoneId]!!.name = holder.zoneNameTextField.text.toString()
                        saveCalculation(filesDir, calculation)
                    }

                })
                holder.addOperationButton.setOnClickListener(){
                    var newOperId = calculation.NUP.getOperationId()
                    calculation.zones[zoneId]!!.operations[newOperId] = calculation.zones[zoneId]!!.Operation()
                    calculation.zones[zoneId]!!.operations[newOperId]!!.id = newOperId
                    calculation.zones[zoneId]!!.operations[newOperId]!!.zoneId = zoneId

                    saveCalculation(filesDir, calculation)
                    holder.operationsLayout.adapter!!.notifyDataSetChanged()
                }
                holder.operationsLayout.layoutManager = LinearLayoutManager(this@CalculationStructure)
                holder.operationsLayout.adapter = OperationAdapter(zone.operations.entries)
                holder.showOperationsButton.setOnClickListener(){
                    if (holder.showOperationsButton.text.toString() == "Показать"){
                        holder.showOperationsButton.text = "Скрыть"
                        holder.operationsCard.visibility = View.VISIBLE
                        holder.addOperationButton.visibility = View.VISIBLE
                        holder.professionCard.visibility = View.GONE
                        holder.showProfessionsButton.text = "Показать"
                    }
                    else{
                        if(holder.showOperationsButton.text.toString() == "Скрыть"){
                            holder.showOperationsButton.text = "Показать"
                            holder.operationsCard.visibility = View.GONE
                            holder.addOperationButton.visibility = View.GONE
                        }
                    }
                }
                holder.professionsLayout.layoutManager = LinearLayoutManager(this@CalculationStructure)
                holder.professionsLayout.adapter = ProfessionsAdapter(zone.humans.entries)
                holder.showProfessionsButton.setOnClickListener(){
                    if (holder.showProfessionsButton.text.toString() == "Показать"){
                        holder.showProfessionsButton.text = "Скрыть"
                        holder.professionsLayout.adapter!!.notifyDataSetChanged()
                        holder.professionCard.visibility = View.VISIBLE
                        holder.addOperationButton.visibility = View.GONE
                        holder.operationsCard.visibility = View.GONE
                        holder.showOperationsButton.text = "Показать"
                    }
                    else{
                        if(holder.showProfessionsButton.text.toString() == "Скрыть"){
                            holder.showProfessionsButton.text = "Показать"
                            holder.professionCard.visibility = View.GONE
                        }
                    }
                }
                holder.card.setOnLongClickListener(){
                    val dialogBuilder = MaterialAlertDialogBuilder(this@CalculationStructure, R.style.ThemeOverlay_App_MaterialAlertDialog_Centered)
                    dialogBuilder.setTitle("Удаление рабочей зоны")
                    dialogBuilder.setMessage("Вы действительно хотите удалить рабочую зону? Отменить это действие будет невозможно")
                    dialogBuilder.setPositiveButton("Удалить") { dialog, which ->
                        calculation.zones.remove(zoneId)
                        calculation.calculate()
                        saveCalculation(filesDir, calculation)
                        notifyItemRemoved(position)
                    }
                    dialogBuilder.setNegativeButton("Отмена"){ dialog, which ->}
                    dialogBuilder.show()
                    true
                }

            }

            override fun getItemCount(): Int {
                return zones.size
            }

        }

        ///Adapter declaration and onButton adding zone
        var structureLayout: RecyclerView = findViewById(R.id.structureLayout)
        structureLayout.layoutManager = LinearLayoutManager(this)
        structureLayout.adapter = StructureAdapter(calculation.zones.entries) as StructureAdapter
        structureLayout.adapter!!.notifyDataSetChanged()
        var addNewButton: ExtendedFloatingActionButton = findViewById(R.id.addNewButton)
        addNewButton.setOnClickListener(){
            var newZoneId = calculation.NUP.getZoneId()
            var newZone = calculation.Zone()
            newZone.id = newZoneId
            calculation.zones[newZoneId] = newZone
            saveCalculation(filesDir, calculation)
            structureLayout.adapter!!.notifyDataSetChanged()
        }


    }
    private fun saveCalculation(filesDir: File, calculation: Calculation){
        try{
            var calculationFile = File(filesDir, calculation.name + ".cal")
            var calculationFOS = FileOutputStream(calculationFile)
            var calculationOOS = ObjectOutputStream(calculationFOS)
            calculationOOS.writeObject(calculation)
            calculationOOS.close()
            calculationFOS.close()
        } catch (e:Exception) {
            Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun textToDurationUnit(text: String): DurationUnit {
        if (text == "Дн."){
            return DurationUnit.DAYS
        }
        if (text == "Сек."){
            return DurationUnit.SECONDS
        }
        if (text == "Мин."){
            return DurationUnit.MINUTES
        }
        if (text == "Час."){
            return DurationUnit.HOURS
        }
        return DurationUnit.SECONDS
    }
    private fun durationToText(duration: Duration, text: String): Double{
        if (text == "Дн."){
            return duration.inWholeDays.toDouble()
        }
        if (text == "Сек."){
            return duration.inWholeSeconds.toDouble()
        }
        if (text == "Мин."){
            return duration.inWholeMinutes.toDouble()
        }
        if (text == "Час."){
            return duration.inWholeHours.toDouble()
        }
        return duration.inWholeSeconds.toDouble()
    }

}