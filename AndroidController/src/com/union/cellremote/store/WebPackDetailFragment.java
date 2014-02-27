package com.union.cellremote.store;

import java.util.ArrayList;
import java.util.List;

import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.dfim.app.activity.TabWebActivity;
import com.dfim.app.activity.WebListenActivity;
import com.dfim.app.common.Constant;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.dao.PackDao;
import com.dfim.app.data.VirtualData;
import com.dfim.app.http.HttpPoster;
import com.dfim.app.interfaces.NobleMan;
import com.dfim.app.thread.Pools;
import com.dfim.app.upnp.BoxControl;
import com.dfim.app.upnp.Player;
import com.dfim.app.util.BitmapUtil;
import com.dfim.app.util.JsonUtil;
import com.dfim.app.util.LoadImageAysnc.ImageCallBack;
import com.dfim.app.widget.StandardCustomDialog;
import com.union.cellremote.R;
import com.union.cellremote.domain.Music;
import com.union.cellremote.domain.PackDetail;
import com.union.cellremote.http.HttpGetter;

//notifyData,连接中断，确定
public class WebPackDetailFragment extends Fragment implements NobleMan {

	private final String TAG = "WebAlbumDetailFragment: ";
	private Context context;
	private Bitmap coverBitmap;
	private String imgUrl = "";

	private View view;
	private LinearLayout llLoading;
	private LinearLayout llNoData;
	private LinearLayout llContent;
	private LinearLayout llTitleMsg;

	private FrameLayout flPackInfo;// flPackInfo.setBackgrountDrawable(bitmap)
	private TextView tvPackName;
	private TextView tvMusicCount;
	private Button btnBuy;
	private ListView lvMusics;
	private BaseAdapter adapter;
	private List<Music> packMusics = new ArrayList<Music>();
	private StandardCustomDialog dialog = null;

	private AnimationDrawable ad;
	private boolean loadingRunning = false;
	private boolean fragmentIsActive = false;

	private long packId = -1L;
	private PackDetail packDetail;
	private int musicCount = -1;
	private String packName = "";

	private double balance = -1L;
	private long idToBuy = -1L;
	private Music musicToBuy = null;
	private int itemPositionInListen = -1;
	private boolean packBought = false;

	/*
	 * private int firstVisibleItemPosition = 0;// 记录停止卷动时第一个ITEM的序号 private int
	 * scrollTop = 0;// 记录停止卷动时第一个ITEM距离顶端的偏移量
	 */
	private final int MSG_LETS_GET_DATA = 0;
	private final int MSG_BALANCE_4_MUSIC = 1;
	private final int MSG_BALANCE_4_PACK = 2;
	private final int MSG_PURCHASE_SUCCESS = 3;
	private final int MSG_PURCHASE_SUCCESS_MUSIC = 4;
	private final int MSG_DATA_GOT_PACKDETAIL = 5;

	private Handler handler = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case MSG_LETS_GET_DATA:
				getData();
				break;

			case MSG_BALANCE_4_MUSIC:
				balance = msg.getData().getDouble("balance", -1L);
				int position = msg.getData().getInt("childPosition", -1);
				if (position != -1) {
					launchWebBuy(position);
				} else {
//					CustomToast.makeText(context, "未能获取商品信息", Toast.LENGTH_SHORT).show();
					UpnpApp.mainHandler.showAlert(R.string.album_data_error);
				}
				break;

			case MSG_BALANCE_4_PACK:
				balance = msg.getData().getDouble("balance", -1L);
				if (packDetail.getPackId() != -1) {
					launchWebBuy();
				} else {
//					CustomToast.makeText(context, "未能获取商品信息", Toast.LENGTH_SHORT).show();
					UpnpApp.mainHandler.showAlert(R.string.album_data_error);
				}
				break;

			case MSG_PURCHASE_SUCCESS:
				// notifyBoxToSyn();// 通知盒子更新数据
				WatchDog.hasNewBought = true;// 本地数据更新标记
				packBought = true;

				btnBuy.setText(getResources().getString(R.string.willCache));
				btnBuy.setEnabled(false);
				adapter.notifyDataSetChanged();
				break;

