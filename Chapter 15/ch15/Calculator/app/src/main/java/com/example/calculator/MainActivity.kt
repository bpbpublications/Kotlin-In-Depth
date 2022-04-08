package com.example.calculator

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.calculator.databinding.ActivityMainBinding
import java.math.BigDecimal
import java.math.RoundingMode

class MainActivity : AppCompatActivity() {
    enum class OpKind {
        ADD, SUBTRACT, MULTIPLY, DIVIDE
    }

    companion object {
        fun OpKind.compute(a: BigDecimal, b: BigDecimal): BigDecimal = when (this) {
            OpKind.ADD -> a + b
            OpKind.SUBTRACT -> a - b
            OpKind.MULTIPLY -> a * b
            OpKind.DIVIDE -> a.divide(b, 10, RoundingMode.HALF_EVEN)
        }
    }

    private lateinit var binding: ActivityMainBinding
    private val txtResult get() = binding.txtResult
    private var lastResult: BigDecimal = BigDecimal.ZERO
    private var lastOp: OpKind? = null
    private var waitingNextOperand: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btn0.setOnClickListener { appendText("0") }
        binding.btn1.setOnClickListener { appendText("1") }
        binding.btn2.setOnClickListener { appendText("2") }
        binding.btn3.setOnClickListener { appendText("3") }
        binding.btn4.setOnClickListener { appendText("4") }
        binding.btn5.setOnClickListener { appendText("5") }
        binding.btn6.setOnClickListener { appendText("6") }
        binding.btn7.setOnClickListener { appendText("7") }
        binding.btn8.setOnClickListener { appendText("8") }
        binding.btn0.setOnClickListener { appendText("9") }
        binding.btnPoint.setOnClickListener {
            appendText(".")
        }

        binding.btnSign.setOnClickListener {
            val currentText = txtResult.text.toString()
            txtResult.text = when {
                currentText.startsWith("-") ->
                    currentText.substring(1, currentText.length)
                currentText != "0" ->
                    "-$currentText"
                else ->
                    return@setOnClickListener
            }
        }

        binding.btnBackspace.setOnClickListener {
            val currentText = txtResult.text.toString()
            val newText = currentText.substring(0, currentText.length - 1)
            txtResult.text =
                if (newText.isEmpty() || newText == "-") "0" else newText
        }

        binding.btnClear.setOnClickListener { clearText() }

        binding.btnPlus.setOnClickListener {
            calc(OpKind.ADD)
        }
        binding.btnMinus.setOnClickListener {
            calc(OpKind.SUBTRACT)
        }
        binding.btnTimes.setOnClickListener {
            calc(OpKind.MULTIPLY)
        }
        binding.btnDivide.setOnClickListener {
            calc(OpKind.DIVIDE)
        }
        binding.btnCalc.setOnClickListener {
            calc(null)
        }

        clearText()

        savedInstanceState?.let {
            txtResult.text = it.getString("currentText")
            lastResult = it.getSerializable(::lastResult.name) as BigDecimal
            lastOp = it.getSerializable(::lastOp.name) as OpKind?
            waitingNextOperand = it.getBoolean(::waitingNextOperand.name)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentText", txtResult.text.toString())
        outState.putSerializable(::lastResult.name, lastResult)
        outState.putSerializable(::lastOp.name, lastOp)
        outState.putBoolean(::waitingNextOperand.name, waitingNextOperand)
    }

    private fun clearText() {
        txtResult.text = "0"
    }

    private fun appendText(text: String) {
        if (waitingNextOperand) {
            clearText()
            waitingNextOperand = false
        }
        val currentText = txtResult.text.toString()
        txtResult.text = if (currentText == "0") text else currentText + text
    }

    private fun calc(nextOp: OpKind?) {
        if (waitingNextOperand) {
            lastOp = nextOp
            return
        }

        val currentValue = BigDecimal(txtResult.text.toString())
        val newValue = try {
            lastOp?.compute(lastResult, currentValue) ?: currentValue
        } catch (e: ArithmeticException) {
            lastOp = null
            waitingNextOperand = true
            Toast.makeText(
                applicationContext,
                "Invalid operation!",
                Toast.LENGTH_SHORT
            ).show()
            return
        }
        if (nextOp != null) {
            lastResult = newValue
        }
        if (lastOp != null) {
            txtResult.text = newValue.toPlainString()
        }
        lastOp = nextOp
        waitingNextOperand = nextOp != null
    }
}