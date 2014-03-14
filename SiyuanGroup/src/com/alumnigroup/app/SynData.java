package com.alumnigroup.app;


import org.apache.http.Header;
import org.json.JSONException;
import org.json.JSONObject;

import com.alumnigroup.api.ActivityAPI;
import com.alumnigroup.api.GroupAPI;
import com.alumnigroup.entity.MActivity;
import com.alumnigroup.entity.MGroup;
import com.alumnigroup.imple.JsonResponseHandler;
import com.alumnigroup.utils.Constants;
import com.google.gson.internal.JsonReaderInternalAccess;
import com.google.gson.stream.JsonReader;

import android.content.Context;
import android.content.Intent;

/**
 * 数据同步，通常修改后去同步
 * 
 * @author Jayin Ton
 * 
 */
public class SynData {
    /**
     * 同步一圈子的信息,并发送修改广播
     * @param context
     * @param groupid
     * @param listener can be  null 
     */
	public static void SyncGroupInfo(final Context context, int groupid,
			final SynDataListener listener) {
		GroupAPI api = new GroupAPI();
		api.view(groupid, new JsonResponseHandler() {

			@Override
			public void onOK(Header[] headers, JSONObject obj) {
				try {
					MGroup _group = MGroup.create_by_json(obj.getJSONObject(
							"group").toString());
					AppCache.changeGroupItem(context, _group); //修改缓存数据
					Intent intent = new Intent(Constants.Action_GroupInfo_Edit);
					intent.putExtra("group", _group);
					context.sendBroadcast(intent);
					if(listener!=null)listener.onSuccess(_group);
				} catch (JSONException e) {
					e.printStackTrace();
					if(listener!=null)listener.onFaild();
				}
			}

			@Override
			public void onFaild(int errorType, int errorCode) {
				if(listener!=null)listener.onFaild();
			}
		});
	}
	/**
	 * 同步活动信息
	 * @param context
	 * @param actyId
	 * @param listener
	 */
	public static void SyncActivityInfo(final Context context,int actyId,final SynDataListener listener){
		   ActivityAPI api = new ActivityAPI();
		   api.view(actyId, new JsonResponseHandler() {
			
			@Override
			public void onOK(Header[] headers, JSONObject obj) {
				 try {
					MActivity acty = MActivity.create_by_json(obj.getJSONObject("activity").toString());
					AppCache.changeActivityInfo(context, acty);
					Intent intent = new Intent(Constants.Action_ActivityInfo_Edit);
					intent.putExtra("activity", acty);
					context.sendBroadcast(intent);
					if(listener!=null)listener.onSuccess(acty);
				} catch (JSONException e) {
					e.printStackTrace();
					if(listener!=null)listener.onFaild();
				}
			}
			
			@Override
			public void onFaild(int errorType, int errorCode) {
				if(listener!=null)listener.onFaild();
			}
		});
	}
	
	
    /**
     * 回调接口
     * @author Jayin Ton
     *
     */
	public interface SynDataListener {
		public void onSuccess(Object result);

		public void onFaild();
	}

}
