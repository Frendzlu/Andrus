package com.mfrancik.apps.dsprojekt;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.strictmode.WebViewMethodCalledOnWrongThreadViolation;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {
	private LinearLayout cameraButton;
	private LinearLayout albumsButton;
	private LinearLayout collageButton;
	private LinearLayout networkingButton;
	private HashMap<Integer, Consumer<Integer>> afterPermissionGranted = new HashMap<Integer, Consumer<Integer>>(){};
	private String filePath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.d("DIS-TAG", "onCreate: aaaaaaa");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		cameraButton = findViewById(R.id.cameraButton);
		albumsButton = findViewById(R.id.albumsButton);
		collageButton = findViewById(R.id.collageButton);
		networkingButton = findViewById(R.id.networkingButton);
		File pic = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES );

		File x = new File(pic, "MateuszFrancik");
		x.mkdir();
		this.filePath = x.getPath();
		cameraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				AlertDialog.Builder alert = new AlertDialog.Builder(MainActivity.this);
				alert.setTitle("Photo source");
				alert.setItems(new String[]{"Camera", "Gallery"}, new DialogInterface.OnClickListener() {

					@RequiresApi(api = Build.VERSION_CODES.N)
					public void onClick(DialogInterface dialog, int which) {
						switch (which) {
							case 0:
								try {
									intentWithPermission(Manifest.permission.CAMERA, 101, (a) -> {
										Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
										if (intent.resolveActivity(getPackageManager()) != null) {
											startActivityForResult(intent, 200);
										}
									});
								} catch (InvocationTargetException | IllegalAccessException e) {
									e.printStackTrace();
								}
								break;
							case 1:
								if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 100)) {
									intent[0] = new Intent(Intent.ACTION_PICK);
									intent[0].setType("image/*");
									startActivityForResult(intent[0], 201);
								}
								break;
						}
					}
				});
				alert.show();
			}
		});

		albumsButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 100)) {
					Intent intent = new Intent(MainActivity.this, Albums.class);
					startActivity(intent);
				}
			}
		});

		albumsButton.setOnClickListener( {
			@Override
			public void onClick(View v) {
				if (checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, 100)) {
					Intent intent = new Intent(MainActivity.this, Albums.class);
					startActivity(intent);
				}
			}
		});

		collageButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, Collage.class);
				startActivity(intent);
			}
		});

		networkingButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(MainActivity.this, Network.class);
				startActivity(intent);
			}
		});
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	public void intentWithPermission(String permission, int requestCode, Consumer<Integer> function) throws InvocationTargetException, IllegalAccessException {
		afterPermissionGranted.put(requestCode, function);
		if (ContextCompat.checkSelfPermission(MainActivity.this, permission) == PackageManager.PERMISSION_DENIED) {
			ActivityCompat.requestPermissions(MainActivity.this, new String[]{permission}, requestCode);
		} else {
			Toast.makeText(MainActivity.this, String.format("Permission '%s' already granted", permission), Toast.LENGTH_SHORT).show();
			afterPermissionGranted.get(requestCode).accept(0);
		}
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
			switch (requestCode) {
				case 100:
					File pic = Environment.getExternalStoragePublicDirectory( Environment.DIRECTORY_PICTURES );

					File x = new File(pic, "MateuszFrancik");
					x.mkdir();
					this.filePath = x.getPath();
					new File(x, "people").mkdir();
					new File(x, "places").mkdir();
					new File(x, "things").mkdir();

					afterPermissionGranted.get(requestCode).accept(0);
					break;
				case 101:
					afterPermissionGranted.get(requestCode).accept(0);
					break;
			}
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case 200:
				if (resultCode == RESULT_OK) {
					ByteArrayOutputStream stream = new ByteArrayOutputStream();
					Bundle extras = data.getExtras();
					Bitmap bitmappe = (Bitmap) extras.get("data");
					bitmappe.compress(Bitmap.CompressFormat.JPEG, 100, stream);
					byte[] byteArray = stream.toByteArray();

					FileOutputStream fs = null;
					try {
						SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
						String d = df.format(new Date());
						Log.d("DIS-TAG", this.filePath);
						fs = new FileOutputStream(this.filePath + "/" + "people/" + d + ".jpg");
						fs.write(byteArray);
						fs.close();
					} catch (IOException e){
						e.printStackTrace();
					}
				}
				break;
			case 201:
				if  (resultCode == RESULT_OK) {
					Uri imgData = data.getData();
					ByteArrayOutputStream streamme = new ByteArrayOutputStream();
					InputStream stream = null;
					try {
						stream = getContentResolver().openInputStream(imgData);
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					Log.d("DIS-TAG", "Got here 1");
					Bitmap b = BitmapFactory.decodeStream(stream);
					b.compress(Bitmap.CompressFormat.JPEG, 100, streamme); // kompresja, typ pliku jpg, png
					byte[] byteArray = streamme.toByteArray();
					Log.d("DIS-TAG", "Got here 2");
					FileOutputStream fs = null;
					try {
						SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd_HHmmss");
						String d = df.format(new Date());
						String x = this.filePath + "/" + "people/" + d + ".jpg";
						Log.d("DIS-TAG", "savng file at: " + x);
						Log.d("DIS-TAG", "Got here 3");
						fs = new FileOutputStream(x);
						fs.write(byteArray);
						fs.close();
					} catch (IOException e){
						e.printStackTrace();
					}
				}
		}
	}
}