package mhd.com.btcsmartsteward.Activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import mhd.com.btcsmartsteward.R;

public class SettingActivity_setinterval extends AppCompatActivity {

    private EditText gaojiatixing;
    private EditText dijiatixing;
    private Button button;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_setting_setinterval);
        SettingActivity_setinterval.this.setFinishOnTouchOutside(false);
        gaojiatixing=(EditText)findViewById(R.id.gaojiatixing_value);
        dijiatixing=(EditText)findViewById(R.id.dijiatixing_value);


    }

    public void onBtnClick(View v){
        if(gaojiatixing.getText().toString().trim().equals("")||
                dijiatixing.getText().toString().trim().equals("")){
            Toast.makeText(this,"输入为空", Toast.LENGTH_SHORT).show();
        }
        Intent result=new Intent();
        result.putExtra("gaojiatixing",gaojiatixing.getText().toString());
        result.putExtra("dijiatixing",dijiatixing.getText().toString());
        setResult(Activity.RESULT_OK,result);
        finish();
    }

}
