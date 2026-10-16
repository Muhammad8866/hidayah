"""Builds app/src/main/assets/azan_catalog.json from Internet Archive items.

Every entry is a Sunni azan picked by hand from the item's own file names and tags.
Size and length come from the Archive metadata; run with --verify to also check each URL.

    python tools/azan/generate_catalog.py [--verify]
"""
import json
import re
import sys
import urllib.parse
import urllib.request
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path

OUT = Path(__file__).resolve().parents[2] / "app/src/main/assets/azan_catalog.json"
FAJR = True

# (archive item, file name, title, muezzin, region, fajr)
ENTRIES = []


def add(item, name, title, muezzin, region, fajr=False):
    ENTRIES.append((item, name, title, muezzin, region, fajr))


# ---- Makkah (Masjid al-Haram) ----
for n, item in (("01", "Athan_220"), ("02", "Athan_786"), ("03", "Athan_994"), ("04", "Athan_636")):
    add(item, f"Ali_Mulla_{n}.MP3", f"Makkah Azan {int(n)}", "Sheikh Ali Ahmed Mulla", "Makkah")
add("MakkahFajrAdhan6913SheikhAliMullah", "Makkah Fajr Adhan 6-9-13 Sheikh Ali Mullah.mp3",
    "Makkah Fajr Azan", "Sheikh Ali Ahmed Mulla", "Makkah", FAJR)
add("Athan_87", "Farook_Hadrawy_01.MP3", "Makkah Azan 1", "Sheikh Farooq Hadrawi", "Makkah")
add("Athan_46", "Farook_Hadrawy_02.MP3", "Makkah Azan 2", "Sheikh Farooq Hadrawi", "Makkah")
add("Athan_929", "Nayef_feda_01.MP3", "Makkah Azan 1", "Sheikh Nayef Faydah", "Makkah")
add("Athan_584", "Nayef_feda_02.MP3", "Makkah Azan 2", "Sheikh Nayef Faydah", "Makkah")
add("Athan_586", "Ahmad_Abdulla_basnawy.MP3", "Makkah Azan", "Sheikh Ahmad Abdullah Basnawi", "Makkah")
add("Athan_15", "Mohammed_Ali_Shakir.MP3", "Makkah Azan", "Sheikh Muhammad Ali Shakir", "Makkah")
add("adhan_202303", "adhan-alharam-almakaa.mp3", "Masjid al-Haram Azan", "Masjid al-Haram", "Makkah")
add("adhan_202303", "alfajr-alharam-almakiya.mp3", "Masjid al-Haram Fajr Azan", "Masjid al-Haram", "Makkah", FAJR)
add("adzan_202208", "Mekkah Versi 1.mp3", "Makkah Azan (short recording)", "Masjid al-Haram", "Makkah")
for stem, who, prayer in (
    ("mgd_abas", "Sheikh Majid al-Abbas", "Asr"),
    ("emad_bakry", "Sheikh Emad Baqri", "Asr"),
    ("es_khan", "Sheikh Essam Khan", "Isha"),
    ("ahmed_khokh", "Sheikh Ahmed Khoja", "Jumu'ah"),
    ("tawfek_almonged", "Sheikh Tawfiq Khoj", "Maghrib"),
    ("m_boasad", "Sheikh Muhammad Basa'd", "Asr"),
    ("hes_shat", "Sheikh Hussain Shahat", "Maghrib"),
    ("tr_hosny", "Sheikh Turki al-Hasani", "Maghrib"),
    ("m_emary", "Sheikh Muhammad al-Umari", "Dhuhr"),
):
    add("athan_sound", f"{stem}.mp3", f"Makkah {prayer} Azan", who, "Makkah")
    add("athan_sound", f"f_{stem}.mp3", "Makkah Fajr Azan", who, "Makkah", FAJR)
add("athan_sound", "h_elsakaf.mp3", "Makkah Maghrib Azan", "Sheikh Hashim al-Saqqaf", "Makkah")

