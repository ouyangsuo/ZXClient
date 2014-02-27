package com.union.cellremote.http;

import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import android.content.Context;

import com.dfim.app.activity.TabWebActivity;
import com.dfim.app.common.Constant;
import com.dfim.app.common.UpnpApp;
import com.dfim.app.common.WatchDog;
import com.dfim.app.http.HttpClientProducer;
import com.dfim.app.util.JsonUtil;
import com.dfim.app.util.SignaturGenUtil;
import com.dfim.app.util.StringUtil;
import com.union.cellremote.R;
import com.union.cellremote.domain.Artist;
import com.union.cellremote.domain.Column;
import com.union.cellremote.domain.Pack;

public class HttpGetter {

	private static final String TAG = "HttpGetter";
	private Context context;

	// // HTTP查询栏目接口
	// public static final String baseUrl =
	// "http://192.168.1.230/zhenxianwang/ws/";
	// // public static final String baseUrl =
	// "http://music1.zhenxian.fm/zhenxianwang/ws/content/";

	public String pKey = "";

	public HttpGetter(Context context) {
		this.context = context;

		if ("".equals(pKey)) {
			getPKey();
		}
	}

	private void getPKey() {
//		pKey = Md5Util.process(WatchDog.currentUserId + "_" + WatchDog.currentPassword);// 正确的key:f39f81eb84d2720eef51a66b8235af15
		pKey="8920f3346d2da84d49e73b9612655c36";
		System.out.println("pKey=" + pKey);
	}

	public String getUri(String whatToQuery, String paramsInOrder) {
		String uri = "";

		String pStringToSign = StringUtil.StringFilter(paramsInOrder);// 去掉参数中的特殊字符
		System.out.println("pstringtosign=" + pStringToSign);
		String signature = SignaturGenUtil.generator(pStringToSign, pKey);

		uri = Constant.getBaseUrl() + whatToQuery + paramsInOrder + "&signature=" + signature;
		System.out.println("uri=" + uri);

		return uri;
	}

	/*
	 * 获取某栏目下的专辑列表
	 */
	public String getColumnAlbumsList(Column botique, int columnType) {
		String whatToQuery = "content/album/list?";
		// int type = columnType;
		// long id=botique.getId();
		int maxitems = 6;
		int startitem = 0;
		// Long timestamp = System.currentTimeMillis();
		String paramsInOrder = "apikey=" + Constant.apikey + "&id=" + botique.getId() + "&maxitems=" + maxitems + "&protocolver=" + Constant.protocolver + "&startitem=" + startitem + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + System.currentTimeMillis() + "&type=" + columnType;
		// String uri=getUri(whatToQuery, paramsInOrder);
		//
		// String json=doHttpGet(uri,botique.getName());
		return doHttpGet(getUri(whatToQuery, paramsInOrder), botique.getName());
	}

	public String getColumnAlbumsList(long columnId, String columnName, int startItem, int maxItems) {
		String whatToQuery = "content/album/list?";
		// int type = getColumnAlbumsType();
		int maxitems = maxItems;
		int startitem = startItem;
		// Long timestamp = System.currentTimeMillis();
		String paramsInOrder = "apikey=" + Constant.apikey + "&id=" + columnId + "&maxitems=" + maxitems + "&protocolver=" + Constant.protocolver + "&startitem=" + startitem + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + System.currentTimeMillis() + "&type=" + getColumnAlbumsType();
		// String uri=getUri(whatToQuery, paramsInOrder);

		String json = doHttpGet(getUri(whatToQuery, paramsInOrder), columnName);
		System.out.println("jsonArtistAlbums=" + json);
		return json;
	}

	private int getColumnAlbumsType() {
		if ("精品聚焦".equals(TabWebActivity.currentMenuItem)) {
			return Constant.COLUMN_ALBUMS_4_BOTIQUES;
		} else if ("TOP100".equals(TabWebActivity.currentMenuItem)) {
			return Constant.COLUMN_ALBUMS_4_TOPS;
		} else if ("类型".equals(TabWebActivity.currentMenuItem)) {
			return Constant.COLUMN_ALBUMS_4_GENRES;
		} else if ("演出者".equals(TabWebActivity.currentMenuItem)) {
			return Constant.COLUMN_ALBUMS_4_ARTISTS;
		} else if ("搜索结果".equals(TabWebActivity.currentMenuItem)) {
			return Constant.COLUMN_ALBUMS_4_ARTISTS;
		}

		return 0;
	}

