package zdz.imageURL.model

import android.annotation.SuppressLint
import io.ktor.http.Url
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object UrlSerializer : KSerializer<Url> {
    override val descriptor = PrimitiveSerialDescriptor("URL", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Url) = encoder.encodeString(value.toString())
    override fun deserialize(decoder: Decoder): Url = Url(decoder.decodeString())
}

object DateSerializer: KSerializer<Date> {
    @SuppressLint("ConstantLocale")
    private val format = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss+00:00", Locale.getDefault())
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor("Date", PrimitiveKind.STRING)
    override fun serialize(encoder: Encoder, value: Date) = encoder.encodeString(format.format(value))
    override fun deserialize(decoder: Decoder): Date = format.parse(decoder.decodeString())!! // TODO: 可空问题
}