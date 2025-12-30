package com.example.samparka;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.samparka.Complaint;
import com.example.samparka.ComplaintDetailsActivity;

import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder> {

    Context context;
    List<Complaint> complaints;

    public ComplaintAdapter(Context context, List<Complaint> complaints) {
        this.context = context;
        this.complaints = complaints;
    }

    public void updateList(List<Complaint> newComplaints) {
        complaints = newComplaints;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgComplaint;
        TextView txtTitle, txtDescription, txtLocation, txtStatus, txtDate;

        public ViewHolder(View itemView) {
            super(itemView);

            imgComplaint = itemView.findViewById(R.id.imgComplaint);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtLocation = itemView.findViewById(R.id.txtLocation);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            txtDate = itemView.findViewById(R.id.txtDate);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_complaint, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Complaint c = complaints.get(position);

        // Image
        if (c.imageUrl != null && !c.imageUrl.isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(c.imageUrl)
                    .placeholder(R.drawable.ic_placeholder)
                    .into(holder.imgComplaint);
        } else {
            holder.imgComplaint.setImageResource(R.drawable.ic_placeholder);
        }


        // Texts
        holder.txtTitle.setText(c.title);
        holder.txtDescription.setText(c.description);
        holder.txtLocation.setText(c.location);
        holder.txtStatus.setText(c.status);
        holder.txtDate.setText(c.date);

        // Status color
        if (c.status.equalsIgnoreCase("Pending")) {
            holder.txtStatus.setBackgroundResource(R.drawable.status_pending);
        } else if (c.status.equalsIgnoreCase("Completed")) {
            holder.txtStatus.setBackgroundResource(R.drawable.status_completed);
        } else if (c.status.equalsIgnoreCase("In Progress")) {
            holder.txtStatus.setBackgroundResource(R.drawable.status_inprogress);
        }

        // --------------------- CLICK → OPEN DETAILS PAGE ---------------------
        holder.itemView.setOnClickListener(v -> {

            Intent intent = new Intent(context, ComplaintDetailsActivity.class);

            intent.putExtra("title", c.title);
            intent.putExtra("description", c.description);
            intent.putExtra("location", c.location);
            intent.putExtra("status", c.status);
            intent.putExtra("date", c.date);
            intent.putExtra("imageUrl", c.imageUrl);
            intent.putExtra("documentId", c.documentId);

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return complaints.size();
    }
}
