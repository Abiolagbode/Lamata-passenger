package com.abiola.initiative.lamatapassenger.adapter;

import android.content.Context;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.abiola.initiative.lamatapassenger.R;
import com.abiola.initiative.lamatapassenger.object.PlaceAutocompleteObject;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBufferResponse;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;


public class PlaceAutocompleteAdapter extends RecyclerView.Adapter<PlaceAutocompleteAdapter.ViewHolder>
    implements Filterable {

    private static final CharacterStyle STYLE_BOLD = new StyleSpan(Typeface.BOLD);
    private ArrayList<PlaceAutocompleteObject> mResultList;
    private Context mContext;
    private GeoDataClient mGeoDataClient;
    private LatLngBounds BOUNDS = new LatLngBounds(new LatLng(0,0), new LatLng(0,0));
    private AutocompleteFilter mPlaceFilter;
    private PlaceAutoCompleteInterface mListener;

    public PlaceAutocompleteAdapter(Context mContext, GeoDataClient mGeoDataClient, AutocompleteFilter mPlaceFilter) {
        this.mContext = mContext;
        this.mGeoDataClient = mGeoDataClient;
        this.mPlaceFilter = mPlaceFilter;
        this.mListener = (PlaceAutoCompleteInterface) mContext;
    }

    public interface PlaceAutoCompleteInterface {
        public void onPlaceClick(ArrayList<PlaceAutocompleteObject> mResultList, int position);
    }

    public void clearList(){
        if(mResultList != null && mResultList.size() > 0){ mResultList.clear(); }
        notifyDataSetChanged();
    }

    @Override
    public PlaceAutocompleteAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = layoutInflater.inflate(R.layout.place_adapter, parent, false);
        ViewHolder mPredictionHolder = new ViewHolder(convertView);
        return mPredictionHolder;
    }

    @Override
    public void onBindViewHolder(PlaceAutocompleteAdapter.ViewHolder holder, final int position) {
        holder.placesTxtVw.setText(mResultList.get(position).getPrimaryText());
        holder.addressTxtVw.setText(mResultList.get(position).getSecondaryText());
        holder.placesTxtVw.setSelected(true);
        holder.addressTxtVw.setSelected(true);
        holder.rowRecVw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onPlaceClick(mResultList, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mResultList != null ? mResultList.size() : 0;
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                FilterResults results = new FilterResults();
                if (constraint != null) {
                    mResultList = getPredictions(constraint);
                    if (mResultList != null) {
                        results.values = mResultList;
                        results.count = mResultList.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                if (results != null && results.count > 0) {
                    notifyDataSetChanged();
                } else { }
            }
        };
    }

    private ArrayList<PlaceAutocompleteObject> getPredictions(CharSequence s) {
        Task<AutocompletePredictionBufferResponse> results =
                mGeoDataClient.getAutocompletePredictions(s.toString(), BOUNDS,
                        mPlaceFilter);

        try { Tasks.await(results, 60, TimeUnit.SECONDS); }
        catch (ExecutionException | InterruptedException | TimeoutException e) { e.printStackTrace(); }

        try {
            final AutocompletePredictionBufferResponse autocompletePredictions = results.getResult();
            results.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    autocompletePredictions.release();
                    autocompletePredictions.close();
                }
            });

            if(autocompletePredictions.isClosed()) {
                return null;
            } else {
                Iterator<AutocompletePrediction> iterator = autocompletePredictions.iterator();
                ArrayList resultList = new ArrayList<>(autocompletePredictions.getCount());
                while (iterator.hasNext()) {
                    AutocompletePrediction prediction = iterator.next();
                    resultList.add(new PlaceAutocompleteObject(prediction.getPlaceId(),
                            prediction.getPrimaryText(STYLE_BOLD), prediction.getSecondaryText(STYLE_BOLD)));
                }
                autocompletePredictions.release();

                return resultList;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView placesTxtVw;
        public TextView addressTxtVw;
        public RelativeLayout rowRecVw;

        public ViewHolder(View v) {
            super(v);
            placesTxtVw = v.findViewById(R.id.placesTxtVw);
            addressTxtVw = v.findViewById(R.id.addressTxtVw);
            rowRecVw = v.findViewById(R.id.rowRecVw);
        }
    }
}
