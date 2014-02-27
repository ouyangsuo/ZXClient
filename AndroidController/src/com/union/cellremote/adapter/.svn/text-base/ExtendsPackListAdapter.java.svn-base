package com.union.cellremote.adapter;

import java.util.List;

import com.dfim.app.data.VirtualData;
import com.dfim.app.domain.Album;
import com.dfim.app.domain.Pack;
import com.dfim.app.upnp.BoxControl;
import com.dfim.app.util.BitmapUtil;
import com.dfim.app.util.LoadImageAysnc.ImageCallBack;
import com.union.cellremote.R;
import com.union.cellremote.adapter.ThemeListAdapter.Holder;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ExtendsPackListAdapter extends BaseAdapter {
    private List<Pack> liPack;
    private Context context;
    private ListView listView;
    private int flag;
    public ExtendsPackListAdapter(){
    	
    }
    public ExtendsPackListAdapter(Context context,List<Pack> lipack,ListView listview,int flag){
    	this.liPack=lipack;
    	this.context=context;
    	this.listView=listview;
    	this.flag=flag;
    }
    
    
	public void setLiPack(List<Pack> liPack) {
		this.liPack = liPack;
	}
	@Override
	public int getCount() {
		return liPack!=null?liPack.size():0;
	}

	@Override
	public Object getItem(int position) {
		return liPack!=null?liPack.get(position):null;
	}

	@Override
	public long getItemId(int position) {
		
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        Holder holder;
		if (convertView==null || convertView.getTag()==null) {
			convertView=LayoutInflater.from(context).inflate(R.layout.themes_item_for_cloud, null);
			holder=new Holder(convertView);		
			//holder.ivCover.setImageResource(R.drawable.icon);
			if(liPack==null){
				return convertView;
			}
	
			convertView.setTag(holder);
			holder.imbutton.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					int pos = ((Integer) v.getTag()).intValue();
					v.setVisibility(View.GONE);
					Pack pack=liPack.get(pos);
				    if(pack!=null&&pack.getIsCloud()==0){
				    	if(flag!=0){
				    	liPack.remove(pack);
				    	}else{
				    		pack.setIsCloud(5);
				    	}
				    	new BoxControl().notifyBoxUpdateCloud(pack.getId()+"", 15);
				    }
				    notifyDataSetChanged();
				}
			});
		}else{
			holder=(Holder) convertView.getTag();
		}
		holder.imbutton.setTag(position);
		Pack pack=liPack.get(position);
		if(pack.getIsCloud()==0){
			holder.imbutton.setVisibility(View.VISIBLE);
			
		}else {
			holder.imbutton.setVisibility(View.GONE);
		}
//		String name=pack.getName().length()>20?(pack.getName().substring(0, 20)+"..."):pack.getName();
		holder.tvName.setText(pack.getName());
     	holder.mcount.setText(liPack.get(position).getMcount()+"首音乐");
		String imageurl="";
		if(pack.getImgurl()!=null){
			final String imageKey=pack.getImgurl()+"150";
			imageurl=pack.getImgurl();
			holder.ivCover.setTag(imageKey);
			//开始异步加载数据
			  Bitmap bitmap=BitmapUtil.loadImageAysnc.loadImage(imageKey,imageurl, 150,false,new ImageCallBack(){

					@Override
					public void imageLoaded(Bitmap bitmap) {
						   ImageView imageViewTag =(ImageView)listView.findViewWithTag(imageKey);
						   if(imageViewTag !=null){
							   imageViewTag.setBackgroundDrawable(new BitmapDrawable(bitmap));
							   
						   }
						
						
					}} );
			  if(bitmap!=null){
				  holder.ivCover.setBackgroundDrawable(new BitmapDrawable(bitmap));
			  }
		}
		

		return convertView;
	}
	class Holder{		
		private ImageView ivCover;
		private TextView tvName;
		private TextView mcount;
		private ImageButton imbutton;
		public Holder(View convertView){
			ivCover=(ImageView) convertView.findViewById(R.id.iv_theme_cover);
			tvName=(TextView) convertView.findViewById(R.id.tv_theme_name);
			mcount=(TextView) convertView.findViewById(R.id.tv_theme_mcount);
			imbutton= (ImageButton) convertView.findViewById(R.id.cloud_download_bt);
		}
	}
}
