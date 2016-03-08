package org.dieschnittstelle.mobile.android.mediaaccess;

import java.util.ArrayList;
import java.util.List;

import org.dieschnittstelle.mobile.android.dataaccess.model.DataItem;
import org.dieschnittstelle.mobile.android.dataaccess.model.IDataItemCRUDOperations;
import org.dieschnittstelle.mobile.android.mediaaccess.webview.ChromeWebViewItemDetailsActivity;
import org.dieschnittstelle.mobile.android.mediaaccess.webview.WebViewItemDetailsActivity;

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
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Show a list of items
 * 
 * @author Joern Kreutel
 * 
 */
public class ItemListActivity extends Activity {

	// the logger
	protected static final String logger = ItemListActivity.class
			.getSimpleName();

	/**
	 * the constant for the subview request
	 */
	public static final int REQUEST_ITEM_DETAILS = 2;

	/**
	 * the constant for the new item request
	 */
	public static final int REQUEST_ITEM_CREATION = 1;

	/**
	 * the items that will be display
	 */
	private List<DataItem> itemlist;

	/**
	 * the listview that will display the items
	 */
	private ListView listview;

	/**
	 * the adapter that mediates between the itemlist and the view
	 */
	private ArrayAdapter<DataItem> adapter;

	/**
	 * the data accessor for the data items
	 */
	private IDataItemCRUDOperations dataAccessor;

