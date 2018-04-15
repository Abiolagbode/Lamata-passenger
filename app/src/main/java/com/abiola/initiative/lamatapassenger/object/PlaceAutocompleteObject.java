package com.abiola.initiative.lamatapassenger.object;



public class PlaceAutocompleteObject {
    private String placeId;
    private CharSequence primaryText;
    private CharSequence secondaryText;

    public PlaceAutocompleteObject(String placeId, CharSequence primaryText, CharSequence secondaryText) {
        this.placeId = placeId;
        this.primaryText = primaryText;
        this.secondaryText = secondaryText;
    }

    public String getPlaceId() {
        return placeId;
    }

    public CharSequence getPrimaryText() {
        return primaryText;
    }

    public CharSequence getSecondaryText() {
        return secondaryText;
    }
}
