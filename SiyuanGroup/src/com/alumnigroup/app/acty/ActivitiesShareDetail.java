package com.alumnigroup.app.acty;

import java.util.ArrayList;
import java.util.List;

import org.apache.http.Header;
import org.json.JSONObject;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.alumnigroup.adapter.CommentAdapter;
import com.alumnigroup.api.IssuesAPI;
import com.alumnigroup.api.RestClient;
import com.alumnigroup.api.StarAPI;
import com.alumnigroup.app.AppInfo;
import com.alumnigroup.app.BaseActivity;
import com.alumnigroup.app.R;
import com.alumnigroup.entity.Comment;
import com.alumnigroup.entity.ErrorCode;
import com.alumnigroup.entity.Issue;
import com.alumnigroup.entity.MActivity;
import com.alumnigroup.entity.User;
import com.alumnigroup.imple.JsonResponseHandler;
import com.alumnigroup.utils.CalendarUtils;
import com.alumnigroup.utils.CommonUtils;
import com.alumnigroup.utils.Constants;
import com.alumnigroup.widget.CommentView;
import com.nostra13.universalimageloader.core.ImageLoader;

/**
 * 活动分享详情
 * 
 * @author Jayin Ton
 * 
 */
public class ActivitiesShareDetail extends BaseActivity {

