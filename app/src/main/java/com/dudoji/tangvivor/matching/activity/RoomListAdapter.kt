import android.util.Log
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.dudoji.tangvivor.R
import com.dudoji.tangvivor.matching.activity.RoomListActivity
import com.dudoji.tangvivor.matching.entity.Room
import com.dudoji.tangvivor.repository.GameRepository
import com.dudoji.tangvivor.repository.RoomRepository
import com.dudoji.tangvivor.repository.UserRepository
import kotlinx.coroutines.launch

class RoomListAdapter(val roomList: List<Room>, val activity: RoomListActivity) : RecyclerView.Adapter<RoomListAdapter.RoomViewHolder>() {

    class RoomViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView = itemView.findViewById<android.widget.TextView>(R.id.item_name)
        val makerTextView = itemView.findViewById<android.widget.TextView>(R.id.item_maker)
    }

    override fun onCreateViewHolder(parent: android.view.ViewGroup, viewType: Int): RoomViewHolder {
        val itemView = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.game_item, parent, false)
        return RoomViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: RoomViewHolder, position: Int) {
        val room = roomList[position]
        holder.nameTextView.text = room.name
        activity.lifecycleScope.launch {
            holder.makerTextView.text = UserRepository.getUser(room.user1!!).name
        }

        holder.itemView.setOnClickListener {
            RoomRepository.db.collection(RoomRepository.COLLECTION_NAME)
                .document(room.name!!)
                .update("user2", UserRepository.me?.id)
                .addOnSuccessListener {
                    Toast.makeText(
                        holder.itemView.context,
                        "Joined room: ${room.name}",
                        Toast.LENGTH_SHORT
                    ).show()
                    RoomRepository.db.collection(RoomRepository.COLLECTION_NAME)
                        .document(room.name!!)
                        .delete()
                    GameRepository.enterGame(room.name!!, activity, 2)
                }
                .addOnFailureListener { e ->
                    Log.d("RoomListAdapter", "Error joining room: ", e)
                    Toast.makeText(
                        holder.itemView.context,
                        "Failed to join room",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun getItemCount(): Int {
        return roomList.size
    }
}