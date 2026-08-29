package com.example.committeespinner;

import android.app.*;
import android.os.*;
import android.content.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import java.util.*;

public class MainActivity extends Activity {
    LinearLayout root, namesBox, historyBox;
    EditText monthsInput, personInput;
    TextView monthLabel, winner;
    ArrayList<String> all = new ArrayList<>(), remaining = new ArrayList<>(), history = new ArrayList<>();
    int months=10, current=1;
    Random random = new Random();

    int dp(float n){ return (int)(n*getResources().getDisplayMetrics().density+.5f); }
    TextView tv(String s,int size){ TextView t=new TextView(this); t.setText(s); t.setTextSize(size); t.setTextColor(Color.rgb(35,35,35)); t.setPadding(dp(8),dp(8),dp(8),dp(8)); return t; }
    Button btn(String s){ Button b=new Button(this); b.setText(s); return b; }

    @Override public void onCreate(Bundle b){
        super.onCreate(b); build();
    }

    void build(){
        root=new LinearLayout(this); root.setOrientation(LinearLayout.VERTICAL); root.setPadding(dp(18),dp(12),dp(18),dp(12));
        ScrollView sv=new ScrollView(this); sv.addView(root); setContentView(sv);

        TextView title=tv("🎡 Committee Spinner",28); title.setGravity(Gravity.CENTER); root.addView(title);
        root.addView(tv("Set months and add all committee members",15));

        LinearLayout settings=new LinearLayout(this); settings.setOrientation(LinearLayout.HORIZONTA
