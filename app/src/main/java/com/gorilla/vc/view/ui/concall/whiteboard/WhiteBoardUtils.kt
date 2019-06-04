package com.gorilla.vc.view.ui.concall.whiteboard

import android.app.AlertDialog
import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.widget.Toast
import com.gorilla.vc.R
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * Simple utility class for whiteboard feature
 */
@Suppress("KDocUnresolvedReference")
object WhiteBoardUtils {

    /**
     * Generates name for whiteboard.
     *
     *
     * Ensures that whiteboard names generated within 5 minutes of each other are same. The format
     * of the name is:
     * board_{day_of_month}_{hour_of_day}_{minute_divided_by_5}
     *
     * @return The generated whiteboard name
     */
    /*
    fun genWhiteboardName(): String {
        val timeStamp = SimpleDateFormat("dd_HH_", Locale.US).format(Calendar.getInstance().time)
        val minute = Calendar.getInstance().time.minutes / 10
        return "board_$timeStamp$minute"
    }
    */

    /**
     * Function to generate random name for the user.
     *
     *
     * THe name will include lower and upper case alphabets.
     *
     * @return the generated username
     */
    /*
    fun generateRandomName(): String {
        val seed = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val length = 6
        val sb = StringBuilder()
        for (i in 0 until length) {
            val random = (Math.random() * seed.length).toInt()
            sb.append(seed[random])
        }
        return sb.toString()
    }
    */

    /**
     * Function to return a random english name from a predefined set.
     *
     * @param context the current context
     * @return the random name
     */
    /*
    fun generateEnglishNames(context: Context): String {
        val englishNames = context.resources.getStringArray(R.array.english_names)
        val length = englishNames.size
        val randomIndex = (Math.random() * length.toFloat()).toInt()
        return englishNames[randomIndex]
    }
    */

    /**
     * Function to confirm that the user want's to save the current whiteboard state as a image in
     * phone gallery
     *
     * @param context the current activity context
     * @param outputStream    the image represented as a Byte Array Output Stream
     */
    fun saveWhiteboardImage(context: Context, outputStream: ByteArrayOutputStream) {
        AlertDialog.Builder(context)
                .setIcon(R.mipmap.ic_dialog_alert)
                .setTitle(context.getString(R.string.confirm_save))
                .setMessage(context.getString(R.string.save_canvas_message))
                .setPositiveButton(context.getString(R.string.confirm)) { _, _ ->
                    val date = Date()
                    val formatter = SimpleDateFormat("yyyy_MM_dd_hh-mm-ss", Locale.US)
                    val fileName = formatter.format(date) + ".png"
                    if (android.os.Environment.getExternalStorageState() == android.os.Environment.MEDIA_MOUNTED) {
                        val sdCard = Environment.getExternalStorageDirectory()
                        val dir = File(sdCard.absolutePath + "/VC_Whiteboard")
                        dir.mkdirs()

                        val file = File(dir, fileName)

                        val f: FileOutputStream
                        try {
                            f = FileOutputStream(file)
                            f.write(outputStream.toByteArray())
                            f.flush()
                            f.close()
                            Toast.makeText(context,
                                    String.format(context.getString(R.string.save_canvas_path), file.absolutePath),
                                    Toast.LENGTH_SHORT).show()
                            // Trigger gallery refresh on photo save
                            MediaScannerConnection.scanFile(
                                    context,
                                    arrayOf(file.toString()),
                                    null
                            ) { _/*path*/, _/*uri*/ -> }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, context.getString(R.string.save_canvas_fail),
                                    Toast.LENGTH_SHORT).show()
                        }

                    }
                }
                .setNegativeButton(context.getString(R.string.cancel), null)
                .show()
    }

    /**
     * Setup an in-memory KeyChain with a default identity.
     *
     * @return keyChain object
     * @throws net.named_data.jndn.security.SecurityException
     */
    /*
    @Throws(net.named_data.jndn.security.SecurityException::class)
    fun buildTestKeyChain(): KeyChain {
        val identityStorage = MemoryIdentityStorage()
        val privateKeyStorage = MemoryPrivateKeyStorage()
        val identityManager = IdentityManager(identityStorage, privateKeyStorage)
        val keyChain = KeyChain(identityManager)
        try {
            keyChain.getDefaultCertificateName()
        } catch (e: net.named_data.jndn.security.SecurityException) {
            keyChain.createIdentity(Name("/test/identity"))
            keyChain.getIdentityManager().setDefaultIdentity(Name("/test/identity"))
        }

        return keyChain
    }
    */
}