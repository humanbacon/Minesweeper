package com.humanbacon.minesweeper.game.appinterface;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Locale;
import java.util.Scanner;

import com.humanbacon.minesweeper.R;
import com.humanbacon.minesweeper.game.appinterface.MainActivity2.CheckBoxListener;
import com.humanbacon.minesweeper.game.appinterface.MainActivity2.SpinnerListener;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Spinner;

public class HelpActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);			
		setContentView(R.layout.activity_help);
		this.overridePendingTransition(R.anim.r_to_l_enter, R.anim.r_to_l_leave);
	}
}