	/**
	 * the details view id as selected by the options menu
	 */
	private int detailsViewId = R.id.option_simpleDetails;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.itemlistview);

		try {
			// access the listview
			/*
			 * access the list view for the options to be displayed
			 */
			listview = (ListView) findViewById(R.id.list);

			// the button for adding new items
			Button newitemButton = (Button) findViewById(R.id.newitemButton);

			// obtain the accessor from the application, and pass it the
			// accesorId we have been passed ourselves by the intent
			dataAccessor = ((MediaAccessRemoteApplication) getApplication())
					.getDataAccessor();			
			Log.i(logger, "will use dataAccessor: " + dataAccessor);

			// initialise the item list
			this.itemlist = new ArrayList<DataItem>();

			// create the adapter
			this.adapter = new ArrayAdapter<DataItem>(this,
					R.layout.item_in_listview, itemlist) {

				// we override getView and manually create the views for each
				// list element
				@Override
				public View getView(int position, View itemView,
						ViewGroup parent) {
					Log.d(logger, "getView() has been invoked for item: "
							+ itemlist.get(position));
					ViewGroup listitemView = (ViewGroup) getLayoutInflater()
							.inflate(R.layout.item_in_listview, null);
					TextView itemnameView = (TextView) listitemView
							.findViewById(R.id.itemName);
					itemnameView.setText(itemlist.get(position).getName());

					// obtain the image view and the description attribute which
					// contains the image id
					final ImageView imgView = (ImageView) listitemView
							.findViewById(R.id.itemIcon);

					final String description = itemlist.get(position)
							.getDescription();

					Log.d(logger, "description is: " + description);

					if (description != null && !"".equals(description)) {

						// we use an async task for loading the image
						new AsyncTask<Void, Void, Object>() {

							/*
							 * the "background process": load the image from the
							 * url
							 */
							@Override
							protected Object doInBackground(Void... arg) {
								try {
									final Drawable imgcontent = new BitmapDrawable(
											BitmapFactory
													.decodeStream(((MediaAccessRemoteApplication) getApplication())
															.readMediaResource(description
																	+ ".png")));
									Log.d(logger,
											"got content: " + imgcontent
													+ " of class " + imgcontent != null ? imgcontent
													.getClass().getName()
													: "<null>");

									return imgcontent;
								} catch (Exception e) {
									return e;
								}
							}

							/**
							 * once the process has been terminated: update the
							 * background or display an error message
							 */
							@Override
							protected void onPostExecute(Object result) {
								if (result instanceof Drawable) {
									imgView.setImageDrawable((Drawable) result);
								} else {
									Toast.makeText(ItemListActivity.this,
											((Exception) result).getMessage(),
											Toast.LENGTH_LONG).show();
									((Exception) result).printStackTrace();
								}
							}

						}.execute();
					}

					return listitemView;
				}
			};
			// the adapter is set to display changes immediately
			this.adapter.setNotifyOnChange(true);

			// set the adapter on the list view
			listview.setAdapter(this.adapter);

			// set a listener that reacts to the selection of an element
			listview.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> adapterView,
						View itemView, int itemPosition, long itemId) {

					Log.i(logger, "onItemClick: position is: " + itemPosition
							+ ", id is: " + itemId);

					DataItem item = itemlist.get(itemPosition);

					processItemSelection(item);
				}

			});

			// set the listview as scrollable
			listview.setScrollBarStyle(ListView.SCROLLBARS_INSIDE_OVERLAY);

			// set a listener for the newItemButton
			newitemButton.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					processNewItemRequest();
				}

			});

			// finally, we add the itemlist asynchronously
			new AsyncTask<Void, Void, List<DataItem>>() {
				@Override
				protected List<DataItem> doInBackground(Void... items) {
					return ItemListActivity.this.dataAccessor
							.readAllDataItems();
				}

				@Override
				protected void onPostExecute(List<DataItem> items) {
					itemlist.addAll(items);
					adapter.notifyDataSetChanged();
				}
			}.execute();

		} catch (Exception e) {
			String err = "got exception: " + e;
			Log.e(logger, err, e);
			Toast.makeText(this, err, Toast.LENGTH_LONG).show();
		}

	}

	protected void processNewItemRequest() {
		Log.i(logger, "processNewItemRequest()");
		// create an intent for opening the details view
		Class<?> detailsViewClass = null;
		switch (detailsViewId) {
		case R.id.option_chromeDetails:
			detailsViewClass = ChromeWebViewItemDetailsActivity.class;
			break;
		case R.id.option_webviewDetails:
			detailsViewClass = WebViewItemDetailsActivity.class;
			break;
		default:
			detailsViewClass = ItemDetailsActivity.class;
		}
		Intent intent = new Intent(ItemListActivity.this, detailsViewClass);

		// start the details activity with the intent
		startActivityForResult(intent, REQUEST_ITEM_CREATION);
	}

	protected void processItemSelection(DataItem item) {
		Log.i(logger, "processItemSelection(): " + item);
		// create an intent for opening the details view (duplicated from above)
		Class<?> detailsViewClass = null;
		switch (detailsViewId) {
		case R.id.option_chromeDetails:
			detailsViewClass = ChromeWebViewItemDetailsActivity.class;
			break;
		case R.id.option_webviewDetails:
			detailsViewClass = WebViewItemDetailsActivity.class;
			break;
		default:
			detailsViewClass = ItemDetailsActivity.class;
		}

		Intent intent = new Intent(ItemListActivity.this, detailsViewClass);
		// pass the item to the intent
		intent.putExtra(ItemDetailsActivity.ARG_ITEM_OBJECT, item);

		// start the details activity with the intent
		startActivityForResult(intent, REQUEST_ITEM_DETAILS);
	}

	/**
	 * process the result of the item details subactivity, which may be the
	 * creation, modification or deletion of an item.
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		Log.i(logger, "onActivityResult(): " + data);

		DataItem item = data != null ? (DataItem) data
				.getSerializableExtra(ItemDetailsActivity.ARG_ITEM_OBJECT)
				: null;

		// check which request we had
		if (requestCode == REQUEST_ITEM_CREATION
				&& resultCode == ItemDetailsActivity.RESPONSE_ITEM_UPDATED) {
			Log.i(logger, "onActivityResult(): adding the created item");

			/**
			 * all accessor calls are executed asynchronously
			 */
			new AsyncTask<DataItem, Void, DataItem>() {
				@Override
				protected DataItem doInBackground(DataItem... items) {
					return ItemListActivity.this.dataAccessor
							.createDataItem(items[0]);
				}

				@Override
				protected void onPostExecute(DataItem item) {
					if (item != null) {
						adapter.add(item);
					}
				}
			}.execute(item);

		} else if (requestCode == REQUEST_ITEM_DETAILS) {
			if (resultCode == ItemDetailsActivity.RESPONSE_ITEM_UPDATED) {
				Log.i(logger, "onActivityResult(): updating the edited item");

				new AsyncTask<DataItem, Void, DataItem>() {
					@Override
					protected DataItem doInBackground(DataItem... items) {
						return ItemListActivity.this.dataAccessor
								.updateDataItem(items[0]);
					}

					@Override
					protected void onPostExecute(DataItem item) {
						if (item != null) {
							// read out the item from the list and update it
							itemlist.get(itemlist.indexOf(item)).updateFrom(
									item);
							// notify the adapter that the item has been changed
							adapter.notifyDataSetChanged();
						}
					}
				}.execute(item);

			} else if (resultCode == ItemDetailsActivity.RESPONSE_ITEM_DELETED) {

				new AsyncTask<DataItem, Void, DataItem>() {
					@Override
					protected DataItem doInBackground(DataItem... items) {
						if (ItemListActivity.this.dataAccessor
								.deleteDataItem(items[0].getId())) {
							return items[0];
						} else {
							Log.e(logger, "the item" + items[0]
									+ " could not be deleted by the accessor!");
							return null;
						}
					}

					@Override
					protected void onPostExecute(DataItem item) {
						if (item != null) {
							adapter.remove(item);
						}
					}
				}.execute(item);
			}
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		Log.i(logger, "onCreateOptionsMenu()");
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.itemlistview, menu);

		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		Log.i(logger, "onPrepareOptionsMenu()");

		// we always deactivate the option that is currently active and set the
		// others as active
		for (int itemId : new int[] { R.id.option_chromeDetails,
				R.id.option_simpleDetails, R.id.option_webviewDetails }) {
			MenuItem item = menu.findItem(itemId);
			if (item != null) {
				item.setEnabled(itemId == this.detailsViewId ? false : true);
			}
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(logger, "onOptionsItemSelected(): " + item);
		this.detailsViewId = item.getItemId();

		return true;
	}

}
