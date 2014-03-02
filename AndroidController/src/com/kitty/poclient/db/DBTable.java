package com.kitty.poclient.db;

public class DBTable {
   public static String  create_table_music=
		   "create table if not exists db_music (id long primary key , name varchar(500),play_time varchar(100) ,file_size double(20) ,first_char varchar(500),track_no integer, media_url varchar(200),img_url varchar(200),buytime Date,disk_id long,pack_id long,lib_id long,isclearcache INTEGER DEFAULT 0 )"; //单曲表 
   
   public static String create_table_album="create table if not exists 'db_album' ('id' long primary key ,'name' varchar(500),img_url varchar(500),buytime varchar(100),orderType varchar(10) , isclearcache INTEGER DEFAULT 0 )"; //专辑表
   
   public static String create_table_disk="create table if not exists db_disk (id long primary key ,name varchar(500),disk_no INTEGER,album_id long  ) "; //碟表
   
   public static String create_table_artist="create table if not exists db_artist(id long primary key ,name varchar(500) ,img_url varchar(200),firstchar varchar(10))"; //演出者表
   
   public static String create_table_product_artist="create table if not exists product_artist(product_id,artist_id)";//商品演出者关联表
   
   public static String create_table_pack="create table if not exists db_pack (id long primary key ,name varchar(500),image_url varchar(500) ,buytime varchar(500),libraryid long ,isclearcache INTEGER DEFAULT 0)";//主题表
   
   public static String create_table_technology="create table if not exists db_technology (id long primary key ,name varchar(100),img_url varchar(200))";
   
   public static String create_table_p_type="create table if not exists db_type (id long primary key ,name varchar(100), img_url varchar(100))";
   
   public static String create_table_product_technology="create table if not exists product_technology (product_id long ,technology_id long )";
   
   public static String create_table_product_pack="create table if not exists product_pack (product_id long ,pack_id long)";
   
   public static String create_table_zx_tag="create table if not exists db_zx_tag(id long ,zxno long ,tag int )";
   
   public static String create_table_search_history="create table if not exists db_search_history(id integer auto increment, search_text varchar(50) primary key,timemillis long,in_use integer default 1 )";
	
}
