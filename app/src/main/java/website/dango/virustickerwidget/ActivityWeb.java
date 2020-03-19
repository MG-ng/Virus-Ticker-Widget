package website.dango.virustickerwidget;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.WindowManager;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;


public class ActivityWeb extends AppCompatActivity {


    private String url = "";
    public static final String URL_RES_ID_KEY = "Key to identify the corresponding url that should be opened";
    private WebView myWebView;


    @Override
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_web_view );

        myWebView = findViewById( R.id.mapWebView );

        myWebView.setWebChromeClient( new WebChromeClient() );
        myWebView.getSettings().setDomStorageEnabled( true );
        myWebView.getSettings().setJavaScriptEnabled( true );

        myWebView.getSettings().setAppCacheMaxSize( 0 );
        myWebView.getSettings().setAllowFileAccess( false );
        myWebView.getSettings().setAppCacheEnabled( false );

        Bundle bundle = getIntent().getExtras();
        if( bundle != null && bundle.getInt( URL_RES_ID_KEY, 0 ) != 0 ) {

            url = getString( bundle.getInt( URL_RES_ID_KEY, R.string.jhu_map_url ) );
        } else
            finish();

        myWebView.loadUrl( url );

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );

        ActionBar bar = getSupportActionBar();
        if( bar != null ) bar.hide();
    }

    @Override
    public boolean onKeyDown( int keyCode, KeyEvent event ) {
        // Check if the key event was the Back button and if there's history

        if( keyCode == KeyEvent.KEYCODE_BACK && myWebView.canGoBack() ) {

            myWebView.goBack();
            return true;
        }
        finish();
        return true;
    }

}
