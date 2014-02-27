package com.union.cellremote.store;

import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dfim.app.activity.TabWebActivity;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.interfaces.NobleMan;
import com.dfim.app.thread.Pools;
import com.dfim.app.util.BitmapUtil;
import com.dfim.app.util.ExitApplication;
import com.dfim.app.util.JsonUtil;
import com.dfim.app.util.LoadImageAysnc.ImageCallBack;
import com.dfim.app.util.PowerfulBigMan;
import com.union.cellremote.R;
import com.union.cellremote.adapter.ArtistAlbumListAdapter;
import com.union.cellremote.domain.Album;
import com.union.cellremote.domain.AlbumDetail;
import com.union.cellremote.domain.Artist;
import com.union.cellremote.domain.ColumnDetail;
import com.union.cellremote.domain.Disk;
import com.union.cellremote.domain.Music;
import com.union.cellremote.http.HttpGetter;

//notifyData,连接中断，确定
public class ArtistDetailFragment extends Fragment implements NobleMan {

	private final String TAG = "ArtistDetailFragment: ";
	private Context context;
	private Artist artist;

	/*
	 * 控件
	 */
	private View view;
	private LinearLayout llLoading;
	private LinearLayout llNoData;
	private LinearLayout llContent;

	private ImageView ivArtist;
	private TextView tvName;
	private TextView tvNumAlbums;
	private TextView tvNumMusics;

	private ListView lvArtistAlbums;
	private ArtistAlbumListAdapter adapter;

	// 动画
	private AnimationDrawable ad;
	private boolean loadingRunning = false;
	private boolean fragmentIsActive = false;

	/*
	 * 数据
	 */
	private ColumnDetail artistDetail;
	private List<Album> artistAlbums = new ArrayList<Album>();
	private long albumId = -1L;
	private AlbumDetail albumdetail;
	private String albumName = "";
	private List<Disk> diskli;
	private String imgUrl = "";
	private Bitmap bitmap;
	private double balance = -1L;
	private long idToBuy = -1L;
	private Music musicToBuy = null;
	private int groupPositionInListen = -1;
	private int childPositionInListen = -1;
	private boolean albumIsBought = false;

