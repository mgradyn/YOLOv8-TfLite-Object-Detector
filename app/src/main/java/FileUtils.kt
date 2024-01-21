import android.content.Context
import android.content.Intent
import android.location.Location
import androidx.core.content.FileProvider
import com.surendramaran.yolov8tflite.entities.Tree
import com.surendramaran.yolov8tflite.fragments.TreeViewModel
import com.surendramaran.yolov8tflite.model.Count
import java.io.File

class FileUtils {
    companion object {
        fun generateFile(context: Context, fileName: String): File? {
            val csvFile = File(context.filesDir, fileName)
            csvFile.createNewFile()

            return if (csvFile.exists()) {
                csvFile
            } else {
                null
            }
        }

        fun goToFileIntent(context: Context, file: File): Intent {
            val intent = Intent(Intent.ACTION_VIEW)
            val contentUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val mimeType = context.contentResolver.getType(contentUri)
            intent.setDataAndType(contentUri, mimeType)
            intent.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

            return intent
        }

        fun saveTree(enteredName: String, location: Location, totalCount:MutableMap<String, Count>, treeViewModel: TreeViewModel) {
            val newTree = Tree(
                name = enteredName,
                latitude = location.latitude,
                longitude = location.longitude,
                isUploaded = false,
                ripe = totalCount["ripe"]?.count ?: 0,
                underripe = totalCount["underripe"]?.count ?: 0,
                unripe = totalCount["unripe"]?.count ?: 0,
                flower = totalCount["flower"]?.count ?: 0,
                abnromal = totalCount["abnormal"]?.count ?: 0,
                total = totalCount.values.sumOf { it.count }
            )
            treeViewModel.insert(newTree)
        }
    }
}