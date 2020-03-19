package website.dango.virustickerwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import static website.dango.virustickerwidget.ActivityWeb.URL_RES_ID_KEY;

/**
 * Implementation of App Widget functionality.
 */
public class AppWidget extends AppWidgetProvider {
    private static String TAG = "AppWidget";


    @Override
    public void onUpdate( Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds ) {
        Log.w( TAG, "Update" );

        // No Internet connection
        if( !ActivityWidgetConfigure.checkInternet( context ) ) return;

        String url = constructURLnDaysBefore( 1 );

        Thread downloader1 = new MyDownloader( url );
        try {
            downloader1.start();
            downloader1.join();
        } catch( InterruptedException e ) {
            Log.w( TAG, "1" );
            e.printStackTrace();
        }
        Log.w( TAG, "2" );

        if( csv.equals( "nothing" ) ) {
            url = constructURLnDaysBefore( 2 );
            Thread downloader2 = new MyDownloader( url );
            try {
                downloader2.start();
                downloader2.join();
            } catch( InterruptedException e ) {
                Log.w( TAG, "3" );
                e.printStackTrace();
            }

            if( csv.equals( "nothing" ) ) {
                Toast.makeText( context, "Try again later", Toast.LENGTH_SHORT ).show();
                Log.e( TAG, "Should display \"try again later\"" );
                return;
            }
            Log.w( TAG, "4" );
        }

        // There may be multiple widgets active, so update all of them
        for( int appWidgetId : appWidgetIds ) {
            updateAppWidget( context, appWidgetManager, appWidgetId );
        }
    }


    @Override
    public void onDeleted( Context context, int[] appWidgetIds ) {
        // When the user deletes the widget_content, delete the preference associated with it.
        for( int appWidgetId : appWidgetIds ) {
            ActivityWidgetConfigure.deleteWidgetSettings( context, appWidgetId );
        }
    }

    @Override
    public void onEnabled( Context context ) {
        // Enter relevant functionality for when the first widget_content is created
        Log.w( TAG, "Enabled" );
    }

    @Override
    public void onDisabled( Context context ) {
        // Enter relevant functionality for when the last widget_content is disabled
    }

    private static CustomWidgetSettings settings;

    public static void updateAppWidget( Context context, AppWidgetManager appWidgetManager,
                                        int appWidgetId ) {
        settings = ActivityWidgetConfigure.loadSettingsPref( context, appWidgetId );
        if( settings == null )
            return;

        int amount = -1;

        String[] lines = csv.split( "\n" );
        for( String line : lines ) {

            if( ActivityWidgetConfigure.extractLocation( line ).equals( settings.getCountry() ) ) {
                switch( settings.getMode() ) {
                    case R.string.deaths:
                        amount = Integer.parseInt( getValue( lines[ 0 ], "Deaths", line ) );
                        break;
                    case R.string.recovered:
                        amount = Integer.parseInt( getValue( lines[ 0 ], "Recovered", line ) );
                        break;
                    case R.string.confirmed:
                        amount = Integer.parseInt( getValue( lines[ 0 ], "Confirmed", line ) );
                        break;
                    default:
                        amount = -1;
                }
            }
        }
        if( amount == -1 )
            Toast.makeText( context, "Not a country\nDelete the widget yourself", Toast.LENGTH_SHORT ).show();

        updateWidgetViews( context, appWidgetManager, appWidgetId, amount );
    }

    private static String getValue( String indexLine, String column, String csv ) {
        String[] indexes = indexLine.split( "," );
        int i = 0;
        while( !indexes[ i ].equals( column ) ) i++;
        String cases = csv.split( "," )[ i ];
        Log.w( TAG, "Cases: " + cases + " at line: " + csv );
        return cases;
    }

