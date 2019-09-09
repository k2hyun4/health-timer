package tanned.bear

import android.databinding.DataBindingUtil
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import tanned.bear.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var mBinding: ActivityMainBinding;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.activity = this@MainActivity;
    }

    //1) 센터 클릭, 타이머 중 센터 클릭
    fun onClickCenter() {

    }

    //3) 휴식시간 종료
    fun onOutOfRestTime() {

    }

    //4) 상단부 - 진동 스위치
    fun onClickSwitchVibration() {

    }

    //4) 상단부 - 휴식시간 설정 팝업
    fun onClickShowRestTimePopup() {

    }

    //4) 상단부 - 세트수 줄이기
    fun onClickMinusSetCount() {

    }

    //4) 상단부 - 세트수 초기화
    fun onClickResetSetCount() {

    }

    //5) 설정 팝업 - 휴식시간 설정
    fun onClickSetResetTime() {

    }
}
