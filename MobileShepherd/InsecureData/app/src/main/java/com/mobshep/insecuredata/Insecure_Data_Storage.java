package com.mobshep.insecuredata;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * This file is part of the Security Shepherd Project.
 * 
 * The Security Shepherd project is free software: you can redistribute it
 * and/or modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version.<br/>
 * 
 * The Security Shepherd project is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details.<br/>
 * 
 * You should have received a copy of the GNU General Public License along with
 * the Security Shepherd project. If not, see <http://www.gnu.org/licenses/>.
 * 
 * @author Sean Duggan
 */

public class Insecure_Data_Storage extends Activity {

	Button login;
	EditText password;
	EditText username;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		setContentView(R.layout.ids);
		referenceXML();

		String destinationDir = this.getFilesDir().getParentFile().getPath()+"/databases/";
		
		/*
		String destinationDir = "/data/data/" + getPackageName()
				+ "/databases/";
	*/
		String destinationPath = destinationDir + "Members";

		File f = new File(destinationPath);

		if (!f.exists()) {
			File directory = new File(destinationDir);
			directory.mkdirs();

			try {
				copyDatabase(getBaseContext().getAssets().open("Members"),
						new FileOutputStream(destinationPath));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

		login.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Toast toast = Toast.makeText(Insecure_Data_Storage.this,
						"Logging in...", Toast.LENGTH_SHORT);
				toast.show();

				String CheckName = username.getText().toString();
				String CheckPass = password.getText().toString();

				if (CheckName.contentEquals("") || CheckPass.contentEquals("")) {
					Toast blank = Toast.makeText(Insecure_Data_Storage.this,
							"Blank fields detected!", Toast.LENGTH_SHORT);
					blank.show();
				}

				if (CheckName.contentEquals("EpicTrees")
						|| CheckName.contentEquals("GraveyBones")
						|| CheckName.contentEquals("Admin")
						|| CheckName.contentEquals("FallenComrade")
						|| CheckName.contentEquals("IronFist")
						|| CheckName.contentEquals("Jumper")
						|| CheckName.contentEquals("99chips")
						|| CheckName.contentEquals("RegularVeg")) {

					Toast locked = Toast.makeText(Insecure_Data_Storage.this,
							"That Account is locked.", Toast.LENGTH_SHORT);
					locked.show();

				}

				else {
					Toast invalid = Toast.makeText(Insecure_Data_Storage.this,
							"Invalid Credentials!", Toast.LENGTH_SHORT);
					invalid.show();

				}
			}
		});

	}

	public void copyDatabase(InputStream iStream, OutputStream oStream)
			throws IOException {
		byte[] buffer = new byte[1024];
		int i;
		while ((i = iStream.read(buffer)) > 0) {
			oStream.write(buffer, 0, i);
		}
		iStream.close();
		oStream.close();
	}

	public void referenceXML() {
		login = (Button) findViewById(R.id.bLogin);
		username = (EditText) findViewById(R.id.etName);
		password = (EditText) findViewById(R.id.etPass);

	}
}