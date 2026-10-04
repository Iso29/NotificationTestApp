package com.example.notificationtestapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.notificationtestapp.databinding.ActivityMainBinding
import com.example.notificationtestapp.databinding.ActivityWorkerBinding
import java.util.concurrent.TimeUnit

class WorkerActivity : AppCompatActivity() {
    private lateinit var binding: ActivityWorkerBinding
    private val workManager by lazy { WorkManager.getInstance(applicationContext) }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWorkerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.btnUpload.setOnClickListener { startUpload("file_42") }
        binding.btnCancel.setOnClickListener {
//            workManager.cancelUniqueWork(UPLOAD_WORK)
            val intent = Intent(this@WorkerActivity,MainActivity::class.java)
            startActivity(intent)
            finish()
        }

        observeUpload()
    }

    private fun startUpload(fileId: String) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val request = OneTimeWorkRequestBuilder<UploadWorker>()
            .setInputData(workDataOf(UploadWorker.KEY_FILE_ID to fileId))
            .setConstraints(constraints)
            .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 10, TimeUnit.SECONDS)
            .addTag("upload")
            .build()

        // Unique work: tapping Upload twice won't create two parallel uploads
        workManager.enqueueUniqueWork(UPLOAD_WORK, ExistingWorkPolicy.REPLACE, request)
    }

    private fun observeUpload() {
        workManager.getWorkInfosForUniqueWorkLiveData(UPLOAD_WORK)
            .observe(this) { infos ->
                val info = infos.firstOrNull() ?: return@observe
                when (info.state) {
                    WorkInfo.State.ENQUEUED -> binding.tvStatus.text = "Waiting for network…"
                    WorkInfo.State.RUNNING -> {
                        val p = info.progress.getInt(UploadWorker.KEY_PROGRESS, 0)
                        binding.progressBar.progress = p
                        binding.tvStatus.text = "Uploading $p%"
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        binding.progressBar.progress = 100
                        binding.tvStatus.text =
                            info.outputData.getString(UploadWorker.KEY_RESULT_URL)
                    }
                    WorkInfo.State.FAILED -> binding.tvStatus.text = "Upload failed"
                    WorkInfo.State.CANCELLED -> binding.tvStatus.text = "Cancelled"
                    WorkInfo.State.BLOCKED -> Unit
                }
            }
    }

    companion object {
        private const val UPLOAD_WORK = "upload_work"
    }
}