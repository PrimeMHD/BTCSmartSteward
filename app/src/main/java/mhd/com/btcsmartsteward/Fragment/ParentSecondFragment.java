package mhd.com.btcsmartsteward.Fragment;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import me.yokeyword.fragmentation.SupportFragment;
import mhd.com.btcsmartsteward.R;


public class ParentSecondFragment extends SupportFragment {

    public WebView webView;
    public ParentSecondFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static ParentSecondFragment newInstance() {
        ParentSecondFragment fragment = new ParentSecondFragment();
        //Bundle args = new Bundle();
        //args.putString(ARG_PARAM1, param1);

        //fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view=inflater.inflate(R.layout.fragment_parentsecond, container, false);
        webView=(WebView)view.findViewById(R.id.info_webview);
        //webView.setBackgroundColor(2); // 设置背景色
        //webView.getBackground().setAlpha(2); // 设置填充透明度 范围：0-255
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.loadUrl("http://www.bitcoin86.com/news/");
        return view;
    }

}
