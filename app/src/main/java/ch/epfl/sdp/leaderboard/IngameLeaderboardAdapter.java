package ch.epfl.sdp.leaderboard;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import ch.epfl.sdp.R;
import ch.epfl.sdp.entity.Player;
import ch.epfl.sdp.entity.PlayerManager;

public class IngameLeaderboardAdapter extends RecyclerView.Adapter<IngameLeaderboardAdapter.IngameLeaderboardViewHolder> {

    @NonNull
    @Override
    public IngameLeaderboardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_ingame_leaderboard_listitem, parent, false);
        return new IngameLeaderboardViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngameLeaderboardViewHolder holder, int position) {
        Player player = PlayerManager.getInstance().getPlayersSortByIngameScore().get(position);

        holder.ingameRanking.setText(String.valueOf(position + 1));
        holder.username.setText(player.getUsername());
        holder.ingameScore.setText(String.valueOf(player.getCurrentGameScore()));
        if (player.getHealthPoints() == 0) {
            holder.isAlive.setText("DEAD");
        } else {
            holder.isAlive.setText("ALIVE");
        }
    }

    @Override
    public int getItemCount() {
        return PlayerManager.getInstance().getPlayersSortByIngameScore().size();
    }

    public class IngameLeaderboardViewHolder extends RecyclerView.ViewHolder {
        TextView ingameRanking;
        TextView username;
        TextView ingameScore;
        TextView isAlive;

        public IngameLeaderboardViewHolder(@NonNull View itemView) {
            super(itemView);
            ingameRanking = itemView.findViewById(R.id.ingame_leaderboard_ranking);
            username = itemView.findViewById(R.id.ingame_leaderboard_username);
            ingameScore = itemView.findViewById(R.id.ingame_leaderboard_score);
            isAlive = itemView.findViewById(R.id.ingame_leaderboard_isalive);
        }
    }
}