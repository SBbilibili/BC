package com.meng.bc.libs;

import android.content.*;
import android.graphics.*;
import android.view.*;
import android.widget.*;
import com.google.gson.*;
import com.meng.bc.*;
import com.meng.bc.activity.*;
import com.meng.bc.activity.main.*;
import com.meng.bc.fragment.*;
import com.meng.bc.javaBean.*;
import java.io.*;

public class UserInfoHeaderView extends LinearLayout {

    private ImageView ivHead;
    private TextView tvName;
    private TextView tvBMain;
	private TextView tvBLive;

    public UserInfoHeaderView(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.main_account_list_header, this);
        ivHead = (ImageView) findViewById(R.id.imageView);
        tvName = (TextView) findViewById(R.id.textView1);
        tvBMain = (TextView) findViewById(R.id.textView2);
		tvBLive = (TextView) findViewById(R.id.textView3);
		if (SJFSettings.getUID() == -1) {
			ivHead.setImageResource(R.drawable.ic_launcher);
			ivHead.setOnClickListener(new OnClickListener(){

					@Override
					public void onClick(View p1) {
						Intent inte=new Intent(MainActivity.instance, Login.class);
						MainActivity.instance.startActivity(inte);
					}
				});
		} else {
			tvName.setVisibility(View.GONE);
			tvBMain.setVisibility(View.GONE);
			tvBLive.setVisibility(View.GONE);
			View v=MainActivity.instance.mDrawerList.getChildAt(1);
			if (v instanceof MengLiveControl) {
				MainActivity.instance.mDrawerList.removeHeaderView(v);
			}
			File imf = new File(MainActivity.instance.mainDic + "bilibili/" + SJFSettings.getUID() + ".jpg");
			if (imf.exists()) {
				Bitmap b = BitmapFactory.decodeFile(imf.getAbsolutePath());
				ivHead.setImageBitmap(b);
			} else {
				MainActivity.instance.threadPool.execute(new DownloadImageRunnable(ivHead, SJFSettings.getUID(), DownloadImageRunnable.BilibiliUser));
			}
			MainActivity.instance.threadPool.execute(new Runnable() {
					@Override
					public void run() {
						final BilibiliMyInfo info = MainActivity.instance.gson.fromJson(Tools.BilibiliTool.getMyInfo(SJFSettings.getCookie()), BilibiliMyInfo.class);
						if (info.code != 0) {
							MainActivity.instance.showToast("cookie过期");
							return;
						}
						UidToLiveRoom sjb = MainActivity.instance.gson.fromJson(Tools.BilibiliTool.getLiveRoomFromUid(info.data.mid), UidToLiveRoom.class);
						MainActivity.instance.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									tvName.setVisibility(View.VISIBLE);
									tvBMain.setVisibility(View.VISIBLE);
									tvName.setText(info.data.name);
									tvBMain.setText("主站 Lv." + info.data.level);
								}
							});
						try {
							String json = Tools.Network.getSourceCode("https://api.live.bilibili.com/live_user/v1/UserInfo/get_anchor_in_room?roomid=" + sjb.data.roomid);
							JsonObject obj = new JsonParser().parse(json).getAsJsonObject();
							final JsonObject obj2=obj.get("data").getAsJsonObject().get("level").getAsJsonObject().get("master_level").getAsJsonObject();
							MainActivity.instance.runOnUiThread(new Runnable() {
									@Override
									public void run() {
										JsonArray ja = obj2.get("next").getAsJsonArray();
										tvBLive.setVisibility(View.VISIBLE);
										tvBLive.setText("主播 Lv." + obj2.get("level").getAsInt() + "(" + obj2.get("anchor_score").getAsInt() + "/" + ja.get(1).getAsInt() + ")");
										if (MainActivity.instance.mDrawerList.getHeaderViewsCount() == 1) {
											MainActivity.instance.mDrawerList.addHeaderView(new MengLiveControl(MainActivity.instance));
										}
									}
								});
						} catch (Exception e) {
							MainActivity.instance.runOnUiThread(new Runnable(){

									@Override
									public void run() {
										tvBLive.setVisibility(View.VISIBLE);
										tvBLive.setText("未开通直播间");
									}
								});
						}
					}
				});
		}
    }
}
