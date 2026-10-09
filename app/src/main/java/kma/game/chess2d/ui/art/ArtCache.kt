package kma.game.chess2d.ui.art

import android.content.Context
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.LruCache
import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntRect
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kma.game.chess2d.ui.theme.GothicColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** Một ảnh cần nạp và cỡ cạnh dài tối đa cần dùng (px) khi hiển thị. */
data class ArtSpec(@DrawableRes val res: Int, val maxEdge: Int)

/**
 * Nạp ảnh PNG lớn từ `res/drawable` ở độ phân giải vừa đủ, có cache.
 *
 * Toàn bộ asset là PNG 1254x1254 (nền 941x1672) đặt trong `drawable/` (không phải nodpi),
 * nên `painterResource` giải mã chúng rồi phóng theo density của máy, mỗi ảnh có thể
 * thành bitmap hàng chục MB. Ở đây tắt `inScaled` và dùng `inSampleSize` để chỉ giải mã
 * tới cỡ thật sự cần, rồi giữ trong [LruCache] để 32 quân cờ dùng chung 12 bitmap.
 */
object ArtCache {
    private const val MAX_BYTES = 64 * 1024 * 1024

    private val cache = object : LruCache<String, ImageBitmap>(MAX_BYTES) {
        override fun sizeOf(key: String, value: ImageBitmap): Int = value.width * value.height * 4
    }
    private val lock = Any()

    private fun key(res: Int, maxEdge: Int) = "$res@$maxEdge"

    /** Lấy ảnh nếu đã có trong cache, không giải mã. */
    fun peek(res: Int, maxEdge: Int): ImageBitmap? = synchronized(lock) { cache.get(key(res, maxEdge)) }

    /** Giải mã (nếu chưa có) và trả ảnh. Gọi ngoài luồng chính. */
    fun load(resources: Resources, res: Int, maxEdge: Int): ImageBitmap = synchronized(lock) {
        val k = key(res, maxEdge)
        val hit = cache.get(k)
        if (hit != null) return@synchronized hit
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            inScaled = false
        }
        BitmapFactory.decodeResource(resources, res, bounds)
        val options = BitmapFactory.Options().apply {
            inScaled = false
            inSampleSize = sampleSizeFor(bounds.outWidth, bounds.outHeight, maxEdge)
            inPreferredConfig = Bitmap.Config.ARGB_8888
        }
        val bitmap = BitmapFactory.decodeResource(resources, res, options)
            ?: error("Khong giai ma duoc drawable $res")
        val image = bitmap.asImageBitmap()
        cache.put(k, image)
        image
    }

    private val bounds = HashMap<String, IntRect>()

    /**
     * Vùng có nội dung (bỏ viền trong suốt) của ảnh [res] ở cỡ [maxEdge]. Bộ asset mới vẽ trên
     * canvas vuông hoặc 3:1 với lề trong suốt khác nhau từng file, nên muốn đặt ảnh đúng khung
     * thiết kế thì phải cắt lề trước. Tính một lần rồi nhớ lại. Gọi ngoài luồng chính.
     */
    fun trimBounds(resources: Resources, res: Int, maxEdge: Int): IntRect {
        val image = load(resources, res, maxEdge)
        return synchronized(lock) {
            bounds.getOrPut(key(res, maxEdge)) {
                val bitmap = image.asAndroidBitmap()
                val row = IntArray(bitmap.width)
                alphaBounds(bitmap.width, bitmap.height) { y ->
                    bitmap.getPixels(row, 0, bitmap.width, 0, y, bitmap.width, 1)
                    row
                }
            }
        }
    }

    /** Lấy vùng nội dung nếu đã tính, không giải mã. */
    fun peekBounds(res: Int, maxEdge: Int): IntRect? = synchronized(lock) { bounds[key(res, maxEdge)] }

    /**
     * Hình chữ nhật nhỏ nhất bao mọi điểm ảnh có alpha > [threshold]. [rowAt] trả các điểm ảnh ARGB
     * của một hàng. Ảnh trong suốt hoàn toàn thì trả cả ảnh để bên vẽ không phải xử lý ca rỗng.
     * Tách ra thành hàm thuần để test được.
     */
    internal fun alphaBounds(width: Int, height: Int, threshold: Int = 24, rowAt: (Int) -> IntArray): IntRect {
        var minX = width
        var minY = height
        var maxX = -1
        var maxY = -1
        for (y in 0 until height) {
            val pixels = rowAt(y)
            for (x in 0 until width) {
                if ((pixels[x] ushr 24) > threshold) {
                    if (x < minX) minX = x
                    if (x > maxX) maxX = x
                    if (y < minY) minY = y
                    maxY = y
                }
            }
        }
        if (maxX < 0) return IntRect(0, 0, width, height)
        return IntRect(minX, minY, maxX + 1, maxY + 1)
    }

    /** Nạp trước một danh sách ảnh, báo tiến độ sau mỗi ảnh. */
    suspend fun preload(context: Context, specs: List<ArtSpec>, onProgress: (done: Int, total: Int) -> Unit) {
        val resources = context.applicationContext.resources
        withContext(Dispatchers.Default) {
            specs.forEachIndexed { index, spec ->
                runCatching { load(resources, spec.res, spec.maxEdge) }
                onProgress(index + 1, specs.size)
            }
        }
    }

    /**
     * Hệ số thu nhỏ (lũy thừa của 2) lớn nhất mà cạnh dài vẫn còn >= [maxEdge].
     * Tách ra thành hàm thuần để test được.
     */
    internal fun sampleSizeFor(width: Int, height: Int, maxEdge: Int): Int {
        val longest = maxOf(width, height)
        var sample = 1
        while (longest / (sample * 2) >= maxEdge) sample *= 2
        return sample
    }
}

