package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.NotificationCompat;

import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.R;
import br.ufpe.cin.if710.podcast.ServiceDownload;
import br.ufpe.cin.if710.podcast.db.PodcastProvider;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;
import br.ufpe.cin.if710.podcast.ui.adapter.XmlFeedAdapter;

import static br.ufpe.cin.if710.podcast.ServiceDownload.ACTION_DOWNLOAD_lIST;
import static br.ufpe.cin.if710.podcast.db.PodcastDBHelper.EPISODE_DATE;
import static br.ufpe.cin.if710.podcast.db.PodcastDBHelper.EPISODE_DESC;
import static br.ufpe.cin.if710.podcast.db.PodcastDBHelper.EPISODE_DOWNLOAD_LINK;
import static br.ufpe.cin.if710.podcast.db.PodcastDBHelper.EPISODE_LINK;
import static br.ufpe.cin.if710.podcast.db.PodcastDBHelper.EPISODE_TITLE;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_LIST_URI;

public class MainActivity extends Activity {

    //ao fazer envio da resolucao, use este link no seu codigo!
    private final String RSS_FEED = "http://leopoldomt.com/if710/fronteirasdaciencia.xml";
    //TODO teste com outros links de podcast
    private PodcastProvider dbHelper;
    private ListView items;
    private boolean onFirstPlan;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new PodcastProvider();
        dbHelper.onCreate();
        items = (ListView) findViewById(R.id.items);

        //registro
        IntentFilter intentFilter = new IntentFilter(ACTION_DOWNLOAD_lIST);
        LocalBroadcastManager.getInstance(getApplicationContext()).registerReceiver(onServiceDownloadCompleteEvent, intentFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        onFirstPlan = true;
        ServiceDownload.startActionDownloadList(this, RSS_FEED);
    }

    @Override
    protected void onStop() {
        super.onStop();
        onFirstPlan = false;
        XmlFeedAdapter adapter = (XmlFeedAdapter) items.getAdapter();
        adapter.clear();
    }

    private BroadcastReceiver onServiceDownloadCompleteEvent = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {

//            //sleep para testar a notificação
//            try {
//                Thread.sleep(10000);
//            }catch (Exception e) {
//                e.printStackTrace();
//
//            }
//            Log.d("<<<<<<<<<<<<<<<<<","PASSEI DA THREADSLEEP");
            //checa se esta em primeiro plano o atributo é atualizado
            if (onFirstPlan) {
                //realiza a query
                Cursor cursor = getContentResolver().query(EPISODE_LIST_URI, null, null, null, null);
                ArrayList<ItemFeed> itemFeeds = new ArrayList<>();

                //evita a chamada no objeto null e Nullpointer
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String title = cursor.getString(cursor.getColumnIndex(EPISODE_TITLE));
                        String link = cursor.getString(cursor.getColumnIndex(EPISODE_LINK));
                        String pubDate = cursor.getString(cursor.getColumnIndex(EPISODE_DATE));
                        String description = cursor.getString(cursor.getColumnIndex(EPISODE_DESC));
                        String downloadLink = cursor.getString(cursor.getColumnIndex(EPISODE_DOWNLOAD_LINK));
                        itemFeeds.add(new ItemFeed(title, link, pubDate, description, downloadLink));
                    }
                    cursor.close();
                }
                //Adapter Personalizado
                XmlFeedAdapter adapter = new XmlFeedAdapter(getApplicationContext(), R.layout.itemlista, itemFeeds);

                //atualizar o list view
                items.setAdapter(adapter);
                items.setTextFilterEnabled(true);
            } else {

                final Intent notificationIntent = new Intent(context, MainActivity.class);
                final PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, notificationIntent, 0);

                NotificationManager mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(getApplicationContext());

                mBuilder.setAutoCancel(true)
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setContentTitle("Atualização realizada com sucesso")
                        .setContentText("Visite a sua aplicação para maiores detalhes")
                        .setContentIntent(pendingIntent)
                        .build();

            }
        }
    };

}