			case MSG_PURCHASE_SUCCESS_MUSIC:
				// notifyBoxToSyn();// 通知盒子更新数据
				WatchDog.hasNewBought = true;// 本地数据更新标记
				if (musicToBuy != null) {
					musicToBuy.setPurchaseState(getResources().getString(R.string.willCache));
					musicToBuy = null;
				}

				adapter.notifyDataSetChanged();
				break;

			case MSG_DATA_GOT_PACKDETAIL:
				if (packDetail == null || packDetail.getMusics() == null || packDetail.getMusics().size() == 0) {
					showNoData();
				} else {
					endLoding();
					initView();
				}
				break;
			}
			super.handleMessage(msg);
		}
	};

	private BroadcastReceiver buyMusicReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (itemPositionInListen != -1) {
				getBalanceNLanunchBuy(itemPositionInListen);
			}
		}
	};

	private BroadcastReceiver webDetailPageUpdateUIReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			initBtnBuy();
			adapter.notifyDataSetChanged();
		}
	};

	public WebPackDetailFragment() {

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

	protected void notifyBoxToSyn() {
		new BoxControl().notifyBoxToSyn();
	}

	public WebPackDetailFragment(Context context, long albumId, String albumName, int musicCount, String imgUrl) {
		this.context = context;
		this.packId = albumId;
		this.packName = albumName;
		this.musicCount = musicCount;
		this.imgUrl = imgUrl;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		parentActivityChangeButton("btnBack");
		parentActivityChangeTitle();
		view = LayoutInflater.from(UpnpApp.context).inflate(R.layout.web_pack_detail, null);

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
		Log.e(TAG, "startLoading()");
		llContent.setVisibility(View.INVISIBLE);
		llLoading.setVisibility(View.VISIBLE);
		llNoData.setVisibility(View.INVISIBLE);

		if (ad == null) {
			ad = (AnimationDrawable) getResources().getDrawable(R.anim.animatior_list);
		}
		llLoading.findViewById(R.id.iv_loading).setBackgroundDrawable(ad);
		ad.start();

		loadingRunning = true;
	}

	private void getData() {
		if (packId != -1) {
			Pools.executorService1.submit(new Runnable() {
				@Override
				public void run() {
					String json = new HttpGetter(context).getPackDetail(packId);
					// System.out.println("jsonPackDetail=" + json);

					packDetail = new JsonUtil().getPackDetail(packId, json);
					packDetail.setMusicCount(musicCount);
					// packDetail.setBitmap(coverBitmap);

					packMusics = packDetail.getMusics();

					handler.sendEmptyMessage(MSG_DATA_GOT_PACKDETAIL);
				}
			});
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// listViewGetFormerPosition();
		fragmentIsActive = true;
	}

	@Override
	public void onPause() {
		if (WatchDog.hasNewBought) {
			notifyBoxToSyn();
//			CustomToast.makeText(context, "正在同步新购买的音乐...", Toast.LENGTH_SHORT).show();
			UpnpApp.mainHandler.showAlert(R.string.store_syn_new_music_info);
		}
		super.onPause();
	}

	@Override
	public void onDetach() {
		unregisterReceivers();
		super.onDetach();
	}

	private void parentActivityChangeTitle() {
		TabWebActivity.tvTitle.setText("主题详情");
	}

	private void parentActivityChangeButton(String which) {
		TabWebActivity.changeButton(which);
	}

	private void initComponents() {

		flPackInfo = (FrameLayout) view.findViewById(R.id.fl_pack_info);
		llLoading = (LinearLayout) view.findViewById(R.id.ll_loading);
		llNoData = (LinearLayout) view.findViewById(R.id.ll_no_data);
		llContent = (LinearLayout) view.findViewById(R.id.ll_content);
		llTitleMsg = (LinearLayout) view.findViewById(R.id.ll_title_msg);

		tvPackName = (TextView) view.findViewById(R.id.tv_pack_name);
		tvMusicCount = (TextView) view.findViewById(R.id.tv_music_count);
		btnBuy = (Button) view.findViewById(R.id.btn_buy);
		lvMusics = (ListView) view.findViewById(R.id.lv_pack_musics);

		tvPackName.setText(packName);
		tvMusicCount.setText("共" + musicCount + "首音乐");
		
		downloadImage(imgUrl);
	}

	protected void downloadImage(final String imgUrl) {
		Pools.executorService2.submit(new Runnable() {
			@Override
			public void run() {
				// Looper.prepare();
				String imageKey = imgUrl + "150";

				Bitmap bitmap = BitmapUtil.loadImageAysnc.loadImageNohandler(imageKey, imgUrl, 150, false, new ImageCallBack() {
					@Override
					public void imageLoaded(Bitmap bitmap) {
						if (bitmap != null && !bitmap.isRecycled()) {
							coverBitmap = bitmap;
							flPackInfo.setBackgroundDrawable(new BitmapDrawable(coverBitmap));
						} else {
							coverBitmap = Constant.packCover;
							flPackInfo.setBackgroundDrawable(new BitmapDrawable(coverBitmap));
						}
					}
				});

				// 得到封面后刷新界面
				if (bitmap != null) {
					coverBitmap = bitmap;
					flPackInfo.setBackgroundDrawable(new BitmapDrawable(coverBitmap));
				} else {
					coverBitmap = Constant.packCover;
					flPackInfo.setBackgroundDrawable(new BitmapDrawable(coverBitmap));
				}

				bitmap = null;
				// Looper.loop();
			}
		});
	}

	private void initListeners() {
		
		llTitleMsg.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// 什么也不干，就是别给老子死到侧滑菜单的监听里去
			}
		});

		btnBuy.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (packDetail != null) {
					getBalanceNLanunchBuy();
				} else {
//					CustomToast.makeText(context, "未能获取专辑信息", Toast.LENGTH_SHORT).show();
					UpnpApp.mainHandler.showAlert(R.string.album_data_error);
				}
			}
		});

		/*
		 * lvMusics.setOnScrollListener(new OnScrollListener() {
		 * 
		 * @Override public void onScrollStateChanged(AbsListView view, int
		 * scrollState) { if (scrollState == OnScrollListener.SCROLL_STATE_IDLE)
		 * { firstVisibleItemPosition = lvMusics.getFirstVisiblePosition(); if
		 * (lvMusics.getChildAt(0) != null) { scrollTop =
		 * lvMusics.getChildAt(0).getTop(); } adapter.notifyDataSetChanged(); }
		 * }
		 * 
		 * @Override public void onScroll(AbsListView view, int
		 * firstVisibleItem, int visibleItemCount, int totalItemCount) { // TODO
		 * Auto-generated method stub
		 * 
		 * } });
		 */

		lvMusics.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

			}
		});

	}

	private void playMusic(String uri) {
		Player p = new Player();
		p.play(uri);
	}

	private void initView() {
		if (new PackDao().checkPackId(packId)) {
			packBought = true;
		} else {
			packBought = false;
		}

		initBtnBuy();
		initListView();
	}

	private void initListView() {
		adapter = new BaseAdapter() {

			TextView getTextView() {
				AbsListView.LayoutParams lp = new AbsListView.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 64);
				TextView textView = new TextView(WebPackDetailFragment.this.context);
				textView.setLayoutParams(lp);
				textView.setBackgroundResource(R.color.groupview_bg);
				textView.setGravity(Gravity.CENTER_VERTICAL);
				textView.setPadding(40, 0, 0, 0);
				textView.setTextSize(30);
				textView.setTextColor(Color.WHITE);

				return textView;
			}

			@Override
			public int getCount() {
				return packMusics.size();
			}

			@Override
			public Object getItem(int position) {
				return packMusics.get(position);
			}

			@Override
			public long getItemId(int position) {
				return 0;
			}

			@Override
			public View getView(final int position, View convertView, ViewGroup parent) {

				ChildHolder holder;
				long musicId = packMusics.get(position).getId();
				String btnBuyText = "";
				boolean btnBuyEnabled = false;

				if (convertView == null || convertView.getTag() == null) {
					convertView = LayoutInflater.from(context).inflate(R.layout.web_musics_item, null);
					holder = new ChildHolder(convertView);
					convertView.setTag(holder);
				} else {
					holder = (ChildHolder) convertView.getTag();
				}

				holder.tvNum.setText((position + 1) + "");
				holder.tvName.setText(packMusics.get(position).getName());
				holder.tvArtist.setText(packMusics.get(position).getArtistName());

				// 整张专辑为缓存中，或缓存中的单曲包含该单曲时，显示缓存中
				if (WatchDog.purchasingPacks.containsKey(packId) || WatchDog.purchasingMusics.containsKey(musicId)) {
					btnBuyText = getResources().getString(R.string.willCache);
					btnBuyEnabled = false;
					holder.btnBuy.setText(btnBuyText);
					holder.btnBuy.setEnabled(btnBuyEnabled);
				}

				// 整张主题为已购买时分类讨论
				else if (packBought == true
				// || "已购买".equals(packMusics.get(position).getPurchaseState())
				) {
					if (VirtualData.musics.contains(packMusics.get(position))) {
						btnBuyText = "在本地";
					} else {
						btnBuyText = "在云端";
					}

					btnBuyEnabled = false;
					// packMusics.get(position).setPurchaseState("已购买");
				}

				// 本地音乐中包含该单曲时显示为在本地
				else if (VirtualData.musics.contains(packMusics.get(position))) { //
					packMusics.get(position).setPurchaseState("已购买");
					btnBuyText = "在本地";
					btnBuyEnabled = false;
				}

				// json数据中的状态值为45时显示为不单卖
				else if ("45".equals(packMusics.get(position).getPurchaseState())) {
					btnBuyText = "不单卖";
					btnBuyEnabled = false;

					holder.btnBuy.setText(btnBuyText);
					holder.btnBuy.setEnabled(false);
				}

				// 即将上架
				else if ("10".equals(packMusics.get(position).getPurchaseState()) || "30".equals(packMusics.get(position).getPurchaseState()) || "35".equals(packMusics.get(position).getPurchaseState()) || "40".equals(packMusics.get(position).getPurchaseState())) {
					btnBuyText = "即将上架";
					holder.btnBuy.setText(btnBuyText);
					holder.btnBuy.setEnabled(false);
					btnBuyEnabled = false;
					// li.get(childPosition).setPurchaseState("不单卖");
				}

				// 其它状态显示价格
				else {
					if (!packMusics.get(position).getPrice().equals("0.0")) {
						btnBuyText = "￥ " + packMusics.get(position).getPrice() + "0";
					} else {
						btnBuyText = getResources().getString(R.string.freeBtnText);
					}
					// packMusics.get(position).setPurchaseState("未购买");
					btnBuyEnabled = true;
				}

				holder.btnBuy.setText(btnBuyText);
				holder.btnBuy.setEnabled(btnBuyEnabled);
				final String btnBuyText2 = btnBuyText;
				final boolean btnBuyEnabled2 = btnBuyEnabled;

				// 点击文字区域发起试听
				holder.llListen.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						itemPositionInListen = position;
						launchWebListen(position, btnBuyText2, btnBuyEnabled2);
					}
				});

				// 点击价格按钮发起购买
				holder.btnBuy.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						getBalanceNLanunchBuy(position);
					}
				});

				return convertView;
			}

		};

		lvMusics.setAdapter(adapter);
	}

	private void initBtnBuy() {

		// 判断是否缓存中
		if (WatchDog.purchasingAlbums.containsKey(packId)) {
			btnBuy.setText(getResources().getString(R.string.willCache));
			btnBuy.setEnabled(false);
		}

		// 判断是否在本地
		else if (packBought == true) {
			// int packLocation = -1;
			int packLocation = new PackDao().getPackDetailById(packId).getIsCloud();
			if (packLocation == 5) {
				btnBuy.setText("在本地");
			} else if (packLocation == 0) {
				btnBuy.setText("在云端");
			} else {
				btnBuy.setText("已购买");
			}
			btnBuy.setEnabled(false);

		}

		// 默认显示价格
		else {
			btnBuy.setText("￥ " + packDetail.getPrice() + "0");
			if (packDetail.getPrice() != 0) {
				btnBuy.setText("￥ " + packDetail.getPrice() + "0");
			} else {
				btnBuy.setText(getResources().getString(R.string.freeBtnText));
			}
		}
	}

	/*
	 * private void checkIfPackBought() { // 本地包含同ID主题且曲目总数相等时，判断该专辑状态为已购买
	 * outer: for (Pack pack : VirtualData.packs) { if (pack.getId() ==
	 * (packDetail.getPackId()) // && pack.getMcount() ==
	 * packDetail.getMusicCount() ) { packBought = true; break outer; } else {
	 * packBought = false; } } }
	 */

	private void launchWebListen(int position, String btnBuyText, boolean btnBuyEnabled) {
		// 发起试听
		Music music = (Music) packMusics.get(position);
		String uri = music.getMediaurl();
		playMusic(uri);
		coverBitmap = new BitmapUtil().processBigBitmap(coverBitmap, 250000, Constant.packCover);

		Intent intent = new Intent(context, WebListenActivity.class);
		intent.putExtra("musicName", music.getName());
		intent.putExtra("artist", music.getArtistName());
		intent.putExtra("bitmap", coverBitmap);
		intent.putExtra("musicIsBought", music.getPurchaseState());
		intent.putExtra("btnBuyText", btnBuyText);
		intent.putExtra("btnBuyEnabled", btnBuyEnabled);

		context.startActivity(intent);
	}

	/* 购买主题单曲 */
	private void launchWebBuy(int position) {
		musicToBuy = (Music) packMusics.get(position);

		if (balance != -1L) {
			String msg = musicToBuy.getName() + " \n价格：" + musicToBuy.getPrice() + "\t当前余额：" + balance + " 元\n\n确认购买吗？";
			idToBuy = musicToBuy.getId();
			if (idToBuy != -1) {
				showPurchaseReassureDialog(Constant.ordertype_audio, msg);
			} else {
//				CustomToast.makeText(context, "未能获取商品信息", Toast.LENGTH_SHORT).show();
				UpnpApp.mainHandler.showAlert(R.string.album_data_error);
			}
			balance = -1L;// 归零余额以备下一次查询
		} else {
//			CustomToast.makeText(context, "未能读取余额，请检查您的网络", Toast.LENGTH_SHORT).show();
			UpnpApp.mainHandler.showAlert(R.string.store_balance_read_alert);
		}
	}

	/* 购买主题 */
	private void launchWebBuy() {
		// 发起购买
		if (balance != -1) {
			String msg = packDetail.getPackName() + " \n价格：" + packDetail.getPrice() + "\t当前余额：" + balance + " 元\n\n确认购买吗？";
			idToBuy = packDetail.getPackId();
			if (idToBuy != -1) {
				showPurchaseReassureDialog(Constant.ordertype_pack, msg);
			} else {
//				CustomToast.makeText(context, "未能获取商品信息", Toast.LENGTH_SHORT).show();
				UpnpApp.mainHandler.showAlert(R.string.album_data_error);
			}
			balance = -1L;// 归零余额以备下一次查询
		} else {
//			CustomToast.makeText(context, "未能读取余额，请检查您的网络", Toast.LENGTH_SHORT).show();
			UpnpApp.mainHandler.showAlert(R.string.store_balance_read_alert);
		}
	}

	private void getBalanceNLanunchBuy(final int itemPosition) {
		Pools.executorService1.submit(new Runnable() {
			@Override
			public void run() {
				String json = new HttpGetter(context).getBalance();
				double _balance = new JsonUtil().getBalance(json);

				Message msg = new Message();
				msg.what = MSG_BALANCE_4_MUSIC;
				Bundle bundle = new Bundle();
				bundle.putDouble("balance", _balance);
				bundle.putInt("childPosition", itemPosition);
				msg.setData(bundle);
				handler.sendMessage(msg);
			}
		});
	}

	private void getBalanceNLanunchBuy() {
		Pools.executorService1.submit(new Runnable() {
			@Override
			public void run() {
				String json = new HttpGetter(context).getBalance();
				double _balance = new JsonUtil().getBalance(json);

				Message msg = new Message();
				msg.what = MSG_BALANCE_4_PACK;
				Bundle bundle = new Bundle();
				bundle.putDouble("balance", _balance);
				msg.setData(bundle);
				handler.sendMessage(msg);
			}
		});

	}

	private void clearDialog() {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
	}

	private void showPurchaseReassureDialog(final String ordertype, String msg) {
		clearDialog();

		final StandardCustomDialog.Builder builder = new StandardCustomDialog.Builder(context);
		builder.setTitle("确认购买");
		builder.setMessage(msg);

		builder.setNegativeButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				showBuyingDialog();
				buy(ordertype, idToBuy);
			}
		});
		builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		dialog = builder.create();
		dialog.show();
	}

	private void showBuyingDialog() {
		clearDialog();

		final StandardCustomDialog.Builder builder = new StandardCustomDialog.Builder(context);
		builder.setTitle("正在购买");
		builder.setMessage(null);

		AnimationDrawable ad = (AnimationDrawable) getResources().getDrawable(R.anim.animatior_searchbox_list);
		String str = "正在购买...";
		LinearLayout llContent = createDialogContent(ad, str);
		builder.setContentView(llContent);

		dialog = builder.create();
		dialog.show();
	}

	private void showBuyResultDialog(String msg) {
		clearDialog();

		final StandardCustomDialog.Builder builder = new StandardCustomDialog.Builder(context);
		builder.setTitle("完成购买");
		builder.setMessage(msg);

		builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		dialog = builder.create();
		dialog.show();
	}

	protected void buy(final String ordertype, final long id) {
		Pools.executorService1.submit(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				String json = "";

				if (Constant.ordertype_audio.equals(ordertype)) {
					json = new HttpPoster().buyAlbumOrMusic(ordertype, id);
				} else if (Constant.ordertype_pack.equals(ordertype)) {
					json = new HttpPoster().buyPack(id);
				} else {
//					CustomToast.makeText(context, "未知的购买类型", Toast.LENGTH_SHORT).show();
					UpnpApp.mainHandler.showAlert(R.string.store_purchase_unknown_type_alert);
					return;
				}

				String resultcode = new JsonUtil().getOrderFeedback(json);
				finishThisBuy(ordertype, id, resultcode);
				Looper.loop();
			}
		});

	}

	protected void finishThisBuy(String ordertype, long id, String resultcode) {
		if ("30".equals(resultcode)) {
//			showBuyResultDialog("操作成功\n\n即将为您缓存曲目");

			if (ordertype.equals(Constant.ordertype_pack)) {
				WatchDog.purchasingPacks.put(id, 0);
				handler.sendEmptyMessage(MSG_PURCHASE_SUCCESS);// 通知主线程发起同步
			} else if (ordertype.equals(Constant.ordertype_audio)) {
				WatchDog.purchasingMusics.put(id, 0);
				handler.sendEmptyMessage(MSG_PURCHASE_SUCCESS_MUSIC);// 通知主线程发起同步
			}

		} else if ("1".equals(resultcode)) {
			// 提示余额不足
			showBuyResultDialog("购买失败：余额不足");
		} else if ("5".equals(resultcode)) {
			// 提示已购买
			showBuyResultDialog("购买失败：您已经购买了该商品");
			if (ordertype.equals(Constant.ordertype_pack)) {
				handler.sendEmptyMessage(MSG_PURCHASE_SUCCESS);// 通知主线程发起同步
			} else if (ordertype.equals(Constant.ordertype_audio)) {
				handler.sendEmptyMessage(MSG_PURCHASE_SUCCESS_MUSIC);// 通知主线程发起同步
			}

		} else if ("10".equals(resultcode)) {
			// 不是在售商品
			showBuyResultDialog("购买失败：不是在售商品");
		} else if ("15".equals(resultcode)) {
			// 绑定用户无效
			showBuyResultDialog("购买失败：不是有效用户");
		} else if ("20".equals(resultcode)) {
			// 密码错误
			showBuyResultDialog("购买失败：密码错误");
		} else if ("25".equals(resultcode)) {
			// 未知错误
			showBuyResultDialog("购买失败：未知错误");
		} else if ("-1".equals(resultcode)) {
			// 通信失败
			showBuyResultDialog("购买失败：通信失败");
		}
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
		context.registerReceiver(buyMusicReceiver, new IntentFilter("buyMusicReceiver"));
		context.registerReceiver(webDetailPageUpdateUIReceiver, new IntentFilter("webDetailPageUpdateUIReceiver"));
	}

	private void unregisterReceivers() {
		// context.unregisterReceiver(updateListReceiver);
		context.unregisterReceiver(buyMusicReceiver);
		context.unregisterReceiver(webDetailPageUpdateUIReceiver);
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
		packMusics = null;
		packDetail = null;
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
