plugins {
  id("com.android.asset-pack")
}

assetPack {
  packName.set("hindi_quran_pdf")
  dynamicDelivery {
    deliveryType.set("on-demand")
  }
}
