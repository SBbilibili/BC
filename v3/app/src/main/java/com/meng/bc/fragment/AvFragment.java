package com.meng.bc.fragment;

import android.graphics.*;
import android.os.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import android.widget.AdapterView.*;
import com.meng.bc.*;
import com.meng.bc.activity.*;
import com.meng.bc.adapters.*;
import com.meng.bc.javaBean.*;
import com.meng.bc.libs.*;
import com.meng.bc.update.*;
import java.io.*;
import java.util.*;
import org.jsoup.*;
import org.xmlpull.v1.*;

import android.view.View.OnLongClickListener;

public class AvFragment extends BaseIdFragment {

	private Button send,zan,coin1,coin2,favorite,findSender;
	private EditText et;
	private TextView info;
	private VideoInfo videoInfo;
	private ImageView ivPreview;
	private Bitmap preview;
	private ArrayList<DanmakuBean> danmakuList=null;
	private SenderAdapter senderAdapter;
	private ListView lv;
	private Spinner part;

	public AvFragment(String type, long liveId) {
		this.type = type;
		id = liveId;
	}

	public TabHost tab;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.tab_fragment, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // TODO: Implement this method
        super.onViewCreated(view, savedInstanceState);
        tab = (TabHost) view.findViewById(android.R.id.tabhost);
		tab.setup();
        LayoutInflater layoutInflater=LayoutInflater.from(getActivity()); 
		layoutInflater.inflate(R.layout.av_fragment, tab.getTabContentView()); 
		layoutInflater.inflate(R.layout.av_fragment2, tab.getTabContentView());
		tab.addTab(tab.newTabSpec("tab1").setIndicator("视频" , null).setContent(R.id.av_fragmentLinearLayout));
        tab.addTab(tab.newTabSpec("tab2").setIndicator("弹幕" , null).setContent(R.id.av_fragment2LinearLayout));
		lv = (ListView) view.findViewById(R.id.av_fragment2_ListView1);
		send = (Button) view.findViewById(R.id.av_fragmentButton_send);
		part = (Spinner) view.findViewById(R.id.av_fragment2Spinner);
		findSender = (Button) view.findViewById(R.id.av_fragment2_ButtonGetDanmakuSender);
		zan = (Button) view.findViewById(R.id.av_fragmentButton_zan);
		coin1 = (Button) view.findViewById(R.id.av_fragmentButton_coin1);
		coin2 = (Button) view.findViewById(R.id.av_fragmentButton_coin2);
		favorite = (Button) view.findViewById(R.id.av_fragmentButton_favorite);
		et = (EditText) view.findViewById(R.id.av_fragmentEditText_msg);
		ivPreview = (ImageView) view.findViewById(R.id.av_fragmentImageView);  
		info = (TextView) view.findViewById(R.id.av_fragmentTextView_info);
		zan.setOnClickListener(this);
		coin1.setOnClickListener(this);
		coin2.setOnClickListener(this);
		favorite.setOnClickListener(this);
		send.setOnClickListener(this);
		findSender.setOnClickListener(this);
		lv.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> p1, View p2, int p3, long p4) {
					DanmakuBean db=(DanmakuBean) p1.getItemAtPosition(p3);
					if (db.uid == -1) {
						MainActivity.instance.showToast("请等待获取id完成");
						return;
					}
					MainActivity.instance.showFragment(UidFragment.class, BaseIdFragment.typeUID , db.uid);
				}
			});
		ivPreview.setOnLongClickListener(new OnLongClickListener(){

				@Override
				public boolean onLongClick(View p1) {
					try {
						saveBitmap(type + id, preview);
						MainActivity.instance.showToast("图片已保存至" + MainActivity.instance.mainDic + type + id + ".png");
					} catch (Exception e) {}
					return true;
				}
			});
		MainActivity.instance.threadPool.execute(new Runnable(){

				@Override
				public void run() {
					videoInfo = MainActivity.instance.gson.fromJson(Tools.Network.getSourceCode("http://api.bilibili.com/x/web-interface/view?aid=" + id), VideoInfo.class);	
					if (videoInfo.code != 0) {
						MainActivity.instance.showToast(videoInfo.message);
						return;
					}
					getActivity().runOnUiThread(new Runnable(){

							@Override
							public void run() {
								info.setText(videoInfo.toString());
								MainActivity.instance.renameFragment(type + id, videoInfo.data.title);
								ArrayList<String> as=new ArrayList<>();
								for (int i = 0;i < videoInfo.data.pages.size();++i) {
									as.add("page" + (i + 1));
								}
								part.setAdapter(new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_dropdown_item, as));
							}
						});
					try {
						Connection.Response response = Jsoup.connect(videoInfo.data.pic).ignoreContentType(true).execute();
						byte[] img = response.bodyAsBytes();
						preview = BitmapFactory.decodeByteArray(img, 0, img.length);
						getActivity().runOnUiThread(new Runnable(){

								@Override
								public void run() {
									ivPreview.setImageBitmap(preview);
								}

							});
					} catch (IOException e) {
						throw new RuntimeException(e.toString());
					}
				}
			});
	}

	@Override
	protected void boom2() {
		try {
			danmakuList = new ArrayList<>();
			int cid=videoInfo.data.pages.get(Integer.parseInt(((String)part.getSelectedItem()).replace("page", "")) - 1).cid;
			Connection.Response response = Jsoup.connect("http://comment.bilibili.com/" + cid + ".xml").ignoreContentType(true).execute();
			FileOutputStream out = (new FileOutputStream(new File(Environment.getExternalStorageDirectory() + "/" + cid + ".xml")));
			out.write(response.bodyAsBytes());           
			out.close();
			HashSet<Long> danmakuIdSet=new HashSet<>();
			InputStream is=new FileInputStream(new File(Environment.getExternalStorageDirectory() + "/" + cid + ".xml"));
			XmlPullParser xp = Xml.newPullParser();
			xp.setInput(is, "utf-8");
			int type = xp.getEventType();
			while (type != XmlPullParser.END_DOCUMENT) {
				if (type == XmlPullParser.START_TAG) {
					if ("d".equals(xp.getName())) {  
						long id = Long.parseLong(xp.getAttributeValue(0).split(",")[6], 16);  
						danmakuIdSet.add(id);				
						String[] d=xp.getAttributeValue(0).split(",");
						DanmakuBean db=new DanmakuBean();
						db.time = Float.parseFloat(d[0]);
						db.mode = Integer.parseInt(d[1]);
						db.fontSize = Integer.parseInt(d[2]);
						db.color = Integer.parseInt(d[3]);
						db.timeStamp = Long.parseLong(d[4]);
						db.danmakuPool = Integer.parseInt(d[5]);
						db.userHash = Long.parseLong(d[6], 16);
						db.databaseId = Long.parseLong(d[7]);
						db.msg = xp.nextText();
						danmakuList.add(db);
					}
				}
				type = xp.next();
			}
		} catch (Exception e) {
			throw new RuntimeException(e.toString());
		}
		senderAdapter = new SenderAdapter(MainActivity.instance, danmakuList);
		getActivity().runOnUiThread(new Runnable(){

				@Override
				public void run() {
					lv.setAdapter(senderAdapter);
				}
			});
		MainActivity.instance.sanaeConnect.addMessageAction(new WebSocketMessageAction(){

				@Override
				public int useTimes() {
					return 1;
				}

				@Override
				public int forOpCode() {
					return BotDataPack.getIdFromHash;
				}

				@Override
				public BotDataPack onMessage(BotDataPack rec) {
					try {
						HashMap<Integer,Integer> result=new HashMap<>();
						while (rec.hasNext()) {
							result.put(rec.readInt(), rec.readInt());
						}
						for (DanmakuBean db:danmakuList) {
							if (result.get((int)db.userHash) == null) {
								db.uid = -1;
							} else {
								db.uid = result.get((int)db.userHash);
							}
						}
						MainActivity.instance.showToast("完成");
						MainActivity.instance.runOnUiThread(new Runnable(){

								@Override
								public void run() {
									senderAdapter.notifyDataSetChanged();
								}
							});
					} catch (Exception e) {
						MainActivity.instance.showToast(e.toString());
					}
					return null;
				}
			});
		BotDataPack toSend=BotDataPack.encode(BotDataPack.getIdFromHash);
		HashSet<Long> is=new HashSet<>();
		for (DanmakuBean db:danmakuList) {
			is.add(db.userHash);
		}
		for (long l:is) {
			toSend.write((int)l);
		}
		MainActivity.instance.sanaeConnect.send(toSend.getData());
	}

	public String getEtText() {
		return et.getText().toString();
	}
}
