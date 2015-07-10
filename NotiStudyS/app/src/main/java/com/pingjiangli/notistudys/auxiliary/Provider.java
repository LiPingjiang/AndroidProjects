package com.pingjiangli.notistudys.auxiliary;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.util.Log;

import com.aware.Aware;
import com.aware.utils.DatabaseHelper;

import java.util.HashMap;

/**
 * Created by Pingjiang.Li on 26/06/15.
 */
public class Provider extends ContentProvider {

    /**
     * Authority of this content provider
     */
    public static String AUTHORITY = "com.pingjiangli.notistudys.provider.notistudy";

    /**
     * ContentProvider database version. Increment every time you modify the database structure
     */
    public static final int DATABASE_VERSION = 1;

    @Override
    public boolean onCreate() {
        Log.d(AUTHORITY,"Provider is on create.");
        AUTHORITY = getContext().getPackageName() + ".provider.notistudy"; //make AUTHORITY dynamic
        sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0], NOTISTUDY); //URI for all records
        sUriMatcher.addURI(AUTHORITY, DATABASE_TABLES[0]+"/#", NOTISTUDY_ID); //URI for a single record

        tableMap = new HashMap<String, String>();
        tableMap.put(NotiStudy_Data._ID, NotiStudy_Data._ID);
        tableMap.put(NotiStudy_Data.TIMESTAMP, NotiStudy_Data.TIMESTAMP);
        tableMap.put(NotiStudy_Data.DEVICE_ID, NotiStudy_Data.DEVICE_ID);
        tableMap.put(NotiStudy_Data.GRAVITY, NotiStudy_Data.GRAVITY);
        tableMap.put(NotiStudy_Data.ACTIVITY_NAME, NotiStudy_Data.ACTIVITY_NAME);
        tableMap.put(NotiStudy_Data.ACTIVITY_TYPE, NotiStudy_Data.ACTIVITY_TYPE);
        tableMap.put(NotiStudy_Data.ACTIVITY_CONFIDENCE, NotiStudy_Data.ACTIVITY_CONFIDENCE);
        tableMap.put(NotiStudy_Data.LOCATION_LONGITUDE, NotiStudy_Data.LOCATION_LONGITUDE);
        tableMap.put(NotiStudy_Data.LOCATION_ALTITUDE, NotiStudy_Data.LOCATION_ALTITUDE);
        tableMap.put(NotiStudy_Data.NOTIFICATION_CATEGORY, NotiStudy_Data.NOTIFICATION_CATEGORY);
        tableMap.put(NotiStudy_Data.NOTIFICATION_PRIORITY, NotiStudy_Data.NOTIFICATION_PRIORITY);
        tableMap.put(NotiStudy_Data.NOTIFICATION_VISIBILITY,NotiStudy_Data.NOTIFICATION_VISIBILITY);
        tableMap.put(NotiStudy_Data.NOTIFICATION_WHEN,NotiStudy_Data.NOTIFICATION_WHEN);
        tableMap.put(NotiStudy_Data.NOTIFICATION_TEMPLATE,NotiStudy_Data.NOTIFICATION_TEMPLATE);
        tableMap.put(NotiStudy_Data.ESM_LOCATION,NotiStudy_Data.ESM_LOCATION);
        tableMap.put(NotiStudy_Data.ESM_IDENTITY,NotiStudy_Data.ESM_IDENTITY);
        tableMap.put(NotiStudy_Data.ESM_PRESURE,NotiStudy_Data.ESM_PRESURE);
        tableMap.put(NotiStudy_Data.ESM_IMPORTANCE,NotiStudy_Data.ESM_IMPORTANCE);
        tableMap.put(NotiStudy_Data.ESM_URGENCE,NotiStudy_Data.ESM_URGENCE);
        tableMap.put(NotiStudy_Data.ESM_PACKAGENAME,NotiStudy_Data.ESM_PACKAGENAME);


        return true; //let Android know that the database is ready to be used.
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,String sortOrder) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        switch (sUriMatcher.match(uri)) {
            case NOTISTUDY:
                qb.setTables(DATABASE_TABLES[0]);
                qb.setProjectionMap(tableMap);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        try {
            Cursor c = qb.query(database, projection, selection, selectionArgs, null, null, sortOrder);
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
        } catch (IllegalStateException e) {
            if (Aware.DEBUG) Log.e(Aware.TAG, e.getMessage());
            return null;
        }
    }

    @Override
    public String getType(Uri uri) {
        switch (sUriMatcher.match(uri)) {
            case NOTISTUDY:
                return NotiStudy_Data.CONTENT_TYPE;
            case NOTISTUDY_ID:
                return NotiStudy_Data.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues new_values) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return null;
        }

        ContentValues values = (new_values != null) ? new ContentValues(new_values) : new ContentValues();

        switch (sUriMatcher.match(uri)) {
            case NOTISTUDY:
                long _id = database.insert(DATABASE_TABLES[0], NotiStudy_Data.DEVICE_ID, values);
                if (_id > 0) {
                    Uri dataUri = ContentUris.withAppendedId(NotiStudy_Data.CONTENT_URI, _id);
                    getContext().getContentResolver().notifyChange(dataUri, null);
                    return dataUri;
                }
                throw new SQLException("Failed to insert row into " + uri);
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY, "Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case NOTISTUDY:
                count = database.delete(DATABASE_TABLES[0], selection,selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        if( ! initializeDB() ) {
            Log.w(AUTHORITY,"Database unavailable...");
            return 0;
        }

        int count = 0;
        switch (sUriMatcher.match(uri)) {
            case NOTISTUDY:
                count = database.update(DATABASE_TABLES[0], values, selection, selectionArgs);
                break;
            default:
                database.close();
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    //ContentProvider query indexes
    private static final int NOTISTUDY = 1;
    private static final int NOTISTUDY_ID = 2;
    /**
     * Database stored in external folder: /AWARE/plugin_example.db
     */
    public static final String DATABASE_NAME = Environment.getExternalStorageDirectory() + "/AWARE/plugin_NotiStudy.db";
    /**
     * Database tables:<br/>
     * - result
     */
    public static final String[] DATABASE_TABLES = {"result"};

    /**
     * Database table fields
     */
    public static final String[] TABLES_FIELDS = {
            NotiStudy_Data._ID + " integer primary key autoincrement," +
                    NotiStudy_Data.TIMESTAMP + " real default 0," +
                    NotiStudy_Data.DEVICE_ID + " text default ''," +
                    NotiStudy_Data.GRAVITY + " real default 0,"+
                    NotiStudy_Data.ACTIVITY_NAME + " text default ''," +
                    NotiStudy_Data.ACTIVITY_TYPE + " integer default 0,"+
                    NotiStudy_Data.ACTIVITY_CONFIDENCE + " real default 0,"+
                    NotiStudy_Data.LOCATION_LONGITUDE + " real default 0,"+
                    NotiStudy_Data.LOCATION_ALTITUDE + " real default 0,"+
                    NotiStudy_Data.NOTIFICATION_CATEGORY + " integer default 0,"+
                    NotiStudy_Data.NOTIFICATION_PRIORITY + " integer default 0,"+
                    NotiStudy_Data.NOTIFICATION_VISIBILITY + " integer default 0,"+
                    NotiStudy_Data.NOTIFICATION_WHEN + " real default 0,"+
                    NotiStudy_Data.NOTIFICATION_TEMPLATE + " real default 0,"+
                    NotiStudy_Data.ESM_LOCATION + " integer default 0,"+
                    NotiStudy_Data.ESM_IDENTITY + " integer default 0,"+
                    NotiStudy_Data.ESM_PRESURE + " integer default 0,"+
                    NotiStudy_Data.ESM_IMPORTANCE + " integer default 0,"+
                    NotiStudy_Data.ESM_URGENCE + " integer default 0,"+
                    NotiStudy_Data.ESM_PACKAGENAME + " text default ''," +

                    "UNIQUE (" + NotiStudy_Data.TIMESTAMP + "," + NotiStudy_Data.DEVICE_ID + ")"
    };

    private static UriMatcher sUriMatcher = null;
    private static HashMap<String, String> tableMap = null;
    private static DatabaseHelper databaseHelper = null;
    private static SQLiteDatabase database = null;

    private boolean initializeDB() {
        if (databaseHelper == null) {
            databaseHelper = new DatabaseHelper( getContext(), DATABASE_NAME, null, DATABASE_VERSION, DATABASE_TABLES, TABLES_FIELDS );
            Log.d(AUTHORITY,DATABASE_NAME);
        }
        if( databaseHelper != null && ( database == null || ! database.isOpen()) ) {
            database = databaseHelper.getWritableDatabase();
        }
        return( database != null && databaseHelper != null);
    }




    public static final class NotiStudy_Data implements BaseColumns {
        private NotiStudy_Data(){};
        /**
         * Your ContentProvider table content URI.<br/>
         * The last segment needs to match your database table name
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + Provider.AUTHORITY + "/result");

        /**
         * How your data collection is identified internally in Android (vnd.android.cursor.dir). <br/>
         * It needs to be /vnd.aware.plugin.XXX where XXX is your plugin name (no spaces!).
         */
        public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.aware.NotiStudy";

        /**
         * How each row is identified individually internally in Android (vnd.android.cursor.item). <br/>
         * It needs to be /vnd.aware.plugin.XXX where XXX is your plugin name (no spaces!).
         */
        public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.aware.NotiStudy";

        public static final String _ID = "_id";
        public static final String TIMESTAMP = "timestamp";
        public static final String DEVICE_ID = "device_id";

        public static final String GRAVITY = "gravity";
        public static final String ACTIVITY_NAME = "activity_name";
        public static final String ACTIVITY_TYPE = "activity_type";
        public static final String ACTIVITY_CONFIDENCE = "activity_confidence";
        public static final String LOCATION_LONGITUDE = "location_longitude";
        public static final String LOCATION_ALTITUDE = "location_altitude";
        public static final String NOTIFICATION_CATEGORY = "notification_category";
        public static final String NOTIFICATION_PRIORITY = "notification_priority";
        public static final String NOTIFICATION_VISIBILITY = "notification_visibility";
        public static final String NOTIFICATION_WHEN = "notification_when";
        public static final String NOTIFICATION_TEMPLATE = "notification_template";
        public static final String ESM_LOCATION = "esm_location";
        public static final String ESM_IDENTITY = "esm_identity";
        public static final String ESM_PRESURE = "esm_presure";
        public static final String ESM_IMPORTANCE = "esm_importance";
        public static final String ESM_URGENCE = "esm_urgence";
        public static final String ESM_PACKAGENAME = "packagename";



    }
}
