package com.example.samparka;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class NotificationsAdapter
        extends RecyclerView.Adapter<NotificationsAdapter.ViewHolder> {

    private final List<NotificationModel> list;

    public NotificationsAdapter(List<NotificationModel> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(
            @NonNull ViewHolder holder, int position) {

        NotificationModel n = list.get(position);
        holder.title.setText(n.title);
        holder.message.setText(n.message);

        holder.itemView.setOnClickListener(v -> {
            Context context = v.getContext();

            // 🔴 SAFETY CHECK
            if (n.complaintId == null || n.complaintId.isEmpty()) return;

            Intent intent = new Intent(
                    context,
                    ComplaintDetailsActivity.class
            );

            // 🔑 PASS COMPLAINT ID
            intent.putExtra("complaintId", n.complaintId);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, message;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.txtTitle);
            message = itemView.findViewById(R.id.txtMessage);
        }
    }
}
