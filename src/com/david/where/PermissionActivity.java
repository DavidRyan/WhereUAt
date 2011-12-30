package com.david.where;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class PermissionActivity extends Activity {

    ListView mPermissionList;
    mCursorAdapter mAdapter;
    Button mAddButton;
    Cursor mCursor;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permission_list);
        
        SQLiteDatabase mDatabase = openOrCreateDatabase(Constants.DB_NAME, SQLiteDatabase.OPEN_READONLY, null);
        mCursor = mDatabase.query(Constants.TABLE_NAME, null, null, null, null, null, null); 

        mCursor.moveToFirst();

        mAdapter = new mCursorAdapter(this, mCursor, R.layout.contact_item, true);
        mPermissionList = (ListView) findViewById(R.id.permission_listview); 
    	mPermissionList.setAdapter(mAdapter);
        mPermissionList.setOnItemClickListener(mAdapter);

        mAddButton = (Button) findViewById(R.id.add_button);
        mAddButton.setOnClickListener(listener);
        
    }

    public OnClickListener listener = new OnClickListener() {
        public void onClick(View aView) {
            switch (aView.getId()) {
                case R.id.add_button:
                    launchGetContacts();
                    break;
                default:
                    break;
            }
        }
    };

    private void launchGetContacts() {
        Intent i = new Intent(this, GetContactsActivity.class);
        startActivity(i);
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
        public View newView(Context aContext, Cursor aCursor, ViewGroup aParent) {  return null; }


        @Override
        public View getView(int aPosition, View convertView, ViewGroup parent) {
            View row = convertView;
            Cursor textCursor = getCursor();
            ViewHolder mHolder;        

            if(row == null) {
                row = mInflater.inflate(mReasourceId, null);
                mHolder = new ViewHolder();
                mHolder.name = (TextView) row.findViewById(R.id.contact_item_name);
                mHolder.number = (TextView) row.findViewById(R.id.contact_item_number);     
                row.setTag(mHolder);
            } else {
                mHolder = (ViewHolder) row.getTag();
            }

            textCursor.moveToPosition(aPosition);
            String mName = textCursor.getString(Constants.NAME_COLUMN);
            mHolder.name.setText(mName);
            String mNumber = textCursor.getString(Constants.NUMBER_COLUMN);
            mHolder.number.setText(mNumber);
                        
            return row;     
        }

            @Override
            public void onItemClick(AdapterView<?> aAdapter, View aView, int aPosition, long aId) {
                
                TextView nameTextView = (TextView) aView.findViewById(R.id.contact_item_name);
                TextView numberTextView = (TextView) aView.findViewById(R.id.contact_item_number);
                final String contact = nameTextView.getText().toString();
                final String number = numberTextView.getText().toString();

                AlertDialog.Builder adb = new AlertDialog.Builder(PermissionActivity.this);
                adb.setTitle("Delete?");
                adb.setMessage("Are you sure you want to delete " + contact + " from your permission list?");
                adb.setNegativeButton("Cancel", null);
                adb.setPositiveButton("Ok", new AlertDialog.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    SQLiteDatabase mDatabase = openOrCreateDatabase(Constants.DB_NAME, SQLiteDatabase.OPEN_READWRITE, null);
                    mDatabase.delete(Constants.TABLE_NAME, Constants.DB_NUMBERROW + "=" + number , null);
                    mDatabase.close();
                    update();
                }});
                adb.show();    
            };
    }

    @Override
    public void onResume() {
        super.onResume();
        update();
    }
    

    public void update() {
        mCursor.requery();
        mAdapter.notifyDataSetChanged();
    }

    private class ViewHolder {
        public TextView name;
        public TextView number;
    }

}
