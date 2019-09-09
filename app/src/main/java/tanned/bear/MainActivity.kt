package tanned.bear

import android.app.Dialog
import android.content.Context
import android.content.DialogInterface
import android.content.SharedPreferences
import android.databinding.DataBindingUtil
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.TextView
import tanned.bear.databinding.ActivityMainBinding
import java.util.*
import kotlin.concurrent.timer

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding      //바인딩 객체
    private lateinit var mSharedPreferences: SharedPreferences      //설정 저장용\

    private var mVibrationFlag: Boolean = false     //진동 설정 여부
    private var mRestTime: Long = 0     //설정된 휴식 시간
    private var mPauseDialog: Dialog? = null     //일시정지시 발생할 다이얼로그

    private var mRemainRestTime: Long = 0       //타이머를 통해 변경될 휴식 시간
    private var mTimerTask: Timer? = null       //휴식시간 변경 타이머
    private var mRunningTimerFlag: Boolean = false      //타이머 진행 여부
    
    private var mSetCount: Int = 0      //세트수 변경 편의를 위해

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main)
        mBinding.activity = this@MainActivity
        mSharedPreferences = getSharedPreferences(getString(R.string.key_shared_preferences), Context.MODE_PRIVATE)

        init()
    }

    //종료시 설정값 저장
    override fun onDestroy() {
        super.onDestroy()
        val editor: SharedPreferences.Editor = mSharedPreferences.edit()
        editor.putBoolean(getString(R.string.key_vibration), mVibrationFlag)
        editor.putLong(getString(R.string.key_rest_time), mRestTime)
        editor.commit()

        mPauseDialog?.dismiss()
        mPauseDialog = null
    }

    //설정값 세팅, 다이얼로그 초기화
    fun init() {
        mVibrationFlag = mSharedPreferences.getBoolean(getString(R.string.key_vibration), false)
        setVibrationText()
        mRestTime = mSharedPreferences.getLong(getString(R.string.key_rest_time), resources.getInteger(R.integer.default_rest_time).toLong())
        mRemainRestTime = mRestTime
        setRestTime(true, false)
        setRestTime(false, false)

        mPauseDialog = Dialog(this)
        mPauseDialog?.setContentView(R.layout.dialog_pause)
        mPauseDialog?.findViewById<TextView>(R.id.reset)?.setOnClickListener(View.OnClickListener {
            mPauseDialog?.cancel()
            stopTimer(false)
        })
        mPauseDialog?.findViewById<TextView>(R.id.resume)?.setOnClickListener(View.OnClickListener {
            mPauseDialog?.cancel()
            onClickCenter()
        })

        mPauseDialog?.create()
    }

    //1) 센터 클릭, 타이머 중 센터 클릭
    fun onClickCenter() {
        if (mRunningTimerFlag) {
            stopTimer(true)
        } else {
            //배경색 초기화
            setCenterBtnColor(true)

            if (mRemainRestTime == mRestTime) {
                //세트수 증가
                ++mSetCount
                setDisplaySetCount()
            }

            //남은 시간 설정 및 타이머 실행
            mTimerTask = timer(period = 10) {
                mRemainRestTime -= 10
                setRestTime(true, true)

                if (mRemainRestTime < 0) {
                    stopTimer(false)
                }
            }

            //runFlag 변경
            mRunningTimerFlag = true
        }
    }

    //4) 상단부 - 진동 스위치
    fun onClickSwitchVibration() {
        mVibrationFlag = !mVibrationFlag
        setVibrationText()
    }

    //4) 상단부 - 휴식시간 설정 팝업
    fun onClickShowRestTimePopup() {

    }

    //4) 상단부 - 세트수 줄이기
    fun onClickMinusSetCount() {
        if (mSetCount != 0) {
            --mSetCount
        }

        setDisplaySetCount()
    }

    //4) 상단부 - 세트수 초기화
    fun onClickResetSetCount() {
        mSetCount = 0
        setDisplaySetCount()
    }

    //5) 설정 팝업 - 휴식시간 설정
    fun onClickSetResetTime() {

    }

    fun setDisplaySetCount() = mBinding.displaySetCount.setText(mSetCount.toString())

    fun setVibrationText() {
        val vibrationTextId: Int

        if (mVibrationFlag) {
            vibrationTextId = R.string.vibration_on
        } else {
            vibrationTextId = R.string.vibration_off
        }

        mBinding.switchVibration.setText(getString(vibrationTextId))
    }

    fun convertTimeToStr(time: Long, addMilisFlag: Boolean): String {
        val oneMinute: Long = 60000
        val minute: Long = time / oneMinute
        val remainMilis: Long = (time % oneMinute)
        val second: Long =  remainMilis / 1000

        if (!addMilisFlag) {
            return String.format("%02d:%02d", minute, second)
        }

        val milis = remainMilis % 1000 / 10

        return String.format("%02d:%02d:%02d", minute, second, milis)
    }

    fun setRestTime(onDisplayFlag: Boolean, forTimerFlag: Boolean) {
        if (onDisplayFlag) {
            if (forTimerFlag) {
                runOnUiThread {
                    mBinding.btnCenter.setText(convertTimeToStr(mRemainRestTime, true))
                }
            } else {
                mBinding.btnCenter.setText(convertTimeToStr(mRestTime, true))
            }

        } else {
            mBinding.setRestTime.setText(convertTimeToStr(mRestTime, false))
        }
    }

    fun stopTimer(pauseFlag: Boolean) {
        mTimerTask?.cancel()

        if (pauseFlag) {
            //다이얼로그 발생시키기
            mPauseDialog?.show()
        } else {
            mRemainRestTime = 0
            setRestTime(true, false)
            mRemainRestTime = mRestTime
            
            //진동
            if (mVibrationFlag) {
                var vibrator: Vibrator = getSystemService(Context.VIBRATOR_SERVICE) as Vibrator

                if (Build.VERSION.SDK_INT >= 26) {
                    vibrator.vibrate(VibrationEffect.createOneShot(100, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    vibrator.vibrate(100)
                }
            }

            //종료 알림
            setCenterBtnColor(false)
        }

        mRunningTimerFlag = false
    }

    //종료시 defaultFlag = false
    fun setCenterBtnColor(defaultFlag: Boolean) {
        val colorId: Int

        if (defaultFlag) {
            colorId = R.color.white
        } else {
            colorId = R.color.magenta
        }

        val colorInt: Int

        if (Build.VERSION.SDK_INT >= 23) {
            colorInt = getColor(colorId)
        } else {
            colorInt = resources.getColor(colorId)
        }

        mBinding.btnCenter.setBackgroundColor(colorInt)
    }
}