	/*
	 * 获取精品聚焦栏目列表
	 */
	public List<Column> getBotiquesList() {
		String whatToQuery = "content/column?";
		int type = 1;
		// Long timestamp = System.currentTimeMillis();
		String paramsInOrder = "apikey=" + Constant.apikey + "&protocolver=" + Constant.protocolver + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + System.currentTimeMillis() + "&type=" + type;
		// String uri = getUri(whatToQuery,paramsInOrder);

		String json = doHttpGet(getUri(whatToQuery, paramsInOrder), "botiques");
		return new JsonUtil().getBotiques(json);
	}
	
	//Constant.apikey =,Constant.apikey=
	public String getBotiquesListII() {
		String whatToQuery = "content/column?";
		int type = 1;
		// Long timestamp = System.currentTimeMillis();
		String paramsInOrder = "apikey=" + Constant.apikey + "&protocolver=" + Constant.protocolver + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + System.currentTimeMillis() + "&type=" + type;
		// String uri = getUri(whatToQuery,paramsInOrder);
		
		String json = doHttpGet(getUri(whatToQuery, paramsInOrder), "botiques");
		return json;
	}

	/*
	 * 获取演出者列表
	 */
	public List<Artist> getArtistsList(int startitem, int maxitems, String firstLetter) {
		// content/artist/list? startitem=STARTITEM&maxitems=MAXITEMS&
		// firstletter=FIRSTLETTER&apikey=DEVICENO&timestamp=TIMESTATMP&
		// protocolver=PROTOCOLVER&signature=SIGNATURE
		String whatToQuery = "content/artist/list?";
		// int startitem=0;
		// int maxitems=20;
		// String firstletter = firstLetter == null ? "all" : firstLetter;
		// Long timestamp = System.currentTimeMillis();
		String paramsInOrder = "apikey=" + Constant.apikey + "&firstletter=" + firstLetter + "&maxitems=" + maxitems + "&protocolver=" + Constant.protocolver + "&startitem=" + startitem + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + System.currentTimeMillis();
		// String uri = getUri(whatToQuery,paramsInOrder);

		String json = doHttpGet(getUri(whatToQuery, paramsInOrder), "artists");

		return new JsonUtil().getArtists(json);
	}

	/*
	 * 获取音乐主题列表
	 */
	public List<Pack> getThemesList(int startitem, int maxitems) {
		// content/pack/list?
		// maxitems=MAXITEMS&startitem=STARTITEM&apikey=DEVICENO&
		// timestamp=TIMESTATMP&protocolver=PROTOCOLVER& signature=SIGNATURE
		String whatToQuery = "content/pack/list?";
		Long timestamp = System.currentTimeMillis();
		String paramsInOrder = "apikey=" + Constant.apikey + "&maxitems=" + maxitems + "&protocolver=" + Constant.protocolver + "&startitem=" + startitem + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + System.currentTimeMillis();
		// String uri = getUri(whatToQuery,paramsInOrder);

		String json = doHttpGet(getUri(whatToQuery, paramsInOrder), "artists");
		System.out.println("jsonThemes=" + json);

		return new JsonUtil().getThemes(json);
	}

