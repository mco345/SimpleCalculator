package com.example.simplecalculator

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.room.Room
import com.example.simplecalculator.model.History
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity() {

    private val expressionTextView: TextView by lazy {
        findViewById(R.id.expressionTextView)
    }

    private val resultTextView: TextView by lazy {
        findViewById(R.id.resultTextView)
    }

    private val historyLayout: View by lazy {
        findViewById(R.id.historyLayout)
    }

    private val historyLinearLayout: LinearLayout by lazy {
        findViewById(R.id.historyLinearLayout)
    }

    lateinit var db: AppDatabase

    private var isOperator = false
    private var hasOperator = false
    private var isResult = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // DB 생성
        db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "historyDB"
        ).build()
    }

    fun buttonClicked(v: View) {
        when (v.id) {
            R.id.button0 -> numberButtonClicked("0")
            R.id.button1 -> numberButtonClicked("1")
            R.id.button2 -> numberButtonClicked("2")
            R.id.button3 -> numberButtonClicked("3")
            R.id.button4 -> numberButtonClicked("4")
            R.id.button5 -> numberButtonClicked("5")
            R.id.button6 -> numberButtonClicked("6")
            R.id.button7 -> numberButtonClicked("7")
            R.id.button8 -> numberButtonClicked("8")
            R.id.button9 -> numberButtonClicked("9")
            R.id.buttonPlus -> operatorButtonClicked("+")
            R.id.buttonMin -> operatorButtonClicked("-")
            R.id.buttonMul -> operatorButtonClicked("x")
            R.id.buttonDiv -> operatorButtonClicked("/")
            R.id.buttonModulo -> operatorButtonClicked("%")
        }
    }

    private fun numberButtonClicked(number: String) {
        // 연산 결과가 expressionTextView로 옮겨온 경우에 숫자를 입력한 경우
        if (isResult) {
            expressionTextView.text = ""    // 원래 결과 지워짐(초기화)
            isResult = false
        }

        // 마지막 문자가 연산자일 올 경우
        if (isOperator) {
            expressionTextView.append(" ")  // 띄어쓰기 추가
        }
        isOperator = false  // 마지막 문자 연산자 아님

        val expressionText = expressionTextView.text.split(" ") // 띄어쓰기를 기준으로 분리(숫자와 연산자 분리)

        // 마지막 인자(즉, 숫자)가 15자 이상일 경우
        if (expressionText.isNotEmpty() && expressionText.last().length >= 15) {
            Toast.makeText(this, "15자리 까지만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
            return
        }
        // 0이 제일 앞에 올 경우
        else if (number == "0" && expressionText.last().isEmpty()) {
            Toast.makeText(this, "0은 제일 앞에 올 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        expressionTextView.append(number)

        resultTextView.text = calculateExpression()
    }

    private fun operatorButtonClicked(operator: String) {
        // 숫자를 입력하지 않은 경우
        if (expressionTextView.text.isEmpty()) {
            return
        }

        when {
            // 연산자를 마지막으로 쓴 경우
            isOperator -> {
                expressionTextView.text = expressionTextView.text.toString()
                    .dropLast(1) + operator   // 연산자 교체(dropLast : 마지막 문자 지우기)
            }
            // (마지막에 숫자가 있지만)연산자가 이미 쓰인 경우
            hasOperator -> {
                Toast.makeText(this, "연산자는 한 번만 사용할 수 있습니다.", Toast.LENGTH_SHORT).show()
                return

            }
            // 정상적으로 연산자가 올 차례인 경우
            else -> {
                expressionTextView.append(" $operator") // 띄어쓰기 후 연산자 추가
            }
        }

        // Spannable
        val ssb = SpannableStringBuilder(expressionTextView.text)

        // 연산자의 글자색 바꾸기
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            ssb.setSpan(
                ForegroundColorSpan(getColor(R.color.green)),   // 초록색으로 변경(전경 색상값 변경)
                expressionTextView.text.length - 1,  // expressionTextView의 마지막 전에서
                expressionTextView.text.length,     // expressionTextView의 마지막까지(연산자만 추출)
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE  //
            )

            expressionTextView.text = ssb
        }


        isOperator = true
        hasOperator = true
        isResult = false

    }

    fun resultButtonClicked(v: View) {
        val expressionTexts = expressionTextView.text.split(" ")    // 숫자, 연산자, 숫자 (3개)

        // 아무것도 입력하지 않았거나, 연산자를 입력하지 않은 경우
        if (expressionTextView.text.isEmpty() || expressionTexts.size == 1) {
            return
        }

        // 첫 번째 숫자와 연산자만 입력하고 두 번째 숫자를 입력하지 않는 경우
        if (expressionTexts.size != 3 && hasOperator) {
            Toast.makeText(this, "아직 완성되지 않는 수식입니다.", Toast.LENGTH_SHORT).show()
            return
        }

        // 첫 번째 숫자와 두 번째 숫자가 숫자가 아닌 경우(발생하면 안되는 오류임)
        if (expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()) {
            Toast.makeText(this, "오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        val expressionText = expressionTextView.text.toString() // 계산 과정 저장
        val resultText = calculateExpression()  // 계산 결과

        Thread(Runnable {
            db.historyDao().insertHistory(History(null, expressionText, resultText))
        }).start()

        resultTextView.text = ""    // resultTextView 비우기
        expressionTextView.text = resultText    // 계산 결과 expressionTextView에 setText

        isOperator = false
        hasOperator = false
        isResult = true
    }

    private fun calculateExpression(): String {
        val expressionTexts = expressionTextView.text.split(" ")    // 숫자, 연산자, 숫자 (3개)

        if (hasOperator.not() || expressionTexts.size != 3) { // 연산자가 없거나, 3개 요소가 아닐 경우
            return ""
        } else if (expressionTexts[0].isNumber().not() || expressionTexts[2].isNumber().not()) {
            return ""
        }

        val exp1 = expressionTexts[0].toBigInteger()
        val exp2 = expressionTexts[2].toBigInteger()
        val op = expressionTexts[1]

        return when (op) {
            "+" -> (exp1 + exp2).toString()
            "-" -> (exp1 - exp2).toString()
            "x" -> (exp1 * exp2).toString()
            "/" -> (exp1 / exp2).toString()
            "%" -> (exp1 % exp2).toString()
            else -> ""
        }
    }

    fun clearButtonClicked(v: View) {
        expressionTextView.text = ""
        resultTextView.text = ""
        isOperator = false
        hasOperator = false
    }

    fun historyButtonClicked(v: View) {
        historyLayout.isVisible = true
        historyLinearLayout.removeAllViews()

        Thread(Runnable {
            db.historyDao().getAll().reversed().forEach {
                runOnUiThread {
                    val historyView =
                        LayoutInflater.from(this).inflate(R.layout.history_row, null, false)
                    historyView.findViewById<TextView>(R.id.expressionTextView).text = it.expression
                    historyView.findViewById<TextView>(R.id.resultTextView).text = "= ${it.result}"

                    historyLinearLayout.addView(historyView)
                }

            }
        }).start()
    }


    fun closeHistoryButtonClicked(view: View) {
        historyLayout.isVisible = false // historyLayout 닫기
    }

    fun historyClearButtonClicked(view: View) {
        // 뷰에서 모든 기록 삭제
        historyLinearLayout.removeAllViews()

        // 디비에서 모든 기록 삭제
        Thread(Runnable {
            db.historyDao().deleteAll()
        }).start()

    }


}

// 해당 String이 숫자인지 아닌지 판별
private fun String.isNumber(): Boolean {
    return try {
        this.toBigInteger()
        true
    } catch (e: NumberFormatException) {
        false
    }
}
