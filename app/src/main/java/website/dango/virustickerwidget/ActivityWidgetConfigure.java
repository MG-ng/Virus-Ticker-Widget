package website.dango.virustickerwidget;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatAutoCompleteTextView;

import com.google.gson.Gson;

import java.util.LinkedList;
import java.util.List;

import static android.appwidget.AppWidgetManager.INVALID_APPWIDGET_ID;


/**
 * The configuration screen for the {@link AppWidget} com.bepatient.website.widgettest.AppWidget.
 */
public class ActivityWidgetConfigure extends Activity implements View.OnClickListener {
    private final static String TAG = "ActivityWidgetConfigure";

    private int mAppWidgetId = INVALID_APPWIDGET_ID;
    private AppCompatAutoCompleteTextView acCountry;
    private RadioGroup rgMode, rgTheme;
    private static final String PREFS_NAME = "AppWidget";
    private static final String PREF_PREFIX_KEY = "appwidget";

    public ActivityWidgetConfigure() {
        super();
    }

    @SuppressLint( "ClickableViewAccessibility" )
    @Override
    public void onCreate( Bundle icicle ) {
        super.onCreate( icicle );

        // Set the result to CANCELED.  This will cause the widget_content host to cancel
        // out of the widget_content placement if the user presses the back button.
        setResult( RESULT_CANCELED );

        setContentView( R.layout.activity_widget_configure );
        // Set layout size of activity
        getWindow().setLayout( ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT );

        acCountry = findViewById( R.id.editCountry );
        acCountry.setOnTouchListener( acCountryListener );
        findViewById( R.id.add_button ).setOnClickListener( this );

        rgMode = findViewById( R.id.rgMode );
        rgTheme = findViewById( R.id.rgThemeMode );


        // Find the widget_content id from the intent.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if( extras != null ) {
            mAppWidgetId = extras.getInt(
                    AppWidgetManager.EXTRA_APPWIDGET_ID, INVALID_APPWIDGET_ID );
        }

        // If this activity was started with an intent without an website widget_content ID, finish with an error.
        if( mAppWidgetId == INVALID_APPWIDGET_ID ) {
            finish();
        }
    }

    /**
     * Adds a widget to the home screen and saves the settings
     * @param context needed for the saving
     * @param settings to save
     */
    private void createWidget( Context context, CustomWidgetSettings settings ) {
        // Store the string locally
        saveWidgetSettings( context, mAppWidgetId, settings );

        // It is the responsibility of the configuration activity to update the website widget_content
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance( context );
        AppWidget.updateAppWidget( context, appWidgetManager, mAppWidgetId );

        // Make sure we pass back the original appWidgetId
        Intent resultValue = new Intent();
        resultValue.putExtra( AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId );
        setResult( RESULT_OK, resultValue );
        finish();
    }

    // Read the prefix from the SharedPreferences object for this widget_content.
    // If there is no preference saved, get the default from a resource
    public static CustomWidgetSettings loadSettingsPref( Context context, int appWidgetId ) {

        SharedPreferences prefs = context.getSharedPreferences( PREFS_NAME, 0 );
        String settingsTextValue = prefs.getString( PREF_PREFIX_KEY + appWidgetId, null );

        if( settingsTextValue != null ) {
            return new Gson().fromJson( settingsTextValue, CustomWidgetSettings.class );
        } else {
            return null;
        }
    }

    /**
     * Deletes the WidgetId so it is not updated anymore
     * @param context needed
     * @param appWidgetId to delete
     */
    public static void deleteWidgetSettings( Context context, int appWidgetId ) {
        SharedPreferences.Editor prefs = context.getSharedPreferences( PREFS_NAME, 0 ).edit();
        prefs.remove( PREF_PREFIX_KEY + appWidgetId );
        prefs.apply();
    }

    // Write the prefix to the SharedPreferences object for this widget_content
    static void saveWidgetSettings( Context context, int appWidgetId, CustomWidgetSettings settings ) {

        SharedPreferences.Editor prefs = context.getSharedPreferences( PREFS_NAME, 0 ).edit();
        prefs.putString( PREF_PREFIX_KEY + appWidgetId, new Gson().toJson( settings ) );
        prefs.apply();
    }


    @Override
    public void onClick( View view ) {

        switch( view.getId() ) {
            case R.id.add_button:
                createWidget( this, readInput() );
                break;
        }
    }

    private CustomWidgetSettings readInput() {
        String country = acCountry.getText().toString().trim();

        int mode;

        switch( rgMode.getCheckedRadioButtonId() ) {
            case R.id.confirmed:
                mode = CustomWidgetSettings.CONFIRMED;
                break;

            case R.id.recovered:
                mode = CustomWidgetSettings.RECOVERED;
                break;

            case R.id.deaths:
            default:
                mode = CustomWidgetSettings.DEATHS;
        }
        boolean theme = rgTheme.getCheckedRadioButtonId() != R.id.lightTheme;

        return new CustomWidgetSettings( country, mode, theme );
    }


    /**
     * Check for Internet connectivity.
     *
     * @return true if Internet connectivity is available, false if not.
     */
    public static boolean checkInternet( Context context ) {
        final ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService( Context.CONNECTIVITY_SERVICE );

        if( connectivityManager != null ) {

            final NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

            return networkInfo != null && networkInfo.isConnected();
        } else {
            Log.e( TAG, "No ConnectivityManager available!" );
            Toast.makeText( context, "No Internet connection available!", Toast.LENGTH_SHORT ).show();
            return false;
        }
    }

    private View.OnTouchListener acCountryListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch( View v, MotionEvent event ) {
            if( checkInternet(ActivityWidgetConfigure.this) ) {
                String url = AppWidget.constructURLnDaysBefore( 2 );

                Thread downloader = new AppWidget.MyDownloader( url );
                try {
                    downloader.start();
                    downloader.join();

                } catch( InterruptedException e ) {
                    e.printStackTrace();
                }
            }
            if( AppWidget.csv.equals( "nothing" ) ) {
                Toast.makeText( ActivityWidgetConfigure.this, "Try again later", Toast.LENGTH_SHORT ).show();
                return false;
            }
            String[] lines = AppWidget.csv.split( "\n" );
            List<String> countries = new LinkedList<>();
            for( int i = 1; i < lines.length; i++ ) {
                countries.add( extractLocation( lines[i] ) );
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>( ActivityWidgetConfigure.this,
                    R.layout.simple_1line_dropdown, countries.toArray( new String[]{} ) );
            acCountry.setAdapter( adapter );
            acCountry.showDropDown();
            return false;
        }
    };


    /**
     * Extracts the location of a line of csv got from the github repo by johns hopkins
     * @param line contains csv, ex. lat, lng, deaths, recovers, confirmed, etc…
     * @return the location
     */
    public static String extractLocation( String line ) {
        String country = "";
        if( line.startsWith( "\"" ) )
            country = line.split( "\"" )[ 1 ];
        else
            country = line.split( "," )[ 0 ];
        Log.w( TAG, "CountryB = " + country );
        if( country.isEmpty() ) country = line.split( "," )[ 1 ];
        Log.w( TAG, "CountryA = " + country );

        return country;
    }
}
