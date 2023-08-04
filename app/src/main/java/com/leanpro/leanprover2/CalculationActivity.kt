package com.leanpro.leanprover2

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.*
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import java.io.File
import java.io.FileOutputStream
import java.io.ObjectOutputStream
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class CalculationActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculation)

        var calculation: Calculation = intent.getSerializableExtra("calculation") as Calculation

        var bottomNavigationView: BottomNavigationView = findViewById(R.id.navigator)
        bottomNavigationView.selectedItemId = R.id.settings
        bottomNavigationView.setOnItemSelectedListener(NavigationBarView.OnItemSelectedListener{ item ->
            if (item.itemId == R.id.structure){
                var intent = Intent(this, CalculationStructure::class.java)
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


        var yearInputLayout: TextInputLayout = findViewById(R.id.yearInputLayout)
        var yearInputTextField: TextInputEditText = findViewById(R.id.yearInputTextField)
        yearInputTextField.setText(String.format("%.3f", calculation.settings.yearPlan))

        var dayInputLayout: TextInputLayout = findViewById(R.id.dayInputLayout)
        var dayInputTextField: TextInputEditText = findViewById(R.id.dayInputTextField)
        dayInputTextField.setText(String.format("%.3f", calculation.settings.dayPlan))

        yearInputTextField.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                if(currentFocus == yearInputTextField){
                    try {
                        calculation.settings.yearPlan =
                            yearInputTextField.text.toString().toDouble()
                        if (calculation.settings.workingDays != 0.0) {
                            calculation.settings.dayPlan =
                                calculation.settings.yearPlan / calculation.settings.workingDays
                        }
                        calculation.calculate()
                        saveCalculation(filesDir, calculation)
                        dayInputTextField.setText(String.format("%.3f", calculation.settings.dayPlan))
                        yearInputLayout.error = null
                    } catch (e: Exception){
                        yearInputLayout.error = "Значение не может быть нулевым"
                    }

                }
            }
        })
        dayInputTextField.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                if(currentFocus == dayInputTextField){
                    try {
                        calculation.settings.dayPlan = dayInputTextField.text.toString().toDouble()
                        calculation.settings.yearPlan =
                            calculation.settings.dayPlan * calculation.settings.workingDays
                        calculation.calculate()
                        ///saveCalculation(filesDir, calculation)
                        yearInputTextField.setText(calculation.settings.yearPlan.toString())
                        dayInputLayout.error = null
                    } catch (e: Exception){
                        dayInputLayout.error = "Значение не может быть нулевым"
                    }
                }
            }
        })
        var allWorkingDaysLayout: TextInputLayout = findViewById(R.id.allWorkingDaysLayout)
        var allWorkingDaysTextField: TextInputEditText = findViewById(R.id.allWorkingDaysTextField)
        allWorkingDaysTextField.setText(String.format("%.0f", calculation.settings.workingDays))


        var shutDownInputTextField: TextInputEditText = findViewById(R.id.shutDownInputTextField)
        var shutDownInputLayout: TextInputLayout = findViewById(R.id.shutDownInputLayout)
        var shutDownMeasureTextField: AutoCompleteTextView = findViewById(R.id.shutDownMeasureTextField)
        shutDownInputTextField.setText(calculation.settings.shutdown.inWholeDays.toString())
        shutDownInputTextField.addTextChangedListener(object : TextWatcher {
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable) {
                if(currentFocus == shutDownInputTextField){
                    try {
                        calculation.settings.shutdown =
                            shutDownInputTextField.text.toString().toDouble().toDuration(textToDurationUnit(shutDownMeasureTextField.text.toString()))
                        calculation.calculate()
                        calculation.settings.dayPlan = calculation.settings.yearPlan / calculation.settings.workingDays
                        calculation.calculate()
                        saveCalculation(filesDir, calculation)
                        dayInputTextField.setText(String.format("%.3f", calculation.settings.dayPlan))
                        shutDownInputLayout.error = null
                        allWorkingDaysTextField.setText(String.format("%.3f", calculation.settings.workingDays))
                    } catch (e: Exception){
                        shutDownInputLayout.error = "Значение не может быть нулевым"
                    }

                }
            }
        })


        var workingDaysInputLayout: TextInputLayout = findViewById(R.id.workingDaysInputLayout)
        var workingDaysInputTextField: TextInputEditText = findViewById(R.id.workingDaysInputTextField)
        var notWorkingDaysInputLayout: TextInputLayout = findViewById(R.id.notWorkingDaysInputLayout)
        var notWorkingDaysInputTextField: TextInputEditText = findViewById(R.id.notWorkingDaysInputTextField)
        var coefficientInputLayout: TextInputLayout = findViewById(R.id.coefficientInputLayout)
        var coefficientTextField: TextInputEditText = findViewById(R.id.coefficientTextField)

        workingDaysInputTextField.setText(String.format("%.3f", calculation.settings.schedule.workingDays))
        workingDaysInputTextField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if(currentFocus == workingDaysInputTextField){
                    try {
                        calculation.settings.schedule.workingDays = workingDaysInputTextField.text.toString().toDouble()
                        calculation.calculate()
                        calculation.settings.dayPlan = calculation.settings.yearPlan / calculation.settings.workingDays
                        calculation.calculate()
                        saveCalculation(filesDir, calculation)
                        dayInputTextField.setText(String.format("%.3f", calculation.settings.dayPlan))
                        coefficientTextField.setText(String.format("%.3f", calculation.settings.schedule.coefficient))
                        allWorkingDaysTextField.setText(String.format("%.3f", calculation.settings.workingDays))
                        workingDaysInputLayout.error = null
                    } catch (e: Exception){
                        workingDaysInputLayout.error = "Значение не может быть нулевым"
                    }

                }
            }

        })
        notWorkingDaysInputTextField.setText(String.format("%.3f", calculation.settings.schedule.notWorkingDays))
        notWorkingDaysInputTextField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if(currentFocus == notWorkingDaysInputTextField){
                    try {
                        calculation.settings.schedule.notWorkingDays = notWorkingDaysInputTextField.text.toString().toDouble()
                        calculation.calculate()
                        calculation.settings.dayPlan = calculation.settings.yearPlan / calculation.settings.workingDays
                        calculation.calculate()
                        saveCalculation(filesDir, calculation)
                        dayInputTextField.setText(String.format("%.3f", calculation.settings.dayPlan))
                        coefficientTextField.setText(String.format("%.3f", calculation.settings.schedule.coefficient))
                        notWorkingDaysInputLayout.error = null
                        allWorkingDaysTextField.setText(String.format("%.3f", calculation.settings.workingDays))
                    } catch (e: Exception){
                        notWorkingDaysInputLayout.error = "Значение не может быть нулевым"
                    }

                }
            }

        })
        coefficientTextField.setText(String.format("%.3f", calculation.settings.schedule.coefficient))

        var periodInputLayout: TextInputLayout = findViewById(R.id.periodInputLayout)
        var periodInputTextField: TextInputEditText = findViewById(R.id.periodInputTextField)
        var periodMeasureTextField: AutoCompleteTextView = findViewById(R.id.periodMeasureTextField)
        periodInputTextField.setText(durationToText(calculation.settings.period, periodMeasureTextField.text.toString()).toString())

        var reglamentedBreaksInputLayout: TextInputLayout = findViewById(R.id.reglamentedBreaksInputLayout)
        var reglamentedBreaksInputTextField: TextInputEditText = findViewById(R.id.reglamentedBreaksInputTextField)
        var reglamentedBreaksMeasureTextField: AutoCompleteTextView = findViewById(R.id.reglamentedBreaksMeasureTextField)
        reglamentedBreaksInputTextField.setText(durationToText(calculation.settings.reglamentedBreaks, reglamentedBreaksMeasureTextField.text.toString()).toString())

        var notReglamentedBreaksInputLayout: TextInputLayout = findViewById(R.id.notReglamentedBreaksInputLayout)
        var notReglamentedBreaksInputTextField: TextInputEditText = findViewById(R.id.notReglamentedBreaksInputTextField)
        var notReglamentedBreaksMeasureTextField: AutoCompleteTextView = findViewById(R.id.notReglamentedBreaksMeasureTextField)
        notReglamentedBreaksInputTextField.setText(durationToText(calculation.settings.nonReglamentedBreaks, notReglamentedBreaksMeasureTextField.text.toString()).toString())

        var periodCountLayout: TextInputLayout = findViewById(R.id.periodCountLayout)
        var periodCountTextField: TextInputEditText = findViewById(R.id.periodCountTextField)
        periodCountTextField.setText(calculation.settings.periodCount.toString())


        var clearWorkingTimeInputLayout: TextInputLayout = findViewById(R.id.clearWorkingTimeInputLayout)
        var clearWorkingTimeTextField: TextInputEditText = findViewById(R.id.clearWorkingTimeTextField)
        var clearWorkingTimeMeasureTextField: AutoCompleteTextView = findViewById(R.id.clearWorkingTimeMeasureTextField)
        clearWorkingTimeTextField.setText(durationToText(calculation.settings.clearWorkingTime, clearWorkingTimeMeasureTextField.text.toString()).toString())

        var tactTimeLayout: TextInputLayout = findViewById(R.id.tactTimeLayout)
        var tactTimeTextField: TextInputEditText = findViewById(R.id.tactTimeTextField)
        var tactTimeMeasureTextField: AutoCompleteTextView = findViewById(R.id.tactTimeMeasureTextField)
        tactTimeTextField.setText(durationToText(calculation.settings.tactTime, tactTimeMeasureTextField.text.toString()).toString())


        periodInputTextField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if(currentFocus == periodInputTextField){
                    try {
                        calculation.settings.period = periodInputTextField.text.toString().toDouble().toDuration(textToDurationUnit(periodMeasureTextField.text.toString()))
                        calculation.calculate()
                        saveCalculation(filesDir, calculation)
                        clearWorkingTimeTextField.setText(durationToText(calculation.settings.clearWorkingTime, clearWorkingTimeMeasureTextField.text.toString()).toString())
                        tactTimeTextField.setText(durationToText(calculation.settings.tactTime, tactTimeMeasureTextField.text.toString()).toString())
                        periodInputLayout.error = null
                    } catch (e: Exception){
                        periodInputLayout.error = "Значение не может быть нулевым"
                    }
                }
            }

        })
        periodMeasureTextField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                periodInputTextField.setText(durationToText(calculation.settings.period, periodMeasureTextField.text.toString()).toString())
            }

        })

        reglamentedBreaksInputTextField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if(currentFocus == reglamentedBreaksInputTextField){
                    try {
                        calculation.settings.reglamentedBreaks = reglamentedBreaksInputTextField.text.toString().toDouble().toDuration(textToDurationUnit(reglamentedBreaksMeasureTextField.text.toString()))
                        calculation.calculate()
                        saveCalculation(filesDir, calculation)
                        clearWorkingTimeTextField.setText(durationToText(calculation.settings.clearWorkingTime, clearWorkingTimeMeasureTextField.text.toString()).toString())
                        tactTimeTextField.setText(durationToText(calculation.settings.tactTime, tactTimeMeasureTextField.text.toString()).toString())
                        reglamentedBreaksInputLayout.error = null
                    } catch (e: Exception){
                        reglamentedBreaksInputLayout.error = "Значение не может быть нулевым"
                    }
                }
            }

        })
        reglamentedBreaksMeasureTextField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                reglamentedBreaksInputTextField.setText(durationToText(calculation.settings.reglamentedBreaks, reglamentedBreaksMeasureTextField.text.toString()).toString())
            }

        })

        notReglamentedBreaksInputTextField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if(currentFocus == notReglamentedBreaksInputTextField){
                    try {
                        calculation.settings.nonReglamentedBreaks = notReglamentedBreaksInputTextField.text.toString().toDouble().toDuration(textToDurationUnit(notReglamentedBreaksMeasureTextField.text.toString()))
                        calculation.calculate()
                        saveCalculation(filesDir, calculation)
                        clearWorkingTimeTextField.setText(durationToText(calculation.settings.clearWorkingTime, clearWorkingTimeMeasureTextField.text.toString()).toString())
                        tactTimeTextField.setText(durationToText(calculation.settings.tactTime, tactTimeMeasureTextField.text.toString()).toString())
                        notReglamentedBreaksInputLayout.error = null
                    } catch (e: Exception){
                        notReglamentedBreaksInputLayout.error = "Значение не может быть нулевым"
                    }
                }
            }

        })
        notReglamentedBreaksMeasureTextField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                notReglamentedBreaksInputTextField.setText(durationToText(calculation.settings.nonReglamentedBreaks, notReglamentedBreaksMeasureTextField.text.toString()).toString())
            }

        })

        periodCountTextField.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                if(currentFocus == periodCountTextField){
                    try {
                        calculation.settings.periodCount = periodCountTextField.text.toString().toInt()
                        calculation.calculate()
                        saveCalculation(filesDir, calculation)
                        clearWorkingTimeTextField.setText(durationToText(calculation.settings.clearWorkingTime, clearWorkingTimeMeasureTextField.text.toString()).toString())
                        tactTimeTextField.setText(durationToText(calculation.settings.tactTime, tactTimeMeasureTextField.text.toString()).toString())
                        periodCountLayout.error = null
                    } catch (e: Exception){
                        periodCountLayout.error = "Значение не может быть нулевым"
                    }
                }
            }

        })
        clearWorkingTimeMeasureTextField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                clearWorkingTimeTextField.setText(durationToText(calculation.settings.clearWorkingTime, clearWorkingTimeMeasureTextField.text.toString()).toString()) }
        })
        tactTimeMeasureTextField.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                tactTimeTextField.setText(durationToText(calculation.settings.tactTime, tactTimeMeasureTextField.text.toString()).toString()) }
        })
    }
    private fun saveCalculation(filesDir: File, calculation: Calculation){
        try{
            var calculationFile = File(filesDir, calculation.name + ".cal")
            var calculationFOS = FileOutputStream(calculationFile)
            var calculationOOS = ObjectOutputStream(calculationFOS)
            calculationOOS.writeObject(calculation)
            calculationOOS.close()
            calculationFOS.close()
        } catch (e:Exception){
            Toast.makeText(this, "${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
    private fun textToDurationUnit(text: String): DurationUnit{
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
        return duration.inWholeMinutes.toDouble()
    }
}