/** Ảnh [res] đã nạp ở cỡ [maxEdge]; `null` trong lúc đang giải mã (ngoài luồng chính). */
@Composable
fun rememberArt(@DrawableRes res: Int, maxEdge: Int): ImageBitmap? {
    val context = LocalContext.current
    var image by remember(res, maxEdge) { mutableStateOf(ArtCache.peek(res, maxEdge)) }
    LaunchedEffect(res, maxEdge) {
        if (image == null) {
            image = withContext(Dispatchers.Default) {
                runCatching { ArtCache.load(context.resources, res, maxEdge) }.getOrNull()
            }
        }
    }
    return image
}

/** Ảnh đã nạp kèm vùng có nội dung (đã bỏ lề trong suốt). */
class TrimmedArt(val image: ImageBitmap, val bounds: IntRect)

/** Ảnh [res] kèm vùng nội dung; `null` trong lúc đang giải mã/quét lề. */
@Composable
fun rememberTrimmedArt(@DrawableRes res: Int, maxEdge: Int): TrimmedArt? {
    val context = LocalContext.current
    var art by remember(res, maxEdge) {
        val image = ArtCache.peek(res, maxEdge)
        val rect = ArtCache.peekBounds(res, maxEdge)
        mutableStateOf(if (image != null && rect != null) TrimmedArt(image, rect) else null)
    }
    LaunchedEffect(res, maxEdge) {
        if (art == null) {
            art = withContext(Dispatchers.Default) {
                runCatching {
                    val rect = ArtCache.trimBounds(context.resources, res, maxEdge)
                    TrimmedArt(ArtCache.load(context.resources, res, maxEdge), rect)
                }.getOrNull()
            }
        }
    }
    return art
}

/**
 * Thay cho `Image(painterResource(...))` với asset lớn: nạp ở cỡ vừa đủ, chưa xong thì vẽ trống.
 *
 * @param trim bỏ lề trong suốt trước khi co ảnh vào khung, để phần có nội dung lấp đúng khung
 *        thiết kế (asset mới thường có lề rộng và lệch).
 */
@Composable
fun ArtImage(
    @DrawableRes res: Int,
    modifier: Modifier = Modifier,
    maxEdge: Int = 1280,
    contentDescription: String? = null,
    contentScale: ContentScale = ContentScale.Fit,
    alignment: Alignment = Alignment.Center,
    alpha: Float = 1f,
    colorFilter: ColorFilter? = null,
    filterQuality: FilterQuality = FilterQuality.Medium,
    trim: Boolean = false,
) {
    if (trim) {
        val art = rememberTrimmedArt(res, maxEdge) ?: return
        val painter = remember(art) {
            BitmapPainter(
                image = art.image,
                srcOffset = IntOffset(art.bounds.left, art.bounds.top),
                srcSize = IntSize(art.bounds.width, art.bounds.height),
                filterQuality = filterQuality,
            )
        }
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = modifier,
            contentScale = contentScale,
            alignment = alignment,
            alpha = alpha,
            colorFilter = colorFilter,
        )
        return
    }
    val image = rememberArt(res, maxEdge) ?: return
    Image(
        bitmap = image,
        contentDescription = contentDescription,
        modifier = modifier,
        contentScale = contentScale,
        alignment = alignment,
        alpha = alpha,
        colorFilter = colorFilter,
        filterQuality = filterQuality,
    )
}

/**
 * Nền toàn màn hình: ảnh phủ kín (Crop) cộng lớp phủ tối để chữ và nút luôn đọc được.
 *
 * Vẽ trên nền [GothicColors.Ink] nên lúc ảnh còn đang nạp vẫn là màn tối liền mạch.
 */
@Composable
fun ArtBackdrop(
    @DrawableRes res: Int,
    modifier: Modifier = Modifier,
    dim: Float = 0.5f,
    maxEdge: Int = 1280,
) {
    Box(modifier = modifier.fillMaxSize().background(GothicColors.Ink)) {
        ArtImage(
            res = res,
            maxEdge = maxEdge,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize(),
        )
        if (dim > 0f) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = dim)))
        }
    }
}
