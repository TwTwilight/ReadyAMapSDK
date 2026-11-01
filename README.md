# ReadyAMapSDK

高德 Android SDK（3dmap 7.1.0）的开箱即用封装

## 引入

```kotlin
repositories {
    google()
    mavenCentral()
    maven("https://jitpack.io")
}

dependencies {
    implementation("com.github.TwTwilight:ReadyAMapSDK:v1.0.4")
}
```

## 地图

```kotlin
val mapView = MapView(context)
mapView.onCreate(null)
val map = mapView.map
map.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(39.9087, 116.3975), 16f))
```


## 搜索

```kotlin
val query = PoiSearch.Query("北京大学", "", "")
query.pageSize = 8
val search = PoiSearch(context, query)
search.setOnPoiSearchListener(object : PoiSearch.OnPoiSearchListener {
    override fun onPoiSearched(result: PoiResult?, code: Int) {
        val pois = result?.pois.orEmpty()
    }
    override fun onPoiItemSearched(item: PoiItem?, code: Int) = Unit
})
search.searchPOIAsyn()
```


## 构建

```bash
./gradlew assembleRelease
```
