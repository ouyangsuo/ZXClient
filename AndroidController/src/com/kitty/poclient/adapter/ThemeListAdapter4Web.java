package com.kitty.poclient.adapter;

import java.lang.ref.SoftReference;
import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.kitty.poclient.R;
import com.kitty.poclient.domain.Pack;
import com.kitty.poclient.fragment.store.ThemesFragment;
import com.kitty.poclient.thread.Pools;
import com.kitty.poclient.util.BitmapUtil;
import com.kitty.poclient.util.LoadImageAysnc.ImageCallBack;

public class ThemeListAdapter4Web extends BaseAdapter {

	private Context context;
//	private ThemesFragment fragment;
	private List<Pack> themes;

	public ThemeListAdapter4Web(Context context, ThemesFragment fragment) {
		this.context = context;
//		this.fragment = fragment;
	}

	public List<Pack> getThemes() {
		return themes;
	}

	public void setThemes(List<Pack> themes) {
		this.themes = themes;
	}

	@Override
	public int getCount() {
		// System.out.println("getCount()=" + themes.size());
		return themes.size();
	}

	@Override
	public Object getItem(int position) {
		return themes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;

		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.themes_item_4_web, null);
			holder = new Holder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		holder.tvName.setText(themes.get(position).getName());
		holder.mcount.setText("共" + themes.get(position).getMcount() + "首音乐");
		holder.ivCover.setImageBitmap(themes.get(position).getBitmap());

//		/* 显示屏幕区域内的图片，释放其它 */
//		if (position >= fragment.getFistVisiblePosition() - 1 && position <= fragment.getLastVisiblePosition() + 1) {
//			System.out.println("load:position=" + position);
//			loadPackBitmap(holder, position);
//		} else {
//			System.out.println("free:position=" + position);
//			freePackBitmap(holder, position);
//		}

		return convertView;
	}

//	private void freePackBitmap(Holder holder, int position) {
//		holder.ivCover.setImageBitmap(null);
//		holder.ivCover.setBackgroundResource(R.drawable.theme_cover_bg);
//		themes.get(position).recyleBitmap();
//	}

//	private void loadPackBitmap(Holder holder, int position) {
//		Bitmap bmp = themes.get(position).getImageBitmap();
//		if (bmp != null && !bmp.equals(Constant.packCover) && !bmp.isRecycled()) {
//			holder.ivCover.setImageBitmap(bmp);
//			holder.ivCover.setBackgroundDrawable(null);
//		} else {
//			downloadImage(themes.get(position), holder, position);
//			System.out.println("downloadImage:position=" + position);
//		}
//		bmp = null;
//	}

	protected void downloadImage(final Pack pack, final Holder holder, final int position) {
		Pools.executorService2.submit(new Runnable() {
			@Override
			public void run() {
				// Looper.prepare();
				String imageKey = pack.getImgurl() + "150";

				Bitmap bitmap = BitmapUtil.loadImageAysnc.loadImageNohandler(imageKey, pack.getImgurl(), 150, false, new ImageCallBack() {
					@Override
					public void imageLoaded(Bitmap bitmap) {
						pack.setBitmap(new SoftReference<Bitmap>(bitmap));				
					}
				});

				// 得到封面后刷新界面
				if (bitmap != null) {
					pack.setBitmap(new SoftReference<Bitmap>(bitmap));					
				} 

				bitmap = null;
				// Looper.loop();
			}
		});
	}

	class Holder {
		private ImageView ivCover;
		private TextView tvName;
		private TextView mcount;

		public Holder(View convertView) {
			ivCover = (ImageView) convertView.findViewById(R.id.iv_theme_cover);
			tvName = (TextView) convertView.findViewById(R.id.tv_theme_name);
			mcount = (TextView) convertView.findViewById(R.id.tv_theme_mcount);
		}
	}

}
