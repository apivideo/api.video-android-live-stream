package video.api.livestream

import org.junit.Assert.*
import org.junit.Test

class ExtensionsKtTest {

    @Test
    fun addTrailingSlashIfNeeded() {
        assertEquals("abcde/", "abcde".addTrailingSlashIfNeeded())
        assertEquals("abcde/", "abcde/".addTrailingSlashIfNeeded())
    }
}