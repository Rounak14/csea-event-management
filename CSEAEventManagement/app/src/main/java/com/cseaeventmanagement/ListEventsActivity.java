package com.cseaeventmanagement;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;

import com.android.volley.Network;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.BasicNetwork;
import com.android.volley.toolbox.HurlStack;
import com.android.volley.toolbox.NoCache;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ListEventsActivity extends AppCompatActivity {
	boolean reqinqueue;
	private ProgressBar p;
	private RequestQueue q;
	private RecyclerView recyclerView;
	private JSONArray resp;
	private JSONObject temp;
	private List<List_Event_Data_POJO> List_Events;
	private ListEventAdapter adapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		reqinqueue = false;
		Network network = new BasicNetwork(new HurlStack());
		q = new RequestQueue(new NoCache(), network);
		q.start();
		setContentView(R.layout.activity_list_events);
		p = findViewById(R.id.loading);
		recyclerView = findViewById(R.id.recyclerView);
		List_Events = new ArrayList<>();
		adapter = new ListEventAdapter(null);
		recyclerView.setHasFixedSize(true);
		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		recyclerView.setAdapter(adapter);
		getEvents();
	}

	private void showProgress(final boolean show) {
		int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

		recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
		recyclerView.animate().setDuration(shortAnimTime).alpha(
				show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
			}
		});

		p.setVisibility(show ? View.VISIBLE : View.GONE);
		p.animate().setDuration(shortAnimTime).alpha(
				show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
			@Override
			public void onAnimationEnd(Animator animation) {
				p.setVisibility(show ? View.VISIBLE : View.GONE);
			}
		});

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		q.cancelAll(this.getClass());
	}

	private void getEvents() {
		StringRequest jor = new StringRequest(
				Request.Method.GET,
				getSharedPreferences(getString(R.string.ip_pref), 0).getString("ip","127.0.0.1:8000") + "api/events/",
				new Response.Listener<String>() {
					@Override
					public void onResponse(String response) {
						Log.d("hello", response);
						showProgress(false);
						try {
							resp = new JSONArray(response);
						} catch (Exception e) {
							Log.d("hello", "Malformed JSON");
						}
						checkResponse();
					}
				},
				new Response.ErrorListener() {
					@Override
					public void onErrorResponse(VolleyError error) {
						Log.d("hello", error.toString());
						showProgress(false);
						Snackbar.make(recyclerView, "Error getting event data. Check your network and try again", Snackbar.LENGTH_SHORT)
								.setAction("Dismiss", new View.OnClickListener() {
									@Override
									public void onClick(View v) {

									}
								}).show();
					}
				}
		);
		jor.setTag(this.getClass());
		q.add(jor);
		showProgress(true);
	}

	private void checkResponse() {
		if (resp.length() == 0)
			return;
		JSONObject[] events = new JSONObject[resp.length()];

		try {
			for (int i = 0; i < resp.length(); i++)
				events[i] = resp.getJSONObject(i);
		} catch (Exception e) {
		}
		Log.d("hello", "haha4");
		for (int i = 0; i < events.length; i++) {
			String q = "";
			String w = "";
			String s = "";
			String ee = "";
			String a = "";
			try {
				ee = events[i].getString("approval");
				q = events[i].getString("name");
				w = events[i].getString("date");
				String[] wt = w.split("-");
				w = "Date=" + wt[2] + "/" + wt[1] + "/" + wt[0];
				if(!events[i].getString("time").equals("null"))
					w+=", Time="+events[i].getString("time");
				a = events[i].getString("summary");
				s = events[i].getString("event_id");
			} catch (Exception e) {
			}
			if (!(ee.equals("Pend")))
				List_Events.add(new List_Event_Data_POJO(q, w, a, s));
		}
		List_Event_Data_POJO[] myArray = new List_Event_Data_POJO[List_Events.size()];
		for (int j = 0; j < List_Events.size(); j++) {
			myArray[j] = List_Events.get(j);
		}
		Log.d("hello", "haha8");
		adapter.setData(myArray);
		adapter.notifyDataSetChanged();

		showProgress(false);

	}
}
