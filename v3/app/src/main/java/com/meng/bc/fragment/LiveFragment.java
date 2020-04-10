package com.meng.bc.fragment;

import android.app.*;
import android.graphics.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.widget.*;
import com.google.gson.*;
import com.meng.bc.*;
import com.meng.bc.activity.*;
import com.meng.bc.javaBean.*;
import com.meng.bc.libs.*;
import com.universalvideoview.*;
import java.io.*;
import org.jsoup.*;

public class LiveFragment extends BaseIdFragment implements UniversalVideoView.VideoViewCallback {

	private Uri uri;
	private ImageView img;
	private Button send,silver,pack,download;
	private EditText et;
	private TextView info;
	private int cachedHeight;

	private Bitmap preview;
	
    View mVideoLayout;
	View mBottomLayout;

	private JsonObject liveInfo;
	private static final String SEEK_POSITION_KEY = "SEEK_POSITION_KEY";


    private int mSeekPosition;
	UniversalVideoView mVideoView;
    UniversalMediaController mMediaController;

	private boolean isFullscreen;


	public LiveFragment(String type, long liveId) {
		this.type = type;
		id = liveId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		return inflater.inflate(R.layout.live_fragment, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		send = (Button) view.findViewById(R.id.live_fragmentButton_send);
		silver = (Button) view.findViewById(R.id.live_fragmentButton_silver);
		pack = (Button) view.findViewById(R.id.live_fragmentButton_pack);
		et = (EditText) view.findViewById(R.id.live_fragmentEditText_danmaku);
		img = (ImageView) view.findViewById(R.id.live_fragmentImageView);  
		info = (TextView) view.findViewById(R.id.live_fragmentTextView_info);
		download = (Button) view.findViewById(R.id.livefragmentButtonDownload);
		download.setOnClickListener(this);
		send.setOnClickListener(this);
		silver.setOnClickListener(this);
		pack.setOnClickListener(this);
		mVideoLayout = view.findViewById(R.id.videoLayout);
        mBottomLayout = view.findViewById(R.id.live_fragmentLinearLayout);
		mVideoView = (UniversalVideoView) view.findViewById(R.id.videoView);
        mMediaController = (UniversalMediaController) view.findViewById(R.id.media_controller);
        mVideoView.setMediaController(mMediaController);
        setVideoAreaSize();
        mVideoView.setVideoViewCallback(this);
		img.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View p1) {
					if (mSeekPosition > 0) {
						mVideoView.seekTo(mSeekPosition);
					}
					mVideoLayout.setVisibility(View.VISIBLE);
					img.setVisibility(View.GONE);
					mVideoView.start();
					mMediaController.setTitle("发发发");
				}
			});
		mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
				@Override
				public void onCompletion(MediaPlayer mp) {
					MainActivity.instance.showToast("播放完成");
					mVideoLayout.setVisibility(View.GONE);
					img.setVisibility(View.VISIBLE);
				}
			});
		img.setOnLongClickListener(new OnLongClickListener(){

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
					JsonParser parser = new JsonParser();
					liveInfo = parser.parse(Tools.Network.getSourceCode("https://api.live.bilibili.com/room/v1/Room/playUrl?cid=" + id + "&quality=4&platform=web")).getAsJsonObject();
					if (liveInfo.get("code").getAsInt() == 19002003) {
						MainActivity.instance.showToast("不存在的房间");
						return;
					}
					final JsonArray ja = liveInfo.get("data").getAsJsonObject().get("durl").getAsJsonArray();
					JsonObject liveToMainInfo=null;
					try {
						liveToMainInfo = new JsonParser().parse(Tools.Network.getSourceCode("https://api.live.bilibili.com/live_user/v1/UserInfo/get_anchor_in_room?roomid=" + id)).getAsJsonObject().get("data").getAsJsonObject().get("info").getAsJsonObject();
					} catch (Exception e) {
						return;
					}
					long uid=liveToMainInfo.get("uid").getAsLong();
					final String uname=liveToMainInfo.get("uname").getAsString();
					final UidToLiveRoom sjb = MainActivity.instance.gson.fromJson(Tools.BilibiliTool.getLiveRoomFromUid(uid), UidToLiveRoom.class);
					try {
						Connection.Response response = Jsoup.connect(MainActivity.instance.gson.fromJson(Tools.BilibiliTool.getLiveRoomFromUid(uid),UidToLiveRoom.class).data.cover).ignoreContentType(true).execute();
						final byte[] imgbs = response.bodyAsBytes();
						preview = BitmapFactory.decodeByteArray(imgbs, 0, imgbs.length);
						getActivity().runOnUiThread(new Runnable(){

								@Override
								public void run() {
									img.setImageBitmap(preview);
								}
							});
					} catch (IOException e) {
						throw new RuntimeException(e.toString());
					}
					
					if (sjb.data.liveStatus != 1) {
						getActivity().runOnUiThread(new Runnable(){

								@Override
								public void run() {
									info.setText("房间号:" + id + "\n主播:" + uname + "\n未直播");
									MainActivity.instance.renameFragment(type + id, uname + "的直播间");
								}
							});
						return;
					} else {
						getActivity().runOnUiThread(new Runnable(){

								@Override
								public void run() {
									uri = Uri.parse(ja.get(0).getAsJsonObject().get("url").getAsString());
									mVideoView.setVideoURI(uri);
									mVideoView.requestFocus();
									mMediaController.setTitle(sjb.data.title);
									info.setText("房间号:" + id + "\n主播:" + uname + "\n标题:" + sjb.data.title);
									MainActivity.instance.renameFragment(type + id, uname + "的直播间");
									MainActivity.instance.showToast("uri:" + uri);
								}
							});
					}
					/*	String html = Tools.Network.getSourceCode("https://live.bilibili.com/" + id);
					 String jsonInHtml = html.substring(html.indexOf("{\"roomInitRes\":"), html.lastIndexOf("}") + 1);
					 final JsonObject data = new JsonParser().parse(jsonInHtml).getAsJsonObject().get("baseInfoRes").getAsJsonObject().get("data").getAsJsonObject();
					 getActivity().runOnUiThread(new Runnable(){

					 @Override
					 public void run() {
					 info.setText("房间号:" + id + "\n主播:" + uname + "\n房间标题:" + data.get("title").getAsString() +
					 "\n分区:" + data.get("parent_area_name").getAsString() + "-" + data.get("area_name").getAsString() +
					 "\n标签:" + data.get("tags").getAsString());
					 }
					 });	*/
				}
			});
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		if (isVisibleToUser) {

		} else {
			if (mVideoView != null && mVideoView.isPlaying()) {
				mSeekPosition = mVideoView.getCurrentPosition();
				mVideoView.pause();
			}
		}
		super.setUserVisibleHint(isVisibleToUser);
	}


    /**
     * 置视频区域大小
     */
    private void setVideoAreaSize() {
        mVideoLayout.post(new Runnable() {
				@Override
				public void run() {
					int width = mVideoLayout.getWidth();
					cachedHeight = (int) (width * 405f / 720f);
//                cachedHeight = (int) (width * 3f / 4f);
//                cachedHeight = (int) (width * 9f / 16f);
					ViewGroup.LayoutParams videoLayoutParams = mVideoLayout.getLayoutParams();
					videoLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
					videoLayoutParams.height = cachedHeight;
					mVideoLayout.setLayoutParams(videoLayoutParams);
					mVideoView.requestFocus();
				}
			});
    }


	/*  @Override
	 protected void onSaveInstanceState(Bundle outState) {
	 super.onSaveInstanceState(outState);
	 Log.d(TAG, "onSaveInstanceState Position=" + mVideoView.getCurrentPosition());
	 outState.putInt(SEEK_POSITION_KEY, mSeekPosition);
	 }

	 @Override
	 protected void onRestoreInstanceState(Bundle outState) {
	 super.onRestoreInstanceState(outState);
	 mSeekPosition = outState.getInt(SEEK_POSITION_KEY);
	 Log.d(TAG, "onRestoreInstanceState Position=" + mSeekPosition);
	 }*/


    @Override
    public void onScaleChange(boolean isFullscreen) {
        this.isFullscreen = isFullscreen;
        if (isFullscreen) {
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
            mVideoLayout.setLayoutParams(layoutParams);
            mBottomLayout.setVisibility(View.GONE);
        } else {
            ViewGroup.LayoutParams layoutParams = mVideoLayout.getLayoutParams();
            layoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            layoutParams.height = this.cachedHeight;
            mVideoLayout.setLayoutParams(layoutParams);
            mBottomLayout.setVisibility(View.VISIBLE);
        }
		switchTitleBar(!isFullscreen);
    }

	private void switchTitleBar(boolean show) {
        ActionBar supportActionBar = getActivity().getActionBar();
        if (supportActionBar != null) {
            if (show) {
                supportActionBar.show();
            } else {
                supportActionBar.hide();
            }
        }
    }

	public String getEtText() {
		return et.getText().toString();
	}

	@Override
	public void onPause(MediaPlayer mediaPlayer) {
		// TODO: Implement this method
	}

	@Override
	public void onStart(MediaPlayer mediaPlayer) {
		// TODO: Implement this method
	}

	@Override
	public void onBufferingStart(MediaPlayer mediaPlayer) {
		// TODO: Implement this method
	}

	@Override
	public void onBufferingEnd(MediaPlayer mediaPlayer) {
		// TODO: Implement this method
	}
}
