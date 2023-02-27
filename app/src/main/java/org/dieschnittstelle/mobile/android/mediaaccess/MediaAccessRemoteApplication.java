package org.dieschnittstelle.mobile.android.mediaaccess;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;

import org.dieschnittstelle.mobile.android.dataaccess.model.IDataItemCRUDOperations;
import org.jboss.resteasy.client.ProxyFactory;
import org.jboss.resteasy.client.core.executors.ApacheHttpClient4Executor;

import android.app.Application;
import android.util.Log;

/**
 * this application contains a couple of methods that are used by more than one activity
 * 
 * @author Joern Kreutel
 */
public class MediaAccessRemoteApplication extends Application {

	protected static String logger = MediaAccessRemoteApplication.class
			.getSimpleName();

	public static final String WEBAPP_BASEURL_LOCALHOST_FROM_ANDROIDSTUDIO_EMULATOR = "http://10.0.2.2:8080/";
	public static final String WEBAPP_BASEURL_LOCALHOST_FROM_GENYMOTION_EMULATOR = "http://10.0.3.2:8080/";
	// TODO: change this url using the ip address of the machine on which the web application is started
	public static final String WEBAPP_BASEURL_IN_LOCAL_NETWORK = "http://192.168.2.101:8080/";

	/**
	 * the baseUrl - TODO: assign the required value for your local development setup
	 */
	private String baseUrl = WEBAPP_BASEURL_LOCALHOST_FROM_ANDROIDSTUDIO_EMULATOR;

	/**
	 * the path where the media content is provided by the webapp
	 */
	public static final String MEDIA_CONTENT_PATH = "mediaaccessContent/";

	/**
	 * the accessors that implement the different alternatives for accessing the
	 * item list
	 */
	private IDataItemCRUDOperations dataAccessor;

	public MediaAccessRemoteApplication() {
		Log.i(logger, "<constructor>()");

		this.dataAccessor = ProxyFactory.create(IDataItemCRUDOperations.class,
				baseUrl + "api/",
				new ApacheHttpClient4Executor());

		Log.i(logger, "<constructor>(): created accessors.");
	}

	/**
	 * get the data accessor
	 */
	public IDataItemCRUDOperations getDataAccessor() {
		return this.dataAccessor;
	}

	/**
	 * get the baseUrl of the webapp in order for activities to load media
	 * resources from there
	 * 
	 * @return
	 */
	public String getWebappBaseUrl() {
		return this.baseUrl;
	}

	/**
	 * read a media resource
	 * 
	 * @param url
	 * @return
	 * @throws IOException
	 */
	public InputStream readMediaResource(String url) throws IOException {
		Log.i(logger, "readMediaResource(): " + url);

		// trim the string
		url = url.trim();

		// create the full url
		URL fullUrl = new URL(this.baseUrl + MEDIA_CONTENT_PATH
				+ (url.startsWith("/") ? url.substring(1) : url));

		// access the url
		return fullUrl.openStream();
	}

	
	/**
	 * 
	 * @param iconId
	 * @return
	 */
	protected int getIconIdIndex(String iconId) {
		return Arrays.asList(
				getResources()
						.getStringArray(R.array.background_resources_list))
				.indexOf(iconId);
	}

	/**
	 * 
	 * @param name
	 * @return
	 */
	protected String getIconId4Iconname(String name) {
		return getResources().getStringArray(R.array.background_resources_list)[Arrays
				.asList(getResources().getStringArray(R.array.background_list))
				.indexOf(name)];
	}

}
