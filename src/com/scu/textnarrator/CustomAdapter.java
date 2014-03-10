package com.scu.textnarrator;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.Inflater;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class CustomAdapter extends ArrayAdapter<String>{
	
	private ArrayList<Bitmap> thumbnails;
	private ArrayList<String> title;
	Context context;
	TextView txtname;
	ImageView img;

	public CustomAdapter(Context context, int layout, 
			ArrayList<String> title, ArrayList<Bitmap> thumbnails) {
		super(context, layout, title);
		this.context=context;
		this.title = title;
		this.thumbnails= thumbnails;
		
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		LayoutInflater inflater =  ((Activity) context).getLayoutInflater(); 
		View row =  inflater.inflate(R.layout.activity_customicon, null);
		txtname =  (TextView)row.findViewById(R.id.label);
		img = (ImageView) row.findViewById(R.id.icon);
		txtname.setText(title.get(position));
		Bitmap b = thumbnails.get(position);
		if(b==null)
			img.setImageResource(R.drawable.messagebox_warning);
		else
			img.setImageBitmap(thumbnails.get(position));
		return row;
		
		
	}
	
	
	

}
