package com.mfrancik.apps.dsprojekt;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Environment;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.util.HashMap;

public class Photos extends AppCompatActivity {
	public LinearLayout albumsList;
	public ImageView addButton;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_photos);
		albumsList = findViewById(R.id.photoListLayout);

		HashMap<String, File> fileMap = new HashMap<String, File>();

		Bundle bundle = getIntent().getExtras();

		File directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		for (File file : directory.listFiles()){
			if (file.getName().equals("MateuszFrancik")) {
				directory = file;
			}
		}
		for (File file : directory.listFiles()){
			if (file.getName().equals(bundle.getString("owoc"))) {
				directory = file;
			}
		}

		if (!directory.getName().equals(bundle.getString("dir"))) {

		} else {
			for (File file : directory.listFiles()) {
				ImageView img = new ImageView(this);
				Bitmap bmp = betterImageDecode(file.getPath());
				img.setImageBitmap(bmp);
				img.setScaleType(ImageView.ScaleType.CENTER_CROP);
				albumsList.addView(img);
			}
		}
	}

	private Bitmap betterImageDecode(String filePath) {
		Bitmap myBitmap;
		BitmapFactory.Options options = new BitmapFactory.Options();    //opcje przekształcania bitmapy
		options.inSampleSize = 4; // zmniejszenie jakości bitmapy 4x
		//
		myBitmap = BitmapFactory.decodeFile(filePath, options);
		return myBitmap;
	}
}