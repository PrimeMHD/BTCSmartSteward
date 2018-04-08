package mhd.com.btcsmartsteward.Fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import me.yokeyword.fragmentation.SupportFragment;
import mhd.com.btcsmartsteward.Activity.MainActivity;
import mhd.com.btcsmartsteward.Activity.SettingActivity_setasset;
import mhd.com.btcsmartsteward.R;


public class ParentThirdFragment extends SupportFragment {


    public TextView myasset;
    public TextView btc_in_hand;
    public TextView inspect1_set;
    public TextView inspect2_set;
    public TextView inspect3_set;
    public ImageView HelperInfo;
    public android.support.v7.widget.SwitchCompat[] mSwitchs;
    public Button btn_setbtc_inhand;
    MainActivity mainActivity;
    public ParentThirdFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSwitchs=new android.support.v7.widget.SwitchCompat[3];
        mainActivity = (MainActivity) this.getActivity();
    }

    // TODO: Rename and change types and number of parameters
    public static ParentThirdFragment newInstance() {
        ParentThirdFragment fragment = new ParentThirdFragment();
        return fragment;
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_parentthird, container, false);
        myasset=(TextView)v.findViewById(R.id.myasset);
        btc_in_hand=(TextView)v.findViewById(R.id.btc_in_hand);
        btn_setbtc_inhand=(Button)v.findViewById(R.id.btn_setbtc_inhand);
        inspect1_set=(TextView)v.findViewById(R.id.inspect1_set);
        inspect2_set=(TextView)v.findViewById(R.id.inspect2_set);
        inspect3_set=(TextView)v.findViewById(R.id.inspect3_set);
        mSwitchs[0]=(android.support.v7.widget.SwitchCompat)v.findViewById(R.id.switch1);
        mSwitchs[1]=(android.support.v7.widget.SwitchCompat)v.findViewById(R.id.switch2);
        mSwitchs[2]=(android.support.v7.widget.SwitchCompat)v.findViewById(R.id.switch3);
        HelperInfo=(ImageView)v.findViewById(R.id.helperinfo);
        mainActivity.setmSwitchs();
        return v;
    }




}
