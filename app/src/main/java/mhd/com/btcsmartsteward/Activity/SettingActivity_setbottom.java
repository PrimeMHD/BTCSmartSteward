package mhd.com.btcsmartsteward.Activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import mhd.com.btcsmartsteward.R;

public class SettingActivity_setbottom extends AppCompatActivity {

    private EditText huishengfudu_value;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_setbottom);
        huishengfudu_value=(EditText)findViewById(R.id.huishengfudu_value);
        SettingActivity_setbottom.this.setFinishOnTouchOutside(false);

    }
public void onBtnClick(View v){
    if(huishengfudu_value.getText().toString().trim().equals("")){
        Toast.makeText(this,"输入为空", Toast.LENGTH_SHORT).show();
    }
    Intent result=new Intent();
    result.putExtra("settedBottomRate",huishengfudu_value.getText().toString());

    setResult(Activity.RESULT_OK,result);
    finish();
}
}