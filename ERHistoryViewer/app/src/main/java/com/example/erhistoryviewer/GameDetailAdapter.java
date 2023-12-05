package com.example.erhistoryviewer;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class GameDetailAdapter extends RecyclerView.Adapter<GameDetailAdapter.ViewHolder>{
    ArrayList<UserGame> items = new ArrayList<UserGame>();

    @NonNull
    @Override
    public GameDetailAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(viewGroup.getContext());
        View itemView = inflater.inflate(R.layout.frg_userinfo, viewGroup, false);

        return new GameDetailAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull GameDetailAdapter.ViewHolder viewHolder, int position) {
        UserGame item = items.get(position);
        viewHolder.setItem(item);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public void addItem(UserGame item) {
        items.add(item);
    }

    public void setItems(ArrayList<UserGame> items) {
        this.items = items;
    }

    public UserGame getItem(int position) {
        return items.get(position);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public ViewHolder(View itemView) {
            super(itemView);

            textView = itemView.findViewById(R.id.txt_username);
        }

        public void setItem(UserGame item) {
            textView.setText(item.nickname);
        }

    }
}
