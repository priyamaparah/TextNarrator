package com.scu.textnarrator;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Scanner;
import java.util.regex.Pattern;

import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.PdfReader;
import com.itextpdf.text.pdf.parser.FilteredTextRenderListener;
import com.itextpdf.text.pdf.parser.LocationTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.PdfReaderContentParser;
import com.itextpdf.text.pdf.parser.PdfTextExtractor;
import com.itextpdf.text.pdf.parser.RegionTextRenderFilter;
import com.itextpdf.text.pdf.parser.RenderFilter;
import com.itextpdf.text.pdf.parser.SimpleTextExtractionStrategy;
import com.itextpdf.text.pdf.parser.TextExtractionStrategy;

import android.os.Bundle;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.speech.tts.UtteranceProgressListener;
import android.support.v4.view.GestureDetectorCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.TextView;

public class PdfTextActivity extends Activity implements OnInitListener {

	TextView txtdisplay;
	TextView txtpage;
	private boolean isStarted;
	// private GestureDetector mDetector;
	private final String TAG = "TAG";
	private OnGestureSwipeListener gesturelistner;
	static int pageNo = 0;
	String path;
	int pageCount = 0;
	PdfReader reader;
	private TextToSpeech tts = null;
	StringBuilder sb = null;
	Scanner sc2 = null;
	String tempStr;
	HashMap<String, String> map;
	int length;
	int start = 0;
	Spannable str;
	String orignalStr;

