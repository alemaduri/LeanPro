package com.leanpro.leanprover2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.chip.Chip
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.textfield.TextInputEditText
import kotlin.time.Duration
import kotlin.time.DurationUnit

class CalculationHumans : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculation_humans)

        var calculation: Calculation = intent.getSerializableExtra("calculation") as Calculation

        var bottomNavigationView: BottomNavigationView = findViewById(R.id.navigator)
        bottomNavigationView.selectedItemId = R.id.humans
        bottomNavigationView.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener{ item ->
            if (item.itemId == R.id.structure){
                var intent = Intent(this, CalculationStructure::class.java)
                intent.putExtra("calculation", calculation)
                startActivity(intent)
                overridePendingTransition(0,0)
            }
            if (item.itemId == R.id.settings){
                var intent = Intent(this, CalculationActivity::class.java)
                intent.putExtra("calculation", calculation)
                startActivity(intent)
                overridePendingTransition(0,0)
            }
            true
        })

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
                holder.workingTimeMeasure.addTextChangedListener(object : TextWatcher {
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
        var humans: RecyclerView = findViewById(R.id.humansLayout)
        humans.layoutManager = LinearLayoutManager(this)
        humans.adapter = ProfessionsAdapter(calculation.humans.entries)
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