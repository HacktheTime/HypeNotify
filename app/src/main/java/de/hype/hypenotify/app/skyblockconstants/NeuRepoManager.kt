package de.hype.hypenotify.app.skyblockconstants

import com.google.gson.Gson
import com.google.gson.JsonObject
import de.hype.hypenotify.app.core.interfaces.MiniCore
import io.github.moulberry.repo.NEUItems
import io.github.moulberry.repo.NEURepository
import io.github.moulberry.repo.data.NEUItem
import org.eclipse.jgit.api.Git
import org.eclipse.jgit.api.errors.GitAPIException
import java.io.BufferedReader
import java.io.File
import java.io.IOException
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Path

object NeuRepoManager {
    val items: NEUItems?
        get() = repository?.getItems()
    val itemIds: MutableList<String?>
        get() = ArrayList<String?>(this.items!!.getItems().keys)
    private const val REPO_URL = "https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO.git"
    var repository: NEURepository? = null
    var LOCAL_PATH: Path? = null
    var core: MiniCore? = null
    public fun init(core: MiniCore) {
        this.core = core
        core.executionService().execute {
            val base = core.context().filesDir
            val neuDir = File(base, "neu-repo")
            neuDir.mkdirs()
            LOCAL_PATH = neuDir.toPath()
            try {
                updateRepo()
            } catch (e: Exception) {
                e.printStackTrace()
            }
            repository = NEURepository.of(LOCAL_PATH!!)
            repository?.reload()
        }
    }

    @Throws(GitAPIException::class, IOException::class)
    private fun updateRepo() {
        val repoDir: File = LOCAL_PATH!!.toFile()
        if (!repoDir.exists() || (repoDir.isDirectory() && repoDir.listFiles().size == 0)) {
            repoDir.mkdirs()
            cloneRepo(repoDir)
        } else {
            fetchChanges(repoDir)
        }
    }

    @Throws(GitAPIException::class)
    private fun cloneRepo(repoDir: File?) {
        println("BingoNet: Cloning NEU Repo: Start")
        Git.cloneRepository()
            .setURI(REPO_URL)
            .setDirectory(repoDir)
            .call()
        println("BingoNet: Cloning NEU Repo: Done")
    }

    @Throws(IOException::class, GitAPIException::class)
    private fun fetchChanges(repoDir: File?) {
        Git.open(repoDir).use { git ->
            println("BingoNet: Fetching latest NEU Repo changes...")
            git.fetch().call()
            println("BingoNet: Checking out the latest NEU Repo changes...")
            git.pull().call()
            println("BingoNet: NEU Repo updated.")
        }
    }

    val sackableItems: Set<NEUItem> by lazy {
        val combinedContents: MutableList<String?> = java.util.ArrayList<String?>()
        try {
            // Download JSON from URL
            val jsonString: String =
                downloadJson("https://raw.githubusercontent.com/NotEnoughUpdates/NotEnoughUpdates-REPO/master/constants/sacks.json")

            // Parse JSON
            val jsonObject: JsonObject = Gson().fromJson(jsonString, JsonObject::class.java)
            val sacks = jsonObject.getAsJsonObject("sacks")

            // Iterate through all sacks
            for (sackName in sacks.keySet()) {
                val sack = sacks.getAsJsonObject(sackName)
                val contentsArray = sack.getAsJsonArray("contents")

                // Add each item to combinedContents list
                for (i in 0..<contentsArray.size()) {
                    combinedContents.add(contentsArray.get(i).getAsString())
                }
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
        return@lazy combinedContents.mapNotNull { items?.items?.get(it) }.toHashSet()
    }

    private fun downloadJson(urlString: String?): String {
        val result = StringBuilder()
        val url = URL(urlString)
        val conn = url.openConnection() as HttpURLConnection
        conn.setRequestMethod("GET")

        BufferedReader(InputStreamReader(conn.getInputStream())).use { reader ->
            var line: String?
            while ((reader.readLine().also { line = it }) != null) {
                result.append(line)
            }
        }
        return result.toString()
    }

    fun getItemByProductId(productId: String): NEUItem? {
        var item = items?.getItemBySkyblockId(productId)
        if (item != null) return item
        return item
    }
}