# ---- Madinah (Masjid an-Nabawi) ----
add("adhan_202303", "alharam-almudnaa.mp3", "Masjid an-Nabawi Azan", "Masjid an-Nabawi", "Madinah")
add("adzan_202208", "Madinah.mp3", "Madinah Azan (short recording)", "Masjid an-Nabawi", "Madinah")
add("athan_sound", "f_amr_sonbel.mp3", "Madinah Fajr Azan", "Sheikh Omar Sunbul", "Madinah", FAJR)
add("athan_sound", "mahdy_bn_yousf.mp3", "Madinah Isha Azan", "Sheikh Mahdi Bari", "Madinah")
add("athan_sound", "f_mahdy_bn_yousf.mp3", "Madinah Fajr Azan", "Sheikh Mahdi Bari", "Madinah", FAJR)
add("athan_sound", "f_esam_bokary.mp3", "Madinah Fajr Azan (27 Ramadan 1432)", "Sheikh Essam Bukhari", "Madinah", FAJR)
add("athan_sound", "hassan_khashokgy.mp3", "Madinah Maghrib Azan", "Sheikh Hassan Khashoggi", "Madinah")
add("athan_sound", "f_hassan_khashokgy.mp3", "Madinah Fajr Azan", "Sheikh Hassan Khashoggi", "Madinah", FAJR)
add("athan_sound", "eyad_shokry.mp3", "Madinah Dhuhr Azan", "Sheikh Iyad Shukri", "Madinah")
add("athan_sound", "f_eyad_shokry.mp3", "Madinah Fajr Azan", "Sheikh Iyad Shukri", "Madinah", FAJR)
add("athan_sound", "f_mohmed_bn_kasas.mp3", "Madinah Fajr Azan", "Sheikh Muhammad Marwan Qassas", "Madinah", FAJR)

# ---- Haramain: published as "Adhan al-Haramain" without naming the mosque ----
for stem, who, prayer in (
    ("saed_flata", "Sheikh Said Fallatah", "Jumu'ah"),
    ("moh_maghriby", "Sheikh Muhammad Maghribi", "Maghrib"),
    ("sahel_hafz", "Sheikh Suhail Hafiz", "Isha"),
    ("sam_rasy", "Sheikh Sami Rayyis", "Isha"),
    ("eb_madny", "Sheikh Ibrahim al-Madani", "Dhuhr"),
    ("ab_albafef", "Sheikh Abdullah Ba'afif", "Maghrib"),
    ("ahmed_alansary", "Sheikh Ahmad al-Ansari", "Jumu'ah"),
):
    add("athan_sound", f"{stem}.mp3", f"Haramain {prayer} Azan", who, "Haramain")
    add("athan_sound", f"f_{stem}.mp3", "Haramain Fajr Azan", who, "Haramain", FAJR)
add("athan_sound", "m_moazen.mp3", "Haramain Dhuhr Azan", "Sheikh Muhammad Mu'adhin", "Haramain")
add("athan_sound", "f_abdallah_elhaneny.mp3", "Haramain Fajr Azan", "Sheikh Abdullah al-Hunayni", "Haramain", FAJR)

# ---- Al-Aqsa (Al-Quds) ----
add("adhan_202303", "naji-qazaaz.mp3", "Masjid al-Aqsa Azan", "Sheikh Naji Qazzaz", "Al-Aqsa")
add("adhan_202303", "adhan-alquds.mp3", "Al-Quds Azan", "Masjid al-Aqsa", "Al-Aqsa")
add("AzanMagribDomeOfRockFri11.4.14", "Azan Magrib Dome of Rock Fri 11.4.14.mp3",
    "Dome of the Rock Maghrib Azan", "Masjid al-Aqsa", "Al-Aqsa")
add("adzan_202208", "Masjidil Aqsa Versi 1.mp3", "Masjid al-Aqsa Azan 1", "Masjid al-Aqsa", "Al-Aqsa")
add("adzan_202208", "Masjidil Aqsa Versi 2.mp3", "Masjid al-Aqsa Azan 2", "Masjid al-Aqsa", "Al-Aqsa")

