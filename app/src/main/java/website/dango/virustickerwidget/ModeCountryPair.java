package website.dango.virustickerwidget;

import java.io.Serializable;

public class ModeCountryPair implements Serializable {

    private String country;
    private int mode;


    public ModeCountryPair( String country, int mode ) {
        this.country = country;
        this.mode = mode;
    }

    public String getCountry() {
        return country;
    }

    public int getMode() {
        return mode;
    }
}