	private View btn_back, btn_share, btn_favourite, btn_comment, btn_space,
			btn_delete, btn_edit;
	private TextView tv_title, tv_body, tv_username, tv_time, tv_notify;
	private ImageView iv_avater;
	private Issue issue;
	// private PullAndLoadListView lv_comment;
	private CommentView lv_comment;
	private List<Comment> data_commet;
	private CommentAdapter adapter_commet;
	private IssuesAPI api;
	private User user;
	private View vistor, owner;
	private BroadcastReceiver mReceiver;
	private MActivity activity;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.acty_activitiessharedetail);
		initData();
		initLayout();
		openReceiver();
	}

	// 评论成功后添加评论条目
	private void openReceiver() {
		mReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				if (intent.getAction()
						.equals(Constants.Action_Issue_Comment_Ok)) {
					if (data_commet.isEmpty())
						tv_notify.setVisibility(View.GONE);
					Comment comment = (Comment) intent
							.getSerializableExtra("comment");
					CommonUtils.reverse(data_commet);
					data_commet.add(comment);
					CommonUtils.reverse(data_commet);
					// adapter_commet.notifyDataSetChanged();
					lv_comment.setAdapter(new CommentAdapter(getContext(),
							data_commet));

				}

			}
		};
		registerReceiver(mReceiver, new IntentFilter(
				Constants.Action_Issue_Comment_Ok));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (mReceiver != null)
			unregisterReceiver(mReceiver);
	}

	@Override
	protected void initData() {
		issue = (Issue) getSerializableExtra("issue");
		activity = (MActivity) getSerializableExtra("activity");
		user = AppInfo.getUser(getContext());
		if (user == null) {
			toast("无用户信息，请重新登录");
			closeActivity();
		}
		api = new IssuesAPI();
		data_commet = new ArrayList<Comment>();
	}

	@Override
	protected void initLayout() {
		tv_notify = (TextView) _getView(R.id.acty_communicationdetail_tv_notify);
		tv_username = (TextView) _getView(R.id.acty_communicationdetail_tv_name);
		tv_time = (TextView) _getView(R.id.acty_communicationdetail_tv_posttime);
		tv_title = (TextView) _getView(R.id.item_lv_acty_comminication_title);
		tv_body = (TextView) _getView(R.id.item_lv_acty_comminication_body);
		iv_avater = (ImageView) _getView(R.id.acty_communicationdetail_iv_avater);

		owner = _getView(R.id.owner);
		vistor = _getView(R.id.visitor);
		if (issue.getUser().getId() == user.getId()) {
			owner.setVisibility(View.VISIBLE);
			vistor.setVisibility(View.GONE);
		} else {
			owner.setVisibility(View.GONE);
			vistor.setVisibility(View.VISIBLE);
		}

		// 头像
		if (issue.getUser().getAvatar() != null) {
			ImageLoader.getInstance().displayImage(
					RestClient.BASE_URL + issue.getUser().getAvatar(),
					iv_avater);
		} else {
			ImageLoader.getInstance().displayImage(
					"drawable://" + R.drawable.ic_image_load_normal, iv_avater);
		}
		tv_username.setText(issue.getUser().getProfile().getName());
		tv_time.setText(CalendarUtils.getTimeFromat(issue.getPosttime(),
				CalendarUtils.TYPE_timeline));
		tv_title.setText(issue.getTitle());
		tv_body.setText(issue.getBody());

		btn_back = _getView(R.id.acty_head_btn_back);
		btn_space = _getView(R.id.acty_communicationdetail_btn_space);
		btn_share = _getView(R.id.acty_communicationdetail_footer_share);
		btn_comment = _getView(R.id.acty_communicationdetail_footer_comment);
		btn_favourite = _getView(R.id.acty_communicationdetail_footer_favourite);
		btn_delete = _getView(R.id.btn_delete);
		btn_edit = _getView(R.id.btn_edit);

		btn_back.setOnClickListener(this);
		btn_space.setOnClickListener(this);
		btn_share.setOnClickListener(this);
		btn_comment.setOnClickListener(this);
		btn_favourite.setOnClickListener(this);
		btn_delete.setOnClickListener(this);
		btn_edit.setOnClickListener(this);

		lv_comment = (CommentView) _getView(R.id.item_lv_acty_comminication_lv_comment);
		adapter_commet = new CommentAdapter(getContext(), data_commet);
		lv_comment.setAdapter(adapter_commet);

		api.view(issue.getId(), new JsonResponseHandler() {
			@Override
			public void onStart() {
				tv_notify.setText("评论加载中....");
				tv_notify.setVisibility(View.VISIBLE);
			}

			@Override
			public void onOK(Header[] headers, JSONObject obj) {
				tv_notify.setVisibility(View.GONE);
				List<Comment> newData_comment = Comment.create_by_jsonarray(obj
						.toString());
				if (newData_comment != null && newData_comment.size() > 0) {
					data_commet.addAll(newData_comment);
					lv_comment.setAdapter(new CommentAdapter(getContext(),
							data_commet));
				} else {
					if (newData_comment == null) {
						tv_notify.setVisibility(View.VISIBLE);
						tv_notify.setText("网络异常,解析错误");
						toast("网络异常,解析错误");
					} else if (newData_comment.size() == 0) {
						toast("还没有人评论!");
						tv_notify.setText("还没有人评论!");
						tv_notify.setVisibility(View.VISIBLE);
					}
				}
			}

			@Override
			public void onFaild(int errorType, int errorCode) {
				toast(ErrorCode.errorList.get(errorCode));
				tv_notify.setVisibility(View.GONE);
			}
		});
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.acty_head_btn_back:
			closeActivity();
			break;
		case R.id.acty_communicationdetail_btn_space:
			// 去个人空间
			Intent intent = null;
			if (issue.getUser().getId() == AppInfo.getUser(getContext())
					.getId()) {
				intent = new Intent(this, SpacePersonal.class);
			} else {
				intent = new Intent(this, SpaceOther.class);
			}
			intent.putExtra("user", issue.getUser());
			openActivity(intent);
			break;
		case R.id.acty_communicationdetail_footer_share:
			// 分享到圈子
			// toast("share");
			break;
		case R.id.acty_communicationdetail_footer_comment:
			// 评论
			Intent comIntent = new Intent(this, CommunicationComment.class);
			comIntent.putExtra("issue", issue);
			openActivity(comIntent);
			break;
		case R.id.acty_communicationdetail_footer_favourite:
			// 收藏
			faviourite();
			break;

		case R.id.btn_delete:
			delete();
			break;
		case R.id.btn_edit:
			edit();
			break;
		default:
			break;
		}
	}

	private void delete() {
		api.deleteIssue(issue.getId(), new JsonResponseHandler() {

			@Override
			public void onOK(Header[] headers, JSONObject obj) {
				toast("删除成功");
				closeActivity();
			}

			@Override
			public void onFaild(int errorType, int errorCode) {
				toast("删除失败  " + ErrorCode.errorList.get(errorCode));
			}
		});

	}

	private void edit() {
		Intent intent = new Intent(this, ActivitiesSharePublish.class);
		intent.putExtra("issue", issue);
		intent.putExtra("activity", activity);
		openActivity(intent);
	}

	// 收藏的remark默认为期类型名
	private void faviourite() {
		StarAPI starapi = new StarAPI();
		starapi.star(StarAPI.Item_type_issue, issue.getId(), "issue",
				new JsonResponseHandler() {

					@Override
					public void onOK(Header[] headers, JSONObject obj) {
						toast("收藏成功");
					}

					@Override
					public void onFaild(int errorType, int errorCode) {
						toast("收藏失败 " + ErrorCode.errorList.get(errorCode));
					}
				});

	}

}