# ---- Egypt ----
add("adhan_202303", "eabd-albasit-eabdalsamad.mp3", "Azan", "Qari Abdul Basit Abdus Samad", "Egypt")
add("athan_sound", "f_abd_el_basset.mp3", "Fajr Azan (1395 AH)", "Qari Abdul Basit Abdus Samad", "Egypt", FAJR)
add("adhan_202303", "mustafaa-iismaeil.mp3", "Azan", "Sheikh Mustafa Ismail", "Egypt")
add("adhan_202303", "abawialeaynayn-shaeishae.mp3", "Cairo Azan", "Sheikh Abul Ainain Shuaisha", "Egypt")
add("adhan_202303", "muhamad-eali-albana.mp3", "Cairo Azan", "Sheikh Mahmoud Ali al-Banna", "Egypt")
add("adhan_202303", "alfajr-ahmad naeayanae.mp3", "Fajr Azan", "Sheikh Ahmad Nuaina", "Egypt", FAJR)
add("adhan_202303", "mahmud-muhamad-altablawi.mp3", "Azan", "Sheikh Muhammad Mahmoud Tablawi", "Egypt")
add("adhan_202303", "sayid-alnaqshabandaa.mp3", "Azan", "Sheikh Sayyid an-Naqshbandi", "Egypt")
add("athan_sound", "islam_sobhy.mp3", "Azan", "Qari Islam Sobhi", "Egypt")
for n in (1, 2, 3):
    add("adzan_202208", f"Mesir Versi {n}.mp3", f"Egypt Azan {n}", "Egypt", "Egypt")

# ---- Kuwait: Sheikh Mishary Rashid Alafasy, by maqam ----
for stem, maqam, fajr_suffix in (
    ("hejaz-2014", "Hijaz", "al-fajer"), ("bayaty-2014", "Bayati", "al-fajr"), ("saba-2014", "Saba", "al-fajr"),
    ("rast-2014", "Rast", "al-fajr"), ("nahawand-2014", "Nahawand", "alfajr"), ("kard-2014", "Kurd", "al-fajr"),
    ("ajam-2014", "Ajam", "al-fajr"), ("saikah-2014", "Sikah", "al-fajr"), ("nawa-athar-2014", "Nawa Athar", "al-fajr"),
    ("lami-2005", "Lami", "al-fajr"),
):
    add("azan-alafasy", f"azan-{stem}-other.mp3", f"Azan - Maqam {maqam}", "Sheikh Mishary Rashid Alafasy", "Kuwait")
    add("azan-alafasy", f"azan-{stem}-{fajr_suffix}.mp3", f"Fajr Azan - Maqam {maqam}",
        "Sheikh Mishary Rashid Alafasy", "Kuwait", FAJR)
add("azan-alafasy", "azan-2019-dubai-tv.mp3", "Azan (Dubai TV, 2019)", "Sheikh Mishary Rashid Alafasy", "Kuwait")
add("adzan_202208", "Kuwait.mp3", "Kuwait Azan", "Kuwait", "Kuwait")

# ---- Saudi Arabia: masajid of Jeddah (quran-jed.net) ----
for name, who in (
    ("AthanHaniAlrefaei.mp3", "Sheikh Hani ar-Rifai"), ("AthanIdreesAbkar.mp3", "Sheikh Idrees Abkar"),
    ("AthanMansoorAlsalmi.mp3", "Sheikh Mansour as-Salimi"), ("AthanSheikhAlshatri.mp3", "Sheikh Abu Bakr ash-Shatri"),
    ("AthanTawfeeqAlsayegh.mp3", "Sheikh Tawfeeq as-Sayegh"), ("AtahnMohammedAlghazali.mp3", "Mohammed al-Ghazali"),
    ("Athan Ahmed Jahaf.mp3", "Ahmed Jahaf"), ("AthanAbdulazizAlzahrani.mp3", "Abdulaziz az-Zahrani"),
    ("AthanAbdulazizBinIbrahim.mp3", "Abdulaziz bin Ibrahim"), ("AthanAbdullahAlsamarqandi.mp3", "Abdullah as-Samarqandi"),
    ("AthanAbdulwaliAlarkani.mp3", "Abdulwali al-Arkani"), ("AthanAdnanNoor.mp3", "Adnan Noor"),
    ("AthanAhmedYounisKhojah.mp3", "Ahmed Younis Khojah"), ("AthanAmerAlmohalhal.mp3", "Amer al-Muhalhal"),
    ("AthanEmadAlmansari.mp3", "Emad al-Mansari"), ("AthanFaisalLabban.mp3", "Faisal Labban"),
    ("AthanHosamKhojah.mp3", "Hussam Khojah"), ("AthanMansorMohyeAldeen.mp3", "Mansour Muhyiddin"),
    ("AthanMohammedAlarkani.mp3", "Mohammed al-Arkani"), ("AthanMohammedAlsharabi.mp3", "Mohammed ash-Sharabi"),
    ("AthanMohammedBazarah.mp3", "Mohammed Bazarah"), ("AthanMohammedSalehAlsheikh.mp3", "Mohammed Saleh ash-Sheikh"),
    ("AthanSaeedAlkhateeb.mp3", "Saeed al-Khateeb"), ("AthanSaeedBakhashwain.mp3", "Saeed Bakhashwain"),
    ("AthanSelimanJammal.mp3", "Sulaiman Jammal"), ("AthanTawfeeqAlreimi.mp3", "Tawfeeq ar-Raimi"),
    ("AthanAmmar.mp3", "Ammar"),
):
    add("AthanJeddah", name, "Jeddah Azan", who, "Saudi Arabia")
