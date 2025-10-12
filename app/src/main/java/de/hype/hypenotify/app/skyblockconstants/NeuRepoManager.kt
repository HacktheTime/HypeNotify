package de.hype.hypenotify.app.skyblockconstants

import android.util.Log
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
import java.lang.ref.WeakReference
import java.net.HttpURLConnection
import java.net.URL
import java.nio.file.Path
import java.util.concurrent.ConcurrentHashMap

object NeuRepoManager {
    // Lazy loading mit Weak References für bessere Speicherverwaltung
    private var _repository: WeakReference<NEURepository>? = null
    private var _items: WeakReference<NEUItems>? = null
    private val itemCache = ConcurrentHashMap<String, WeakReference<NEUItem>>()

    // Bazaar-spezifische Caches (persistent für Tracking)
    private val bazaarItemCache = ConcurrentHashMap<String, WeakReference<NEUItem>>()
    private var isBazaarTrackingMode = false

    val items: NEUItems?
        get() {
            var items = _items?.get()
            if (items == null) {
                items = repository?.getItems()
                if (items != null) {
                    _items = WeakReference(items)
                }
            }
            return items
        }

    val itemIds: MutableList<String?>
        get() = ArrayList<String?>(this.items?.getItems()?.keys ?: emptySet())

    private const val REPO_URL = "https://github.com/NotEnoughUpdates/NotEnoughUpdates-REPO.git"

    var repository: NEURepository?
        get() {
            var repo = _repository?.get()
            if (repo == null && LOCAL_PATH != null) {
                repo = NEURepository.of(LOCAL_PATH!!)
                _repository = WeakReference(repo)
            }
            return repo
        }
        private set(value) {
            _repository = if (value != null) WeakReference(value) else null
        }

    var LOCAL_PATH: Path? = null
    var core: MiniCore? = null

    @Volatile
    private var isInitialized = false

    fun init(core: MiniCore) {
        if (isInitialized) return

        this.core = core
        core.executionService().execute {
            try {
                val base = core.context().filesDir
                val neuDir = File(base, "neu-repo")
                neuDir.mkdirs()
                LOCAL_PATH = neuDir.toPath()

                updateRepo()

                repository = NEURepository.of(LOCAL_PATH!!)
                repository?.reload()

                isInitialized = true
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    // Memory-efficient item retrieval mit Bazaar-Support
    fun getItem(itemId: String): NEUItem? {
        // Erst Bazaar-Cache prüfen wenn im Tracking-Modus
        if (isBazaarTrackingMode) {
            val bazaarCached = bazaarItemCache[itemId]?.get()
            if (bazaarCached != null) return bazaarCached
        }

        val cachedRef = itemCache[itemId]
        val cached = cachedRef?.get()
        if (cached != null) {
            // Bei Bazaar-Tracking auch in persistenten Cache speichern
            if (isBazaarTrackingMode) {
                bazaarItemCache[itemId] = WeakReference(cached)
            }
            return cached
        }

        val item = items?.items?.get(itemId)
        if (item != null) {
            itemCache[itemId] = WeakReference(item)
            if (isBazaarTrackingMode) {
                bazaarItemCache[itemId] = WeakReference(item)
            }
        }
        return item
    }

    // Selektive Cache-Bereinigung für Bazaar-Tracking
    fun clearItemCacheOnly() {
        Log.d("NeuRepoManager", "Clearing item cache only (preserving bazaar cache)")
        itemCache.clear()
        // bazaarItemCache wird NICHT geleert für kontinuierliches Tracking
        System.gc()
    }

    // Vollständige Cache-Bereinigung
    fun clearCache() {
        Log.d("NeuRepoManager", "Clearing all caches")
        itemCache.clear()
        bazaarItemCache.clear()
        _items?.clear()
        _repository?.clear()
        System.gc()
    }

    // Bazaar-Tracking Modus aktivieren/deaktivieren
    fun setBazaarTrackingMode(enabled: Boolean) {
        isBazaarTrackingMode = enabled
        Log.d("NeuRepoManager", "Bazaar tracking mode: $enabled")

        // Bei Deaktivierung Bazaar-Cache leeren
        if (!enabled) {
            bazaarItemCache.clear()
        }
    }

    // Speicher-Status für Monitoring
    fun getMemoryStatus(): String {
        return "ItemCache: ${itemCache.size}, BazaarCache: ${bazaarItemCache.size}, " +
                "Repository: ${if (_repository?.get() != null) "loaded" else "null"}, " +
                "Items: ${if (_items?.get() != null) "loaded" else "null"}"
    }

    // Optimierte Methode für Bazaar-Items (häufig verwendet)
    fun getBazaarItem(productId: String): NEUItem? {
        // Direkt aus Bazaar-Cache wenn verfügbar
        if (isBazaarTrackingMode) {
            bazaarItemCache[productId]?.get()?.let { return it }
        }

        val item = getItem(productId) ?: items?.getItemBySkyblockId(productId)

        // In Bazaar-Cache speichern für zukünftige Zugriffe
        if (item != null && isBazaarTrackingMode) {
            bazaarItemCache[productId] = WeakReference(item)
        }

        return item
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