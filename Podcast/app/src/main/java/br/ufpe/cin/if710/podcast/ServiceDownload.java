package br.ufpe.cin.if710.podcast;

import android.app.IntentService;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import org.xmlpull.v1.XmlPullParserException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import br.ufpe.cin.if710.podcast.db.PodcastProviderContract;
import br.ufpe.cin.if710.podcast.domain.ItemFeed;
import br.ufpe.cin.if710.podcast.domain.XmlFeedParser;

import static br.ufpe.cin.if710.podcast.db.PodcastDBHelper.EPISODE_DATE;
import static br.ufpe.cin.if710.podcast.db.PodcastDBHelper.EPISODE_DESC;
import static br.ufpe.cin.if710.podcast.db.PodcastDBHelper.EPISODE_DOWNLOAD_LINK;
import static br.ufpe.cin.if710.podcast.db.PodcastDBHelper.EPISODE_LINK;
import static br.ufpe.cin.if710.podcast.db.PodcastDBHelper.EPISODE_TITLE;
import static br.ufpe.cin.if710.podcast.db.PodcastDBHelper.columns;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.DESCRIPTION;
import static br.ufpe.cin.if710.podcast.db.PodcastProviderContract.EPISODE_LIST_URI;

public class ServiceDownload extends IntentService {
    public static final String ACTION_DOWNLOAD_lIST = "br.ufpe.cin.if710.podcast.action.DOWNLOAD_LIST";
    private static final String ACTION_BAZ = "br.ufpe.cin.if710.podcast.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "br.ufpe.cin.if710.podcast.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "br.ufpe.cin.if710.podcast.extra.PARAM2";

    public ServiceDownload() {
        super("ServiceDownload");
    }

    public static void startActionDownloadList(Context context, String linkLista) {
        Intent intent = new Intent(context, ServiceDownload.class);
        intent.setAction(ACTION_DOWNLOAD_lIST);
        intent.putExtra(EXTRA_PARAM1, linkLista);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void startActionBaz(Context context, String param1, String param2) {
        Intent intent = new Intent(context, ServiceDownload.class);
        intent.setAction(ACTION_BAZ);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_DOWNLOAD_lIST.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                handleActionDownloadList(param1);
            } else if (ACTION_BAZ.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionBaz(param1, param2);
            }
        }
    }

    private void handleActionDownloadList(String EXTRA_PARAM1) {
        Log.d(">>>>>>>>>>>>>>>>>>","OPA");
        List<ItemFeed> itemList = new ArrayList<>();
        try {
            itemList = XmlFeedParser.parse(getRssFeed(EXTRA_PARAM1));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        adicionaItem(itemList);

        //intent avisando pra view ser atualizada
        Intent downloadComplete = new Intent(ACTION_DOWNLOAD_lIST);
        LocalBroadcastManager.getInstance(this).sendBroadcast(downloadComplete);



    }

    private void handleActionBaz(String param1, String param2) {
        // TODO: Handle action Baz
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private String getRssFeed(String feed) throws IOException {
        InputStream in = null;
        String rssFeed = "";

        try {
            URL url = new URL(feed);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            in = conn.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for (int count; (count = in.read(buffer)) != -1; ) {
                out.write(buffer, 0, count);
            }
            byte[] response = out.toByteArray();
            rssFeed = new String(response, "UTF-8");
        } finally {

            if (in != null) {
                in.close();
            }
        }
        return rssFeed;
    }

    private boolean adicionaItem (List<ItemFeed> feed){
        ContentValues values = new ContentValues();
        //para todos os itens da lista de feed, checa se existe na tabela e adiciona-os
        for(int i =0; i<feed.size(); i++) {

            //Consulta para descobrir se o item do dia tal ja existe no banco
            String consulta = EPISODE_TITLE + " =? AND "+EPISODE_DATE +" =?" ;
            String[] comparativo = new String[]{
                    feed.get(i).getTitle(), feed.get(i).getPubDate()
            };

            //caso a query retorne algum item checaItem
            Cursor checaItem = getContentResolver().query(PodcastProviderContract.EPISODE_LIST_URI, columns, consulta, comparativo, null);

            //checa se a quantidade é vazia
            if ((checaItem != null)&& (checaItem.getCount() == 0)){
                values.put(EPISODE_TITLE, feed.get(i).getTitle());
                values.put(EPISODE_DATE, feed.get(i).getPubDate());
                values.put(EPISODE_DOWNLOAD_LINK, feed.get(i).getDownloadLink());
                values.put(EPISODE_DESC, feed.get(i).getDescription());
                values.put(EPISODE_LINK, feed.get(i).getLink());
                getContentResolver().insert(EPISODE_LIST_URI, values);
                Log.d(">>>>>>>>>>", "Inseri ao item " + feed.get(i).getTitle());
                //limpar pra evitar repetições dos itens
                values.clear();
                checaItem.close();
            } else if (checaItem != null) {
                checaItem.close();
            }


        }
       return true;
    }
}
