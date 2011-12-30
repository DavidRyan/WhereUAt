package com.david.where;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import android.app.Activity;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.Contacts.People;
import android.provider.ContactsContract.Contacts.Data;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter; 
import android.view.LayoutInflater;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.io.InputStream;
import java.io.ByteArrayInputStream;

import com.david.where.*;

public class GetContactsActivity extends Activity {

    SQLiteDatabase mDatabase;
    ListView mListView;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.contactget);
	    
        mListView = (ListView)findViewById(R.id.contactList);
        String SELECTION = ContactsContract.Contacts.HAS_PHONE_NUMBER + "='1'";
        String ORDER =  ContactsContract.Contacts.DISPLAY_NAME + " ASC";
	    Cursor cursor = managedQuery(ContactsContract.Contacts.CONTENT_URI, null, SELECTION, null, ORDER);
        mCursorAdapter mAdapter = new mCursorAdapter(this, cursor, R.layout.contact_item, true);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(mAdapter);
    }
    
    public class mCursorAdapter extends CursorAdapter implements AdapterView.OnItemClickListener {

        int mReasourceId;
        LayoutInflater mInflater;
        
        public mCursorAdapter(Context aContext, Cursor aCursor, int aReasourceId, boolean aAutoRequery) {
            super(aContext, aCursor);
            mReasourceId = aReasourceId;
            mInflater = LayoutInflater.from(aContext); 
        }
        
        @Override
        public void bindView(View view, Context context, Cursor cursor) {}

        @Override
        public View newView(Context aContext, Cursor aCursor, ViewGroup aParent) { return null; }


        @Override
        public View getView(int aPosition, View convertView, ViewGroup parent) {
            
            View row = convertView;
            Cursor textCursor = getCursor();
            ViewHolder mHolder;        

            if(row == null) {
                row = mInflater.inflate(mReasourceId, null);
                mHolder = new ViewHolder();
                mHolder.name = (TextView) row.findViewById(R.id.contact_item_name);
                mHolder.image = (ImageView) row.findViewById(R.id.contact_item_image);     
                mHolder.number = (TextView) row.findViewById(R.id.contact_item_number);
                row.setTag(mHolder);
            } else {
                mHolder = (ViewHolder) row.getTag();
            }

            textCursor.moveToPosition(aPosition);
            String mName = textCursor.getString(textCursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
            mHolder.name.setText(mName);

            long mId = Long.parseLong(textCursor.getString(textCursor.getColumnIndex(ContactsContract.Contacts._ID)));        
            String mNumber = getContactNumber(Long.toString(mId));
            mNumber = mNumber.replace("-","");
            mNumber = mNumber.replace("+","");

            mHolder.number.setText(mNumber);

            return row;     
        }

		@Override
		public void onItemClick(AdapterView<?> aList, View aView, int aPosition, long arg3) {
            mDatabase = openOrCreateDatabase(Constants.DB_NAME, SQLiteDatabase.CREATE_IF_NECESSARY, null);
            ContentValues values = new ContentValues();
            values.put(Constants.DB_NAMEROW, ((TextView) aView.findViewById(R.id.contact_item_name)).getText().toString());
            values.put(Constants.DB_NUMBERROW, ((TextView) aView.findViewById(R.id.contact_item_number)).getText().toString());
            long id = mDatabase.insert(Constants.TABLE_NAME, null, values);
            mDatabase.close();
            finish();
        }         
    }


    private String getContactNumber(String aId) {
    	
    		ContentResolver cr = this.getContentResolver();
            Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?", new String[] { aId }, null);

            if(pCur.moveToFirst()) {
                String number = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                return number;
            }
            return "";
    }
    
    class ViewHolder {
        TextView name;
        ImageView image;
        TextView number;
    }

}
