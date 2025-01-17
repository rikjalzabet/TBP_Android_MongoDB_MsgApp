package hr.foi.msgappmongodb.Helpers

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.IOException

class HelperChatClass {
    fun decodeBase64ToBitmap(base64: String): Bitmap {
        val decodedString = Base64.decode(base64, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
    }
    fun convertImageToBase64(imageUri: Uri, contentResolver: ContentResolver): String? {
        return try {
            // Decode the image from URI into a Bitmap
            val inputStream = contentResolver.openInputStream(imageUri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()

            // Compress the Bitmap to reduce its size
            val byteArrayOutputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, byteArrayOutputStream) // Adjust quality as needed
            val compressedByteArray = byteArrayOutputStream.toByteArray()
            byteArrayOutputStream.close()

            // Convert the compressed byte array to Base64
            val base64String = Base64.encodeToString(compressedByteArray, Base64.DEFAULT)

            // Ensure the string is under 10MB
            if (base64String.length > 10 * 1024 * 1024) {
                throw Exception("Compressed image exceeds 10MB")
            }

            base64String
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    fun encodeImageToBase64(image: Bitmap): String {
        val byteArrayOutputStream = ByteArrayOutputStream()
        image.compress(Bitmap.CompressFormat.JPEG, 5, byteArrayOutputStream)
        val byteArray = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.NO_WRAP)
    }

}