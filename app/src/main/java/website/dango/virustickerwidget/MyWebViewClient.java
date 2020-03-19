package website.dango.virustickerwidget;


import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import java.util.List;


public class MyWebViewClient extends WebViewClient {

    private static final String TAG = "MyWebViewClient";


    // shouldOverrideUrlLoading makes this `WebView` the default handler for URLs inside the website, so that links are not kicked out to other website.
    @SuppressWarnings("deprecation")
    @Override
    public boolean shouldOverrideUrlLoading( WebView view, String url) {
        Log.d( TAG, url );

        if (url.startsWith("mailto:")) {
            //set the main emailIntent to ACTION_SEND for looking for applications that share information
            Uri email = Uri.parse( url );

            Intent emailIntent = new Intent( Intent.ACTION_VIEW, email );

            //all the extras that will be passed to the email website
            emailIntent.putExtra( Intent.EXTRA_CC, new String[]{ "markus@gockel.co" } );

            // Verify it resolves
            PackageManager packageManager = view.getContext().getPackageManager();
            List<ResolveInfo> activities = packageManager.queryIntentActivities( emailIntent, 0 );
            boolean isIntentSafe = activities.size() > 0;

            // Start an activity if it's safe
            if (isIntentSafe) {
                view.getContext().startActivity( Intent.createChooser( emailIntent, "Sending Feedback using..." ) );
            }
        } else if (url.startsWith("tel:")) {
            //Handle telephony Urls
            view.getContext().startActivity( new Intent( Intent.ACTION_DIAL, Uri.parse( url ) ) );
        } else {
            // TODO: Get all postproviders
            // if( Uri.parse( url ).getHost().equals( any postprovider Ressource urls ) ) {

            // ; let my WebView load the page
            view.loadUrl( url );
            return false;
        }
        // Otherwise, the link is not for a page on my site, so launch another Activity that handles URLs
        Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( url ) );
        view.getContext().startActivity( intent );
        return true;
    }

    @TargetApi( Build.VERSION_CODES.N )
    @Override
    public boolean shouldOverrideUrlLoading( WebView view, WebResourceRequest request ) {

        return shouldOverrideUrlLoading( view, request.getUrl().getPath() );
    }

    @Override
    public void onPageFinished( WebView view, String url ) {

    }

}
