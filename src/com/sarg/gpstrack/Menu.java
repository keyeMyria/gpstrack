package com.sarg.gpstrack;

import android.app.ListActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;

public class Menu extends ListActivity{

	static final String[] ITEMS = new String[] { "First", "Second", "Third" };

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		
		ArrayAdapter<String> menuItems = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, ITEMS );
		setListAdapter(menuItems);
	}
}