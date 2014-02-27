package com.dfim.app.util;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.Drawable;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.dfim.app.common.Constant;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.http.HttpPoster;
import com.dfim.app.thread.Pools;
import com.dfim.app.upnp.BoxControl;
import com.dfim.app.widget.StandardCustomDialog;
import com.union.cellremote.R;
import com.union.cellremote.domain.Music;
import com.union.cellremote.http.HttpGetter;

public class Buyer {

	private static final String TAG = "Buyer";
	
	private Context context;
	private double balance;
	private Music musicToBuy;
	private StandardCustomDialog dialog = null;

	public Buyer(Context context) {
		this.context = context;
	}

	public Music getMusicToBuy() {
		return musicToBuy;
	}

	public void setMusicToBuy(Music musicToBuy) {
		this.musicToBuy = musicToBuy;
	}

	public void getBalanceNLanunchBuy() {
		Pools.executorService1.submit(new Runnable() {
			@Override
			public void run() {
				String json = new HttpGetter(context).getBalance();
				balance = new JsonUtil().getBalance(json);
				UpnpApp.mainHandler.post(new Runnable() {

					@Override
					public void run() {
						launchWebBuy(musicToBuy);
					}
				});

			}
		});
	}

	private void launchWebBuy(Music musicToBuy) {

		if (balance != -1L) {
			String msg = musicToBuy.getName() + " \n价格：" + musicToBuy.getPrice() + "\t当前余额：" + balance + " 元\n\n确认购买吗？";
			Log.e(TAG, "musicToBuy.getPrice()="+musicToBuy.getPrice());
			Log.e(TAG, "musicToBuy.getId()="+musicToBuy.getId());
			
			if (musicToBuy.getId() != -1 && Double.parseDouble(musicToBuy.getPrice()) != 0) {// Invalid double: ""
				showPurchaseReassureDialog(Constant.ordertype_audio, msg);
			} else if (musicToBuy.getId() != -1 && Double.parseDouble(musicToBuy.getPrice()) == 0) {
				buy(Constant.ordertype_audio, musicToBuy.getId());
			} else {
//				CustomToast.makeText(context, "未能获取商品信息", Toast.LENGTH_SHORT).show();
				UpnpApp.mainHandler.showAlert(R.string.get_commodity_info_failure);
			}
			balance = -1L;// 归零余额以备下一次查询
		} else {
			UpnpApp.mainHandler.showAlert(R.string.get_balance_failure);
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
				buy(ordertype, musicToBuy.getId());
			}
		});

		builder.setPositiveButton("取消", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (Buyer.this.dialog != null && Buyer.this.dialog.isShowing()) {
					Buyer.this.dialog.dismiss();
				}
			}
		});

		try {
			dialog = builder.create();
			dialog.show();
		} catch (Exception e) {
			System.out.println("e=" + e);
			e.printStackTrace();
		}
	}

	private void clearDialog() {
		if (dialog != null && dialog.isShowing()) {
			dialog.dismiss();
			dialog = null;
		}
	}

	private void showBuyingDialog() {
		clearDialog();

		final StandardCustomDialog.Builder builder = new StandardCustomDialog.Builder(context);
		builder.setTitle("正在购买");
		builder.setMessage(null);

		AnimationDrawable ad = (AnimationDrawable) context.getResources().getDrawable(R.anim.animatior_searchbox_list);
		String str = "正在购买...";
		LinearLayout llContent = createDialogContent(ad, str);
		builder.setContentView(llContent);

		dialog = builder.create();
		dialog.show();
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

	protected void buy(final String ordertype, final long id) {
		Pools.executorService1.submit(new Runnable() {
			@Override
			public void run() {
				Looper.prepare();
				String json = new HttpPoster().buyAlbumOrMusic(ordertype, id);
				String resultcode = new JsonUtil().getOrderFeedback(json);
				finishThisBuy(ordertype, id, resultcode);
				Looper.loop();
			}
		});
	}

	protected void finishThisBuy(String ordertype, long id, String resultcode) {
		Log.e(TAG, "resultcode="+resultcode);
		if ("30".equals(resultcode)) {
//			showBuyResultDialog("操作成功\n\n即将为您缓存曲目");

			if (ordertype.equals(Constant.ordertype_album)) {

			} else if (ordertype.equals(Constant.ordertype_audio)) {
				WatchDog.purchasingMusics.put(id, 0);
				WatchDog.hasNewBought = true;// 本地数据更新标记
				new BoxControl().notifyBoxToSyn();
				if (musicToBuy != null) {
					musicToBuy.setPurchaseState("已购买");
					musicToBuy = null;
				}
			}

		} else if ("1".equals(resultcode)) {
			// 提示余额不足
			showBuyResultDialog("购买失败：余额不足");
		} else if ("5".equals(resultcode)) {
			// 提示已购买
			showBuyResultDialog("购买失败：您已经购买了该商品");
			if (ordertype.equals(Constant.ordertype_album)) {

			} else if (ordertype.equals(Constant.ordertype_audio)) {
				WatchDog.hasNewBought = true;// 本地数据更新标记
				if (musicToBuy != null) {
					musicToBuy.setPurchaseState("已购买");
					musicToBuy = null;
				}
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
		
		WatchDog.currentListeningMusic = null;
		
	}

	private void showBuyResultDialog(String msg) {
		Log.e(TAG, "showBuyResultDialog");
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

}
