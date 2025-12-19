package com.example.samparka;

import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CommunityUpdateAdapter extends RecyclerView.Adapter<CommunityUpdateAdapter.UpdateViewHolder> {

    private List<CommunityUpdate> updateList;

    public CommunityUpdateAdapter(List<CommunityUpdate> updateList) {
        this.updateList = updateList;
    }

    @NonNull
    @Override
    public UpdateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_community_update, parent, false);
        return new UpdateViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UpdateViewHolder holder, int position) {
        CommunityUpdate update = updateList.get(position);
        holder.title.setText(update.getTitle());
        holder.message.setText(update.getMessage());

        // Format timestamp
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(update.getTimestamp());
        String date = DateFormat.format("dd-MM-yyyy hh:mm a", cal).toString();
        holder.time.setText(date);
    }

    @Override
    public int getItemCount() {
        return updateList.size();
    }

    public static class UpdateViewHolder extends RecyclerView.ViewHolder {
        TextView title, message, time;

        public UpdateViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.updateTitle);
            message = itemView.findViewById(R.id.updateMessage);
            time = itemView.findViewById(R.id.updateTime);
        }
    }
}
