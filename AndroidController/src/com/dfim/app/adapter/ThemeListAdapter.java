package com.dfim.app.adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.dfim.app.bean.LocalCache;
import com.dfim.app.bean.LocalTheme;
import com.dfim.app.bean.LocalThemes;
import com.dfim.app.common.Constant;
import com.dfim.app.data.VirtualData;
import com.dfim.app.util.BitmapUtil;
import com.dfim.app.util.LoadImageAysnc.ImageCallBack;
import com.union.cellremote.R;
import com.union.cellremote.domain.Pack;

public class ThemeListAdapter extends BaseAdapter {

	private Context context;
	private ListView listview; //主题列表
	
	private LocalThemes themes;

	public ThemeListAdapter(Context context, ListView listview, LocalThemes themes) {
		this.context = context;
		this.listview = listview;
		
		//translate: Pack > Theme
        this.themes = themes;
	}


	@Override
	public int getCount() {
		if (VirtualData.packs == null) {
			// CustomToast.makeText(context, "没有专辑数据",
			// Toast.LENGTH_SHORT).show();
			return 0;
		} else {
			return VirtualData.packs.size();
		}
	}

	@Override
	public Object getItem(int position) {
		return VirtualData.packs != null ? VirtualData.packs.get(position) : 0;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;

		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.themes_item, null);
			holder = new Holder(convertView);
			if (VirtualData.packs == null) {
				return convertView;
			}

			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}
		Pack pack = VirtualData.packs.get(position);
		
		holder.mainTitleTextView.setText(pack.getName());	//1-3主标题
		
		//
		int subTitleTextColor = context.getResources().getColor(R.color.sub_title_textcolor);
		holder.subTitleTextView.setTextColor(subTitleTextColor);
		
//		themes.refreshAlbumCacheStatus(position);
		
		LocalTheme currentTheme = themes.get(position);
		int totalMusicNum = currentTheme.getTotalMusicNum();
		
		int resourseId = R.drawable.wait;
		Log.i("ThemeCache", "getView-currentTheme.getCacheStatus()=" + currentTheme.getCacheStatus());
		switch(currentTheme.getCacheStatus()){
			case LocalCache.CACHE_STATUS_DOWNLOADED:
				resourseId = R.drawable.downloaded;
				break;
			case LocalCache.CACHE_STATUS_DOWNLOADING:
				resourseId = R.drawable.downloading;
				break;
			case LocalCache.CACHE_STATUS_WAIT:
				resourseId = R.drawable.wait;
				break;
			case LocalCache.CACHE_STATUS_FAILURE_NOSPACE:
//				resourseId = R.drawable.alert;
				resourseId = R.drawable.wait;
				break;
			default:
				resourseId = R.drawable.wait;
				break;
		}
//		Log.i("ThemeCache", "6,resourseId=" + resourseId);
//		Log.i("ThemeCache", "6,is(CACHE_STATUS_WAIT)=" + (resourseId ==R.drawable.wait));
		holder.cacheStatusImageView.setImageDrawable(context.getResources().getDrawable(resourseId)); //2-3缓存状态图标

		String subTitle = "";
		if(currentTheme.getCacheStatus()==LocalCache.CACHE_STATUS_DOWNLOADED){
			subTitle = totalMusicNum + "首音乐";
		}else{
			subTitle = "已缓存" + currentTheme.getDownloadedMusicNum() + "/" + totalMusicNum;
		}
		holder.subTitleTextView.setText(subTitle); 													//3-3副标题
		
		String imageurl = "";

		if (pack.getImgurl() != null) {
			final String imageKey = pack.getImgurl() + "150";
			imageurl = pack.getImgurl();
			holder.themeCoverImageView.setTag(imageKey);

			// 开始异步加载数据
			Bitmap bitmap = BitmapUtil.loadImageAysnc.loadImage(imageKey, imageurl, 150, false, new ImageCallBack() {

				@Override
				public void imageLoaded(Bitmap bitmap) {
					ImageView imageViewTag = (ImageView) listview.findViewWithTag(imageKey);
					if (imageViewTag != null && !bitmap.isRecycled()) {
						imageViewTag.setBackgroundDrawable(new BitmapDrawable(bitmap));
					}
				}
			});

			if (bitmap != null && !bitmap.isRecycled()) {
				holder.themeCoverImageView.setBackgroundDrawable(new BitmapDrawable(bitmap));
			} else {
				holder.themeCoverImageView.setBackgroundDrawable(new BitmapDrawable(Constant.packCover));
			}

		}

		return convertView;
	}

	class Holder {
		private ImageView themeCoverImageView;
		private TextView mainTitleTextView;
		private ImageView cacheStatusImageView;
		private TextView subTitleTextView;

		public Holder(View convertView) {
			themeCoverImageView = (ImageView) convertView.findViewById(R.id.iv_theme_cover);
			mainTitleTextView = (TextView) convertView.findViewById(R.id.tv_theme_name);
			subTitleTextView = (TextView) convertView.findViewById(R.id.tv_theme_mcount);
			cacheStatusImageView = (ImageView) convertView.findViewById(R.id.cache_status);
		}
	}

}
