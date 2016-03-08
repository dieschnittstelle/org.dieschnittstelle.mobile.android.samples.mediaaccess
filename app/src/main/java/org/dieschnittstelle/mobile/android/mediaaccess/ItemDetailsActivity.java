package org.dieschnittstelle.mobile.android.mediaaccess;

import org.dieschnittstelle.mobile.android.dataaccess.model.DataItem;

import android.app.Activity;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

/**
 * Show the details of an item
 * 
 * this activity exemplifies the case where the activity itself implements the
 * OnClickListener interface
 * 
 * @author Joern Kreutel
 * 
 */
public class ItemDetailsActivity extends Activity {

	/**
	 * the logger
	 */
	protected static final String logger = ItemDetailsActivity.class
			.getSimpleName();

	/**
	 * the argname with which we will pass the item to the subview
	 */
	public static final String ARG_ITEM_OBJECT = "itemObject";

	/**
	 * the result code that indicates that some item was changed
	 */
	public static final int RESPONSE_ITEM_UPDATED = 1;

	/**
	 * the result code that indicates that the item shall be deleted
	 */
	public static final int RESPONSE_ITEM_DELETED = 2;

	/**
	 * the result code that indicates that nothing has been changed
	 */
	public static final int RESPONSE_NOCHANGE = -1;

	/**
	 * the item we are dealing with (which might be created by ourselves in case
	 * the user has chosen the create item action
	 */
	private DataItem item;

	/**
	 * the ui fields the hold the name and the description of the item
	 */
	private EditText itemName;
	private EditText itemDescription;

	/**
	 * the actions for save and delete
	 */
	private MenuItem saveAction;
	private MenuItem deleteAction;

	/**
	 * the spinner for selecting the iconpath
	 */
	protected Spinner iconpathSpinner;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.itemview);

		try {
			// obtain the ui elements
			this.itemName = (EditText) findViewById(R.id.item_name);
			this.itemDescription = (EditText) findViewById(R.id.item_description);
			this.iconpathSpinner = (Spinner) findViewById(R.id.item_iconname);

			// check whether we have been passed an item
			this.item = (DataItem) getIntent().getSerializableExtra(
					ARG_ITEM_OBJECT);
			Log.i(logger, "obtained item from intent: " + this.item);

			// if we do not have an item, we assume we need to create a new one
			if (this.item != null) {
				// set name and description
				itemName.setText(item.getName());
				itemDescription.setText(item.getDescription());
				if (item.getDescription() != null
						&& !"".equals(item.getDescription())) {
					iconpathSpinner
							.setSelection(((MediaAccessRemoteApplication) getApplication())
									.getIconIdIndex(item.getDescription()));
				}
			} else {
				this.item = new DataItem(-1, "New Item", "");
			}

			// set a listener on the spinner
			iconpathSpinner
					.setOnItemSelectedListener(new OnItemSelectedListener() {

						@Override
						public void onItemSelected(AdapterView<?> arg0,
								View arg1, int arg2, long arg3) {
							Log.d(logger, "got a selection: " + arg2);
							String selectedItem = (String) arg0
									.getSelectedItem();

							String imgpath = ((MediaAccessRemoteApplication) getApplication())
									.getIconId4Iconname(selectedItem);

							updateBackgroundImage(imgpath);
							itemDescription.setText(imgpath);
						}

						@Override
						public void onNothingSelected(AdapterView<?> arg0) {
							// TODO Auto-generated method stub

						}
					});

		} catch (Exception e) {
			String err = "got exception: " + e;
			Log.e(logger, err, e);
			Toast.makeText(this, err, Toast.LENGTH_LONG).show();
		}

	}

	/**
	 * save the item and finish
	 */
	protected void processItemSave() {
		// re-set the fields of the item
		this.item.setName(this.itemName.getText().toString());
		this.item
				.setDescription(((MediaAccessRemoteApplication) getApplication())
						.getIconId4Iconname(String.valueOf(iconpathSpinner
								.getSelectedItem())));

		// and return to the calling activity
		Intent returnIntent = new Intent();

		// set the item on the intent
		returnIntent.putExtra(ARG_ITEM_OBJECT, this.item);

		// set the result code
		setResult(RESPONSE_ITEM_UPDATED, returnIntent);

		// and finish
		finish();
	}

	/**
	 * delete the item and finish
	 */
	protected void processItemDelete() {
		// and return to the calling activity
		Intent returnIntent = new Intent();

		// set the item
		returnIntent.putExtra(ARG_ITEM_OBJECT, this.item);

		// set the result code
		setResult(RESPONSE_ITEM_DELETED, returnIntent);

		// and finish
		finish();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(logger, "onCreateOptionsMenu()");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.itemview, menu);

		this.deleteAction = menu.findItem(R.id.action_deleteItem);
		this.saveAction = menu.findItem(R.id.action_saveItem);
		
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (item == this.saveAction) {
			Log.i(logger, "got onClick() on saveButton");
			processItemSave();
		} else if (item == this.deleteAction) {
			Log.i(logger, "got onClick() ond deleteButton");
			processItemDelete();
		} else {
			Log.w(logger,
					"got onOptionsItemSelected() which will not be handled: "
							+ item);
		}
		
		return true;
	}

	/**
	 * update the background image
	 */
	private void updateBackgroundImage(final String resourcepath) {

		Log.d(logger, "updating background image using resource: "
				+ resourcepath);

		// we use an async task for loading the image
		new AsyncTask<Void, Void, Object>() {

			/*
			 * the "background process": load the image from the url
			 */
			@Override
			protected Object doInBackground(Void... arg) {
				try {
					final Drawable imgcontent = new BitmapDrawable(
							BitmapFactory
									.decodeStream(((MediaAccessRemoteApplication) getApplication())
											.readMediaResource(resourcepath
													+ ".png")));
					Log.d(logger, "got content: " + imgcontent + " of class "
							+ imgcontent != null ? imgcontent.getClass()
							.getName() : "<null>");

					return imgcontent;
				} catch (Exception e) {
					return e;
				}
			}

			/**
			 * once the process has been terminated: update the background or
			 * display an error message
			 */
			@Override
			protected void onPostExecute(Object result) {
				if (result instanceof Drawable) {
//					((ViewGroup) findViewById(R.id.item_description).getParent())
//							.setBackgroundDrawable((Drawable) result);
					((View) findViewById(R.id.item_description)/*.getParent()*/)
					.setBackgroundDrawable((Drawable) result);
				} else {
					Toast.makeText(ItemDetailsActivity.this,
							((Exception) result).getMessage(),
							Toast.LENGTH_LONG).show();
					((Exception) result).printStackTrace();
				}
			}

		}.execute();
	}

}
