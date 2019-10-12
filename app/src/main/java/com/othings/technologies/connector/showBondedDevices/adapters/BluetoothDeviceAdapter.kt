package com.othings.technologies.connector.showBondedDevices.adapters

import android.bluetooth.BluetoothDevice
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView
import com.othings.technologies.connector.R

class BluetoothDeviceAdapter(private val items : MutableList<BluetoothDevice>) : RecyclerView.Adapter<BluetoothDeviceAdapter.ViewHolder>() {

    private var onClickListener:MutableLiveData<Int> = MutableLiveData()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val inflater = LayoutInflater.from(parent.context)
        val view = inflater.inflate(R.layout.bluetooth_device_item,parent,false)
        return ViewHolder(view)

    }

    override fun getItemCount(): Int {

        return items.size

    }

    fun setOnClickItemListener():MutableLiveData<Int>{

        return onClickListener

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        holder.bind(items.get(position))

    }


    inner class ViewHolder( itemView: View) : RecyclerView.ViewHolder(itemView) {

        private var name: TextView = itemView.findViewById(R.id.name)
        private var macAddress: TextView = itemView.findViewById(R.id.macAddress)
        private var box:LinearLayout = itemView.findViewById(R.id.box)

        fun bind(item:BluetoothDevice){

            name.text = item.name
            macAddress.text = item.address
            box.setOnClickListener {

                onClickListener.value = adapterPosition

            }

        }

    }

}