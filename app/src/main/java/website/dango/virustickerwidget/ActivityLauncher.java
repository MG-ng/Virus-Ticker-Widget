package website.dango.virustickerwidget;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import static website.dango.virustickerwidget.ActivityWeb.URL_RES_ID_KEY;

public class ActivityLauncher extends AppCompatActivity implements View.OnClickListener {


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_launcher );

        TextView openDisclaimer = findViewById( R.id.headerLinks );
        openDisclaimer.setOnClickListener( this );

        TextView openJHU = findViewById( R.id.open_JHU );
        openJHU.setOnClickListener( this );

        TextView openJC = findViewById( R.id.open_JC );
        openJC.setOnClickListener( this );

        TextView openWHO = findViewById( R.id.open_who );
        openWHO.setOnClickListener( this );

        TextView openACOER = findViewById( R.id.open_chooser_germany );
        openACOER.setOnClickListener( this );
        
        TextView openLearn = findViewById( R.id.open_learn );
        openLearn.setOnClickListener( this );

        TextView openChooser = findViewById( R.id.open_chooser );
        openChooser.setOnClickListener( this );
    }


    @Override
    public void onClick( View view ) {

        Intent intent;

        switch( view.getId() ) {

            case R.id.headerLinks:
                openDisclaimer();
                break;

            case R.id.open_JHU:
                openWebsite( this, R.string.jhu_map_url );
                break;

            case R.id.open_JC:
                openWebsite( this, R.string.john_coene_url );
                break;

            case R.id.open_who:
                openWebsite( this, R.string.who_news_url );
                break;
                
            case R.id.open_chooser_germany:
                openGermanItems();
                break;

            case R.id.open_learn:
                intent = new Intent( this, ActivityLearn.class );
                startActivity( intent );
                break;

            case R.id.open_chooser:
                Intent sendIntent = new Intent();
                sendIntent.setAction( Intent.ACTION_SEND );
                sendIntent.putExtra( Intent.EXTRA_TEXT, "I discovered a virus ticker widget in the Play Store! Pretty useful" );
                sendIntent.setType( "text/plain" );

                Intent shareIntent = Intent.createChooser( sendIntent, null );
                startActivity( shareIntent );
                break;
        }

    }

    public static void openWebsite( Context context, int url_res_id ) {
        Intent intent = new Intent( context, ActivityWeb.class );
        intent.putExtra( URL_RES_ID_KEY, url_res_id );
        context.startActivity( intent );
    }

    public void openGermanItems() {
        new AlertDialog.Builder( this )
                .setItems( new CharSequence[]{ getString( R.string.rki ), getString( R.string.jetzt ) }
                        , new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick( DialogInterface dialogInterface, int i ) {
                                switch( i ) {
                                    case 0:
                                        openWebsite( ActivityLauncher.this, R.string.rki_url );
                                        break;
                                    case 1:
                                        openWebsite( ActivityLauncher.this, R.string.jetzt_url );
                                        break;
                                }
                            }
                        } )
                .setCancelable( true )
                .create().show();
    }

    public void openDisclaimer() {
        new AlertDialog.Builder( this )
                .setMessage( R.string.disclaimer )
                .setCancelable( true )
                .create().show();
    }

}
