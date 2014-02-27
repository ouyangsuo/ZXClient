package com.union.cellremote.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.union.cellremote.R;
import com.union.cellremote.store.PurchasedFragment;

public class SlidingMenuListAdapter extends BaseAdapter {

	private Context context;
	private String[] menuItems;
	private String currentTab;
	private String currentFragmentInTabMusic;
	private String currentFragmentInTabWeb;

	public SlidingMenuListAdapter(Context context, String[] menuItems, String currentTab, String currentFragment) {
		this.context = context;
		this.menuItems = menuItems;
		this.currentTab = currentTab;

		if ("tab_music".equals(currentTab)) {
			this.currentFragmentInTabMusic = currentFragment;
		} else if ("tab_web".equals(currentTab)) {
			this.currentFragmentInTabWeb = currentFragment;
		}
	}

	@Override
	public int getCount() {
		return menuItems.length;
	}

	@Override
	public Object getItem(int position) {
		return menuItems[position];
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		Holder holder;

		if (convertView == null || convertView.getTag() == null) {
			convertView = LayoutInflater.from(context).inflate(R.layout.sling_menu_item, null);
			holder = new Holder(convertView);

			convertView.setTag(holder);
		} else {
			holder = (Holder) convertView.getTag();
		}

		holder.tvMenuItem.setText(menuItems[position]);

		// 设置菜单条目的图标
		if (currentTab.equals("tab_music")) {
			if (menuItems[position].equals("专辑")) {
				holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_album);
			} else if (menuItems[position].equals("单曲")) {
				holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_single);
			} else if (menuItems[position].equals("主题")) {
				holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_theme);
			}

			// 当前选中菜单项
			if (currentFragmentInTabMusic.equals(menuItems[position])) {
				// 设置菜单条目的背景色
				holder.llRoot.setBackgroundColor(context.getResources().getColor(R.color.sliding_menu_selected_bg));
				holder.ivArrow.setBackgroundResource(R.drawable.arrow);

				// 设置高亮图标
				if (menuItems[position].equals("专辑")) {
					holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_album_2);
				} else if (menuItems[position].equals("单曲")) {
					holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_single_2);
				} else if (menuItems[position].equals("主题")) {
					holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_theme_2);
				} else if (menuItems[position].equals("歌单")) {
					holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_album_2);
				}
			} else {
				holder.llRoot.setBackgroundColor(context.getResources().getColor(R.color.transparent));
				holder.ivArrow.setBackgroundResource(R.drawable.arrow_unselect);
			}

		} else if (currentTab.equals("tab_web")) {
			if (menuItems[position].equals("精品聚焦")) {
				holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_botique);
			} else if (menuItems[position].equals("TOP100")) {
				holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_top);
			} else if (menuItems[position].equals("演出者")) {
				holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_artist);
			} else if (menuItems[position].equals("类型")) {
				holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_type);
			} else if (menuItems[position].equals("音乐主题")) {
				holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_music_theme);
			} else if (menuItems[position].equals("我的钱包")) {
				holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_purse);
			}else if(menuItems[position].equals(PurchasedFragment.FragmentName)){ //"已购音乐"
				holder.ivMenuItem.setImageResource(R.drawable.pursedmusic);
			}

			// 当前选中菜单项
			if (currentFragmentInTabWeb.equals(menuItems[position])) {
				// 设置菜单条目的背景色
				holder.llRoot.setBackgroundColor(context.getResources().getColor(R.color.sliding_menu_selected_bg));
				holder.ivArrow.setBackgroundResource(R.drawable.arrow);

				// 设置高亮图标
				if (menuItems[position].equals("精品聚焦")) {
					holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_botique_2);
				} else if (menuItems[position].equals("TOP100")) {
					holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_top_2);
				} else if (menuItems[position].equals("演出者")) {
					holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_artist_2);
				} else if (menuItems[position].equals("类型")) {
					holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_type_2);
				} else if (menuItems[position].equals("音乐主题")) {
					holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_music_theme_2);
				} else if (menuItems[position].equals("我的钱包")) {
					holder.ivMenuItem.setImageResource(R.drawable.sliding_menu_purse_2);
				} else if (menuItems[position].equals(PurchasedFragment.FragmentName)) {//"已购音乐"
					holder.ivMenuItem.setImageResource(R.drawable.pursedmusic_pres);
				}
			} else {
				holder.llRoot.setBackgroundColor(context.getResources().getColor(R.color.transparent));
				holder.ivArrow.setBackgroundResource(R.drawable.arrow_unselect);
			}

		} else if (currentTab.equals("tab_device")) {

		} else if (currentTab.equals("tab_settings")) {

		}

		return convertView;
	}

	class Holder {
		private ImageView ivMenuItem;
		private ImageView ivArrow;
		private TextView tvMenuItem;
		private LinearLayout llRoot;

		public Holder(View convertView) {
			ivMenuItem = (ImageView) convertView.findViewById(R.id.iv_menu_item);
			ivArrow = (ImageView) convertView.findViewById(R.id.iv_arrow);
			tvMenuItem = (TextView) convertView.findViewById(R.id.tv_menu_item);
			llRoot = (LinearLayout) convertView.findViewById(R.id.ll_menuitem_root);
		}
	}

}
