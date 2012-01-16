package com.david.whereu;

import android.app.Activity;
import android.os.Bundle;
import android.content.SharedPreferences;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.EditText;


public class SettingsActivity extends Activity {

    Button mSaveButton;
    TextView mKeywordText;
    EditText mKeywordBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.settings);
      
      mSaveButton = (Button)findViewById(R.id.saveButton);
      mKeywordText = (TextView)findViewById(R.id.keywordText);
      mKeywordBox = (EditText)findViewById(R.id.keywordBox); 
      
      mSaveButton.setOnClickListener(listener);
      mKeywordBox.setOnClickListener(listener);


      SharedPreferences settings = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);  
      String current = settings.getString(Constants.PREFERENCES_KEYWORD, "Where u at?");
      mKeywordBox.setText(current);
    }

    public OnClickListener listener = new OnClickListener() {
        public void onClick(View aView) {
            switch (aView.getId()) {
                case R.id.saveButton:
                    updateSharedPreferences();
                    break;
                default:
                    break;
            }
        }
   
    };
    
    private void updateSharedPreferences() {
        SharedPreferences settings = getSharedPreferences(Constants.PREFERENCES, MODE_PRIVATE);  
        SharedPreferences.Editor prefEditor = settings.edit();  
        prefEditor.putString(Constants.PREFERENCES_KEYWORD, mKeywordBox.getText().toString());  
        prefEditor.commit();  
        Toast.makeText(getBaseContext(), "Saved" , Toast.LENGTH_LONG).show();
        finish();
    }
}
