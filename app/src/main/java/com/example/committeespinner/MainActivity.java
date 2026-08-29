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

        LinearLayout settings=new LinearLayout(this); settings.setOrientation(LinearLayout.HORIZONTAL);
        monthsInput=new EditText(this); monthsInput.setHint("Months"); monthsInput.setInputType(2); monthsInput.setText("10");
        settings.addView(monthsInput,new LinearLayout.LayoutParams(0,dp(60),1));
        Button set=btn("Set Months"); settings.addView(set,new LinearLayout.LayoutParams(0,dp(60),1));
        root.addView(settings);
        set.setOnClickListener(v->{ try{months=Math.max(1,Integer.parseInt(monthsInput.getText().toString())); current=1; history.clear(); historyBox.removeAllViews(); monthLabel.setText("Month 1 of "+months); winner.setText("Ready to spin");}catch(Exception e){}});

        monthLabel=tv("Month 1 of 10",20); monthLabel.setGravity(Gravity.CENTER); root.addView(monthLabel);
        winner=tv("Ready to spin",26); winner.setGravity(Gravity.CENTER); winner.setPadding(0,dp(24),0,dp(24)); root.addView(winner);

        Button spin=btn("🎯  SPIN");
        root.addView(spin,new LinearLayout.LayoutParams(-1,dp(62)));
        spin.setOnClickListener(v->spin());

        root.addView(tv("Members",20));
        LinearLayout add=new LinearLayout(this); personInput=new EditText(this); personInput.setHint("Enter name"); add.addView(personInput,new LinearLayout.LayoutParams(0,dp(60),1));
        Button addBtn=btn("Add"); add.addView(addBtn,new LinearLayout.LayoutParams(dp(100),dp(60))); root.addView(add);
        addBtn.setOnClickListener(v->addName());

        namesBox=new LinearLayout(this); namesBox.setOrientation(LinearLayout.VERTICAL); root.addView(namesBox);

        Button reset=btn("↻  New Committee / Reset"); root.addView(reset); reset.setOnClickListener(v->{remaining=new ArrayList<>(all); history.clear(); current=1; monthLabel.setText("Month 1 of "+months); winner.setText("Ready to spin"); refresh(); historyBox.removeAllViews();});

        root.addView(tv("History",20)); historyBox=new LinearLayout(this); historyBox.setOrientation(LinearLayout.VERTICAL); root.addView(historyBox);
        load();
    }

    void addName(){
        String n=personInput.getText().toString().trim();
        if(n.isEmpty()) return;
        all.add(n); remaining.add(n); personInput.setText(""); save(); refresh();
    }
    void spin(){
        if(current>months){ winner.setText("All months completed 🎉"); return; }
        if(remaining.isEmpty()){ winner.setText("No members left. Add members or reset."); return; }
        int i=random.nextInt(remaining.size()); String n=remaining.remove(i);
        winner.setText("🏆 "+n);
        history.add("Month "+current+":  "+n);
        historyBox.addView(tv(history.get(history.size()-1),18));
        current++;
        if(current<=months) monthLabel.setText("Month "+current+" of "+months); else monthLabel.setText("Completed "+months+" months");
        save(); refresh();
    }
    void refresh(){
        namesBox.removeAllViews();
        for(String n:all){
            LinearLayout row=new LinearLayout(this); TextView x=tv((remaining.contains(n)?"• ":"✓ ")+n,18);
            row.addView(x,new LinearLayout.LayoutParams(0,dp(48),1));
            Button del=btn("Delete"); row.addView(del,new LinearLayout.LayoutParams(dp(90),dp(48)));
            del.setOnClickListener(v->{all.remove(n); remaining.remove(n); refresh(); save();});
            namesBox.addView(row);
        }
    }
    void save(){
        getPreferences(0).edit().putString("all",String.join("\u001F",all)).putString("remaining",String.join("\u001F",remaining))
        .putString("history",String.join("\u001F",history)).putInt("months",months).putInt("current",current).apply();
    }
    void load(){
        android.content.SharedPreferences p=getPreferences(0); months=p.getInt("months",10); current=p.getInt("current",1);
        monthsInput.setText(""+months); monthLabel.setText("Month "+current+" of "+months);
        String a=p.getString("all",""), r=p.getString("remaining",""), h=p.getString("history","");
        if(!a.isEmpty()) all.addAll(Arrays.asList(a.split("\u001F",-1)));
        if(!r.isEmpty()) remaining.addAll(Arrays.asList(r.split("\u001F",-1)));
        if(!h.isEmpty()) for(String s:h.split("\u001F",-1)){history.add(s); historyBox.addView(tv(s,18));}
        refresh();
    }
}