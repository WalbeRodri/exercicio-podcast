package br.ufpe.cin.if710.podcast.ui;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import br.ufpe.cin.if710.podcast.R;

public class EpisodeDetailActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode_detail);

        TextView descricaoView = findViewById(R.id.descricaoDetails);
        TextView tituloView = findViewById(R.id.tituloDetails);
        TextView dataView = findViewById(R.id.dataDetails);

        String descricaoIntent = getIntent().getStringExtra("descricao");
        String tituloIntent = getIntent().getStringExtra("titulo");
        String dataIntent = getIntent().getStringExtra("data");
        Log.d(">>>>>>>>>>>>>", dataIntent + " " + tituloIntent + " " + descricaoIntent);

        if (dataView!=null && tituloView != null && descricaoView!= null) {
            dataView.setText(dataIntent);
            tituloView.setText(tituloIntent);
            descricaoView.setText(descricaoIntent);
        }

    }
}
