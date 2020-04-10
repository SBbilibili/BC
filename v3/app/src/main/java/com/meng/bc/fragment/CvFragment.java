package com.meng.bc.fragment;

import android.graphics.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.meng.bc.*;
import com.meng.bc.activity.*;
import com.meng.bc.javaBean.*;
import com.meng.bc.libs.*;
import java.io.*;
import org.jsoup.*;

public class CvFragment extends BaseIdFragment {

	private Button send,zan,coin1,coin2,favorite;
	private EditText et;
	private TextView info;
	private CvInfo cvInfo;
	private ImageView ivPreview;
	private Bitmap preview;

	public CvFragment(String type, long cvId) {
		this.type = type;
		id = cvId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.cv_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		send = (Button) view.findViewById(R.id.cv_fragmentButton_send);
		zan = (Button) view.findViewById(R.id.cv_fragmentButton_zan);
		coin1 = (Button) view.findViewById(R.id.cv_fragmentButton_coin1);
		coin2 = (Button) view.findViewById(R.id.cv_fragmentButton_coin2);
		favorite = (Button) view.findViewById(R.id.cv_fragmentButton_favorite);
		et = (EditText) view.findViewById(R.id.cv_fragmentEditText_msg);
		ivPreview = (ImageView) view.findViewById(R.id.cv_fragmentImageView);  
		info = (TextView) view.findViewById(R.id.cv_fragmentTextView_info);
		zan.setOnClickListener(this);
		coin1.setOnClickListener(this);
		coin2.setOnClickListener(this);
		favorite.setOnClickListener(this);
		send.setOnClickListener(this);
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
					cvInfo = MainActivity.instance.gson.fromJson(Tools.BilibiliTool.getCvInfo(id), CvInfo.class);	
					if (cvInfo.code != 0) {
						MainActivity.instance.showToast(cvInfo.message);
						return;
					}
					getActivity().runOnUiThread(new Runnable(){

							@Override
							public void run() {
								info.setText(cvInfo.toString());
								MainActivity.instance.renameFragment(type + id, cvInfo.data.title);
							}
						});
					try {
						Connection.Response response = Jsoup.connect(cvInfo.data.banner_url).ignoreContentType(true).execute();
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

	public String getEtText() {
		return et.getText().toString();
	}
}