	/*
	 * handler&receivers
	 */
	private final int MSG_LETS_GET_DATA = 0;
	private final int MSG_DATA_GOT = 1;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);

			switch (msg.what) {
			case MSG_LETS_GET_DATA:
				getArtistAlbumsData();
				break;

			case MSG_DATA_GOT:
				updateUI();
				break;

			default:
				break;
			}
		}
	};

	public ArtistDetailFragment() {

	}

	protected void updateUI() {
		if (adapter != null && artistAlbums != null && artistAlbums.size() != 0) {
			if (loadingRunning == true) {
				endLoading();
			}

			tvNumAlbums.setText("相关专辑：" + artistDetail.getNum() + " 张");
			tvNumMusics.setText("个人专辑：" + artistDetail.getTotal() + " 张");

			adapter.setAlbums(artistAlbums);
			System.out.println("adapter.getAlbums().size()=" + adapter.getAlbums().size());
			adapter.notifyDataSetChanged();
		} else {
			showNoData();
		}
	}

	protected void endLoading() {
		if (ad != null && ad.isRunning()) {
			ad.stop();
		}

		llContent.setVisibility(View.VISIBLE);
		llLoading.setVisibility(View.GONE);
		llNoData.setVisibility(View.GONE);

		loadingRunning = false;
	}

	public ArtistDetailFragment(Context context, Artist artist) {
		this.context = context;
		this.artist = artist;
	}

	protected void getArtistAlbumsData() {
		Pools.executorService1.submit(new Runnable() {
			@Override
			public void run() {
				String json = new HttpGetter(context).getColumnAlbumsList(artist.getId(), artist.getName(), 0, 30);
				// System.out.println("jsonArtistDetail="+json);//ok

				artistDetail = new JsonUtil().getColumnDetail(json);
				artistAlbums = artistDetail.getAlbums();
				handler.sendEmptyMessage(MSG_DATA_GOT);

				for (Album album : artistAlbums) {
					// album.setCoverBitmap(Constant.albumCover);
					downloadImage(album);
				}
			}
		});

	}

	protected void downloadImage(final Album album) {
		Pools.executorService2.submit(new Runnable() {
			@Override
			public void run() {
				String imageKey = album.getImgUrl() + "150";
				Bitmap bitmap = BitmapUtil.loadImageAysnc.loadImageNohandler(imageKey, album.getImgUrl(), 150, false, new ImageCallBack() {
					@Override
					public void imageLoaded(Bitmap bitmap) {
						// bitmap = new BitmapUtil().processBigBitmap(bitmap,
						// 1000000, Constant.albumCover);
						// 得到专辑封面后刷新界面
						album.setBitmap(new SoftReference<Bitmap>(bitmap));
						handler.sendEmptyMessage(MSG_DATA_GOT);
					}
				});

				// 得到封面后刷新界面
				if (bitmap != null && !bitmap.isRecycled()) {
					album.setBitmap(new SoftReference<Bitmap>(bitmap));
					handler.sendEmptyMessage(MSG_DATA_GOT);
				}

				bitmap = null;
			}
		});
	}

	protected void endLoding() {
		llLoading.setVisibility(View.GONE);
		llNoData.setVisibility(View.GONE);
		llContent.setVisibility(View.VISIBLE);
	}

	protected void showNoData() {
		llLoading.setVisibility(View.GONE);
		llContent.setVisibility(View.GONE);
		llNoData.setVisibility(View.VISIBLE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		parentActivityChangeButton("btnBack");
		parentActivityChangeTitle();
		view = LayoutInflater.from(UpnpApp.context).inflate(R.layout.artist_detail, null);

		initComponents();
		startLoading();
		getDataWhenActive();
		initListeners();
		registerReceivers();

		return view;
	}

	private void getDataWhenActive() {
		new Thread(new Runnable() {
			@Override
			public void run() {
				while (fragmentIsActive == false) {
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				handler.sendEmptyMessage(MSG_LETS_GET_DATA);
			}
		}).start();
	}

	private void startLoading() {
		llContent.setVisibility(View.GONE);
		llLoading.setVisibility(View.VISIBLE);
		llNoData.setVisibility(View.GONE);

		if (ad == null) {
			ad = (AnimationDrawable) getResources().getDrawable(R.anim.animatior_list);
		}
		llLoading.findViewById(R.id.iv_loading).setBackgroundDrawable(ad);
		ad.start();

		loadingRunning = true;
	}

	@Override
	public void onResume() {
		super.onResume();
		// listViewGetFormerPosition();
		fragmentIsActive = true;
		((TabWebActivity) ExitApplication.getInstance().getTabWebActivity()).activate();
	}

	@Override
	public void onDetach() {
		unregisterReceivers();
		super.onDetach();
	}

	private void parentActivityChangeTitle() {
		TabWebActivity.tvTitle.setText(artist.getName());
		((TabWebActivity) context).useTitleStyle(TabWebActivity.TITLE_STYLE_NORMAL);
	}

	private void parentActivityChangeButton(String which) {
		TabWebActivity.changeButton(which);
	}

	private void initComponents() {

		llLoading = (LinearLayout) view.findViewById(R.id.ll_loading);
		llNoData = (LinearLayout) view.findViewById(R.id.ll_no_data);
		llContent = (LinearLayout) view.findViewById(R.id.ll_content);

		ivArtist = (ImageView) view.findViewById(R.id.iv_artist);
		tvName = (TextView) view.findViewById(R.id.tv_artist_name);
		tvNumAlbums = (TextView) view.findViewById(R.id.tv_num_albums);
		tvNumMusics = (TextView) view.findViewById(R.id.tv_num_musics);
		// tvNumMusics.setVisibility(View.GONE);

		tvName.setText(artist.getName());
/*		if (artist.getBitmap() != null) {
			ivArtist.setImageBitmap(artist.getBitmap());
		}
*/
		initListView();
	}

	private void initListView() {
		lvArtistAlbums = (ListView) view.findViewById(R.id.lv_artist_albums);
		adapter = new ArtistAlbumListAdapter(context, artist.getName());
		adapter.setAlbums(artistAlbums);
		lvArtistAlbums.setAdapter(adapter);

		lvArtistAlbums.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				// 进入该专辑详情页
				if (PowerfulBigMan.testClickInterval() == false) {
					return;
				}
				Album album = artistAlbums.get(position);

				if (album != null) {
					long albumId = album.getId();
					String name = album.getName();
					imgUrl = album.getImgUrl();
					Bitmap bitmap = album.getBitmap();
					showAlbumContent(albumId, name, bitmap);
				} else {
//					CustomToast.makeText(context, "读取专辑信息失败", Toast.LENGTH_SHORT).show();
					UpnpApp.mainHandler.showAlert(R.string.album_data_error);
				}
			}
		});

	}

	protected void showAlbumContent(long id, String name, Bitmap bitmap) {
		if (bitmap == null) {
			bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pic);
		}

		if (bitmap.getByteCount() > 250000) {// nullporinter*2
			byte[] bmBytes = BitmapUtil.Bitmap2Bytes(bitmap);
			bitmap = BitmapUtil.Bytes2Bimap(bmBytes, 2);

			// 如此仁至义尽了
			if (bitmap.getByteCount() > 250000) {// nullpointer
				bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.pic1);
			}
		}

		Intent intent = new Intent("showAlbumContentReceiver");
		intent.putExtra("albumId", id);
		intent.putExtra("albumName", name);
		intent.putExtra("bitmap", bitmap);
		intent.putExtra("imgUrl", imgUrl);
		intent.putExtra("layout", R.id.ll_web_root);

		// UpnpApp.context.sendBroadcast(intent);
		((TabWebActivity) context).showAlbumContentReceiverOnReceive(intent);
	}

	private void initListeners() {

	}

	protected LinearLayout createDialogContent(Drawable drawable, String str) {
		LinearLayout llContent = (LinearLayout) LayoutInflater.from(context).inflate(R.layout.common_dialog_content, null);
		ImageView iv = (ImageView) llContent.findViewById(R.id.iv_common);
		TextView tv = (TextView) llContent.findViewById(R.id.tv_common);

		iv.setBackgroundDrawable(drawable);
		if (drawable instanceof AnimationDrawable) {
			((AnimationDrawable) drawable).start();
		}
		tv.setText(str);

		return llContent;
	}

	private void registerReceivers() {

	}

	private void unregisterReceivers() {

	}

	private boolean isThisListPlaying(List<Music> list) {
		if (WatchDog.currentList == null) {
			return false;
		} else {
			String name1 = list.get(0).getName();
			String name2 = WatchDog.currentList.get(0).getName();
			return name1.equals(name2);
		}
	}

	class ChildHolder {
		private TextView tvNum;
		private TextView tvName;
		private TextView tvArtist;
		private Button btnBuy;
		private LinearLayout llListen;

		ChildHolder(View convertView) {
			tvNum = (TextView) convertView.findViewById(R.id.tv_num);
			tvName = (TextView) convertView.findViewById(R.id.tv_music_name);
			tvArtist = (TextView) convertView.findViewById(R.id.tv_music_artist);
			btnBuy = (Button) convertView.findViewById(R.id.btn_buy);
			llListen = (LinearLayout) convertView.findViewById(R.id.ll_listen);
		}
	}

	@Override
	public void letsSeeHeaven() {
		for (Album album : artistAlbums) {
			album.recyleBitmap();
		}
		bitmap.recycle();

		artistAlbums = null;
		albumdetail = null;
		diskli = null;
		bitmap = null;
	}

	@Override
	public void recordCurrentDataAndPosition() {
		// TODO Auto-generated method stub

	}

	@Override
	public void getSavedDataAndPosition() {
		// TODO Auto-generated method stub

	}

	@Override
	public int getFistVisiblePosition() {
		return -1;
	}

	@Override
	public int getLastVisiblePosition() {
		return -1;
	}

	@Override
	public void recycleBitmaps() {
		// TODO Auto-generated method stub

	}

}
