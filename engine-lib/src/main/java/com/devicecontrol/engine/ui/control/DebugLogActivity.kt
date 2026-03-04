package com.devicecontrol.engine.ui.control

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.devicecontrol.engine.databinding.ActivityDebugLogBinding
import com.devicecontrol.engine.databinding.ItemDebugLogBinding
import com.devicecontrol.engine.log.DebugLogHolder

/**
 * 通讯调试日志页：展示 [DebugLogHolder] 中缓存的 EngineLog 输出，便于回看连接、收发等逻辑。
 */
class DebugLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDebugLogBinding
    private lateinit var adapter: DebugLogAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDebugLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        adapter = DebugLogAdapter()
        binding.rvLogs.layoutManager = LinearLayoutManager(this)
        binding.rvLogs.adapter = adapter

        loadLogs()

        binding.btnClear.setOnClickListener {
            DebugLogHolder.clear()
            loadLogs()
        }
    }

    private fun loadLogs() {
        val list = DebugLogHolder.getLogs()
        adapter.submitList(list) {
            if (list.isNotEmpty()) binding.rvLogs.smoothScrollToPosition(list.size - 1)
        }
    }
}

private class DebugLogAdapter : ListAdapter<String, DebugLogAdapter.Holder>(DiffCallback) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val b = ItemDebugLogBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Holder(b)
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.binding.tvLogLine.text = getItem(position)
    }

    class Holder(val binding: ItemDebugLogBinding) : RecyclerView.ViewHolder(binding.root)

    private object DiffCallback : DiffUtil.ItemCallback<String>() {
        override fun areItemsTheSame(a: String, b: String) = a === b
        override fun areContentsTheSame(a: String, b: String) = a == b
    }
}
