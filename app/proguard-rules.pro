# Quy tắc giữ lại khi R8 rút gọn bản release (mục 7.1).
#
# Nguyên tắc: chỉ giữ những gì bị gọi qua phản chiếu hoặc bị nhận diện bằng tên,
# vì R8 không "thấy" những đường gọi đó và sẽ xóa mất. Giữ nhiều hơn mức cần
# thiết thì APK phình và mất luôn ý nghĩa của việc bật R8.

# --- kotlinx.serialization ---
# Thư viện tìm serializer của một lớp qua trường/phương thức sinh sẵn tên
# `Companion` và `$serializer`. R8 không thấy đường gọi này nên phải nói rõ.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**

-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1> {
    static <1>$Companion Companion;
}

-if @kotlinx.serialization.Serializable class ** {
    static **$* *;
}
-keepclassmembers class <2>$<3> {
    kotlinx.serialization.KSerializer serializer(...);
}

-if @kotlinx.serialization.Serializable class ** {
    public static ** INSTANCE;
}
-keepclassmembers class <1> {
    public static <1> INSTANCE;
    kotlinx.serialization.KSerializer serializer(...);
}

# --- Giao thức LAN ---
# Tên các lớp con của NetMessage chính là discriminator nằm trong JSON truyền qua
# mạng, tức là một phần của giao thức. Để R8 đổi tên thì hai máy cùng phiên bản
# app nhưng khác lần build vẫn không hiểu nhau.
-keepnames class kma.game.chess2d.net.NetMessage
-keepnames class kma.game.chess2d.net.NetMessage$* { *; }
-keepnames class kma.game.chess2d.net.RoomBeacon
-keepnames class kma.game.chess2d.net.StateSync
