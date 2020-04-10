package com.meng.bc.fragment;

import android.app.*;
import android.content.*;
import android.graphics.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.google.gson.*;
import com.meng.bc.*;
import com.meng.bc.activity.*;
import com.meng.bc.adapters.*;
import com.meng.bc.javaBean.*;
import com.meng.bc.libs.*;
import java.io.*;
import java.util.*;
import org.jsoup.*;

public class BaseIdFragment extends Fragment implements View.OnClickListener{

	public static final String typeUID = "uid";
	public static final String typeAv = "av";
	public static final String typeLive = "lv";
	public static final String typeCv = "cv";

	protected static final int SendDanmaku=0;
	protected static final int Silver=1;
	protected static final int Pack=2;
	protected static final int Sign=3;
	protected static final int SendVideoJudge=4;
	protected static final int LikeVideo=5;
	protected static final int VideoCoin1=6;
	protected static final int VideoCoin2=7;
	protected static final int Favorite=8;
	protected static final int SendCvJudge=9;
	protected static final int CvCoin1=10;
	protected static final int CvCoin2=11;
	protected static final int LikeArtical=12;


	protected long id;
	protected String type;

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}


	protected void sendBili(int opValue, String msg) {
		opSwitch(opValue, msg);
	}
	
	public String getEtText() {
		return null;
	}
	
	protected void boom2(){
		
	}

	private void opSwitch(final int opValue, final String msg) {
		MainActivity.instance.threadPool.execute(new Runnable(){

				@Override
				public void run() {
					switch (opValue) {
						case SendDanmaku:
							Tools.BilibiliTool.sendLiveDanmaku(msg, SJFSettings.getCookie(), id);
							break;
						case Silver:
							MainActivity.instance.runOnUiThread(new Runnable(){

									@Override
									public void run() {
										final EditText editText = new EditText(getActivity());
										new AlertDialog.Builder(getActivity()).setIcon(R.drawable.ic_launcher).setTitle("输入辣条数").setView(editText).setNegativeButton("取消", null).setPositiveButton("确定", new DialogInterface.OnClickListener() {
												@Override
												public void onClick(DialogInterface dialog, int which) {
													MainActivity.instance.threadPool.execute(new Runnable(){

															@Override
															public void run() {
																String content = editText.getText().toString();
																JsonObject liveToMainInfo=null;
																try {
																	liveToMainInfo = new JsonParser().parse(Tools.Network.getSourceCode("https://api.live.bilibili.com/live_user/v1/UserInfo/get_anchor_in_room?roomid=" + id)).getAsJsonObject().get("data").getAsJsonObject().get("info").getAsJsonObject();
																} catch (Exception e) {
																	return;
																}
																long uid=liveToMainInfo.get("uid").getAsLong();
																Tools.BilibiliTool.sendHotStrip(SJFSettings.getUID(), uid, id, Integer.parseInt(content), SJFSettings.getCookie());
															}
														});
												}
											}).show();
									}
								});
							break;
						case Pack:
							sendPackDialog();
							break;
						case Sign:
							Tools.BilibiliTool.sendLiveSign(SJFSettings.getCookie());
							break;
						case SendVideoJudge:
							Tools.BilibiliTool.sendVideoJudge(msg, id, SJFSettings.getCookie());
							break;
						case LikeVideo:
							Tools.BilibiliTool.sendAvLike(id, SJFSettings.getCookie());
							break;
						case VideoCoin1:
							Tools.BilibiliTool.sendAvCoin(1, id, SJFSettings.getCookie());
							break;
						case VideoCoin2:
							Tools.BilibiliTool.sendAvCoin(2, id, SJFSettings.getCookie());
							break;
						case Favorite:
							MainActivity.instance.showToast("未填坑");
							break;
						case SendCvJudge:
							Tools.BilibiliTool.sendArticalJudge(id, msg, SJFSettings.getCookie());
							break;
						case CvCoin1:
							Tools.BilibiliTool.sendCvCoin(1, id, SJFSettings.getCookie());
							break;
						case CvCoin2:
							Tools.BilibiliTool.sendCvCoin(2, id, SJFSettings.getCookie());
							break;
						case LikeArtical:
							Tools.BilibiliTool.sendCvLike(id, SJFSettings.getCookie());
							break;
					}
				}
			});
	}

	private void sendPackDialog() {
		JsonObject liveToMainInfo = new JsonParser().parse(Tools.Network.getSourceCode("https://api.live.bilibili.com/live_user/v1/UserInfo/get_anchor_in_room?roomid=" + id)).getAsJsonObject().get("data").getAsJsonObject().get("info").getAsJsonObject();
		final long uid=liveToMainInfo.get("uid").getAsLong();
		final GiftBag liveBag = new Gson().fromJson(Tools.Network.getSourceCode("https://api.live.bilibili.com/xlive/web-room/v1/gift/bag_list?t=" + System.currentTimeMillis(), SJFSettings.getCookie()), GiftBag.class);
		if (liveBag.data.list.size() == 0) {
			//	MainActivity.instance.showToast("包裹中什么也没有");
		}
		getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					ListView listView=new ListView(getActivity());
					new AlertDialog.Builder(getActivity()).setView(listView).setTitle("选择").show();
					final GiftAdapter giftAdapter = new GiftAdapter(getActivity(), liveBag.data.list);
					listView.setAdapter(giftAdapter);
					MainActivity.instance.showToast("共有" + getStripCount(liveBag.data.list) + "辣条");
					listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
							@Override
							public void onItemClick(final AdapterView<?> parent, View view, final int p, long itemid) {
								final EditText editText = new EditText(getActivity());
								editText.setHint("要赠送的数量");
								new AlertDialog.Builder(getActivity()).setView(editText).setTitle("编辑").setPositiveButton("确定", new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface p11, int p2) {
											MainActivity.instance.threadPool.execute(new Runnable() {
													@Override
													public void run() {
														int num=Integer.parseInt(editText.getText().toString());
														if (num > getStripCount(liveBag.data.list)) {
															MainActivity.instance.showToast("辣条不足");	
															return;
														}
														for (GiftBag.ListItem i:liveBag.data.list) {
															if (i.gift_name.equals("辣条")) {
																if (num > i.gift_num) {
																	sendHotStrip(SJFSettings.getUID(), uid, id, i.gift_num, SJFSettings.getCookie(), i);
																	num -= i.gift_num;
																	i.gift_num = 0;
																} else {
																	sendHotStrip(SJFSettings.getUID(), uid, id, num, SJFSettings.getCookie(), i);											
																	i.gift_num -= num;
																	break;	
																}
															}
														}
														if (getStripCount(liveBag.data.list) == 0) {
															MainActivity.instance.showToast("已送出全部礼物🎁");
														}
														for (int i=0;i < liveBag.data.list.size();++i) {
															if (liveBag.data.list.get(i).gift_name.equals("辣条") && liveBag.data.list.get(i).gift_num == 0) {
																liveBag.data.list.remove(i);
															}
														}										
														getActivity().runOnUiThread(new Runnable() {
																@Override
																public void run() {
																	giftAdapter.notifyDataSetChanged();
																}
															});
													}
												});
										}
									}).setNegativeButton("取消", null).show();
							}
						});
					listView.setOnItemLongClickListener(new OnItemLongClickListener() {

							@Override
							public boolean onItemLongClick(final AdapterView<?> p1, View p2, final int p3, long p4) {
								MainActivity.instance.threadPool.execute(new Runnable() {
										@Override
										public void run() {
											sendHotStrip(SJFSettings.getUID(), uid, id, liveBag.data.list.get(p3).gift_num, SJFSettings.getCookie(), liveBag.data.list.get(p3));
											liveBag.data.list.remove(p3);
											if (liveBag.data.list.size() == 0) {
												MainActivity.instance.showToast("已送出全部礼物🎁");
											}
											getActivity().runOnUiThread(new Runnable() {
													@Override
													public void run() {
														giftAdapter.notifyDataSetChanged();
													}
												});
										}
									});
								return true;
							}
						});
				}
			});
	}

	private int getStripCount(ArrayList<GiftBag.ListItem> list) {
		int ii=0;
		for (GiftBag.ListItem i:list) {
			if (i.gift_name.equals("辣条")) {
				ii += i.gift_num;
			}
		}
		return ii;
	}

	public static void sendHotStrip(long uid, long ruid, long roomID, int num, String cookie,  GiftBag.ListItem liveBagDataList) {
		Connection connection = Jsoup.connect("https://api.live.bilibili.com/gift/v2/live/bag_send");
		String csrf = Tools.Network.cookieToMap(cookie).get("bili_jct");
		connection.userAgent(MainActivity.instance.userAgent)
			.headers(Tools.liveHead)
			.ignoreContentType(true)
			.referrer("https://live.bilibili.com/" + roomID)
			.cookies(Tools.Network.cookieToMap(cookie))
			.method(Connection.Method.POST)
			.data("uid", String.valueOf(uid))
			.data("gift_id", String.valueOf(liveBagDataList.gift_id))
			.data("ruid", String.valueOf(ruid))
			.data("gift_num", String.valueOf(num))
			.data("bag_id", String.valueOf(liveBagDataList.bag_id))
			.data("platform", "pc")
			.data("biz_code", "live")
			.data("biz_id", String.valueOf(roomID))
			.data("rnd", String.valueOf(System.currentTimeMillis() / 1000))
			.data("storm_beat_id", "0")
			.data("metadata", "")
			.data("price", "0")
			.data("csrf_token", csrf)
			.data("csrf", csrf)
			.data("visit_id", "");	
		Connection.Response response=null;
		try {
			response = connection.execute();
		} catch (IOException e) {
			MainActivity.instance.showToast("连接出错");
			return;
		}
		if (response.statusCode() != 200) {
			MainActivity.instance.showToast(String.valueOf(response.statusCode()));
		}
		JsonParser parser = new JsonParser();
		JsonObject obj = parser.parse(response.body()).getAsJsonObject();
		MainActivity.instance.showToast(obj.get("message").getAsString());
	}

	protected void saveBitmap(String bitName, Bitmap mBitmap) throws Exception {
		File f = new File(Environment.getExternalStorageDirectory() + "/pictures/" + bitName + ".png");
		f.createNewFile();
		FileOutputStream fOut = null;
		fOut = new FileOutputStream(f);
		mBitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
		fOut.flush();
		fOut.close();
		getActivity().getApplicationContext().sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(f)));
	}
	
	@Override
	public void onClick(View p1) {
		switch (p1.getId()) {
			case R.id.cv_fragmentButton_send:
						sendBili(SendCvJudge, getEtText());
				break;
			case R.id.cv_fragmentButton_zan:
				sendBili(LikeArtical, "");
				break;
			case R.id.cv_fragmentButton_coin1:
				sendBili(CvCoin1, "");
				break;
			case R.id.cv_fragmentButton_coin2:
				sendBili(CvCoin2, "");
				break;
			case R.id.cv_fragmentButton_favorite:
				sendBili(Favorite, "");
				break;
			case R.id.av_fragmentButton_send:
				sendBili(SendVideoJudge, getEtText());
				break;
			case R.id.av_fragmentButton_zan:
				sendBili(LikeVideo, "");
				break;
			case R.id.av_fragmentButton_coin1:
				sendBili(VideoCoin1, "");
				break;
			case R.id.av_fragmentButton_coin2:
				sendBili(VideoCoin2, "");
				break;
			case R.id.av_fragmentButton_favorite:
				sendBili(Favorite, "");
				break;
			case R.id.av_fragment2_ButtonGetDanmakuSender:
				p1.setVisibility(View.GONE);
				MainActivity.instance.showToast("开始连接");
				MainActivity.instance.threadPool.execute(new Runnable(){

						@Override
						public void run() {
							boom2();
						}
					});
				break;
			case R.id.live_fragmentButton_send:
				sendBili(SendDanmaku, getEtText());
				break;
			case R.id.live_fragmentButton_pack:
				sendBili(Pack, "");
				break;
			case R.id.live_fragmentButton_silver:
				sendBili(Silver, "");
				break;
				/*	case R.id.livefragmentButtonDownload:
				 // 本地存储路径
				 final JsonArray ja = liveInfo.get("data").getAsJsonObject().get("durl").getAsJsonArray();
				 Uri uri = Uri.parse(ja.get(0).getAsJsonObject().get("url").getAsString());
				 DownloadManager downloadManager=(DownloadManager)getActivity().getSystemService(getActivity().DOWNLOAD_SERVICE);
				 DownloadManager.Request request=new DownloadManager.Request(uri);
				 long downloadId=downloadManager.enqueue(request);
				 break;*/
				
		}
	}
}