	// BackgroundColorSpan bgcolor = new BackgroundColorSpan(Color.CYAN);
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pdf_text);

		txtdisplay = (TextView) findViewById(R.id.txtDisplay);
		txtpage = (TextView) findViewById(R.id.txtpage);
		txtpage.setTypeface(null, Typeface.BOLD);
		// To handle gesture events
		gesturelistner = new OnGestureSwipeListener(this);
		txtdisplay.setOnTouchListener(gesturelistner);
		Intent intent = getIntent();
		long id = intent.getLongExtra("Id", -1);
		path = intent.getStringExtra("Path");
		File f = new File(path);
		if (f.length() == 0) {
			finish();
		} else {
			if (id != -1) {
				try {
					reader = new PdfReader(path);
					pageNo = 1;
					pageCount = reader.getNumberOfPages();
					// display the first page
					displayText(path, pageNo);

				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	private void displayText(String path, int pageNo) throws IOException {
		// TODO Auto-generated method stub

		Log.d("TAG", path + "  ");
		Log.d(TAG, String.valueOf(pageNo));
		if (pageNo >= 1 && pageNo <= reader.getNumberOfPages()) {
			// Reference: http://api.itextpdf.com/itext/
			isStarted = false;
			if (tts != null) {
				tts.stop();
				tts.shutdown();
			}
			// Set the toggle button to play
			invalidateOptionsMenu();
			start = 0;

			PdfReaderContentParser parser = new PdfReaderContentParser(reader);

			TextExtractionStrategy strategy;
			sb = new StringBuilder();
			// Reference : http://api.itextpdf.com/itext/
			strategy = parser.processContent(pageNo,
					new SimpleTextExtractionStrategy());
			String strText = strategy.getResultantText();
			sb.append(strText);
			// Log.d("TAG", sb.toString());
			txtdisplay.setText(sb.toString());
			length = txtdisplay.getText().toString().length();
			removeSpan(start, length);
			Log.d("TAG", txtdisplay.getText().toString());
			txtpage.setText("Page " + pageNo + " of " + pageCount);
		}
	}

	private void removeSpan(int startSelection, int endSelection) {
		// Set the string to default from index startselection to index end
		// Selection
		str = new SpannableString(txtdisplay.getText());
		str.setSpan(new BackgroundColorSpan(Color.TRANSPARENT), start, length,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		runOnUiThread(new Runnable() {
			@Override
			public void run() {

				txtdisplay.setText(str);

			}
		});
		;
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		super.dispatchTouchEvent(ev);
		return gesturelistner.onTouch(null, ev);
	}

	//OnGestureListner class
	class OnGestureSwipeListener implements OnTouchListener {

		private final GestureDetector gestureDetector;

		public OnGestureSwipeListener(Context ctx) {
			gestureDetector = new GestureDetector(ctx, new GestureListener());
		}

		private final class GestureListener extends SimpleOnGestureListener {

			private static final int SWIPE_THRESHOLD = 100;
			private static final int SWIPE_VELOCITY_THRESHOLD = 100;

			@Override
			public boolean onDown(MotionEvent e) {
				// Log.d("DEBUG_TAG", "OnDown");
				return true;
			}

			//Called when user flings on screen
			@Override
			public boolean onFling(MotionEvent e1, MotionEvent e2,
					float velocityX, float velocityY) {
				Log.d("DEBUG_TAG", "OnFling");
				boolean result = false;
				try {
					float diffY = e2.getY() - e1.getY();
					float diffX = e2.getX() - e1.getX();
					if (Math.abs(diffX) > Math.abs(diffY)) {
						if (Math.abs(diffX) > SWIPE_THRESHOLD
								&& Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
							if (diffX > 0) {
								// Display the prev page
								onSwipeRight();

							} else {
								// Display the next page
								onSwipeLeft();
							}
						}
					}
					// } else {
					// if (Math.abs(diffY) > SWIPE_THRESHOLD &&
					// Math.abs(velocityY)
					// > SWIPE_VELOCITY_THRESHOLD) {
					// if (diffY > 0) {
					// onSwipeBottom();
					// } else {
					// onSwipeTop();
					// }
					// }
					// }
				} catch (Exception exception) {
					exception.printStackTrace();
				}
				return result;
			}
		}

		public void onSwipeRight() throws Exception {
			Log.d("DEBUG_TAG", "OnSwipeRight");
			if (pageNo >= 1 && pageNo <= reader.getNumberOfPages()) {
				if (pageNo > 1) {
					pageNo--;
					displayText(path, pageNo);
				}

			}

		}

		public void onSwipeLeft() throws Exception {
			Log.d("DEBUG_TAG", "OnSwipeLefffftttt");
			if (pageNo >= 1 && pageNo <= reader.getNumberOfPages()) {
				if (pageNo < reader.getNumberOfPages()) {
					pageNo++;
					displayText(path, pageNo);
				}

			}
		}

		@Override
		public boolean onTouch(View arg0, MotionEvent event) {
			// TODO Auto-generated method stub
			return gestureDetector.onTouchEvent(event);
		}
	}

//	private void speak() {
//		this.tts = new TextToSpeech(this, new OnInitListener() {
//			@Override
//			public void onInit(int status) {
//				tts.setLanguage(Locale.US);
//				// tts.setSpeechRate(0.5f);
//				HashMap<String, String> map = new HashMap<String, String>();
//				map.put(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS,
//						"true");
//				// map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "ID");
//				if (sb != null) {
//
//					String result = sb.toString();
//
//					// Log.d(TAG, result);
//
//					sc2 = new Scanner(result);
//					while (sc2.hasNextLine()) {
//						tempStr = sc2.nextLine();
//						tempStr.replaceAll("[^.,:\\w\\s]", "");
//						Log.d(TAG, tempStr);
//						tts.speak(tempStr, TextToSpeech.QUEUE_ADD, map);
//					}
//
//					sc2.close();
//
//				}
//			}
//		});
//	}
	// Method called to play the text
	private void speak() {
		removeSpan(start, length);
		start = 0;
		//Get the length of text
		length = txtdisplay.getText().toString().length();
		//Remove special characters from string
		final String expression = "[^.,:+\\-\\w\\s]";
		if (sb != null) {

			String result = sb.toString();

			// Log.d(TAG, result);
			
			sc2 = new Scanner(result);
			if (sc2.hasNextLine()) {
				//Get the next line
				tempStr = sc2.nextLine();
				orignalStr = tempStr;
				//remove special characters
				tempStr = tempStr.replaceAll(expression, "");
				map = new HashMap<String, String>();
				map.put(TextToSpeech.Engine.KEY_FEATURE_NETWORK_SYNTHESIS,
						"true");
				//Set Utterance ID for OnUtteranceProgressListner
				map.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID,
						"SOME MESSAGE");
				tts = new TextToSpeech(PdfTextActivity.this,
						(OnInitListener) this);

				tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {

					@Override
					public void onStart(String utteranceId) {
						// TODO Auto-gene)rated method stub
						Log.d("TAG", "onStart");
					}

					@Override
					public void onError(String utteranceId) {
						// TODO Auto-generated method stub
						Log.d("TAG", "onError");

					}

					//Called when tts is done
					@Override
					public void onDone(String utteranceId) {
						// TODO Auto-generated method stub
						Log.d("TAG", "OnDone");
						removeSpan(start, length);

						if (sc2.hasNextLine()) {
							//play the next line
							tempStr = sc2.nextLine();
							orignalStr = tempStr;
							tempStr = tempStr.replaceAll(expression, "");
							start = length + 1;
							length = orignalStr.length() + start;
							Log.d("TAG", String.valueOf(orignalStr.length()));
							Log.d("Start", String.valueOf(start));
							Log.d("Length", String.valueOf(length));
							//highlight the line
							str.setSpan(new BackgroundColorSpan(Color.CYAN),
									start, length,
									Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
							runOnUiThread(new Runnable() {
								@Override
								public void run() {

									txtdisplay.setText(str);

								}
							});
							// txtdisplay.setText(str);
							Log.d("TAG", tempStr);
							tts.speak(tempStr, TextToSpeech.QUEUE_FLUSH, map);
						} else {
							isStarted = false;
							invalidateOptionsMenu();
							if (sc2 != null) {
								sc2.close();
							}
						}
					}

				});

			} else {
				isStarted = false;
				//Toggle the play button and set it to default at end of text
				invalidateOptionsMenu();
				if (sc2 != null) {
					sc2.close();
				}
			}
		}
	}
	
	
	//One time call when text to speech engine is started
	@Override
	public void onInit(int status) {
		tts.setLanguage(Locale.US);
		// tts.setSpeechRate(0.5f);
		Log.d("TAG", "onInit");
		Log.d("TAG", tempStr);
		length = orignalStr.length();

		Log.d("onInitStart", String.valueOf(start));
		Log.d("onInitStart", String.valueOf(length));
		str.setSpan(new BackgroundColorSpan(Color.CYAN), start, length,
				Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		txtdisplay.setText(str);
		tts.speak(tempStr, TextToSpeech.QUEUE_FLUSH, map);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		menu.removeItem(R.id.action_stop);
		getMenuInflater().inflate(R.menu.pdf_text, menu);

		return true;
	}

	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case R.id.action_play:
			isStarted = true;
			invalidateOptionsMenu();
			speak();
			return true;
		case R.id.action_stop:
			isStarted = false;
			//Calls onPrepareOptionsMenu
			invalidateOptionsMenu();
			if (tts != null) {
				tts.stop();
				tts.shutdown();
			}
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	//Called to update the menu by method invalidateOptionsMenu
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// TODO Auto-generated method stub
		// return super.onPrepareOptionsMenu(menu);
		menu.findItem(R.id.action_play).setVisible(!isStarted);
		menu.findItem(R.id.action_stop).setVisible(isStarted);
		return true;

	}

	@Override
	protected void onStop() {
		// TODO Auto-generated method stub
		super.onStop();
		if (tts != null) {
			this.tts.stop();
			this.tts.shutdown();
		}
		if (sc2 != null)
			sc2.close();

	}

}