add("AthanJeddah", "AthanFajr-Ammar.mp3", "Jeddah Fajr Azan", "Ammar", "Saudi Arabia", FAJR)
add("adhan_202303", "yasir-aldawsari.mp3", "Azan", "Sheikh Yasser ad-Dosari", "Saudi Arabia")
add("adhan.notifications", "Nasser_al_Qatami_Adhan.mp3", "Azan", "Sheikh Nasser al-Qatami", "Saudi Arabia")
add("TvQuran.com__Athan", "TvQuran.com__01.athan.mp3", "Azan", "Mansour az-Zahrani", "Saudi Arabia")
add("TvQuran.com__Athan", "TvQuran.com__06.athan.mp3", "Azan", "Hamad Daghriri", "Saudi Arabia")

# ---- Syria ----
add("athan_sound", "azan_alekhwa.mp3", "Umayyad Mosque Azan (Damascus)", "Abu Sha'r brothers", "Syria")
add("athan_sound", "raptet_demash.mp3", "Umayyad Mosque group Azan (Damascus)", "Rabitat Dimashq", "Syria")
add("athan_sound", "abdelhader_elmaerashey.mp3", "Azan", "Abdul Qadir al-Marashli", "Syria")
add("adzan_202208", "Suriah.mp3", "Syria Azan", "Syria", "Syria")

# ---- Jordan ----
add("athan_sound", "f_abdall_khazala.mp3", "Amman unified Fajr Azan - Maqam Rast",
    "Sheikh Abdullah Abu Ghazaleh", "Jordan", FAJR)

# ---- Turkey ----
add("FatihOzer-EzanIstanbulMakami", "FatihOzer-EzanIstanbulMakami.mp3", "Ezan - Istanbul makam", "Fatih Ozer", "Turkey")
add("athan_sound", "esm_gosh.mp3", "Turkish Ezan", "Ismail Cosar", "Turkey")
add("adzan_202208", "Turki Versi 1.mp3", "Turkey Ezan 1", "Turkey", "Turkey")
add("adzan_202208", "Turki Versi 2.mp3", "Turkey Ezan 2", "Turkey", "Turkey")

# ---- UAE ----
add("MostBeautifulAzanEverHeardWhichWillMakeYouSheikhZayedGrandMosqmp3cut.net",
    "Most beautiful azan ever heard which will make you - Sheikh Zayed Grand Mosq (mp3cut.net).mp3",
    "Sheikh Zayed Grand Mosque Azan (Abu Dhabi)", "Sheikh Zayed Grand Mosque", "UAE")
add("adzan_202208", "Uni Emirat Arab.mp3", "UAE Azan", "UAE", "UAE")

