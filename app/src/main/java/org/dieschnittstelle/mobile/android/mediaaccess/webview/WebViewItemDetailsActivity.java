package org.dieschnittstelle.mobile.android.mediaaccess.webview;

import org.dieschnittstelle.mobile.android.mediaaccess.ItemDetailsActivity;
import org.dieschnittstelle.mobile.android.mediaaccess.MediaAccessRemoteApplication;
import org.dieschnittstelle.mobile.android.mediaaccess.R;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

/**
 * Show the details of an item
 * 
 * @author Joern Kreutel
 * 
 */
public class WebViewItemDetailsActivity extends ItemDetailsActivity {

	/**
	 * the webview
	 */
	private WebView webview;

	/**
	 * the clear action for clearing the text on the webview
	 */
	private MenuItem clearAction;

	/**
	 * Called when the activity is first created.
	 * 
	 * @throws ResourceException
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// get the web view
		this.webview = (WebView) findViewById(R.id.item_webview);
		webview.getSettings().setJavaScriptEnabled(true);
		webview.getSettings().setBlockNetworkLoads(false);

		// add JavaScriptExtensions
		webview.addJavascriptInterface(new MyJavaScriptExtensions(),
				"androidjs");

		// load the uri
		try {
			webview.loadUrl(((MediaAccessRemoteApplication) getApplication())
					.getWebappBaseUrl()
					+ MediaAccessRemoteApplication.MEDIA_CONTENT_PATH
					+ "formattedtext.html");
		} catch (Exception e) {
			String err = "got exception: " + e;
			Log.e(getClass().getName(), err, e);
			Toast.makeText(this, err, Toast.LENGTH_LONG).show();
		}
	}

	@Override
	/**
	 * we ignore the content view that is actually supposed to be set by a superclass and use our own one
	 */
	public void setContentView(int id) {
		super.setContentView(R.layout.itemwebview);
	}

	/**
	 * this class is passed to the webview for allowing callbacks from
	 * javascript to this activity
	 * 
	 * @author joern
	 * 
	 */
	public class MyJavaScriptExtensions {

		public static final int TOAST_LONG = Toast.LENGTH_LONG;
		public static final int TOAST_SHORT = Toast.LENGTH_SHORT;

		public void toast(String message, int length) {
			Log.i(getClass().getName(), "toast has been requested: " + message
					+ ", with length: " + length);
			Toast.makeText(WebViewItemDetailsActivity.this, message, length)
					.show();
		}

	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(logger, "onCreateOptionsMenu()");
		// let the menu be inflated by the super class
		super.onCreateOptionsMenu(menu);
		// and add our own items
		getMenuInflater().inflate(R.menu.itemwebview, menu);
		
		this.clearAction = menu.findItem(R.id.action_clearText);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == this.clearAction) {
			Log.i(logger, "got selection on clearButton");
			// this shows how to call a javascript method from a native
			// application component
			this.webview.loadUrl("javascript:clear()");
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}

}
