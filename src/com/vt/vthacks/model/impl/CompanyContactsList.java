package com.vt.vthacks.model.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import com.vt.vthacks.model.ICompany;
import com.vt.vthacks.model.ICompanyContactsList;

public class CompanyContactsList extends ArrayList<ICompany> implements ICompanyContactsList {

	private static final String COMPANIES = "companies";
	private static final String TAG = "CompanyContactsList";

	/**
	 * For serialization.
	 */
	private static final long serialVersionUID = -5873618902852338862L;
	private static final String CONTACTS_ENDPOINT = "http://vthacks-env-pmkrjpmqpu.elasticbeanstalk.com/get_contacts";


	public CompanyContactsList(JSONObject root) {
		super();
		if (root == null) {
			return;
		}

		
		// Add all the companies, fail if they do not exist.
		JSONArray companies = root.optJSONArray(COMPANIES);
		if (companies == null) {
			return;
		}

		for (int i = 0; i < companies.length(); i++) {
			this.add(new Company(companies.optJSONObject(i)));
		}
	}

	public static ICompanyContactsList fromAssets(Context context, String string) {
		AssetManager assetManager = context.getAssets();
		try {
			return fromInputStream(assetManager.open(string));
		} catch (IOException e) {
		}

		return null;
	}
	
	public static ICompanyContactsList fromServer() {
		try {
			URL url = new URL(CONTACTS_ENDPOINT);
			return fromInputStream(url.openStream());
		} catch (IOException e) {
		}
		
		return null;
	}

	private static ICompanyContactsList fromInputStream(InputStream is) {
		String jsString = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(is)); 
			String line = null;
			while((line = reader.readLine()) != null){
				jsString += line;
			}
			is.close();
			reader.close();
		}
		catch (IOException e) {
		}

		try {
			JSONObject root = new JSONObject(jsString);
			return new CompanyContactsList(root);
		}
		catch (JSONException e) {
			Log.d(TAG, "jse");
		}

		return null;
	}
}