# ---- South & Southeast Asia, Europe ----
add("adzan_202208", "Pakistan.mp3", "Pakistan Azan", "Pakistan", "Pakistan")
add("adzan_202208", "Indonesia.mp3", "Indonesia Azan", "Indonesia", "Indonesia")
add("AzanIndonesiamp3cut.net", "Azan - Indonesia (mp3cut.net).mp3", "Indonesia Azan 2", "Indonesia", "Indonesia")
add("adzan_202208", "Malaysia Versi 1.mp3", "Malaysia Azan 1", "Malaysia", "Malaysia")
add("adzan_202208", "Malaysia Versi 2.mp3", "Malaysia Azan 2", "Malaysia", "Malaysia")
add("EzanUzivo-NisadEfendija", "EzanNisad.mp3", "Bosnia Ezan (live)", "Nisad efendija", "Bosnia")
add("adzan_202208", "Bosnia.mp3", "Bosnia Ezan", "Bosnia", "Bosnia")

# ---- Other well-known voices ----
add("adhan.notifications", "Ahmed_al_Imadi_Adhan.mp3", "Azan", "Ahmed al-Imadi", "More voices")
add("adhan.notifications", "Majed_al_Hamathani_Adhan.mp3", "Azan", "Majed al-Hamathani", "More voices")
add("adhan.notifications", "Mokhtar_Hadj_Slimane_Adhan.mp3", "Azan", "Mokhtar Hadj Slimane", "More voices")
add("adhan_202303", "ahmad-alhadaad.mp3", "Azan", "Sheikh Ahmad al-Haddad", "More voices")
add("adhan_202303", "ahmad-alkurdi.mp3", "Azan", "Ahmad al-Kurdi", "More voices")
add("TvQuran.com__Athan", "TvQuran.com__02.athan.mp3", "Azan", "Ghazi as-Sadoun", "More voices")
add("TvQuran.com__Athan", "TvQuran.com__03.athan.mp3", "Azan", "Ibrahim al-Arkani", "More voices")
add("TvQuran.com__Athan", "TvQuran.com__07.athan-fjer.mp3", "Fajr Azan", "Malik Shaybat al-Hamd", "More voices", FAJR)
add("AdzanDunia", "Adzan Subuh - Al Ustadz Abu Hazim Muhsin (Yaman).mp3", "Fajr Azan (Yemen)",
    "Ustadh Abu Hazim Muhsin", "More voices", FAJR)


def metadata(item):
    with urllib.request.urlopen(f"https://archive.org/metadata/{item}", timeout=60) as response:
        return {f["name"]: f for f in json.load(response)["files"]}


def check(url):
    request = urllib.request.Request(url, method="HEAD", headers={"User-Agent": "Mozilla/5.0"})
    error = None
    for _ in range(3):  # Archive storage nodes time out now and then
        try:
            with urllib.request.urlopen(request, timeout=60) as response:
                return response.status, response.headers.get("Content-Type", "")
        except OSError as failure:
            error = failure
    return 0, str(error)


def main():
    items = sorted({entry[0] for entry in ENTRIES})
    with ThreadPoolExecutor(8) as pool:
        files = dict(zip(items, pool.map(metadata, items)))
    catalog = []
    for item, name, title, muezzin, region, fajr in ENTRIES:
        info = files[item].get(name)
        if info is None:
            sys.exit(f"Missing on the Archive: {item}/{name}")
        slug = re.sub(r"[^a-z0-9]+", "-", f"{item}-{Path(name).stem}".lower()).strip("-")
        catalog.append({
            "id": slug, "title": title, "muezzin": muezzin, "region": region, "fajr": fajr,
            "url": f"https://archive.org/download/{item}/{urllib.parse.quote(name)}",
            "size": int(info.get("size", 0)), "seconds": int(float(info.get("length") or 0)),
        })
    ids = [track["id"] for track in catalog]
    if len(set(ids)) != len(ids):
        sys.exit("Duplicate ids: " + ", ".join(sorted({i for i in ids if ids.count(i) > 1})))
    if "--verify" in sys.argv:
        with ThreadPoolExecutor(8) as pool:
            results = list(pool.map(lambda t: check(t["url"]), catalog))
        bad = [(t["url"], r) for t, r in zip(catalog, results) if r[0] != 200 or "audio" not in r[1]]
        if bad:
            sys.exit("Not playable:\n" + "\n".join(f"{url} -> {result}" for url, result in bad))
    OUT.write_text(json.dumps(catalog, ensure_ascii=False, separators=(",", ":")), encoding="utf-8")
    print(f"{len(catalog)} azans -> {OUT}")


if __name__ == "__main__":
    main()
