package com.meng.bc.fragment;

import android.content.*;
import android.os.*;
import android.preference.*;
import com.meng.bc.*;

public class SJFSettings extends PreferenceFragment {

    Preference clean;
	private static SharedPreferences sp;

	public static void init(Context c) {
		sp = c.getSharedPreferences("settings", 0);
	}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getPreferenceManager().setSharedPreferencesName("settings");
        addPreferencesFromResource(R.xml.preference);


		/*	CheckBoxPreference cb=(CheckBoxPreference)findPreference("useLightTheme");
		 cb.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
		 @Override
		 public boolean onPreferenceChange(Preference preference,Object newValue){
		 //     Intent i = new Intent(getActivity().getApplicationContext(), PixivDownloadMain.class);
		 //     i.putExtra("setTheme", true);
		 //     getActivity().startActivity(i);
		 getActivity().startActivity(new Intent(getActivity().getApplicationContext(),MainActivity.class).putExtra("setTheme",true));
		 getActivity().finish();
		 getActivity().overridePendingTransition(0,0);
		 return true;
		 }
		 });
		 clean=findPreference(Data.preferenceKeys.cleanTmpFilesNow);
		 clean.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
		 @Override
		 public boolean onPreferenceClick(Preference preference){
		 File frameFileFolder = new File(Environment.getExternalStorageDirectory().getPath()+File.separator+"Pictures/picTool/tmp");
		 deleteFiles(frameFileFolder);
		 return true;
		 }
		 });
		 }
		 }*/

    }

	public static String getCookie() {
		return sp.getString("cookie", null);
	}

	public static void setCookie(String s) {
		putString("cookie", s);
	}

	public static String getVersion() {
		return sp.getString("newVersion", "0.0.0");
	}

	public static void setVersion(String v) {
		putString("newVersion", v);
	}

	public static long getUID() {
		return sp.getLong("uid", -1);
	}

	public static void setUID(long ac) {
		putLong("uid", ac);
	}

	public static long getLiveRoom() {
		return sp.getLong("liveRoom", -1);
	}

	public static void setLiveRoom(long lvr) {
		putLong("liveRoom", lvr);
	}

	public static long getPhone() {
		return sp.getLong("phone", -1);
	}

	public static void setPhone(long p) {
		putLong("phone", p);
	}

	public static String getPassword() {
		return sp.getString("cookie", null);
	}

	public static void setPassword(String s) {
		putString("cookie", s);
	}

	public static boolean getShowNotify() {
		return sp.getBoolean("notifi", false);
	}

	public static void setShowNotify(boolean b) {
		putBoolean("notifi", b);
	}

	public static void setOpenDrawer(boolean b) {
		putBoolean("opendraw", b);
	}

	public static boolean getOpenDrawer() {
		return sp.getBoolean("opendraw", true);
	}

	public static boolean getExit0() {
		return sp.getBoolean("exit", false);
	}

	public static void setExit0(boolean b) {
		putBoolean("exit", b);
	}

	private static void putLong(String key, long value) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putLong(key, value);
		editor.apply();
	}

	private static void putBoolean(String key, Boolean value) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putBoolean(key, value);
		editor.apply();
	}

	private static void putString(String key, String value) {
		SharedPreferences.Editor editor = sp.edit();
		editor.putString(key, value);
		editor.apply();
	}

}
	
