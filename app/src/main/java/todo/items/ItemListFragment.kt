package ro.ubbcluj.cs.ilazar.myapp2.todo.items

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_item_list.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import ro.ubbcluj.cs.ilazar.myapp2.R
import ro.ubbcluj.cs.ilazar.myapp2.auth.data.AuthRepository
import ro.ubbcluj.cs.ilazar.myapp2.core.TAG
import todo.data.ItemServerRepository

class ItemListFragment : Fragment() {
    companion object{
        var listenForNotifications = false
    }
    private lateinit var itemListAdapter: ItemListAdapter
    private lateinit var itemsModel: ItemListViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.v(TAG, "onCreate")
    }

    override fun onStart() {
        super.onStart()
        listenForNotifications = true
        CoroutineScope(Dispatchers.Main).launch { ItemServerRepository.receiveNotifications() }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_item_list, container, false)
    }

    override fun onStop() {
        super.onStop()
        listenForNotifications = false
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.v(TAG, "onActivityCreated")
        if (!AuthRepository.isLoggedIn) {
            findNavController().navigate(R.id.fragment_login)
            return;
        }
        setupItemList()
        fab.setOnClickListener {
            Log.v(TAG, "add new item")
            findNavController().navigate(R.id.action_ItemListFragment_to_ItemEditFragment)
        }
    }

    private fun setupItemList() {
        itemListAdapter = ItemListAdapter(this)
        item_list.adapter = itemListAdapter
        itemsModel = ViewModelProvider(this).get(ItemListViewModel::class.java)
        itemsModel.items.observe(viewLifecycleOwner, { items ->
            Log.v(TAG, "update items")
            itemListAdapter.items = items
        })
        itemsModel.loading.observe(viewLifecycleOwner, { loading ->
            Log.i(TAG, "update loading")
            progress.visibility = if (loading) View.VISIBLE else View.GONE
        })
        itemsModel.loadingError.observe(viewLifecycleOwner, { exception ->
            if (exception != null) {
                Log.i(TAG, "update loading error")
                val message = "Loading exception ${exception.message}"
                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show()
            }
        })
        itemsModel.refresh()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.v(TAG, "onDestroy")
    }
}