    private static void updateWidgetViews( Context context, AppWidgetManager appWidgetManager,
                                           int appWidgetId, int amount ) {

        if( settings == null )
            return;

        // Construct the RemoteViews object
        RemoteViews views;
        if( settings.isDarkTheme() ) {
            views = new RemoteViews( context.getPackageName(), R.layout.initial_widget_layout_dark );
        } else {
            views = new RemoteViews( context.getPackageName(), R.layout.initial_widget_layout_light );
        }


        Intent webIntent = new Intent( context, ActivityWeb.class );
        webIntent.putExtra( URL_RES_ID_KEY, "https://gisanddata.maps.arcgis.com/apps/opsdashboard/index.html#/85320e2ea5424dfaaa75ae62e5c06e61" );
        PendingIntent pendingIntent = PendingIntent.getActivity( context, 0, webIntent, 0 );

        views.setTextViewText( R.id.country, settings.getCountry() );
        views.setOnClickPendingIntent( R.id.country, pendingIntent );

        views.setTextViewText( R.id.mode, context.getString( settings.getMode() ) );

        views.setTextViewText( R.id.amount, amount + "" );
        views.setOnClickPendingIntent( R.id.amount, getPendingSelfIntent( context, onUpdateTag, appWidgetId ) );

        Log.w( TAG, "Wanna update: " + settings.getCountry() + " - " + amount );

        // Instruct the widget_content manager to update the widget_content

        appWidgetManager.updateAppWidget( appWidgetId, views );
    }

    private static final String appWidgetIdTAG = "TAG for the AppWidgetId";
    private static final String onUpdateTag = "Update the widget with a click on the amount";
    protected static PendingIntent getPendingSelfIntent( Context context, String action, int appWidgetId ) {
        Intent intent = new Intent( context, AppWidget.class );
        intent.setAction( action );
        intent.putExtra( appWidgetIdTAG, appWidgetId );
        return PendingIntent.getBroadcast( context, 0, intent, 0 );
    }

    /**
     * This hack is needed because normal onClick is not supported in widgets
     * This function should update the number in the widget
     * @param context is needed for the {@link AppWidgetManager}
     * @param intent is needed to get the action (Every activity can send a broadcast)
     */
    public void onReceive( Context context, Intent intent ) {
        if( onUpdateTag.equals( intent.getAction() ) ) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance( context );
            onUpdate( context, appWidgetManager,
                    appWidgetManager.getAppWidgetIds( new ComponentName( context, AppWidget.class.getName() ) ) );
        }
    };


    public static String csv = "";

    public static class MyDownloader extends Thread {
        private final String url;
        MyDownloader( String url ) {
            this.url = url;
        }

        @Override
        public void run() {

            final long start = System.currentTimeMillis();
            StringBuilder content = new StringBuilder( "nothing" );
            InputStream is = null;

            try {
                URL validURL = new URL( url );
                Log.w( TAG, "URL = " + url );

                is = validURL.openStream();  // throws an IOException
                BufferedReader br = new BufferedReader( new InputStreamReader( is ) );
                String line;

                while( (line = br.readLine()) != null ) {
                    content.append( line );
                    content.append( "\n" );
                }
            } catch( MalformedURLException e ) {
                Log.e( TAG, "Malformed URL Exception! " + Log.getStackTraceString( e ) );
            } catch( IOException e ) {
                Log.e( TAG, "IOException !" + Log.getStackTraceString( e ) );
            } finally {
                try {
                    if( is != null ) is.close();
                } catch( IOException ioe ) {
                    // nothing to see here
                }
                Log.d( TAG, "Request took: " + (System.currentTimeMillis() - start) );
            }
            Log.w( TAG, "Got from url: " + content );
            csv = content.toString();
        }
    }

    public static String constructURLnDaysBefore( int n ) {
        String base_url = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_daily_reports/";
        String suffix = ".csv";

        int month = Calendar.getInstance().get( Calendar.MONTH );
        int day = Calendar.getInstance().get( Calendar.DAY_OF_MONTH );
        int year = Calendar.getInstance().get( Calendar.YEAR );

        // Will probably not work on the first day of the month
        return base_url + twoDigits( month + 1 ) + "-" + twoDigits( day - n ) + "-" + year + suffix;
    }

    private static String twoDigits( int number ) {
        if( number >= 100 )
            Log.e( TAG, "number too big" );
        else if( number < 0 )
            Log.e( TAG, "number too small" );
        else if( number < 10 )
            return "0" + number;
        else
            return number + "";
        return "00";
    }
}
