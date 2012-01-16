package com.david.whereu;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.Location;
import android.os.*;
import android.util.Log;
import android.widget.Toast;

import java.lang.Runnable;
import java.util.Vector;

public class LocationService extends Service implements LocationListener {

    public static final String TAG = "LocationService";

    public static final int MSG_LOCATION               = 2;
    public static final int MSG_REQUEST_LOCATION       = 4;
    public static final int MSG_LOCATION_RESPONSE      = 5;
    public static final int MSG_LOCATION_STATUS        = 6;
    public static final int MSG_REGISTER_CLIENT        = 7;
    public static final int MSG_UNREGISTER_CLIENT      = 8;
    
    public static class Status {
        public static final int SUCCESS = 0;
        public static final int FAILURE = 1;
        public static final int ERROR   = 2;
    }

    public static class Type {
        public static final int BY_GPS = 1;
    }

    private Vector<Messenger> mClients    = new Vector<Messenger>();
    private MessageHandler mHandler       = new MessageHandler();
    public final Messenger mMessenger     = new Messenger(mHandler);
    private Location mBestLocation;
    private LocationManager mLocationManager;

    @Override
    public IBinder onBind(Intent intent) {
        setupLocationCallbacks();
        return mMessenger.getBinder();
    }

    @Override
    public int onStartCommand(Intent aIntent, int aFlags, int aStartId) {
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLocationManager.removeUpdates(this);
    }

    public class MessageHandler extends Handler { 
        @Override
        public void handleMessage(Message aMsg) {
            switch (aMsg.what) {
                case MSG_REGISTER_CLIENT:
                    registerClient(aMsg);
                    break;
                case MSG_UNREGISTER_CLIENT:
                    unregisterClient(aMsg);
                    break;
                case MSG_LOCATION:
                    updateLocation();
                    break; 
                case MSG_REQUEST_LOCATION:
                    sendLocation();
                    break;
            }
        }
    }

    private void registerClient(Message aMsg) {
        mClients.add(aMsg.replyTo);
    }

    private void unregisterClient(Message aMsg) {
        mClients.remove(aMsg.replyTo);
    }

    private void notifyStatus() {
        for (Messenger client : mClients) {
            try {
                client.send(Message.obtain(null, MSG_LOCATION_STATUS, 0, 0));
            } catch (RemoteException e) {
                mClients.remove(client);
            }
        }
    }

    private void sendMessage(int aMessage) {
        for (Messenger client : mClients) {
            try {
                client.send(Message.obtain(null, aMessage));
            } catch (RemoteException e) {
                mClients.remove(client);
            }
        }
    }

    private void sendMessage(int aMessage, int aArg1, int aArg2) {
        for (Messenger client : mClients) {
            try {
                client.send(Message.obtain(null, aMessage, aArg1, aArg2));
            } catch (RemoteException e) {
                mClients.remove(client);
            }
        }
    }

    private void sendMessage(int aMessage, int aArg1, int aArg2, Object aObj) {
        for (Messenger client : mClients) {
            try {
                Message msg = Message.obtain(null, aMessage, aArg1, aArg2);
                msg.obj = aObj;
                client.send(msg);
            } catch (RemoteException e) {
                mClients.remove(client);
            }
        }
    }

    private boolean isServiceEnabled(String provider) {
        return provider != null && !provider.equals("");
    }

    private void setupLocationCallbacks() {
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mBestLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
    }

    private void updateLocation() {
        notifyStatus();
        mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 500, 0, this);
        Runnable r = new Runnable() {
            public void run() {
                sendLocation();
                removeUpdates();
            }
        };
        mHandler.postDelayed(r, 30000);
    } 

    private void sendLocation() {

        Location lastGPS;
        lastGPS = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (lastGPS != null && isBetterLocation(mBestLocation, lastGPS)) {
            mBestLocation = lastGPS;
        }

        if (isServiceEnabled(LocationManager.GPS_PROVIDER)) {
            try {
                if (mBestLocation != null) {
                    sendMessage(MSG_LOCATION_RESPONSE, Type.BY_GPS, Status.SUCCESS, mBestLocation);
                }
            } catch (Exception aException) {
                sendMessage(MSG_LOCATION_RESPONSE, Type.BY_GPS, Status.ERROR);
                aException.printStackTrace();
            }
        } else {
            Log.d(TAG, "GPS_FAILED");
            sendMessage(MSG_LOCATION_RESPONSE, Type.BY_GPS, Status.FAILURE);
        }
        notifyStatus();
    }

    private void removeUpdates() {
        mLocationManager.removeUpdates(this);
        notifyStatus();
    }


    //GPS Listener Functions

    public void onLocationChanged(Location aLocation) {
        if (isBetterLocation(mBestLocation, aLocation)) {
            Log.d(TAG, "Recieved Better Location: (" + Double.toString(aLocation.getLatitude())
                + ", " + Double.toString(aLocation.getLongitude()) + ")");
            mBestLocation = aLocation;
        } 
    }

    public void onProviderDisabled(String aProvider) {
    }

    public void onProviderEnabled(String aProvider) {
    }

    public void onStatusChanged(String aProvider, int aStatus, Bundle aExtras) {
    }

    protected boolean isBetterLocation(android.location.Location aBestLocation, android.location.Location aLocation) {
        if (aBestLocation == null) {
            return true;
        }

        long timeDelta = aLocation.getTime() - aBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > 1200;
        boolean isSignificantlyOlder = timeDelta < -1200;
        boolean isNewer = timeDelta > 0;

        if (isSignificantlyNewer) {
            return true;
        } else if (isSignificantlyOlder) {
            return false;
        }

        int accuracyDelta = (int) (aLocation.getAccuracy() - aBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        boolean isFromSameProvider = isSameProvider(aLocation.getProvider(),
            aBestLocation.getProvider());

        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }

        return false;
    }

    private boolean isSameProvider(String aProvider1, String aProvider2) {
        if (aProvider1 == null) {
            return aProvider2 == null;
        }

        return aProvider1.equals(aProvider2);
    }
}