	private String doHttpGet(String uri, String tag) {
		String json = "";

//		DefaultHttpClient client = new DefaultHttpClient();
//		client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, 100);
//		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, 100);
		
		DefaultHttpClient client = new HttpClientProducer().getHttpClient(Constant.CONNECTION_TIMEOUT_MILLIS, Constant.SOCKET_TIMEOUT_MILLIS);//设置client的连接超时和读取数据超时时间

		HttpGet httpGet = new HttpGet(uri);
		// HttpContext httpContext = new BasicHttpContext();

		try {
			HttpResponse response = client.execute(httpGet);

			if (response.getStatusLine().getStatusCode() != 200) {
				 System.out.println("获取数据失败!");
				 System.out.println("response.getStatusLine().getStatusCode()=" + response.getStatusLine().getStatusCode());
				UpnpApp.mainHandler.showAlert(R.string.network_unnormal_alert);
				
				json=UpnpApp.context.getResources().getString(R.string.data_load_failed);
				currentFragmentShowNoData();
				
				httpGet.abort();
			} else {
				System.out.println(">>>>>>>>>>>>>>获取数据成功<<<<<<<<<<<<<<<<");
				HttpEntity entity = response.getEntity();
				json = EntityUtils.toString(entity, "utf-8").trim();
				// System.out.println(tag+": " + json);
				// System.out.println(">>>>>>>>>>>>>>--------------<<<<<<<<<<<<<<<<");
			}

		} catch (Exception e) {
			System.out.println("发现异常：e="+e);
			e.printStackTrace();

			json=UpnpApp.context.getResources().getString(R.string.data_load_failed);
			currentFragmentShowNoData();
			
		} finally {
			client.getConnectionManager().shutdown();
			System.out.println("client 已关闭");
		}

		System.out.println("json="+json);
		return json;
	}	

	private void currentFragmentShowNoData() {
		WatchDog.currentSelfReloader.onDataLoadFailed();
	}

	/*
	 * 根据专辑ID获取专辑详情数据
	 */
	public String getAlbumDetail(long albumId) {
		String whatToQuery = "content/album/detail?";
		// Long timestamp = System.currentTimeMillis();
		String paramsInOrder = "apikey=" + Constant.apikey + "&id=" + albumId + "&protocolver=" + Constant.protocolver + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + System.currentTimeMillis();
		// String uri=getUri(whatToQuery, paramsInOrder);

		String json = doHttpGet(getUri(whatToQuery, paramsInOrder), "getAlbumDetail");
		return json;
	}

	/*
	 * 根据专辑ID获取音乐主题详情数据
	 */
	public String getPackDetail(long packId) {
		// content/pack/detail?id=PACKID &apikey=DEVICENO&timestamp=TIMESTATMP&
		// protocolver=PROTOCOLVER&signature=SIGNATURE
		String whatToQuery = "content/pack/detail?";
		// Long timestamp = System.currentTimeMillis();
		String paramsInOrder = "apikey=" + Constant.apikey + "&id=" + packId + "&protocolver=" + Constant.protocolver + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + System.currentTimeMillis();
		// String uri=getUri(whatToQuery, paramsInOrder);

		// String json=doHttpGet(getUri(whatToQuery,
		// paramsInOrder),"getPackDetail");
		return doHttpGet(getUri(whatToQuery, paramsInOrder), "getPackDetail");
	}

	public String getBalance() {
		String whatToQuery = "balanceforpad?";
		String paramsInOrder = "apikey=" + Constant.apikey + "&protocolver=" + Constant.protocolver + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + System.currentTimeMillis();
		return doHttpGet(getUri(whatToQuery, paramsInOrder), "getBalance");
	}

	/*
	 * 查询排行榜栏目
	 */
	public List<Column> getTopsList() {
		String whatToQuery = "content/column?";
		int type = 5;// 排行榜
		// Long timestamp = System.currentTimeMillis();
		String paramsInOrder = "apikey=" + Constant.apikey + "&protocolver=" + Constant.protocolver + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + System.currentTimeMillis() + "&type=" + type;
		// String uri = getUri(whatToQuery,paramsInOrder);

		String json = doHttpGet(getUri(whatToQuery, paramsInOrder), "tops");

		return new JsonUtil().getTops(json);
	}

	/*
	 * 查询类型（栏目）
	 */
	public List<Column> getGenresList() {
		String whatToQuery = "content/genres?";
		// int id=10000;//固定值//ken 2013.10.21 既然是固定值就不要写出来了呀
		// Long timestamp = System.currentTimeMillis();
		String paramsInOrder = "apikey=" + Constant.apikey + "&id=" + 10000 + "&protocolver=" + Constant.protocolver + "&terminaltype=" + Constant.terminaltype + "&timestamp=" + System.currentTimeMillis();
		// String uri = getUri(whatToQuery,paramsInOrder);

		String json = doHttpGet(getUri(whatToQuery, paramsInOrder), "genres");
		System.out.println("jsonGenres=" + json);

		return new JsonUtil().getGenres(json);
	}

}