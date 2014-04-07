package com.vt.vthacks;

import java.util.ArrayList;
import java.util.List;

import com.vt.vthacks.model.IPhotoStreamItem;
import com.vt.vthacks.model.impl.TwitterPhotoStreamItem;
import com.vt.vthacks.view.OnImageClickListener;
import com.vt.vthacks.view.PhotoStreamAdapter;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import android.widget.ListView;

// -------------------------------------------------------------------------
/**
 * This class handles the interaction/data for the Social page
 *
 * @author Brandon Potts
 * @version Mar 10, 2014
 */
public class SocialActivity
extends Activity
{

	private static final String TAG = "SocialActivity";
	private static final String QUERY = "filter:images +exclude:retweets #ratchet";
	private ListView listView;
	private PhotoStreamAdapter adapter;
	private Twitter twitter;
	private View previewHolder;
	private ImageView imageView;
	private QueryResult lastResult;

	// ----------------------------------------------------------
	/**
	 * Sets up the Social page
	 *
	 * @param savedInstanceState
	 *            is data that was most recently supplied
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.social);

		listView = (ListView) findViewById(R.id.listView);
		previewHolder = findViewById(R.id.previewBox);
		imageView = (ImageView) findViewById(R.id.fullImageView);

		ConfigurationBuilder cb = new ConfigurationBuilder();
		cb.setDebugEnabled(true);
		TwitterFactory tf = new TwitterFactory(cb.build());
		twitter = tf.getInstance();

		adapter = new PhotoStreamAdapter(SocialActivity.this, new ArrayList<IPhotoStreamItem>(),
				new OnImageClickListener() {

			@Override
			public void onImageClicked(Bitmap bitmap) {
				imageView.setImageBitmap(bitmap);
				previewHolder.setVisibility(View.VISIBLE);
				imageView.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						previewHolder.setVisibility(View.GONE);
					}
				});
			}
		});
		listView.setAdapter(adapter);

		new TwitterTask().execute();
	}
	
	private void resetList() {
		adapter.clear();
		adapter.notifyDataSetChanged();
		lastResult = null;
	}

	private class TwitterTask extends AsyncTask<Void, Void, List<IPhotoStreamItem>> {

		@Override
		protected List<IPhotoStreamItem> doInBackground(Void... arg0) {
			Query query = null;
			if (lastResult == null) {
				query = new Query(QUERY);
			}
			else {
				query = lastResult.nextQuery();
			}

			List<IPhotoStreamItem> photoStream = new ArrayList<IPhotoStreamItem>();
			try {
				lastResult = twitter.search(query);
			}
			catch (TwitterException e) {
				Log.d(TAG, "Problem searching for tweets.");
				return photoStream;
			}

			if (lastResult != null) {
				List<twitter4j.Status> statuses = lastResult.getTweets();
				if (statuses != null) {
					for (twitter4j.Status status : statuses) {
						photoStream.add(new TwitterPhotoStreamItem(status));
					}
				}
			}

			return photoStream;
		}

		@Override
		protected void onPostExecute(List<IPhotoStreamItem> photoStream) {
			super.onPostExecute(photoStream);

			adapter.addAll(photoStream);
			adapter.notifyDataSetChanged();
		}
	}
}
