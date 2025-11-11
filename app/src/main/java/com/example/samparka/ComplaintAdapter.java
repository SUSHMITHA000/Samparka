package com.example.samparka;

// File: app/src/main/java/<your_package>/ComplaintAdapter.java

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ComplaintAdapter extends RecyclerView.Adapter<ComplaintAdapter.ViewHolder> {
    private List<Complaint> complaints;

    public ComplaintAdapter(List<Complaint> complaints) {
        this.complaints = complaints;
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
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_complaint, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Complaint c = complaints.get(position);
        holder.imgComplaint.setImageResource(c.imageResId);
        holder.txtTitle.setText(c.title);
        holder.txtDescription.setText(c.description);
        holder.txtLocation.setText(c.location);
        holder.txtStatus.setText(c.status);
        holder.txtDate.setText(c.date);

        // Status indicator background (add drawables for each status: status_pending, etc.)
        if (c.status.equalsIgnoreCase("Pending")) {
            holder.txtStatus.setBackgroundResource(R.drawable.status_pending);
        } else if (c.status.equalsIgnoreCase("Completed") || c.status.equalsIgnoreCase("Done")) {
            holder.txtStatus.setBackgroundResource(R.drawable.status_completed);
        } else if (c.status.equalsIgnoreCase("In Progress") || c.status.equalsIgnoreCase("Progress")) {
            holder.txtStatus.setBackgroundResource(R.drawable.status_inprogress);
        }
    }

    @Override
    public int getItemCount() {
        return complaints.size();
    }
}

