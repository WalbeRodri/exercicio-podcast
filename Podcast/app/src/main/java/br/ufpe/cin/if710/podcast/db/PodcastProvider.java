package br.ufpe.cin.if710.podcast.db;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;

import static br.ufpe.cin.if710.podcast.db.PodcastDBHelper.DATABASE_TABLE;

public class PodcastProvider extends ContentProvider {
    PodcastDBHelper dbHelper;
    Context context;
    //Unica classe pra acesso ao  DBHelper. Lembrar de perguntar a necessidade de mais uma abstracao!
    //construtor
    public PodcastProvider() {

    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return dbHelper.getWritableDatabase().delete(DATABASE_TABLE, selection,selectionArgs);
    }

    @Override
    // pra que serve isso?
    public String getType(Uri uri) {
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override

    public Uri insert(Uri uri, ContentValues values) {
        long retorno = dbHelper.getWritableDatabase().insert(DATABASE_TABLE, null, values);
        return Uri.withAppendedPath(PodcastProviderContract.EPISODE_LIST_URI, Long.toString(retorno));
    }

    @Override
    public boolean onCreate() {
        dbHelper = PodcastDBHelper.getInstance(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        return dbHelper.getReadableDatabase().query(DATABASE_TABLE, projection, selection, selectionArgs, null, null, sortOrder);

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
       return dbHelper.getWritableDatabase().update(PodcastDBHelper.DATABASE_TABLE,values,selection,selectionArgs);
    }
}
