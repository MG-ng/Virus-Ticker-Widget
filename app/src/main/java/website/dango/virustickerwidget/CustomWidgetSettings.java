package website.dango.virustickerwidget;

import website.dango.virustickerwidget.R;

public class CustomWidgetSettings {

    public static final int CONFIRMED = R.string.confirmed, DEATHS = R.string.deaths, RECOVERED = R.string.recovered;

    private String country;
    private int mode;
    private boolean darkTheme;

    public CustomWidgetSettings( String country, int mode, boolean darkTheme ) {
        this.country = country;
        this.mode = mode;
        this.darkTheme = darkTheme;
    }

    public String getCountry() {
        return country;
    }

    public int getMode() {
        return mode;
    }

    public boolean isDarkTheme() {
        return darkTheme;
    }
}
