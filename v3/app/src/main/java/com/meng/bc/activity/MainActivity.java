package com.meng.bc.activity;

import android.*;
import android.app.*;
import android.content.*;
import android.content.pm.*;
import android.content.res.*;
import android.net.*;
import android.os.*;
import android.support.v4.widget.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.google.gson.*;
import com.google.gson.reflect.*;
import com.meng.bc.*;
import com.meng.bc.adapters.*;
import com.meng.bc.fragment.*;
import com.meng.bc.javaBean.*;
import com.meng.bc.libs.*;
import com.meng.bc.materialDesign.*;
import com.meng.bc.update.*;
import java.io.*;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.regex.*;
import org.java_websocket.client.*;

import com.meng.bc.R;


public class MainActivity extends Activity {

    public static MainActivity instance;
    private DrawerLayout mDrawerLayout;
    public ListView mDrawerList;
    private RelativeLayout rightDrawer;
    public ListView lvRecent;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerArrowDrawable drawerArrow;

	public HashMap<String,Fragment> fragments=new HashMap<>();
    public TextView tvMemory;
    public final String userAgent = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:28.0) Gecko/20100101 Firefox/28.0";
    public Gson gson = new Gson();

    public String mainDic = "";

    public static boolean onWifi = false;

	public static final String AccountManager = "管理账号";
	public static final String Settings = "设置";

	public ExecutorService threadPool = Executors.newCachedThreadPool();

	public SanaeConnect sanaeConnect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);
        instance = this;
		ExceptionCatcher.getInstance().init(getApplicationContext());
        SJFSettings.init(this);
		//  DataBaseHelper.init(getBaseContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 321);
        }
        findViews();
        setListener();
        if (SJFSettings.getOpenDrawer()) {
            mDrawerLayout.openDrawer(mDrawerList);
        } else {
            mDrawerLayout.closeDrawer(mDrawerList);
        }
        mainDic = Environment.getExternalStorageDirectory() + "/Pictures/grzx/";
        File ff = new File(mainDic + "group/");
        if (!ff.exists()) {
            ff.mkdirs();
        }
        File f2 = new File(mainDic + "user/");
        if (!f2.exists()) {
            f2.mkdirs();
        }
        File f3 = new File(mainDic + "bilibili/");
        if (!f3.exists()) {
            f3.mkdirs();
        }
        File f4 = new File(mainDic + ".nomedia");
        if (!f4.exists()) {
            try {
                f4.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
		try {
			sanaeConnect = new SanaeConnect();
			sanaeConnect.addOnOpenAction(new WebSocketOnOpenAction(){

					@Override
					public int useTimes() {
						return 1;
					}

					@Override
					public void action(WebSocketClient wsc) {
						try {
							PackageInfo packageInfo = MainActivity.instance.getPackageManager().getPackageInfo(MainActivity.instance.getPackageName(), 0);
							CheckNewBean cnb=new CheckNewBean();
							cnb.packageName = packageInfo.packageName;
							cnb.nowVersionCode = packageInfo.versionCode;
							wsc.send(new Gson().toJson(cnb));
						} catch (PackageManager.NameNotFoundException e) {
							MainActivity.instance.showToast(e.toString());
						}
					}
				});
			sanaeConnect.connect();
		} catch (Exception e) {
			showToast(e.toString());
		}
		mDrawerList.addHeaderView(new UserInfoHeaderView(this));
        onWifi = ((ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE)).getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected();
	}

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 321) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
                    showToast("缺失权限会使应用工作不正常");
                } else {
					//      showFragment(NaiFragment.class);
                }
            }
        }
    }

    private void setListener() {
        drawerArrow = new DrawerArrowDrawable(this) {
            @Override
            public boolean isLayoutRtl() {
                return false;
            }
        };
        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, drawerArrow, R.string.open, R.string.close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
                invalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        mDrawerList.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, android.R.id.text1, new String[]{
															"输入ID","AVBV转换", "设置", "退出"
														}));
		lvRecent.addHeaderView(tvMemory);

		threadPool.execute(new Runnable(){

				@Override
				public void run() {
					while (true) {
						try {
							Thread.sleep(1000);
						} catch (InterruptedException e) {}
						runOnUiThread(new Runnable(){

								@Override
								public void run() {
									tvMemory.setText("最大内存:" + (Runtime.getRuntime().maxMemory() * 1.0f / (1024 * 1024)) + "M\n当前分配:" + (Runtime.getRuntime().totalMemory() * 1.0f / (1024 * 1024)) + "M");
								}
							});
					}
				}
			});
		lvRecent.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
					String s=(String)p1.getAdapter().getItem(p3);
					showFragment(s);
					showToast(s);
				}
			});
        mDrawerList.setOnItemClickListener(itemClickListener);
        lvRecent.setOnItemClickListener(itemClickListener);
	}

    AdapterView.OnItemClickListener itemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (view instanceof TextView) {
                switch (((TextView) view).getText().toString()) {
					case "输入ID":
						final View seView = getLayoutInflater().inflate(R.layout.input_id_selecter, null);
						final EditText et = (EditText) seView.findViewById(R.id.input_id_selecterEditText_id);
						new AlertDialog.Builder(MainActivity.this)
							.setTitle("输入ID")
							.setView(seView)
							.setNegativeButton("取消", null)
							.setPositiveButton("确定", new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									String content = et.getText().toString();
									RadioButton uid,av,live,cv;
									uid = (RadioButton) seView.findViewById(R.id.input_id_selecterRadioButton_uid);
									av = (RadioButton)seView.findViewById(R.id.input_id_selecterRadioButton_av);
									live = (RadioButton) seView.findViewById(R.id.input_id_selecterRadioButton_live);
									cv = (RadioButton) seView.findViewById(R.id.input_id_selecterRadioButton_cv);
									if (uid.isChecked()) {
										showFragment(UidFragment.class, BaseIdFragment.typeUID , getId(content));
									} else if (av.isChecked()) {
										if (content.startsWith("BV")) {
											showFragment(AvFragment.class, BaseIdFragment.typeAv , AvBvConverter.decode(content));
										} else {
											showFragment(AvFragment.class, BaseIdFragment.typeAv , getId(content));
										}
									} else if (live.isChecked()) {
										showFragment(LiveFragment.class, BaseIdFragment.typeLive , getId(content));
									} else if (cv.isChecked()) {
										showFragment(CvFragment.class, BaseIdFragment.typeCv , getId(content));
									}
								}
							}).show();
						break;
					case "AVBV转换":
						showFragment(AvBvConvertFragment.class, "AVBV转换");
						break;
                    case "设置":
                        showFragment(SJFSettings.class, Settings);
                        break;
                    case "退出":
                        if (SJFSettings.getExit0()) {
                            System.exit(0);
                        } else {
                            finish();
                        }
                        break;
                }
            }
            mDrawerToggle.syncState();
            mDrawerLayout.closeDrawer(mDrawerList);
            mDrawerLayout.closeDrawer(rightDrawer);
        }
    };

	private int getId(String s) {
		String reg = "\\D{0,}(\\d{3,})\\D{0,}";
		Pattern p2 = Pattern.compile(reg);  
		Matcher m2 = p2.matcher(s);  
		int historyHighestLevel = -1;
		if (m2.find()) {  
			historyHighestLevel = Integer.parseInt(m2.group(1));
		}
		return historyHighestLevel;
	}

    private void findViews() {
        tvMemory = new TextView(this);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerList = (ListView) findViewById(R.id.navdrawer);
        rightDrawer = (RelativeLayout) findViewById(R.id.right_drawer);
        lvRecent = (ListView) findViewById(R.id.right_list);
    }


	public void showFragment(String id) {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		Fragment frag = fragments.get(id);
		if (frag == null) 	{
			throw new RuntimeException("获取不存在的碎片");
		}
		hideFragment();
		transaction.show(frag);
		transaction.commit();
	}

	public <T extends Fragment> void showFragment(Class<T> c, String type) {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		Fragment frag = fragments.get(type);
		if (frag == null) {
			try {
				Class<?> cls = Class.forName(c.getName());
				frag = (Fragment) cls.newInstance();
				fragments.put(type, frag);
				transaction.add(R.id.main_activityLinearLayout, frag);	
			} catch (Exception e) {
				throw new RuntimeException("反射爆炸:" + e.toString());
			}
		}
		hideFragment();
		transaction.show(frag);
        transaction.commit();
	}

	public <T extends Fragment> void showFragment(Class<T> c, String type, long id) {
		FragmentTransaction transaction = getFragmentManager().beginTransaction();
		Fragment frag = fragments.get(type + id);
		if (frag == null) {
			try {
				Class<?> cls = Class.forName(c.getName());
				Constructor con = cls.getConstructor(String.class, long.class);
				frag = (Fragment) con.newInstance(type, id);
				fragments.put(type + id, frag);
				transaction.add(R.id.main_activityLinearLayout, frag);	
			} catch (Exception e) {
				throw new RuntimeException("反射爆炸:" + e.toString());
			}
		}
		hideFragment();
		transaction.show(frag);
        transaction.commit();
	}

	public void renameFragment(String origin, String newName) {
		Fragment f=fragments.get(origin);
		fragments.put(newName, f);
	}

    public void hideFragment() {
		FragmentTransaction ft=getFragmentManager().beginTransaction();
        for (Fragment f : fragments.values()) {
			ft.hide(f);
        }
		ft.commit();
    }

	public void removeFragment(String id) {
		if (!fragments.containsKey(id)) {
			throw new RuntimeException("no such key");
		}
		Fragment f = fragments.get(id);
		Iterator<Map.Entry<String,Fragment>> iterator = fragments.entrySet().iterator();
		while (iterator.hasNext()) {
			Map.Entry<String,Fragment> entry = iterator.next();
			if (entry.getValue() == f) {
				iterator.remove();
			}
		}
		getFragmentManager().beginTransaction().remove(f).commit();
		fragments.remove(id);
	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

	/*  public void doVibrate(long time) {
	 Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
	 if (vibrator != null) {
	 vibrator.vibrate(time);
	 }
	 }*/

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK || keyCode == KeyEvent.KEYCODE_MENU) {
            if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
                mDrawerLayout.closeDrawer(mDrawerList);
            } else {
                mDrawerLayout.openDrawer(mDrawerList);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    public void showToast(final String msg) {
        runOnUiThread(new Runnable() {

				@Override
				public void run() {
					Toast.makeText(MainActivity.this, msg, Toast.LENGTH_LONG).show();
				}
			});
    }
}
