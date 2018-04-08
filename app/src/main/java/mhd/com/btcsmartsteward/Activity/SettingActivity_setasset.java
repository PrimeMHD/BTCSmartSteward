package mhd.com.btcsmartsteward.Activity;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import mhd.com.btcsmartsteward.R;

public class SettingActivity_setasset extends AppCompatActivity {

    private Button button;
    private EditText chibishu_value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting_setasset);
        button=(Button)findViewById(R.id.submit_setting_setasset);
        chibishu_value=(EditText)findViewById(R.id.chibishu_value);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(chibishu_value.getText().toString().trim().equals("")){
                    Toast.makeText(SettingActivity_setasset.this,"输入为空", Toast.LENGTH_SHORT).show();
                    finish();
                }
                Intent result=new Intent();
                result.putExtra("chibishu",chibishu_value.getText().toString());
                setResult(Activity.RESULT_OK,result);
                finish();
            }
        });
    }
}
