package com.abiola.initiative.lamatapassenger.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.abiola.initiative.lamatapassenger.R;
import com.abiola.initiative.lamatapassenger.object.TrustedContactObject;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class TrustedContactAdapter extends RecyclerView.Adapter<TrustedContactAdapter.ViewHolder> implements Filterable {

    private Context context;
    private TrustedContactAdapterInterface mListener;
    private ArrayList<TrustedContactObject> resultList = new ArrayList<>();
    private ValueEventListener userRefValueListener;
    private DatabaseReference userRef;
    private int lastPosition = -1;

    public TrustedContactAdapter(Context context, TrustedContactAdapterInterface mListener) {
        this.context = context;
        this.mListener = mListener;
        userRefValueListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                resultList.clear();
                for(DataSnapshot pssngr : dataSnapshot.getChildren()) {
                    if(!pssngr.getKey().equals(FirebaseAuth.getInstance().getUid())) {
                        String name = "";
                        String email = "";
                        String mobnum = "";
                        String profpic = "";
                        for(DataSnapshot pssngerDetails : pssngr.getChildren()) {
                            if(pssngerDetails.getKey().equals("name"))
                                name = pssngerDetails.getValue().toString();

                            if(pssngerDetails.getKey().equals("email"))
                                email = pssngerDetails.getValue().toString();

                            if(pssngerDetails.getKey().equals("mobnum"))
                                mobnum = pssngerDetails.getValue().toString();

                            if(pssngerDetails.getKey().equals("profile_pic"))
                                profpic = pssngerDetails.getValue().toString();
                        }

                        resultList.add(new TrustedContactObject(pssngr.getKey(), name, email, mobnum, profpic));
                        notifyDataSetChanged();
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userRef = FirebaseDatabase.getInstance().getReference("users/passenger");
        getUsers();
    }

    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence charSequence) {
                lastPosition = -1;
                FilterResults results = new FilterResults();
                if(userRefValueListener != null)
                    userRef.removeEventListener(userRefValueListener);
                if(!charSequence.toString().isEmpty()) {
                    resultList = searchUser(charSequence);
                    if(resultList != null) {
                        results.values = resultList;
                        results.count = resultList.size();
                    }
                } else {
                    getUsers();
                    if(resultList != null) {
                        results.values = resultList;
                        results.count = resultList.size();
                    }
                }
                return results;
            }

            @Override
            protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                if (filterResults != null && filterResults.count > 0)
                    notifyDataSetChanged();
            }
        };
    }

    private ArrayList<TrustedContactObject> searchUser(CharSequence filter) {
        ArrayList<TrustedContactObject> filteredUsers = new ArrayList<>();
        for(TrustedContactObject user : resultList) {
            if(user.getName().contains(filter)) {
                filteredUsers.add(user);
            }
        }

        return filteredUsers;
    }


    private void getUsers() {
        if(userRefValueListener != null)
            userRef.removeEventListener(userRefValueListener);
        userRef.addValueEventListener(userRefValueListener);
    }

    @Override
    public TrustedContactAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View convertView = layoutInflater.inflate(R.layout.user_adapter, parent, false);
        ViewHolder mPredictionHolder = new ViewHolder(convertView);
        return mPredictionHolder;
    }

    public interface TrustedContactAdapterInterface {
         void onUserClick(ArrayList<TrustedContactObject> mResultList, int position);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, final int position) {
        if(resultList == null) lastPosition = -1;
        holder.name_txt_vw.setText(resultList.get(position).getName().equals("null") ? "None" : resultList.get(position).getName());
        holder.email_txt_vw.setText(resultList.get(position).getEmail().equals("null") ? "None" : resultList.get(position).getEmail());
        holder.mobnum_txt_vw.setText(resultList.get(position).getMobnum().equals("null") ? "None" : resultList.get(position).getMobnum());
        if(!resultList.get(position).getProf_pic().equals("default"))
            Glide.with(context)
                    .load(resultList.get(position).getProf_pic())
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.profpic_img_vw);
        else
            Glide.with(context)
                    .load(getImage("placeholder"))
                    .apply(RequestOptions.circleCropTransform())
                    .into(holder.profpic_img_vw);
        holder.row_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onUserClick(resultList, position);
            }
        });
        setAnimation(holder.itemView, position);
    }

    @Override
    public int getItemCount() {
        return resultList != null ? resultList.size() : 0;
    }

    public int getImage(String imageName) {
        int drawableResourceId = context.getResources()
                .getIdentifier(imageName, "drawable", context.getPackageName());
        return drawableResourceId;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView name_txt_vw;
        public TextView email_txt_vw;
        public TextView mobnum_txt_vw;
        public ImageView profpic_img_vw;
        public LinearLayout row_layout;

        public ViewHolder(View v) {
            super(v);
            name_txt_vw = v.findViewById(R.id.name_txt_vw);
            email_txt_vw = v.findViewById(R.id.email_txt_vw);
            mobnum_txt_vw = v.findViewById(R.id.mobnum_txt_vw);
            profpic_img_vw = v.findViewById(R.id.profpic_img_vw);
            row_layout = v.findViewById(R.id.row_layout);
        }
    }

    private void setAnimation(View viewToAnimate, int position) {
        if (position > lastPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, android.R.anim.slide_in_left);
            viewToAnimate.startAnimation(animation);
            lastPosition = position;
        }
    }
}
